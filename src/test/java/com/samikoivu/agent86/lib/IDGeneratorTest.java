package com.samikoivu.agent86.lib;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.samikoivu.agent86.lib.IDGenerator;

class IDGeneratorTest {

	@Test
	void assertUniqueGeneratedIDs() {
		IDGenerator gen = IDGenerator.getInstance();
		String a = gen.getNextID();
		String b = gen.getNextID();
		String c = gen.getNextID();
		
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		
		assertNotEquals(a, b);
		assertNotEquals(b, c);
		assertNotEquals(a, c);
	}

}
