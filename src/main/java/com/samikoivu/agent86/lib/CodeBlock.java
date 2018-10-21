package com.samikoivu.agent86.lib;

import java.util.List;

import net.sf.rej.java.AccessFlags;
import net.sf.rej.java.JavaType;
import net.sf.rej.java.Method;

/**
 * CodeBlock represents a Method or a Constructor or a static initializer. Used for inspection and targeting injection.
 */
public class CodeBlock {

	/**
	 * Name of the method. <init> for constructors
	 */
	private String name;
	
	/**
	 * Types of the arguments of this method, in JavaType
	 */
	private List<JavaType> params;
	
	/**
	 * Method access flags
	 */
	private int flags;
	
	/**
	 * Underlying reJ Method instance for this method.
	 */
	private Method method;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	public void setMethod(Method method) {
		this.method = method;
	}

	public boolean isConstructor() {
		return this.name.equals("<init>");
	}

	public boolean matches(String methodName, Class ... mtdArgs) {
		if (!this.name.equals(methodName)) return false;
		
		if (mtdArgs.length != this.params.size()) return false;
		
		for (int i=0; i < this.params.size(); i++) {
			// TODO array dimensions
			if (!params.get(i).getType().equals(mtdArgs[i].getName())) {
				return false;
			}
		}
		
		return true;
	}

	public void setParameters(List<JavaType> params) {
		this.params = params;
	}

	public boolean isPublic() {
		return AccessFlags.isPublic(this.flags);
	}

	public Method getMethod() {
		return this.method;
	}

	public String getName() {
		return this.name;
	}

	public boolean isAbstract() {
		return AccessFlags.isAbstract(this.flags);
	}

}
