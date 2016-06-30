package org.snrg_nyc.model;

import java.util.ArrayList;
import java.util.List;

class IntegerRangeProperty extends EnumeratorProperty {
	private static final long serialVersionUID = 1L;
	protected List<Range<Integer>> ranges;
	
	public IntegerRangeProperty(){
		super();
		ranges = new ArrayList<Range<Integer>>();
	}
	public IntegerRangeProperty(String name, String desc){
		super(name, desc);
		ranges = new ArrayList<Range<Integer>>();
	}
	@Override
	public int addRange(){
		if(distributionsAreSet()){
			throw new IllegalStateException("Cannot edit range labels once distributions are set");
		}
		int len = ranges.size();
		//Add new ranges into empty spots if available
		for(int rid = 0; rid < len; rid++){
			if(ranges.get(rid) == null){
				ranges.set(rid, new Range<Integer>());
				return rid;
			}
		}
		ranges.add(new Range<Integer>());
		return ranges.size()-1;
	}
	
	@Override
	public void removeRange(int rid){
		if(distributionsAreSet()){
			throw new IllegalStateException("Cannot edit range labels once distributions are set");
		}
		assert_validRID(rid);
		ranges.set(rid, null);
	}

	//Getters
	@Override
	public List<Integer> getUnSortedRangeIDs(){
		List<Integer> sorted = new ArrayList<>();
		int len  = ranges.size();
		for(int i = 0; i < len; i++){
			if(ranges.get(i) != null){
				sorted.add(i);
			}
		}
		return sorted;
	}
	
	@Override
	public Integer getRangeWithLabel(String label){
		for(Range r : ranges){
			if(r != null && r.getLabel().equals(label)){
				return ranges.indexOf(r);
			}
		}
		return null;
	}
	@Override
	public String getRangeLabel(int rid){
		return ranges.get(rid).getLabel();
	}
	
	public int getRangeMin(int rid){
		assert_validRID(rid);
		return ranges.get(rid).getMin(); 
	}
	
	public int getRangeMax(int rid){
		assert_validRID(rid);
		return ranges.get(rid).getMax(); 
	}
	
	//Setters
	
	@Override
	public void setRangeLabel(int rid, String label){
		assert_validRID(rid);
		
		for(Range<Integer> r : ranges){
			if(r != null && label.equals(r.getLabel())){
				throw new IllegalArgumentException("Duplicate Label: "+label);
			}
		}
		ranges.get(rid).setLabel(label);
	}
	
	public void setRangeMin(int rid, int min) throws IllegalArgumentException {
		assert_validRID(rid);
		ranges.get(rid).setMin(min);
	}
	public void setRangeMax(int rid, int max) throws IllegalArgumentException {
		assert_validRID(rid);
		ranges.get(rid).setMax(max);
	}
	//Other Methods

	@Override public boolean validRID(int rid){
		return (rid >= 0 && rid < ranges.size() && ranges.get(rid) != null);
	}
	
	@Override
	public boolean rangeIsSet(int rid) {
		assert_validRID(rid);
		return ranges.get(rid).isSet();
	}
	
	@Override
	int compareRanges(int rid1, int rid2){
		if(ranges.get(rid1) == null || ranges.get(rid2) == null){
			return 0;
		}
		else {
			return ranges.get(rid1).compareTo(ranges.get(rid2));
		}
	}
}