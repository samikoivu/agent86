package com.samikoivu.agent86.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.samikoivu.agent86.callbacks.Callbacks;

import net.sf.rej.java.AccessFlags;
import net.sf.rej.java.ClassFile;
import net.sf.rej.java.Code;
import net.sf.rej.java.Descriptor;
import net.sf.rej.java.Disassembler;
import net.sf.rej.java.JavaType;
import net.sf.rej.java.Method;
import net.sf.rej.java.attribute.CodeAttribute;
import net.sf.rej.java.constantpool.ConstantPool;
import net.sf.rej.java.instruction.Instruction;
import net.sf.rej.java.instruction.ParameterType;
import net.sf.rej.java.instruction.Parameters;
import net.sf.rej.java.instruction._aload;
import net.sf.rej.java.instruction._aload_0;
import net.sf.rej.java.instruction._dload;
import net.sf.rej.java.instruction._fload;
import net.sf.rej.java.instruction._iload;
import net.sf.rej.java.instruction._invokestatic;
import net.sf.rej.java.instruction._lload;

/**
 * This class does all the classfile inspection and bytecode injection. Using reJ, but this class could be altered to use something
 * better supported without changing any of the other classes of the agent.
 */
public class InspectorInjector {
	
	/**
	 * Has the underlying class been modified, ie. do we need to recalculate the class definition from the model, or can we use the
	 * original provided byte array.
	 */
	private boolean modified;
	
	/**
	 * The original class definition we use as a starting point for any modifications.
	 */
	private byte[] originalData;
	
	/**
	 * Class name. Lazily initialized from class definition if not available otherwise. This should not be accessed directly, but rahter
	 * getName() should be called.
	 */
	private String name;
	
	/**
	 * reJ bytecode engineering Class definition
	 */
	private ClassFile cls = null;
	
	public static enum Option {
		AT_START, PASS_ARGS, BEFORE_RETURN, PASS_THIS
	}

	public InspectorInjector(String className, byte[] data) {
		if (className != null) {
			this.name = className.replace('/', '.'); // translate from internal representation to dots, TODO anything else? subclasses?
		}
		this.originalData = data;
		this.modified = false;
	}
	
	/**
	 * Lazy decompilation
	 */
	private ClassFile getClassFile() {
		if (this.cls == null) {
			this.cls = Disassembler.readClass(this.originalData);
		}
		
		return this.cls;
	}

	public byte[] getData() {
		if (!modified) {
			return originalData;
		} else {
			return getClassFile().getData();
		}
	}
	
	public String getName() {
		if (this.name == null) {
			this.name = getClassFile().getFullClassName();
		}
		
		return this.name;
	}

	public boolean isFor(Class<?> klass) {
		return klass.getName().equals(getName());
	}

	public List<CodeBlock> getMethods() {
		List<Method> mtds = getClassFile().getMethods();
		List<CodeBlock> ret = new ArrayList<>();
		for (Method mtd : mtds) {
			CodeBlock cb = new CodeBlock();
			cb.setName(mtd.getName());
			cb.setParameters(mtd.getDescriptor().getParamList());
			cb.setFlags(mtd.getAccessFlags());
			cb.setMethod(mtd);
			ret.add(cb);
		}
		return ret;
	}

