package org.snrg_nyc.model.internal;


public class EnumeratorProperty extends ValuesListProperty<ListValue> {
	private static final long serialVersionUID = 1L;
	
	public 
	EnumeratorProperty(){
		super(()->new ListValue());
	}
	public 
	EnumeratorProperty(String name, String description){
		super(name, description, ()->new ListValue());
	}
}