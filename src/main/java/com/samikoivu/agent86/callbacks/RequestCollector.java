package com.samikoivu.agent86.callbacks;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.samikoivu.agent86.lib.IDGenerator;
import com.samikoivu.agent86.lib.SuperSimpleLogger;

/**
 * Collects info about HTTPServlet invocations.
 */
public class RequestCollector {
	
	/**
	 * For storing a RequestCapture for each Thread we're recording
	 */
	private static Map<Thread, RequestCapture> requestsByThread = new HashMap<>();

	public static void beginRequest(HttpServletRequest req, HttpServletResponse res) {
		Thread t = Thread.currentThread();
		RequestCapture capture = new RequestCapture();
		// turn on capturing of strings for this thread
		StringCollector.startCollecting();
		
		// turn on capturing of new class defs for this thread
		ClassCollector.startCollecting();
		
		requestsByThread.put(t, capture);
		
		String id = IDGenerator.getInstance().getNextID();
		if (res != null) {
			res.addHeader("Agent86-ID", id); // inject an ID as a response header
		}
		capture.setID(id);
		capture.setStart(System.currentTimeMillis());
	}

	public static void endRequest() {
		Thread t = Thread.currentThread();
		StringCollector.stopCollecting();
		ClassCollector.stopCollecting();
		RequestCapture capture = requestsByThread.remove(t); // intentional get + remove
		capture.setEnd(System.currentTimeMillis());
		capture.setStrings(StringCollector.getCollectedStringsAndClear());
		capture.setClasses(ClassCollector.getCollectedClassesAndClear());
		
		SuperSimpleLogger logger = SuperSimpleLogger.getInstance();
		PrintStream log = logger.getStream();
		capture.printCapturedInfo(log);
	}

}
