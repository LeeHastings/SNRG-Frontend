package org.snrg_nyc.model.internal;

public class AttachmentProperty extends BooleanRangeProperty {
	private static final long serialVersionUID = 1L;
	
	private int pathogenID;	
	private String pathogenName;
	private boolean set;
	public AttachmentProperty(){
		super();
	}
	public AttachmentProperty(String name, String description){
		super(name, description);
	}
	public boolean isSet(){
		return set;
	}
	public int getPathogenID(){
		return pathogenID;
	}
	public void setPathogenID(int pathogen){
		if(set){
			throw new IllegalStateException(
					"Cannot change the pathogen an attachment property is bound to");
		}
		else {
			this.pathogenID = pathogen;
		}
	}
	public String getPathogenName(){
		return pathogenName;
	}
	public void setPathogenName(String name){
		pathogenName = name;
		this.name = "infection_attachment_"+name;
	}
}
