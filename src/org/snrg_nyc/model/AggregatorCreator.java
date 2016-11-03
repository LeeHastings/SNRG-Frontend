package org.snrg_nyc.model;

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
	
}
