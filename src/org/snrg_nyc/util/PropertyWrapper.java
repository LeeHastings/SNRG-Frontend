package org.snrg_nyc.util;

public class PropertyWrapper<T>  {
	private T data;
	public PropertyWrapper(){
		data = null;
	}
	public PropertyWrapper(T data){
		this.data = data;
	}
	public T get(){
		return data;
	}
	public void set(T data){
		this.data = data;
	}
}
