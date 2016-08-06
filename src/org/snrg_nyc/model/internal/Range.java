package org.snrg_nyc.model.internal;

class Range<T extends Number> extends ListValue {
	private T max;
	private T min;
	
	public 
	Range(String label, T min, T max){
		super(label);
		if(min.doubleValue() >= max.doubleValue()){
			throw new IllegalArgumentException("Min ("+min+") must be strictly less than max ("+max+")");
		} else{
			this.min = min;
			this.max = max;
		}
	}
	
	public 
	Range(){
		super(null);
		min = null;
		max = null;
	}
	public T 
	getMin(){
		return min;
	}
	public T 
	getMax(){
		return max;
	}
	public void 
	setMin(T min){
		if(this.max != null && min.doubleValue() >= this.max.doubleValue()){
			throw new IllegalArgumentException("The lower bound on range '"
		+getLabel()+"' must be less than the upper bound.");
		}
		else {
			this.min = min;
		}
	}
	public void 
	setMax(T max){
		if(this.min != null && max.doubleValue() <= this.min.doubleValue()){
			throw new IllegalArgumentException("The upper bound on range '"
		+getLabel()+"' must be greater than the lower bound.");
		}
		else {
			this.max = max;
		}
	}
	@Override
	public boolean 
	isReady(){
		return (super.isReady() && min != null && max != null 
				&& min.doubleValue() <= max.doubleValue());
	}
	
	/**
	 * Order ranges by their lower bounds
	 * @param rhs A Range object of the same type
	 */
	public int 
	compareTo(Range<T> rhs) {
		if(this.getMin() == null || rhs.getMin() == null){
			return 0;
		}
		return (int) (this.getMin().doubleValue() - rhs.getMin().doubleValue());
	}
}