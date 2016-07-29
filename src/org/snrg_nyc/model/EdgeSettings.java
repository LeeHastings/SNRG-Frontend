package org.snrg_nyc.model;

import com.google.gson.annotations.SerializedName;

class EdgeSettings extends SNRG_Settings{
	private static final long serialVersionUID = 1L;
	
	@SerializedName("LayerID")
	private String layerName;
	
	@SerializedName("TriangleBias")
	private int bias = 3;
	
	@SerializedName("TriangleBiasCumulative")
	private boolean cumulativeBias = true;
	
	public EdgeSettings(String layerName){
		super();
		this.layerName = layerName;
	}
	
	public String getLayerName(){
		return layerName;
	}
	public void setLayerName(String name){
		if(name == null || name.length() == 0){
			throw new IllegalArgumentException("Cannot use an empty/null layer name");
		}
		else {
			layerName = name;
		}
	}
	
	@Override
	public String getObjectID() {
		return layerName;
	}

}
