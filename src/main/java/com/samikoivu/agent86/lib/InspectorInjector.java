package com.samikoivu.agent86.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.samikoivu.agent86.callbacks.CallbackDefinition;

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
	 * reJ bytecode engineering Class definition. Should only be accessed by getter method as it performs the decompilation on first
	 * invocation.
	 */
	private ClassFile cls = null;

	/** Injection options
	 * 
	 */
	public static enum Option {
		/**
		 * Inject callback code at the start of the method
		 */
		AT_START,
		/**
		 * Pass all method arguments with the injected callback
		 */
		PASS_ARGS, 
		/**
		 * Inject callback code at all exit points, ie. before every return statement
		 */
		BEFORE_RETURN, 
		/**
		 * Pass "this" as an argument to the callback method
		 */
		PASS_THIS
	}

	public InspectorInjector(String className, byte[] data) {
		if (className != null) {
			this.name = className.replace('/', '.'); // translate from internal representation to dots, TODO anything else? subclasses?
		}
		this.originalData = data;
		this.modified = false;
	}
	
	/**
	 * Only decompile if required, ie. someone calls this method.
	 */
	private ClassFile getClassFile() {
		if (this.cls == null) {
			this.cls = Disassembler.readClass(this.originalData);
		}
		
		return this.cls;
	}

	/**
	 * Return bytecode. If callbacks have been injected, the new bytecode is derived from the internal model, otherwise the original
	 * array is returned.
	 * @return bytecode as a byte array
	 */
	public byte[] getData() {
		if (!modified) {
			return originalData;
		} else {
			return getClassFile().getData();
		}
	}
	
	/**
	 * The fully qualified name of the class being inspected and injected.
	 * @return Name of the class being inspected.
	 */
	public String getName() {
		if (this.name == null) {
			this.name = getClassFile().getFullClassName();
		}
		
		return this.name;
	}

	/**
	 * Returns true if this Inspector is for the class given as argument. Only the fully qualified name of the class is considered.
	 * (Class identity in Java is name + ClassLoader).
	 * @param klass Class to compare
	 * @return true if argument points at a class with the same name as the definition this Inspector works on.
	 */
	public boolean isFor(Class<?> klass) {
		return klass.getName().equals(getName());
	}

	/**
	 * Return a List containing all the Methods (and Constructors) of this class as <code>CodeBlock</code> instances.
	 * @return list of the methods of this class.
	 */
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

	/**
	 * Inject a callback to the method or constructor defined by codeBlock argument. The method to be called is defined by the callback argument
	 * and callbackOptions determines when the injected  callback happens and what the arguments are.
	 * @param codeBlock Method to inject
	 * @param callback Method to callback
	 * @param callbackOptions Options defining the specifics of the callback
	 */
	public void addCallback(CodeBlock codeBlock, CallbackDefinition callback, Option ... callbackOptions) {
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
	
	/*
	 * The actual low-level injection logic
	 */
	private void injectCallback(int offset, List<Option> opts, CallbackDefinition callback, String signature, Code code, ConstantPool cp, Method method) {
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

	/**
	 * Returns true if the type being inspected is an interface.
	 * @return is the interface
	 */
	public boolean isInterface() {
		return AccessFlags.isInterface(getClassFile().getAccessFlags());
	}

}