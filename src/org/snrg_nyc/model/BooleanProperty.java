package org.snrg_nyc.model;

class BooleanProperty extends EnumeratorProperty {
	public BooleanProperty(){
		super();
		values.add("true");
		values.add("false");
	}
	public BooleanProperty(String name, String description){
		super(name, description);
		values.add("true");
		values.add("false");
	}
	@Override
	public void setRangeLabel(int rid, String label){
		throw new UnsupportedOperationException("Cannot edit labels of a boolean property.");
	}
	@Override
	public int addRange(){
		throw new UnsupportedOperationException("Cannot add labels to a boolean property.");
	}
	@Override
	public void removeRange(int rid){
		throw new UnsupportedOperationException("Cannot remove labels from a boolean property.");
	}
}
