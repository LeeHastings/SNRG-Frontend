package org.snrg_nyc.model.properties;

public interface ValueProperty<T>{
	public void setInitValue(T init);
	public T getInitValue();
	public boolean hasInitValue();
}
