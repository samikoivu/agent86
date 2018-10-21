package com.samikoivu.agent86.callbacks;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;

public class RequestCapture {

	/**
	 * The id generated for this request
	 */
	private String id;
	
	/**
	 * Request start time in ms
	 */
	private long startMilis;
	
	/**
	 * Request end time in ms
	 */
	private long endMillis;
	
	/**
	 * A Set of the unique strings collected during this request
	 */
	private Set<String> collectedStrings;
	
	/**
	 * A Set of the classes loaded during this request
	 */
	private List<String> classes;

	public void setStart(long starMillis) {
		this.startMilis = starMillis;
	}

	public void setID(String id) {
		this.id = id;
	}

	public void printCapturedInfo(PrintStream out) {
		out.println("===================================================");
		out.println("Request ID: " + this.id);
		out.println("Duration: " + (this.endMillis-this.startMilis) + "ms");
		out.println("Unique strings created: " + this.collectedStrings.size());
//		out.println("(" + this.collectedStrings + ")");
		out.println("Classes loaded: " + this.classes.size());
//		out.println("(" + this.classes + ")");
		out.println();
	}

	public void setEnd(long end) {
		this.endMillis = end;
	}

	public void setStrings(Set<String> collectedStrings) {
		this.collectedStrings = collectedStrings;
	}

	public void setClasses(List<String> classes) {
		this.classes = classes;
	}

}
