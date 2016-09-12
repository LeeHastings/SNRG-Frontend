package org.snrg_nyc.model.properties;

import org.snrg_nyc.model.EditorException;

public class EnumeratorProperty extends ValuesListProperty<ListValue> {
	private static final long serialVersionUID = 1L;
	
	public 
	EnumeratorProperty(){
		super(()->new ListValue());
	}
	public 
	EnumeratorProperty(String name, String description) throws EditorException{
		super(name, description, ()->new ListValue());
	}
}