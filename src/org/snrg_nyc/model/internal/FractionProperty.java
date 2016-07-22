package org.snrg_nyc.model.internal;

public class FractionProperty extends NodeProperty {
	private static final long serialVersionUID = 1L;
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
}
