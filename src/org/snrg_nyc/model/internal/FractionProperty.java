package org.snrg_nyc.model.internal;

import org.snrg_nyc.model.EditorException;

public class FractionProperty extends ValueProperty<Float> {
	private static final long serialVersionUID = 1L;
	
	public FractionProperty() throws EditorException{
		super();
	}
	public FractionProperty(String name, String desc) throws EditorException{
		super(name, desc);
	}

}
