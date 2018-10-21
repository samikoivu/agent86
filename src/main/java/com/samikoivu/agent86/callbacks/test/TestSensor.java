package com.samikoivu.agent86.callbacks.test;

/**
 * Test class for Callback related tests.
 */
public class TestSensor {
	
	public static interface TestCallback {
		void testCallback();

		void testCallback(Long l1, Long l2, Integer i1);
	}
	
	private static TestCallback testCallback;

	private static boolean called = false;
	
	public static void sensor() {
		called = true;
		if (testCallback != null) {
			testCallback.testCallback();
		}
	}

	public static void sensor(Long l1, Long l2, Integer i1) {
		called = true;
		if (testCallback != null) {
			testCallback.testCallback(l1, l2, i1);
		}
	}

	public static void registerCallback(TestCallback tc) {
		testCallback = tc;
	}
	
	public static void clear() {
		called = false;
	}
	
	public static boolean wasCalled() {
		return called;
	}
	
}
