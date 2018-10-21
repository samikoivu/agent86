package com.samikoivu.agent86.callbacks;

public class ClassLoaderUtil extends ClassLoader {
	public ClassLoaderUtil() {
	}
	
	public Class defineClassUtil(String name, byte[] b) {
		return defineClass(name, b, 0, b.length);
	}
}