	public void addCallback(CodeBlock codeBlock, Callbacks callback, Option ... callbackOptions) {
		this.modified = true; // mark as modified
		List<Option> opts = Arrays.asList(callbackOptions);

		Method m = codeBlock.getMethod();
		CodeAttribute ca = m.getAttributes().getCode();
		Code code = ca.getCode();
		ConstantPool cp = getClassFile().getPool();

		String signature = "()V"; // in case were not passing anything
		if (opts.contains(Option.PASS_THIS)) {
			Descriptor desc = new Descriptor("()V");
			JavaType type = new JavaType(getName());
			List<JavaType> list = new ArrayList<>();
			list.add(type);
			desc.setParamList(list);
			signature = desc.getRawDesc();
		}
		if (opts.contains(Option.PASS_ARGS)) {
			Descriptor desc = new Descriptor("()V");
			desc.setParamList(m.getDescriptor().getParamList());
			signature = desc.getRawDesc();
		}

		if (opts.contains(Option.AT_START)) {
			injectCallback(0, opts, callback, signature, code, cp, m);
		}
			
		if (opts.contains(Option.BEFORE_RETURN)) {
			// find all returns
			List<Instruction> instrs = code.getInstructions();
			// back to front in order to be able to add and get confused with the indices
			for (int i=instrs.size()-1; i >=0; i--) {
				Instruction instr = instrs.get(i);
				if (instr.getMnemonic().contains("return")) {
					injectCallback(i, opts, callback, signature, code, cp, m);					
				}
			}
		}
	}
	
	private void injectCallback(int offset, List<Option> opts, Callbacks callback, String signature, Code code, ConstantPool cp, Method method) {
		if (opts.contains(Option.PASS_THIS)) {
			// regular method - pass this: just do an aload_0 followed by invokestatic
			_invokestatic invoke = new _invokestatic();
			Parameters invokeParams = new Parameters();
			invokeParams.addParam(ParameterType.TYPE_CONSTANT_POOL_METHOD_REF);
			int methodRef = cp.optionalAddMethodRef(callback.getCallbackClass().getName(), callback.getMethodName(), signature);
			invokeParams.addValue(methodRef);
			invoke.setParameters(invokeParams);

			code.add(offset + 0, new _aload_0());
			code.add(offset + 1, invoke);
		} else if (opts.contains(Option.PASS_ARGS)) {
			// regular method - pass args

			// push args
			int pos = AccessFlags.isStatic(method.getAccessFlags()) ? 0 : 1; // arg position (for non-static 0 = this)
			int i = 0; // list index to add next instruction
			for (Class<?> param : callback.getMethodArgs()) {
				if (param == int.class || param == byte.class || param == boolean.class || param == short.class || param == char.class) {
					code.add(i, new _iload(pos)); // optimize to use iload_0, etc?
				} else if (param == long.class) {
					code.add(i, new _lload(pos)); // optimize to use lload_0, etc?
				} else if (param == float.class) {
					code.add(i, new _fload(pos)); // optimize to use fload_0, etc?
				} else if (param == double.class) {
					code.add(i, new _dload(pos)); // optimize to use dload_0, etc?
				} else {
					code.add(i, new _aload(pos)); // optimize to use aload_0, etc?
				}
				
				// increment position
				pos++;
				i++;
				if (param == double.class || param == long.class) {
					pos++; // two slots for double and long
				}
			}
			
			_invokestatic invoke = new _invokestatic();
			Parameters invokeParams = new Parameters();
			invokeParams.addParam(ParameterType.TYPE_CONSTANT_POOL_METHOD_REF);
			int methodRef = cp.optionalAddMethodRef(callback.getCallbackClass().getName(), callback.getMethodName(), signature);
			invokeParams.addValue(methodRef);
			invoke.setParameters(invokeParams);
			code.add(offset + i, invoke);
			
		} else {
			// regular method - pass nothing: just do an invoke static
			_invokestatic invoke = new _invokestatic();
			Parameters invokeParams = new Parameters();
			invokeParams.addParam(ParameterType.TYPE_CONSTANT_POOL_METHOD_REF);
			int methodRef = cp.optionalAddMethodRef(callback.getCallbackClass().getName(), callback.getMethodName(), signature);
			invokeParams.addValue(methodRef);
			invoke.setParameters(invokeParams);

			code.add(offset, invoke);
		}

	}

	public boolean isInterface() {
		return AccessFlags.isInterface(getClassFile().getAccessFlags());
	}

}

