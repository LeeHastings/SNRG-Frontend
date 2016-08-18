package org.snrg_nyc.model.internal;

import org.snrg_nyc.model.EditorException;

public class BooleanRangeProperty extends EnumeratorProperty {
	private static final long serialVersionUID = 1L;
	public BooleanRangeProperty(){
		super();
		init();
	}
	public BooleanRangeProperty(String name, String description) throws EditorException{
		super(name, description);
		init();
	}
	private void init(){
		values.add(new ListValue("true"));
		values.add(new ListValue("false"));
	}
	
	@Override
	public void setRangeLabel(int rid, String label) throws EditorException{
		throw new EditorException("Cannot edit labels of a boolean property.");
	}
	@Override
	public int addRange() throws EditorException{
		throw new EditorException("Cannot add labels to a boolean property.");
	}
	@Override
	public void removeRange(int rid) throws EditorException{
		throw new EditorException("Cannot remove labels from a boolean property.");
	}
	@Override
	public boolean
	rangesAreSet(){
		return true;
	}
}
