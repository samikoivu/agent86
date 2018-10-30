package com.samikoivu.agent86.lib.bytecode;

import java.util.ArrayList;
import java.util.List;

import net.sf.rej.java.AccessFlags;
import net.sf.rej.java.Method;

/**
 * This class does all the classfile inspection and bytecode injection. Using reJ, but this class could be altered to use something
 * better supported without changing any of the other classes of the agent.
 */
public class Inspector {
	
	private ByteCodeTools bct;

	Inspector(ByteCodeTools bct) {
		this.bct = bct;
	}
		
	/**
	 * The fully qualified name of the class being inspected and injected.
	 * @return Name of the class being inspected.
	 */
	public String getName() {
		return this.bct.getName();
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
		List<Method> mtds = this.bct.getClassFile().getMethods();
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
	 * Returns true if the type being inspected is an interface.
	 * @return is the interface
	 */
	public boolean isInterface() {
		return AccessFlags.isInterface(this.bct.getClassFile().getAccessFlags());
	}

}