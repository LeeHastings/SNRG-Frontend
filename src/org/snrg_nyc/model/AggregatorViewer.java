package org.snrg_nyc.model;

import java.util.Collection;
import java.util.Map;

/**
 * The interface for viewing committed aggregator objects
 * @author Devin Hastings
 *
 */
public interface AggregatorViewer {
	
	/**
	 * Get the layer ID this aggregator is bound to
	 * @param aID The ID of the aggregator
	 * @return The layer ID bound to the aggregator
	 * @throws EditorException Thrown if the aID is invalid
	 */
	public int
	aggrV_getLayerID(int aID) throws EditorException;
	
	/**
	 * Get the node property IDs listed in the aggregator
	 * @param aID The ID of the aggregator
	 * @return The list of node property IDs linked to the aggregator
	 * @throws EditorException Thrown if the aID is invalid
	 */
	public Collection<Integer>
	aggrV_getNodePropertyIDs(int aID) throws EditorException;
	
	/**
	 * Get the layer property IDs listed in the aggregator
	 * @param aID The ID of the aggregator
	 * @return The list of layer properties added to the aggregator
	 * @throws EditorException Thrown if the aID is invalid
	 */
	public Collection<Integer>
	aggrV_getLayerPropertyIDs(int aID) throws EditorException;
	
	/**
	 * Get the pathogen IDs of the aggregator
	 * @param aID The ID of the aggregator
	 * @return The list of pathogen IDs added to the aggregator
	 * @throws EditorException Thrown if the aID is invalid
	 */
	public Collection<Integer>
	aggrV_getPathogenIDs(int aID) throws EditorException;
	
	/**
	 * Get the ID
	 * @param aID The ID of the aggregator
	 * @param pathID The ID of a pathogen added to the aggregator
	 * @return The pathogen property IDs added to the aggregator
	 * @throws EditorException Thrown if the aID and pathID are invalid
	 */
	public Collection<Integer>
	aggrV_getPathPropertyIDs(int aID, int pathID) throws EditorException;
	
	/**
	 * Get the bivariate distribution linked to this node property in the 
	 * aggregator
	 * @param aID The ID of the aggregator
	 * @param pid The ID of a node property added to the aggregator
	 * @return A double map of the property's range IDs to weights
	 * @throws EditorException Thrown if the aID and pid are invalid
	 */
	public Map<Integer, Map<Integer, Float>>
	aggrV_getNodePropBiDist(int aID, int pid) throws EditorException;
	
	/**
	 * Get the bivariate distribution linked to this layer property in the 
	 * aggregator
	 * @param aID The ID of the aggregator
	 * @param pid The ID of the layer property
	 * @return A double map of the property's range IDs to weights
	 * @throws EditorException
	 */
	public Map<Integer, Map<Integer, Float>>
	aggrV_getLayerPropBiDist(int aID, int pid) throws EditorException;
	
	/**
	 * Get the bivaraite distribution linked to this pathogen property in the 
	 * aggregator
	 * @param aID The ID of the aggregator
	 * @param pathID The ID of an added pathogen
	 * @param pid The ID of the pathogen property to look at
	 * @return A double map of the property's range IDs to weights
	 * @throws EditorException Thrown if the IDs are invalid
	 */
	public Map<Integer, Map<Integer, Float>>
	aggrV_getPathPropBiDist(int aID, int pathID, int pid) 
			throws EditorException;
}
