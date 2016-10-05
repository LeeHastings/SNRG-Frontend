package org.snrg_nyc.util;

public class ReadOnlyPair<T, S> {
	protected T val1;
	protected S val2;
	public ReadOnlyPair(T value1, S value2){
		val1 = value1;
		val2 = value2;
	}
	
	public T get1(){
		return val1;
	}
	public S get2(){
		return val2;
	}
}
