package com.samikoivu.agent86.callbacks;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Tag enums for callbacks. Avoiding magic strings to the max, so that the involved classes can be refactored and early warnings are presented
 * if something doesn't add up, rather than generating invalid bytecode later.
 * 
 * If we switched to Java 9 we could use method references. We might also use annotations on methods that are used for callbacks, but it is
 * unclear if it would be cleaner.
 */
public enum Callbacks implements CallbackDefinition {
	
	COLLECT_STRING(StringCollector.class, void.class, "collect", String.class),
	START_COLLECTING(StringCollector.class, void.class, "startCollecting"),
	STOP_COLLECTING(StringCollector.class, void.class, "stopCollecting"),
		
	BEGIN_REQUEST(RequestCollector.class, void.class, "beginRequest", HttpServletRequest.class, HttpServletResponse.class),
	END_REQUEST(RequestCollector.class, void.class, "endRequest");
	

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

	private Callbacks(Class<?> cbClass, Class<?> returnType, String methodName, Class<?> ... mtdArgs) {
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
