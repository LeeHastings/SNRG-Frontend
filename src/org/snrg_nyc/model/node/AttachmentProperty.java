package org.snrg_nyc.model.node;

import com.google.gson.annotations.SerializedName;

class AttachmentProperty extends BooleanProperty {
	private static final long serialVersionUID = 1L;
	
	@SerializedName("PathogenType")
	private String pathogen;	
	public AttachmentProperty(){
		super();
		pathogen = null;
	}
	public AttachmentProperty(String name, String description){
		super(name, description);
		pathogen = null;
	}
	public String getPathogen(){
		return pathogen;
	}
	public void setPathogen(String pathogen){
		this.pathogen = pathogen;
		name = "infection_attachment_"+pathogen;
	}
}
