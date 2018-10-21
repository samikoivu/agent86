package com.samikoivu.agent86.lib.test;

public class InjectionTest {
	
	private int count = 0;
	
	public void inc() {
		count++;
	}
	
	public void add(int another) {
		count +=another;
	}
	
	public int getValue() {
		return count;
	}
	
	public void addAll(Long l1, Long l2, Integer i) {
		this.count += l1.intValue();
		this.count += l2.intValue();
		this.count += i.intValue();
	}
}
