package com.samikoivu.agent86.callbacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects info about classes being defined by given Threads. Ugly all-static implementation to make injection simpler.
 */
public class ClassCollector {

	/**
	 * A List of Threads that we are currently "recording"
	 */
	private static List<Thread> collecting = new ArrayList<>();
	
	/**
	 * Map structure for storing class names for each Thread.
	 */
	private static Map<Thread, List<String>> classesByThread = new HashMap<>();
	
	public static void startCollecting() {
		Thread t = Thread.currentThread();
		collecting.add(t);
		List<String> list = classesByThread.get(t);

		if (list == null) {
			// first time collecting for this thread, create the list
			list = new ArrayList<String>();
			classesByThread.put(t, list);
		} else {
			// clear out any previously collected strings
			list.clear();
		}
	}

	public static void stopCollecting() {
		Thread t = Thread.currentThread();
		collecting.remove(t);
	}

	public static List<String> getCollectedClassesAndClear() {
		Thread t = Thread.currentThread();
		return classesByThread.remove(t);
	}

	public static void collect(String className) {
		Thread current = Thread.currentThread();
		if (isCollectingFor(current)) {
			List<String> list = classesByThread.get(current);
			list.add(className);
		}
	}

	public static boolean isCollectingFor(Thread t) {
		return collecting.contains(t);
	}
	
}
