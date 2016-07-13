package org.snrg_nyc.model.editor;

import com.google.gson.annotations.SerializedName;

class PathogenSettings extends NodeSettings {
	private static final long serialVersionUID = 1L;
	
	@SerializedName("PathogenType")
	private String pathogen;
	
	public PathogenSettings(String pathogen){
		super();
		this.pathogen = pathogen;
	}

}
