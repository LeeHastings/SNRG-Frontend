package org.snrg_nyc.model.properties;

import org.snrg_nyc.model.EditorException;

public class BooleanProperty extends EnumeratorProperty 
	implements ValueProperty<Boolean> 
{
	private static final long serialVersionUID = 1L;
	Boolean init = null;
	
	{
		int id;
		try {
			id = super.addRange();
			super.setRangeLabel(id, "True");
			id = super.addRange();
			super.setRangeLabel(id, "False");
		} catch (EditorException e) {
			e.printStackTrace();
		}
		this.setDistributionType(DistType.NULL);
	}
	
	public BooleanProperty(){
		super();
	}
	public BooleanProperty(String name, String description) throws EditorException{
		super(name, description);
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
	@Override
	public void setInitValue(Boolean init) {
		this.init = init;
	}
	@Override
	public Boolean getInitValue() {
		return init;
	}
	@Override
	public boolean hasInitValue() {
		return init != null;
	}
}
