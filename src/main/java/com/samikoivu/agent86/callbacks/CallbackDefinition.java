package com.samikoivu.agent86.callbacks;

/**
 * Interface for Callbacks. This is only supposed to be implemented by the Callbacks enum which defines all of the callbacks in one place.
 */
public interface CallbackDefinition {

	/**
	 * Class to be called.
	 * @return class to be called.
	 */
	Class<?> getCallbackClass();

	/**
	 * Method to be called.
	 * @return method name
	 */
	String getMethodName();

	/**
	 * Types of arguments of the method to be called.
	 * @return list of argument types.
	 */
	@SuppressWarnings("rawtypes") // not mixing arrays and generics
	Class[] getMethodArgs();

}
