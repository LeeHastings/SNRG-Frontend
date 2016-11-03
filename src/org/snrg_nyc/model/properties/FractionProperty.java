package org.snrg_nyc.model.properties;

import org.snrg_nyc.model.EditorException;

public class FractionProperty extends NodeProperty 
	implements ValueProperty<Float> 
{
	private Float init = null;
	
	private static final long serialVersionUID = 1L;
	
	{
		setDependencyLevel(0);
	}
	public FractionProperty() throws EditorException{
		super();
	}
	public FractionProperty(String name, String desc) throws EditorException{
		super(name, desc);
	}
	
	@Override
	public void setInitValue(Float init) {
		this.init = init;
	}
	@Override
	public Float getInitValue() {
		return init;
	}
	@Override
	public boolean hasInitValue() {
		return init!= null;
	}

}
