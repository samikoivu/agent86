package com.samikoivu.agent86.agent;

import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import com.samikoivu.agent86.lib.AgentTransformer;
import com.samikoivu.agent86.lib.SuperSimpleLogger;

/**
 * Java-Agent class. Adds our Transformer.
 * 
 * Things for Agent to do:
 * 
 * Count how many string objects were created for a single page request or RESTful request
 * Instrument the response to include a unique ID
 * 
 * Time the request from start to finish.
 * How much memory does a single page request take?
 * How many assemblies/classes/methods were loaded (depends on language)?
 *
 */
public class A86 {
	
	public static void premain(String args, Instrumentation inst) {
		SuperSimpleLogger logger = SuperSimpleLogger.getInstance();
		PrintStream log = logger.getStream();
		log.println("Agent86 v0.017 reporting to duty.");
		try {
			inst.addTransformer(new AgentTransformer(), true);
			inst.retransformClasses(String.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
