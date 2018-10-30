package com.samikoivu.agent86.lib.bytecode;

import net.sf.rej.java.ClassFile;
import net.sf.rej.java.Disassembler;

public class ByteCodeTools {
	
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
	 * reJ bytecode engineering Class definition. Should only be accessed by getter method as it performs the lazy decompilation on first
	 * invocation.
	 */
	private ClassFile cls = null;

	public ByteCodeTools(String className, byte[] data) {
		if (className != null) {
			this.name = className.replace('/', '.'); // translate from internal representation to dots, TODO anything else? subclasses?
		}
		this.originalData = data;
		this.modified = false;
	}

	/**
	 * Mark the class we are inspecting/injecting as having been modified.
	 */
	void setModified() {
		this.modified = true;
	}

	/**
	 * Only decompile if required, ie. someone calls this method.
	 */
	ClassFile getClassFile() {
		if (this.cls == null) {
			this.cls = Disassembler.readClass(this.originalData);
		}
		
		return this.cls;
	}
	
	/**
	 * The fully qualified name of the class being inspected and injected.
	 * @return Name of the class being inspected.
	 */
	String getName() {
		if (this.name == null) {
			this.name = getClassFile().getFullClassName();
		}
		
		return this.name;
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

	public Inspector getInspector() {
		return new Inspector(this);
	}

	public Injector getInjector() {
		return new Injector(this);
	}
	

}
