package org.snrg_nyc.model.internal;

import org.snrg_nyc.model.EditorException;

public class BooleanProperty extends ValueProperty<Boolean>{
	private static final long serialVersionUID = 1L;
	
	public BooleanProperty() throws EditorException{
		super();
	}
	public BooleanProperty(String name, String description) throws EditorException{
		super(name, description);
	}
}
