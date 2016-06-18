package org.snrg_nyc.model;

class FractionProperty extends NodeProperty {
	private Float init;
	
	public FractionProperty(){
		super();
		this.dependencyLevel = 0;
		init = null;
	}
	public FractionProperty(String name, String desc){
		super(name, desc);
		this.dependencyLevel = 0;
		init = null;
	}
	public void setInitValue(float init){
		this.init = init;
	}
	public float getInitValue(){
		return init;
	}
	public boolean hasInitValue(){
		return init != null;
	}
	public void print(){
		super.print();
		System.out.println("Initial Value: "+init);
	}
}
