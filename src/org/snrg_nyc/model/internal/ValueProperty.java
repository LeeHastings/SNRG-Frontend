package org.snrg_nyc.model.internal;

import org.snrg_nyc.model.EditorException;

public abstract class ValueProperty<T> extends NodeProperty {
	private static final long serialVersionUID = 1L;
	private T value;
	
	public ValueProperty(String name, String desc) throws EditorException {
		super(name, desc);
		init();
	}
	public ValueProperty() throws EditorException {
		super();
		init();
	}
	private void init() throws EditorException{
		super.setDependencyLevel(0);
		value = null;
	}
	public void setInitValue(T init){
		this.value = init;
	}
	public T getInitValue(){
		return value;
	}
	public boolean hasInitValue(){
		return value != null;
	}
}
