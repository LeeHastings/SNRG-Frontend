package org.snrg_nyc.model.internal;

import org.snrg_nyc.util.Transferable;

import com.google.gson.annotations.SerializedName;

public class ExperimentInfo implements Transferable {
	private static final long serialVersionUID = 1L;
	
	@SerializedName("ExperimentName")
	private String name = "default";
	
	@SerializedName("Description")
	private String description = "";
	
	@SerializedName("UserName")
	private String user = "default";
	
	public ExperimentInfo(){}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String 
	getObjectID() {
		return ExperimentInfo.class.getSimpleName();
	}
	
}