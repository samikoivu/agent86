package com.samikoivu.agent86.callbacks;

import org.junit.jupiter.api.Test;

import com.samikoivu.agent86.callbacks.ClassCollector;
import com.samikoivu.agent86.callbacks.RequestCollector;
import com.samikoivu.agent86.callbacks.StringCollector;

class RequestCollectorTest {

	@Test
	void test() throws Exception {
		RequestCollector.beginRequest(null, null);
		StringCollector.collect("hello");
		ClassCollector.collect("hello.Class");
		Thread.sleep(1000);
		RequestCollector.endRequest();
	}

}
