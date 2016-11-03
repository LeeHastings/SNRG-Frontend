package org.snrg_nyc.model.internal;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.util.LambdaErrorWrapper;

public class Aggregator {
	private Map<Integer, BivariateDistribution> nodeProperties, layerProperties;
	
	private Map<Integer, Map<Integer, BivariateDistribution>> pathogenList;
	
	int layerID;

	{
		nodeProperties = new HashMap<>();
		layerProperties = new HashMap<>();
		pathogenList = new HashMap<>();
	}
	Aggregator(){};
	public Aggregator(int layerID){
		this.layerID = layerID;
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
	
	public int layerID(){
		return layerID;
	}
	
	public String name(PropertiesEditor e) throws EditorException{
		return("aggregator_"+e.layer_getName(layerID));
	}
	
	public Collection<BivariatDistributionSettings> 
	getBiDistSettings() throws EditorException{
		
		List<BivariatDistributionSettings> settings = new ArrayList<>();
		LambdaErrorWrapper<EditorException> err = new LambdaErrorWrapper<>();
		
		Consumer<? super BivariateDistribution> addToList = (b)->{
			if(err.hasError()){
				return;
			}
			try {
				settings.add(BivariatDistributionSettings.of(b));
			} 
			catch (EditorException e) {
				err.setError(e);
			}
		};
		nodeProperties.values().forEach(addToList);
		err.validate();
		layerProperties.values().forEach(addToList);
		err.validate();
		for(Map<Integer, BivariateDistribution> m : pathogenList.values()){
			m.values().forEach(addToList);
			err.validate();
		}
		return settings;
	}
	protected void
	addLayerDist(int plid, BivariateDistribution bd){
		layerProperties.put(plid, bd);
	}
	protected void
	addPropertyDist(int pid, BivariateDistribution bd){
		nodeProperties.put(pid, bd);
	}
	protected void
	addPathogenDist(int pathID, int pid, BivariateDistribution bd){
		if(!pathogenList.containsKey(pathID)){
			pathogenList.put(pathID, new HashMap<>());
		}
		pathogenList.get(pathID).put(pid, bd);
	}
	protected Set<Integer> 
	propertyIDs(){
		return nodeProperties.keySet();
	}
	protected Set<Integer>
	layerPropIDs(){
		return layerProperties.keySet();
	}
	protected Set<Integer>
	pathogenIDs(){
		return pathogenList.keySet();
	}
	protected Set<Integer>
	pathogenPropIDs(int pathID){
		return pathogenList.get(pathID).keySet();
	}
}
