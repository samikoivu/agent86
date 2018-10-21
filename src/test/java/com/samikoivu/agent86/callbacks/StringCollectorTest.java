package com.samikoivu.agent86.callbacks;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.samikoivu.agent86.callbacks.StringCollector;

class StringCollectorTest {

	@Test
	void testStringCollection() {
		StringCollector.startCollecting();
		StringCollector.collect("hello");
		StringCollector.stopCollecting();
		Set<String> strings = StringCollector.getCollectedStringsAndClear();
		assertNotNull(strings, "getCollectedStringsAndClear should not return null");
		assertTrue(strings.size() == 1, "Size should be 1");
		assertEquals(strings.iterator().next(), "hello", "Collected string should be 'hello'");
		Set<String> secondCall = StringCollector.getCollectedStringsAndClear();
		assertNull("Second call to getCollectedStringsAndClear should return null", secondCall);
	}

}
