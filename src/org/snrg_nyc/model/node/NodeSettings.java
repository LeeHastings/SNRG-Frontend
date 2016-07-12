package org.snrg_nyc.model.node;

import java.io.Serializable;
import java.util.List;

import org.snrg_nyc.model.NodeLayer;
import org.snrg_nyc.model.NodeProperty;

import com.google.gson.annotations.SerializedName;

class NodeSettings implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@SerializedName("PropertyDefinitionList")
	private List<NodeProperty> properties;
	
	@SerializedName("LayerAttributesList")
	private List<NodeLayer> layers;
	
	public NodeSettings(){
		properties = null;
		layers = null;
	}
	public void setPropertyDefinitionList(List<NodeProperty> props){
		properties = props;
	}
	public void setLayerAttributesList(List<NodeLayer> layers){
		this.layers = layers;
	}
	public List<NodeProperty> getPropertyDefinitionList(){
		return properties;
	}
	public List<NodeLayer> getLayerAttributesList(){
		return layers;
	}
}
