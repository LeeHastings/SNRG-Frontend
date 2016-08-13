package org.snrg_nyc.model;

import com.google.gson.annotations.SerializedName;

class PathogenSettings extends NodeSettings {
	private static final long serialVersionUID = 1L;
	
	@SerializedName(value="PathogenID", alternate={"PathogenType"})
	private String pathogen;
	
	public PathogenSettings(String pathogen){
		super();
		this.pathogen = pathogen;
	}
	public String getName(){
		return pathogen;
	}
	@Override
	public String getObjectID() {
		return pathogen;
	}
}