/*class CFT implements ClassFileTransformer {

@Override
public byte[] transform(ClassLoader loader, String className,
		Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
		byte[] classfileBuffer) throws IllegalClassFormatException {
//	System.out.println("Agent looking at " + className + " from " + classBeingRedefined);
//	if ("java/io/ObjectInputStream".equals(className)) {
//	    return transformClass(classBeingRedefined, classfileBuffer);
//	}
	if (!className.startsWith("java")) {
		instrumentServletMethods(classBeingRedefined, classfileBuffer);
	}
	
	// find String constructors? and factory methods? refresh your brain implementation
	// then, if thread is in map, record the created strings
	
	return classfileBuffer;
}

private byte[] instrumentServletMethods(Class classToTransform, byte[] b) {
	byte[] transformed = b.clone();
	// look for protected void service (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	// and add a callback at the start that records start time and thread id
	// and a callback at the end^H^H^H before every return bytecode instruction which calculates duration and logs the created strings and adds ID to the response
	return transformed;
}
private byte[] transformClass(Class classToTransform, byte[] b) {
	byte[] transformed = b.clone();
	try {
		System.out.println("Transforming class");
		ClassFile cls = Disassembler.readClass(b);
		ConstantPool cp = cls.getPool();
		List<Method> methods = cls.getMethods();
		for (Method m : methods) {
			if (m.getName().equals("readObject")) {
				System.out.println("Transforming method");
				CodeAttribute ca = m.getAttributes().getCode();
				Code code = ca.getCode();

//				int fieldid = cp.indexOfFieldRef("java.io.ObjectInputStream", "unsharedMarker", "Ljava/lang/Object;");
//				System.out.println("fieldid: " + fieldid);
//				_getstatic getStatic = new _getstatic(fieldid);
//				
//				_invokevirtual invoke = new _invokevirtual();
//				Parameters invokeParams = new Parameters();
//				invokeParams.addParam(ParameterType.TYPE_CONSTANT_POOL_METHOD_REF);
//				int methodRef = cp.optionalAddMethodRef(Object.class.getName(), "toString", "()Ljava/lang/String;");
//				invokeParams.addValue(methodRef);
//				invoke.setParameters(invokeParams);
//
//				code.add(0, getStatic);
//				code.add(1, invoke);
//				code.add(2, new _pop());

				
				_invokestatic invoke = new _invokestatic();
				Parameters invokeParams = new Parameters();
				invokeParams.addParam(ParameterType.TYPE_CONSTANT_POOL_METHOD_REF);
				int methodRef = cp.optionalAddMethodRef(AgentLib.class.getName(), "check", "()V");
				invokeParams.addValue(methodRef);
				invoke.setParameters(invokeParams);

				code.add(0, invoke);

			}
		}
		
		transformed = cls.getData();
	} catch (Throwable t) {
		t.printStackTrace();
	}
	System.out.println("Done transforming class");
			
//	List<Field> fields = cls.getFields();
//	for (Field f : fields) {
//		if (f.getName().equals("unsharedMarker")) {
//			System.out.println("unshared marker made non-final");
//			AccessFlags accessFlags = new AccessFlags(f.getAccessFlags());
//			accessFlags.setFinal(false);
//			f.setAccessFlags(accessFlags);
//		}
//	}
//	cls.setFields(fields);
	
//	try {
//		FileOutputStream fos = new FileOutputStream("C:\\Users\\skoivu\\Desktop\\ObjectInputStream_.class");
//		fos.write(transformed);
//		fos.flush();
//		fos.close();
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	System.out.println(b.length);
//	System.out.println(transformed.length);
//	for (int i=0; i < Math.min(b.length, transformed.length); i++) {
//		if (b[i] == transformed[i]) continue;
//		
//		System.out.println(Integer.toHexString(i) + ": " + b[i]);
//		System.out.println(Integer.toHexString(i) + ": " + transformed[i]);
//	}
	
	return transformed;
}
}
*/