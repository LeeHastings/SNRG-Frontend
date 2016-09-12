package org.snrg_nyc.model;

import java.util.ArrayList;
import java.util.List;

import org.snrg_nyc.model.properties.NodeProperty;
import org.snrg_nyc.persistence.Transferable;

import com.google.gson.annotations.SerializedName;

abstract class SNRG_Settings extends Transferable{
	private static final long serialVersionUID = 1L;
	
	@SerializedName("PropertyDefinitionList")
	private List<NodeProperty> properties;
	
	public SNRG_Settings(){
		properties = new ArrayList<>();
	}

	public List<NodeProperty> 
	getPropertyDefinitionList(){
		return properties;
	}

	public void 
	setPropertyDefinitionList(List<NodeProperty> props){
		properties = props;
	}
}
