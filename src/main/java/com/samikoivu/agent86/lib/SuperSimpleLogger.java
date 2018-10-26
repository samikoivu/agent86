package com.samikoivu.agent86.lib;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Quick and dirty file logger that is independent of the logger configurations of the process where the agent runs.
 */
public class SuperSimpleLogger {
	private static SuperSimpleLogger INSTANCE = null;

	private PrintStream ps;
	
	private SuperSimpleLogger() {
		try {
			ps = new PrintStream("agentlog.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * This is not really thread safe at all, but as long as the first invocation starts and finishes on the same thread it should
	 * not be a problem.
	 */
	public static SuperSimpleLogger getInstance() {
		if (INSTANCE == null) {
			INSTANCE  = new SuperSimpleLogger();
		}
		
		return INSTANCE;
	}

	public PrintStream getStream() {
		return ps;
	}
}
