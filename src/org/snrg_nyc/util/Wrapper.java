package org.snrg_nyc.util;

import java.util.ArrayList;
import java.util.List;

public class Wrapper<T>  {
	public interface Listener<T>{
		public void run(Wrapper<T> wrapper, T oldValue, T newValue);
	}
	
	private T data;
	private List<Listener<T>> listeners = new ArrayList<>();
	private List<Wrapper<T>> boundProperties = new ArrayList<>();
	
	public Wrapper(){
		data = null;
	}
	
	public Wrapper(T data){
		this.data = data;
	}
	
	public boolean exists(){
		return data != null;
	}
	
	public T get(){
		return data;
	}
	
	public void set(T data){
		for(Listener<T> l : listeners){
			l.run(this, this.data, data);
		}
		this.data = data;
	}
	
	public void addChangeListener(Listener<T> listener){
		listeners.add(listener);
	}
	
	public void removeChangeListener(Listener<T> listener){
		int i = listeners.indexOf(listener);
		if(i < 0){
			throw new IllegalArgumentException(
					"Tried to remove change listener that was never added");
		}
		else {
			listeners.remove(i);
		}
	}
	
	public void bindTwoWay(Wrapper<T> wrapper){
		bindOneWay(wrapper);
		addChangeListener((w, oldVal, newVal)->{
			wrapper.set(newVal);
		});
	}
	
	public void bindOneWay(Wrapper<T> wrapper){
		if(boundProperties.contains(wrapper)){
			throw new IllegalStateException("Tried to bind a property that was already bound");
		}
		boundProperties.add(wrapper);
		wrapper.addChangeListener((w, oldval, newval)->{
			this.set(newval);
		});
	}
	
	public void unBind(Wrapper<T> wrapper){
		int i = boundProperties.indexOf(wrapper);
		if(i < 0){
			throw new IllegalArgumentException("Tried to unbind property that was never bound");
		}
		else {
			boundProperties.remove(i);
		}
	}
}
