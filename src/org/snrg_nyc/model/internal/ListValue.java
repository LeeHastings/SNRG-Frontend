package org.snrg_nyc.model.internal;

class ListValue {
	private String label;
	
	public 
	ListValue(String label){
		this.label = label;
	}
	public ListValue(){}
	
	public String 
	getLabel(){
		return label;
	}
	
	public void 
	setLabel(String label){
		if(label == null || label.length() == 0){
			throw new IllegalArgumentException(
					"The label for a property value cannot be empty");
		}
		else {
			this.label = label;
		}
	}
	public boolean
	labelIs(String label){
		return this.label != null && this.label.equals(label);
	}
}

