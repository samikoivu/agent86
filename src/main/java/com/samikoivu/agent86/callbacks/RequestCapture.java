package com.samikoivu.agent86.callbacks;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;

/**
 * Instances of this class represent one captured HTTP request.
 */
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

	/**
	 * Request URI. Not required for the project, but makes things clearer.
	 */
	private String uri;

	public void setStart(long starMillis) {
		this.startMilis = starMillis;
	}

	public void setID(String id) {
		this.id = id;
	}

	/**
	 * Could make this synchronized - slower, but would guarantee that two requests won't get printed on top of each other
	 * @param out
	 */
	public void printCapturedInfo(PrintStream out) {
		out.println("===================================================");
		out.println("Request URI: " + this.uri);
		out.println("Request ID: " + this.id);
		out.println("Duration: " + (this.endMillis-this.startMilis) + "ms");
		out.println("Unique strings created: " + this.collectedStrings.size());
//		out.println("(" + this.collectedStrings + ")");
		out.println("Classes loaded: " + this.classes.size());
//		out.println("(" + this.classes + ")");
		out.println();
	}

	/**
	 * Set request end time in milliseconds
	 * @param end
	 */
	public void setEnd(long end) {
		this.endMillis = end;
	}

	/**
	 * Set the list of unique Strings instantiated during this request
	 * @param collectedStrings
	 */
	public void setStrings(Set<String> collectedStrings) {
		this.collectedStrings = collectedStrings;
	}

	/**
	 * Set the list of classes loaded during this request
	 * @param classes
	 */
	public void setClasses(List<String> classes) {
		this.classes = classes;
	}

	/**
	 * Set the URI this request is referring to
	 * @param uri
	 */
	public void setURI(String uri) {
		this.uri = uri;
	}
	
}
