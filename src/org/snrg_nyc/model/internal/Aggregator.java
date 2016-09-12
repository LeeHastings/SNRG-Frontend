package org.snrg_nyc.model.internal;

import java.util.Map;
import java.util.HashMap;

import org.snrg_nyc.model.EditorException;

public class Aggregator {
	private 
	Map<Integer, BivariateDistribution> nodeProperties, layerProperties;
	
	private Map<Integer, Map<Integer, BivariateDistribution>> pathogenList;

	public Aggregator(){
		nodeProperties = new HashMap<>();
		layerProperties = new HashMap<>();
		pathogenList = new HashMap<>();
	}
	
	public BivariateDistribution 
	getNodeDist(int propertyID) throws EditorException{
		if(nodeProperties.containsKey(propertyID)){
			return nodeProperties.get(propertyID);
		}
		else {
			throw new EditorException("Unknown property ID in aggregator: "
					+ propertyID);
		}
	}
	public BivariateDistribution
	getLayerDist(int propertyID) throws EditorException{
		if(layerProperties.containsKey(propertyID)){
			return layerProperties.get(propertyID);
		}
		else {
			throw new EditorException(
					"Unknown layer property ID in aggregator: "
					+ propertyID);
		}
	}
	public BivariateDistribution
	getPathogenDist(int pathogenID, int propertyID) throws EditorException{
		if(pathogenList.containsKey(pathogenID)){
			if(pathogenList.get(pathogenID).containsKey(propertyID)){
				return pathogenList.get(pathogenID).get(propertyID);
			}
			else {
				throw new EditorException(
						"Unknown property ID in pathogen "
						+pathogenID + ": "
						+propertyID);
			}
		}
		else {
			throw new EditorException("Unknown pathogen ID: "+pathogenID);
		}
	}
	
	public void 
	setNodeDist(int propertyID, BivariateDistribution bd){
		nodeProperties.put(propertyID, bd);
	}
	public void
	setLayerDist(int propertyID, BivariateDistribution bd){
		layerProperties.put(propertyID, bd);
	}
	public void
	setPathogenDist(int pathogenID, int propertyID, BivariateDistribution bd){
		if(!pathogenList.containsKey(pathogenID)){
			pathogenList.put(pathogenID, new HashMap<>());
		}
		pathogenList.get(pathogenID).put(propertyID, bd);
	}
}
