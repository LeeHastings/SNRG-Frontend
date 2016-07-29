package org.snrg_nyc.model;

import java.util.List;

import org.snrg_nyc.model.internal.NodeProperty;

import com.google.gson.annotations.SerializedName;

abstract class SNRG_Settings implements Transferable{
	
	@SerializedName("PropertyDefinitionList")
	private List<NodeProperty> properties;
	
	public SNRG_Settings(){
		properties = null;
	}

	public List<NodeProperty> getPropertyDefinitionList(){
		return properties;
	}

	public void setPropertyDefinitionList(List<NodeProperty> props){
		properties = props;
	}

}
