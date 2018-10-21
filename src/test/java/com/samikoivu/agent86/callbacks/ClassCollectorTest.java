package com.samikoivu.agent86.callbacks;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.samikoivu.agent86.callbacks.ClassCollector;

class ClassCollectorTest {

	@Test
	void testClassCollection() {
		ClassCollector.startCollecting();
		ClassCollector.collect("hello.Class");
		ClassCollector.stopCollecting();
		List<String> strings = ClassCollector.getCollectedClassesAndClear();
		assertNotNull(strings, "getCollectedClassesAndClear should not return null");
		assertTrue(strings.size() == 1, "Size should be 1");
		assertEquals(strings.get(0), "hello.Class", "Collected class name should be 'hello.Class'");
		List<String> secondCall = ClassCollector.getCollectedClassesAndClear();
		assertNull("Second call to getCollectedClassesAndClear should return null", secondCall);
	}

}
