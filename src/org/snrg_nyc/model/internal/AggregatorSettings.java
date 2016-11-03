package org.snrg_nyc.model.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.persistence.Transferable;

import com.google.gson.annotations.SerializedName;

public class AggregatorSettings implements Transferable {
	private static final long serialVersionUID = 1L;

	static class BiDistLink{
		@SerializedName("PropertyName")
		private String prop;
		@SerializedName("BiDistributionID")
		private String distID;
		
		BiDistLink(String prop, String dist){
			this.prop = prop;
			this.distID = dist;
		}
	}
	static class PathogenLink{
		@SerializedName("PathogenID")
		private String pathID;
		@SerializedName("PathogenProperties")
		private List<BiDistLink> links;
		
		PathogenLink(String id, List<BiDistLink> links){
			this.pathID = id;
			this.links = links;
		}
	}
	@SerializedName("BindToLayerID")
	private String layerName;
	
	@SerializedName("NodeProperties")
	private List<BiDistLink> nodes;
	
	@SerializedName("LayerAttributes")
	private List<BiDistLink> layers;
	
	@SerializedName("PathogensList")
	private List<PathogenLink> pathogens;
	
	AggregatorSettings(){
		nodes = new ArrayList<>();
		layers = new ArrayList<>();
		pathogens = new ArrayList<>();
	}
	
	/**
	 * Create a serializable object from an {@link Aggregator}.
	 * @param a The {@link Aggregator} to convert
	 * @param e The {@link PropertiesEditor} that the aggregator is from.
	 * @return A new {@link AggregatorSettings} object made from the data in the
	 * given Aggregator.
	 * @throws EditorException Thrown if the {@link Aggregator} has invalid 
	 * information (or the wrong {@link PropertiesEditor} is given.
	 */
	public static AggregatorSettings 
	of(Aggregator a, PropertiesEditor e) throws EditorException{
		AggregatorSettings as = new AggregatorSettings();
		
		for(Integer pid : a.propertyIDs()){
			as.nodes.add(new BiDistLink(
					e.nodeProp_getName(pid), 
					a.getNodeDist(pid).getId()));
		}
		
		int lid = a.layerID();
		as.layerName = e.layer_getName(lid);
		
		for(Integer plid : a.layerPropIDs()){
			if(plid == null){
				throw new EditorException(
						"There are null property IDs in layer "+
						e.layer_getName(lid)+"!");
			}
			as.layers.add(new BiDistLink(
					e.nodeProp_getName(lid, plid),
					a.getLayerDist(lid).getId()));
		}
		
		for(int pathID : a.pathogenIDs()){
			List<BiDistLink> pathList = new ArrayList<>();
			PropertiesEditor p = e.pathogen_getEditor(pathID);
			for(int pid : a.pathogenPropIDs(pathID)){
				pathList.add(new BiDistLink(
						p.nodeProp_getName(pid), 
						a.getPathogenDist(pathID, pid).getId()));
			}
			as.pathogens.add(
					new PathogenLink(e.pathogen_getName(pathID), pathList));
		}
		return as;
	}
	/**
	 * Convert a serializable {@link AggregatorSettings} object into the 
	 * internal {@link Aggregator} object
	 * @param e The {@link PropertiesEditor} that data will be drawn from
	 * @param bidists The list of {@link BivariateDistribution} objects that
	 * will be mapped to properties in the aggregator.
	 * @return An instance of {@link Aggregator} based on the data stored in 
	 * this object
	 * @throws EditorException Thrown if this object is somehow invalid, 
	 * likely because some information has not been loaded yet.
	 */
	public Aggregator 
	toInternal(PropertiesEditor e, Map<String, BivariateDistribution> bidists) 
			throws EditorException
	{
		Integer layerID = e.search_layerWithName(layerName);
		if(layerID == null){
			throw new EditorException("No layer with name "+layerName);
		}
		Aggregator a = new Aggregator(layerID);
		
		for(BiDistLink bdlink : nodes){
			a.addPropertyDist(
					e.search_nodePropWithName(bdlink.prop), 
					getDist(bidists, bdlink.distID));
		}
		for(BiDistLink bdLink : layers){
			Integer pid = e.search_nodePropWithName(bdLink.prop, layerID);
			if(pid == null){
				throw new EditorException("No property with name "+bdLink.prop+
						" in layer "+e.layer_getName(layerID));
			}
			a.addLayerDist(
					pid,
					getDist(bidists, bdLink.distID));
		}
		for(PathogenLink plink : pathogens){
			int pathID = e.search_pathogenWithName(plink.pathID);
			PropertiesEditor p = e.pathogen_getEditor(pathID);
			
			for(BiDistLink bdlink : plink.links){
				a.addPathogenDist(
						pathID,
						p.search_nodePropWithName(bdlink.prop), 
						getDist(bidists, bdlink.distID));
			}
		}
		return a;
	}
	
	private BivariateDistribution
	getDist(Map<String, BivariateDistribution> bidists, String distID) 
			throws EditorException
	{
		if(bidists.containsKey(distID)){
			return bidists.get(distID);
		}
		else {
			String msg = "No Bivariate Distribution with ID: "+distID
					+"\nAvailable distributions:\n";
			for(String id : bidists.keySet()){
				msg += "\t"+id+"\n";
			}
			throw new EditorException(msg);
		}
	}
	
	@Override
	public String getObjectID() {
		return layerName;
	}
	
}
