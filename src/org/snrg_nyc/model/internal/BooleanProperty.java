package org.snrg_nyc.model.internal;

public class BooleanProperty extends EnumeratorProperty {
	private static final long serialVersionUID = 1L;
	public BooleanProperty(){
		super();
		init();
	}
	public BooleanProperty(String name, String description){
		super(name, description);
		init();
	}
	private void init(){
		values.add(new ListValue("true"));
		values.add(new ListValue("false"));
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
