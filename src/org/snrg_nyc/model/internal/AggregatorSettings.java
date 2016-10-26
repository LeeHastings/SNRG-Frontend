package org.snrg_nyc.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
		
		for(Integer pid : a.nodeProperties.keySet()){
			as.nodes.add(new BiDistLink(
					e.nodeProp_getName(pid), 
					a.getNodeDist(pid).getId()));
		}
		
		int lid = a.layerID();
		for(Integer plid : a.layerProperties.keySet()){
			as.layers.add(new BiDistLink(
					e.nodeProp_getName(lid, plid),
					a.layerProperties.get(lid).getId()));
		}
		
		for(int pathID : a.pathogenList.keySet()){
			List<BiDistLink> pathList = new ArrayList<>();
			PropertiesEditor p = e.pathogen_getEditor(pathID);
			for(int pid : a.pathogenList.get(pathID).keySet()){
				pathList.add(new BiDistLink(
						p.nodeProp_getName(pid), 
						a.pathogenList.get(pathID).get(pid).getId()));
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
	 * @param biDists The list of {@link BivariateDistribution} objects that
	 * will be mapped to properties in the aggregator.
	 * @return An instance of {@link Aggregator} based on the data stored in 
	 * this object
	 * @throws EditorException Thrown if this object is somehow invalid, 
	 * likely because some information has not been loaded yet.
	 */
	public Aggregator 
	toInternal(PropertiesEditor e, Collection<BivariateDistribution> biDists) 
			throws EditorException
	{
		int layerID = e.search_layerWithName(layerName);
		Aggregator a = new Aggregator(layerID);
		
		for(BiDistLink bdlink : nodes){
			a.nodeProperties.put(
					e.search_nodePropWithName(bdlink.prop), 
					popDist(biDists, bdlink.distID));
		}
		for(BiDistLink bdLink : nodes){
			a.layerProperties.put(
					e.search_nodePropWithName(bdLink.prop, layerID),
					popDist(biDists, bdLink.distID));
		}
		for(PathogenLink plink : pathogens){
			int pathID = e.search_pathogenWithName(plink.pathID);
			PropertiesEditor p = e.pathogen_getEditor(pathID);
			a.pathogenList.put(pathID, new HashMap<>());
			
			for(BiDistLink bdlink : plink.links){
				a.pathogenList.get(pathID).put(
						p.search_nodePropWithName(bdlink.prop), 
						popDist(biDists, bdlink.distID));
			}
		}
		return a;
	}
	
	private BivariateDistribution
	popDist(Collection<BivariateDistribution> dists, String distID) 
			throws EditorException
	{
		Iterator<BivariateDistribution> distItr = dists.iterator();
		while(distItr.hasNext()){
			BivariateDistribution dist = distItr.next();
			if(dist.getId().equals(distID)){
				distItr.remove();
				return dist;
			}
		}
		throw new EditorException("No Bivariate Distribution with ID: "+distID);
	}
	
	@Override
	public String getObjectID() {
		return layerName;
	}
	
}
