package org.snrg_nyc.model;

import java.util.List;
import org.snrg_nyc.model.internal.NodeLayer;
import com.google.gson.annotations.SerializedName;

class NodeSettings extends SNRG_Settings  {
	private static final long serialVersionUID = 2L;
	
	@SerializedName("LayerAttributesList")
	private List<NodeLayer> layers;
	
	public NodeSettings(){
		layers = null;
	}
	public void setLayerAttributesList(List<NodeLayer> layers){
		this.layers = layers;
	}
	public List<NodeLayer> getLayerAttributesList(){
		return layers;
	}
	@Override
	public String getObjectID() {
		return this.getClass().getSimpleName();
	}
}
