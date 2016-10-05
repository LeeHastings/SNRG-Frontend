package org.snrg_nyc.util;

public class Pair<T, S> extends ReadOnlyPair<T,S> {

	public Pair(T value1, S value2) {
		super(value1, value2);
	}

	public void set1(T val1){
		this.val1 = val1;
	}
	public void set2(S val2){
		this.val2 = val2;
	}
	
}
