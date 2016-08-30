package org.snrg_nyc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.snrg_nyc.model.internal.BivariatDistributionSettings;
import org.snrg_nyc.model.internal.BivariateDistribution;
import org.snrg_nyc.persistence.Transferable;

class AggregatorSettings implements Transferable {
	private static final long serialVersionUID = 1L;
	
	enum Op {
		SUM,
		MULTIPLY
	}
	private Op operation;
	private String layerName;
	
	Map<Integer, BivariateDistribution> nodeMap, layerMap;
	
	Map<Integer, Map<Integer, BivariateDistribution>> 
		pathogenMap;
	
	{
		nodeMap = new HashMap<>();
		pathogenMap = new HashMap<>();
		layerMap = new HashMap<>();
	}
	
	public AggregatorSettings(String layer){
		layerName = layer;
	}
	public void setOperation(Op operation){
		this.operation = operation;
	}
	
	public Collection<BivariatDistributionSettings> 
	getDistributionSettings() {

		Collection<BivariatDistributionSettings> bdSettings = new ArrayList<>();
		BiConsumer<? super Integer, ? super BivariateDistribution> 
			consumer = 
			(id, dist) ->{
				try{
					bdSettings.add(new BivariatDistributionSettings(dist));
				}
				catch(Exception e){
					e.printStackTrace();
				}
			};
			
		nodeMap.forEach(consumer);
		pathogenMap.values().forEach(map->map.forEach(consumer));
		layerMap.forEach(consumer);
		
		return bdSettings;
		
	}
	
	public void
	setNodePropertyDist(int pid, BivariateDistribution bd){
		nodeMap.put(pid, bd);
	}
	public void
	setPathogenDist(int pathID, int pid, BivariateDistribution bd){
		if(!pathogenMap.containsKey(pathID)) {
			pathogenMap.put(pathID, new HashMap<>());
		}
		pathogenMap.get(pathID).put(pid, bd);
	}
	public void
	setLayerDist(int pid, BivariateDistribution bd){
		layerMap.put(pid, bd);
	}
	
	public BivariateDistribution 
	getNodePropertyDistribution(int pid){
		return nodeMap.get(pid);
	}
	public BivariateDistribution
	getPathogenDistribution(int pathID, int pid){
		return pathogenMap.get(pathID).get(pid);
	}
	public BivariateDistribution
	getLayerDistribution( int pid){
		return layerMap.get(pid);
	}
	
	@Override
	public String 
	getObjectID() {
		return layerName;
	}
	
}
