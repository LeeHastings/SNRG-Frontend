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
	private EdgeSettings parent;
	
	private Map<Integer, BivariateDistribution> nodeMap;
	
	private Map<Integer, Map<Integer, BivariateDistribution>> 
		pathogenMap, layersMap;
	
	{
		nodeMap = new HashMap<>();
		pathogenMap = new HashMap<>();
		layersMap = new HashMap<>();
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
		layersMap.values().forEach(map->map.forEach(consumer));
		
		return bdSettings;
		
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
	getLayerDistribution(int lid, int pid){
		return layersMap.get(lid).get(pid);
	}
	
	@Override
	public String 
	getObjectID() {
		return parent.getLayerName();
	}
	
}
