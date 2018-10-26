package com.samikoivu.agent86.callbacks.test;

import com.samikoivu.agent86.callbacks.CallbackDefinition;

/**
 * Callbacks enum for TEST callbacks.
 * 
 * Tag enums for callbacks. Avoiding magic strings to the max, so that the involved classes can be refactored and early warnings are presented
 * if something doesn't add up, rather than generating invalid bytecode later. If we switched to Java 9 we could use method references.
 */
public enum TestCallbacks implements CallbackDefinition {
		
	TEST_SENSOR(TestSensor.class, void.class, "sensor"),
	TEST_SENSOR_WITH_ARGS(TestSensor.class, void.class, "sensor", Long.class, Long.class, Integer.class);
	

	/**
	 * Callback class. The class the injected code will call.
	 */
	private Class<?> cbClass;
	
	/**
	 * Return type of callback method. Currently always void
	 */
	@SuppressWarnings("unused") // for future considerations
	private Class<?> returnType;

	/**
	 * Name of the method the injected code will call.
	 */
	private String methodName;

	@SuppressWarnings("rawtypes") // not mixing arrays and generics
	/**
	 * Argument types for callback method.
	 */
	private Class[] mtdArgs;

	private TestCallbacks(Class<?> cbClass, Class<?> returnType, String methodName, Class<?> ... mtdArgs) {
		this.cbClass = cbClass;
		this.returnType = returnType;
		this.methodName = methodName;
		this.mtdArgs = mtdArgs;

	}
	
	public Class<?> getCallbackClass() {
		return this.cbClass;
	}

	public String getMethodName() {
		return methodName;
	}
	
	@SuppressWarnings("rawtypes") // not mixing arrays and generics
	public Class[] getMethodArgs() {
		return this.mtdArgs.clone();
	}

}
