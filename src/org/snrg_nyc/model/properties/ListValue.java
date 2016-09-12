package org.snrg_nyc.model.properties;

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
			this.label = null;
		}
		else {
			this.label = label;
		}
	}
	public boolean
	labelIs(String label){
		return this.label != null && this.label.equals(label);
	}
	public boolean
	isReady(){
		return label != null && label.length() > 0;
	}
}

