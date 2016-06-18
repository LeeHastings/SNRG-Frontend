package org.snrg_nyc.model;

class Range<T extends Number> implements Comparable<Range<T>> {
	private T max;
	private T min;
	private String label;
	
	public Range(String label, T min, T max){
		this.label = label;
		if(min.doubleValue() >= max.doubleValue()){
			throw new IllegalArgumentException("Min ("+min+") must be strictly less than max ("+max+")");
		} else{
			this.min = min;
			this.max = max;
		}
	}
	
	public Range(){
		label = null;
		min = null;
		max = null;
	}

	public void setLabel(String label){
		if(label == null || label == ""){
			label = (label == null) ? "<null>" : label;
			throw new IllegalArgumentException("New label is invalid: "+label);
		} 
		else {
			this.label = label;
		}
	}
	
	public String getLabel(){
		return label;
	}
	public T getMin(){
		return min;
	}
	public T getMax(){
		return max;
	}
	public void setMin(T min){
		if(this.max != null && min.doubleValue() >= this.max.doubleValue()){
			throw new IllegalArgumentException("The lower bound on range '"+label+"' must be less than the upper bound.");
		}
		else {
			this.min = min;
		}
	}
	public void setMax(T max){
		if(this.min != null && max.doubleValue() <= this.min.doubleValue()){
			throw new IllegalArgumentException("The upper bound on range '"+label+"' must be greater than the lower bound.");
		}
		else {
			this.max = max;
		}
	}
	public boolean isSet(){
		return (label != null && min != null && max != null);
	}
	
	/**
	 * Compare ranges by their lower bounds
	 * @param rhs A Range object of the same type
	 */
	@Override
	public int compareTo(Range<T> rhs) {
		if(this.getMin() == null || rhs.getMin() == null){
			return 0;
		}
		return (int) (this.getMin().doubleValue() - rhs.getMin().doubleValue());
	}
}