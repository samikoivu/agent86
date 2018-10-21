package com.samikoivu.agent86.callbacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Callback class for collecting created String instances with static methods and state to simplify injected code.
 */
public class StringCollector {

	/**
	 * A List of Threads that we are currently "recording"
	 */
	private static List<Thread> collecting = new ArrayList<>();
	
	/**
	 * Map structure for storing Strings for each Thread. Using IdentityHashMap here because we are interested
	 * in unique instances not unique strings (equals method)
	 */
	private static Map<Thread, IdentityHashMap<String, String>> stringsByThread = new HashMap<>();
	
	/**
	 * Turn on string collection for given thread. This doesn't require synchronization, because a given thread will only enter once at a
	 * time, and different threads cause no problems.
	 */
	public static void startCollecting() {
		Thread t = Thread.currentThread();
		collecting.add(t);
		IdentityHashMap<String, String> list = stringsByThread.get(t);

		if (list == null) {
			// first time collecting for this thread, create the list
			list = new IdentityHashMap<String, String>();
			stringsByThread.put(t, list);
		} else {
			// clear out any previously collected strings
			list.clear();
		}
	}
	
	public static void stopCollecting() {
		Thread t = Thread.currentThread();
		collecting.remove(t);
	}
	
	public static void collect(String str) {
		Thread current = Thread.currentThread();
		if (isCollectingFor(current)) {
			IdentityHashMap<String, String> list = stringsByThread.get(current);
			if (!list.containsKey(str)) {
				list.put(str, str);
			}
		}
	}

	public static boolean isCollectingFor(Thread t) {
		return collecting.contains(t);
	}
	
	public static Set<String> getCollectedStringsAndClear() {
		Thread t = Thread.currentThread();
		IdentityHashMap<String, String> set = stringsByThread.remove(t);
		if (set != null) {
			return set.keySet();
		} else {
			return null;
		}
	}

}
