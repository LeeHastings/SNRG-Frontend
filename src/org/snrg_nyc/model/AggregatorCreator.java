package org.snrg_nyc.model;

import java.util.Collection;
import java.util.Map;

/**
 * An interface for creating aggregators for layers
 * @author Devin Hastings
 *
 */
public interface AggregatorCreator {
	/**
	 * Create a new aggregator for a layer.
	 * @param lid The ID of the layer.
	 * @throws EditorException Thrown if the layer ID is invalid, or if the
	 * layer already has an aggregator
	 */
	public void 
	aggr_new(int lid) throws EditorException;
	
	/**
	 * Add a node property to the aggregator list
	 * @param pid The ID of the property to add
	 * @throws EditorException Thrown if the property ID is invalid
	 */
	public void
	aggr_addNodeProperty(int pid) throws EditorException;
	
	/**
	 * Remove a node property to the aggregator list
	 * @param pid The ID of the property to remove
	 * @throws EditorException Thrown if the property ID is invalid
	 */
	public void
	aggr_removeNodeProperty(int pid) throws EditorException;
	
	/**
	 * Add a property from the aggregator's layer into the aggregator list
	 * @param pid The ID of the property in the layer to add
	 * @throws EditorException Thrown if the property ID is not valid
	 */
	public void 
	aggr_addLayerProperty(int pid) throws EditorException;
	
	/**
	 * Remove a layer property from the aggregator list
	 * @param pid The ID of the property to remove
	 * @throws EditorException Thrown if the property ID is not valid
	 */
	public void 
	aggr_removeLayerProperty(int pid) throws EditorException;
	
	/**
	 * Add a pathogen property to the aggregator list
	 * @param pathID The ID of the pathogen
	 * @param pid The ID of the pathogen property
	 * @throws EditorException Thrown if the IDs are invalid
	 */
	public void 
	aggr_addPathogenProperty(int pathID, int pid) throws EditorException;
	
	/**
	 * Remove an added pathogen property from the aggregator list
	 * @param pathID The ID of the pathogen
	 * @param pid The ID of the pathogen property
	 * @throws EditorException Thrown if the IDs are invalid
	 */
	public void
	aggr_removePathogenProperty(int pathID, int pid) throws EditorException;
	
	/**
	 * Get the IDs of added properties in the aggregator
	 * @return The property IDs added to the aggregator
	 */
	public Collection<Integer>
	aggr_nodePropertyIDs();
	
	/**
	 * Get the IDs of layer properties in the aggregator
	 * @return The layer property IDs added to the aggregator
	 */
	public Collection<Integer>
	aggr_layerPropertyIDs();
	
	/**
	 * Get the IDs of the pathogens available to the aggregator
	 * @return The pathogen IDs added to the aggregators
	 */
	public Collection<Integer>
	aggr_pathogenIDs();
	
	/**
	 * Get the pathogen property IDs added to the aggregator
	 * @param pathID The pathogen ID 
	 * @return The pathogen property IDs added to the aggregator
	 * @throws EditorException Thrown if the pathID is invalid
	 */
	public Collection<Integer>
	aggr_pathogenPropertyIDs(int pathID) throws EditorException;
	
	/**
	 * Get a copy of the bivariate distribution linked to this node property
	 * @param pid The ID of the property
	 * @return A double map of the property's range IDs to probabilities
	 * @throws EditorException Thrown if the pid is invalid
	 */
	public Map<Integer, Map<Integer, Float>>
	aggr_getNodePropertyBiDist(int pid) throws EditorException;

	/**
	 * Set the bivariate distribution in the given property
	 * @param pid The ID of the property
	 * @param map The new double map of the property's range IDs to weights
	 * @throws EditorException Thrown if the pid is invalid, or if the map is
	 * malformed (the range IDs aren't all set, there are negative weights, 
	 * etc.)
	 */
	public void
	aggr_setNodePropertyBiDist(int pid, Map<Integer, Map<Integer, Float>> map) 
			throws EditorException;
	
	/**
	 * Get the bivariate distribution for a layer property in the aggregator
	 * @param pid The ID of the layer property
	 * @return A double map of the property's range IDs to weights
	 * @throws EditorException Thrown if the pid is invalid
	 */
	public Map<Integer, Map<Integer, Float>>
	aggr_getLayerPropertyBiDist(int pid) throws EditorException;
	
	/**
	 * Set the bivariate distribution for an added layer property
	 * @param pid The ID of the layer property 
	 * @param map A double map of the property's range IDs to weights
	 * @throws EditorException Thrown if the pid is invalid, or if the map is
	 * malformed (the range IDs aren't all set, there are negative weights, 
	 * etc.)
	 */
	public void 
	aggr_setLayerPropertyBiDist(int pid, Map<Integer, Map<Integer, Float>> map)
			throws EditorException;
	
	/**
	 * Get the bivariate distribution linked to the given pathogen property
	 * @param pathID The ID of the pathogen
	 * @param pid The ID of the property in the pathogen
	 * @return A double map of the property's range IDs to weights
	 * @throws EditorException Thrown if the IDs are invalid
	 */
	public Map<Integer, Map<Integer, Float>>
	aggr_getPathPropertyBiDist(int pathID, int pid) throws EditorException;
	
	/**
	 * Set the bivariate distribution of the pathogen property
	 * @param pathID The ID of the pathogen
	 * @param pid The ID of the property in the pathogen
	 * @param map A double map of the property's range IDs to weights
	 * @throws EditorException Thrown if the IDs are invalid, or if the map is
	 * malformed (the range IDs aren't all set, there are negative weights, 
	 * etc.)
	 */
	public void
	aggr_setPathPropertyBiDist(int pathID, int pid, 
			Map<Integer, Map<Integer, Float>> map) 
			throws EditorException;
}
