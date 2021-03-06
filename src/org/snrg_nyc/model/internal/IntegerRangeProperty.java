package org.snrg_nyc.model.internal;

import org.snrg_nyc.model.EditorException;

public class IntegerRangeProperty extends ValuesListProperty<Range<Integer>> {
	private static final long serialVersionUID = 1L;
	
	public 
	IntegerRangeProperty(){
		super( ()-> new Range<Integer>() );
	}
	
	public 
	IntegerRangeProperty(String name, String desc) throws EditorException{
		super(name, desc, ()->new Range<Integer>());
	}
	
	public Integer 
	getRangeMin(int rid) throws EditorException{
		assert_validRID(rid);
		return values.get(rid).getMin(); 
	}
	
	public Integer 
	getRangeMax(int rid) throws EditorException{
		assert_validRID(rid);
		return values.get(rid).getMax(); 
	}
	
	//Setters
	
	public void 
	setRangeMin(int rid, int min) throws EditorException {
		assert_validRID(rid);
		values.get(rid).setMin(min);
	}
	public void 
	setRangeMax(int rid, int max) throws EditorException{
		assert_validRID(rid);
		values.get(rid).setMax(max);
	}
	//Other Methods
	@Override
	public boolean 
	rangeIsSet(int rid) throws EditorException {
		assert_validRID(rid);
		return values.get(rid).isReady();
	}
	
	@Override
	int 
	compareRanges(int rid1, int rid2){
		if(values.get(rid1) == null || values.get(rid2) == null){
			return 0;
		}
		else {
			return values.get(rid1).compareTo(values.get(rid2));
		}
	}
}