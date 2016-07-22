package org.snrg_nyc.model;

import java.util.List;

import org.snrg_nyc.model.internal.NodeLayer;
import org.snrg_nyc.model.internal.NodeProperty;

import com.google.gson.annotations.SerializedName;

class NodeSettings implements Transferable  {
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
	@Override
	public String getObjectID() {
		return this.getClass().getSimpleName();
	}
}
