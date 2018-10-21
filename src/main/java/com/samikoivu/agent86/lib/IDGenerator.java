package com.samikoivu.agent86.lib;

import java.util.Random;

/**
 * Generates "unique" IDs to inject into HTTP responses (as a header).
 * 
 * Shoddy implementation doesn't really guarantee uniqueness to any real degree. Also uses regular Random rather than SecureRandom.
 */
public class IDGenerator {
	
	private static IDGenerator INSTANCE = new IDGenerator();
	
	private IDGenerator() {
	}
	
	public static IDGenerator getInstance() {
		return INSTANCE;
	}
	
	private Random random = new Random();
	
	public synchronized String getNextID() {
		return Long.toHexString(random.nextLong());
	}
}
