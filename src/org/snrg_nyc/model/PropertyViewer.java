package org.snrg_nyc.model;

import java.util.List;
import java.util.Map;

/**
 * The interface for viewing properties.  Part of {@link PropertiesEditor}.
 * @author Devin Hastings
 *
 */
public interface PropertyViewer {
	/*                       *\
	 * Node Property Getters *
	\*                       */
	
	/**
	 * Get the IDs of all the node properties, which are used by most methods 
	 * beginning with the
	 * <i>nodeProp</i>prefix
	 * @return A list of node property IDs
	 */
	public List<Integer> 
	nodeProp_getPropertyIDs();
	
	/**
	 * Get the node property IDs for a layer
	 * @param lid The ID of the layer to search in, should be from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @return A list of node property IDs for the layer
	 * @throws EditorException Thrown if the layer ID is not valid
	 */
	public List<Integer> 
	nodeProp_getPropertyIDs(int lid) throws EditorException;
	
	/**
	 * Get the unique IDs for all ranges in a Ranged node property
	 * @param pid The ID of the node property, it must be a ranged property
	 * @return The IDs of the ranges in a node property
	 * @throws EditorException Thrown if the pid is invalid, or if the property 
	 * given is not ranged.
	 */
	public List<Integer> 
	nodeProp_getRangeItemIDs(int pid) throws EditorException;
	
	/**
	 * Get the unique IDs for all ranges in a Ranged node property within a 
	 * layer
	 * @param lid The ID of the layer, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}.
	 * It must also be a ranged property.
	 * @return The IDs of the range items in the property
	 * @throws EditorException Thrown if the lid or pid is invalid, or if 
	 * the property linked to is not a ranged property
	 */
	public List<Integer> 
	nodeProp_getRangeItemIDs(int lid, int pid) throws EditorException;
	
	/**
	 * Get the label on a node property's range
	 * @param pid The ID of the node property, it must be a ranged property
	 * @param rid The ID of the range label, must be in 
	 * {@link PropertyViewer#nodeProp_getRangeItemIDs(int)}
	 * @return The label of the given range item
	 * @throws EditorException Thrown if the pid or rid is invalid, or if the 
	 * property given is not ranged.
	 */
	public String 
	nodeProp_getRangeLabel(int pid, int rid) throws EditorException;
	
	/**
	 * Get a label on a range item in a layer property
	 * @param lid The ID of the layer, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}.
	 * It must also be a ranged property.
	 * @param rid The ID of the range item, from 
	 * {@link PropertyViewer#nodeProp_getRangeItemIDs(int, int)}
	 * @return The label of the range item
	 * @throws EditorException Thrown if the lid, pid, or rid is invalid, or if 
	 * the property linked to is not a ranged property
	 */
	public String 
	nodeProp_getRangeLabel(int lid, int pid, int rid) throws EditorException;
	
	/**
	 * Get the upper bound of a node property's range
	 * @param pid The ID of the node property, it must be a ranged property
	 * @param rid The ID of the range, must be in 
	 * {@link PropertyViewer#nodeProp_getRangeItemIDs(int)}
	 * @return The upper bound of the range, as a numerical string
	 * @throws EditorException Thrown if the pid or rid is invalid, or if the 
	 * property given is not ranged.
	 */
	public int nodeProp_getRangeMax(int pid, int rid) throws EditorException;
	
	/**
	 * Get a upper bound on a range item in a layer property
	 * @param lid The ID of the layer, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}.
	 * It must also be a ranged property.
	 * @param rid The ID of the range item, from 
	 * {@link PropertyViewer#nodeProp_getRangeItemIDs(int, int)}
	 * @return The upper bound of the range item as a string
	 * @throws EditorException Thrown if the lid, pid, or rid is invalid, or if 
	 * the property linked to is not a ranged property
	 */
	public int 
	nodeProp_getRangeMax(int lid, int pid, int rid) throws EditorException;
	
	/**
	 * Get the lower bound for a node property's range
	 * @param pid The ID of the node property, it must be a ranged property
	 * @param rid The ID of the range item in the property, must be in 
	 * {@link PropertyViewer#nodeProp_getRangeItemIDs(int)}
	 * @return The lower bound of the range as a string
	 * @throws EditorException Thrown if the pid or rid is invalid, or if the 
	 * property given is not ranged.
	 */
	public int 
	nodeProp_getRangeMin(int pid, int rid) throws EditorException;
	
	/**
	 * Get the lower bound for the range item in a layer property
	 * @param lid The ID of the layer, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}.
	 * It must also be a ranged property.
	 * @param rid The ID of the range item, from 
	 * {@link PropertyViewer#nodeProp_getRangeItemIDs(int, int)}
	 * @return The lower bound of the range item as a string
	 * @throws EditorException Thrown if the lid, pid, or rid is invalid, or if 
	 * the property linked to is not a ranged property
	 */
	public int 
	nodeProp_getRangeMin(int lid, int pid, int rid) throws EditorException;
	
	/**
	 * Get the name of a node property.
	 * @param pid The ID of the node property, should be from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs()}
	 * @return The name of the property.
	 * @throws EditorException Thrown if the ID does not point to a valid node
	 * property.
	 */
	public String 
	nodeProp_getName(int pid) throws EditorException;
	
	/**
	 * Return the name of a layer property
	 * @param lid The ID of the layer, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}.
	 * @return The name of the layer property
	 * @throws EditorException Thrown if the layer or property ID does not point
	 *  to a non-null item.
	 */
	public String 
	nodeProp_getName(int lid, int pid) throws EditorException;
	
	/**
	 * Get the pathogen ID for an attachment property
	 * @param pid The Property ID
	 * @return The pathogen ID
	 * @throws EditorException Thrown if the property is not an attachment 
	 * property
	 */
	public int 
	nodeProp_getPathogenID(int pid) throws EditorException;
	
	/**
	 * Get the pathogen ID for an attachment property
	 * @param lid The layer ID
	 * @param pid The Property ID
	 * @return The pathogen ID
	 * @throws EditorException Thrown if the layer or pathogen ID is invalid,
	 * or if the property is not an attachment property
	 */
	public int 
	nodeProp_getPathogenID(int lid, int pid) throws EditorException;
	
	/**
	 * Get the type of a node property.
	 * @param pid The ID of the node property, should be from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs()}
	 * @return The type of the property as a string.
	 * @throws EditorException Thrown if the ID does not point to a valid node 
	 * property.
	 */
	public String 
	nodeProp_getType(int pid) throws EditorException;
	
	/**
	 * Get the type of a layer property
	 * @param lid The ID of the layer, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}.
	 * @return The type of the property, which is one of 
	 * {@link PropertiesEditor#getPropertyTypes()}
	 * @throws EditorException Thrown if the layer or property ID does not point
	 *  to a non-null item.
	 */
	public String 
	nodeProp_getType(int lid, int pid) throws EditorException;
	
	/**
	 * Get the dependency level of a node property.
	 * @param pid The ID of the node property, should be from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs()} 
	 * @return The dependency level, as an integer
	 * @throws EditorException Thrown if the ID does not point to a valid node 
	 * property.
	 */
	public int 
	nodeProp_getDependencyLevel(int pid) throws EditorException;
	
	/**
	 * Get the dependency level of a node property in a layer
	 * @param lid The ID of the layer, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}.
	 * @return The dependency level of the given layer property.
	 * @throws EditorException Thrown if the layer or property ID does not point
	 *  to a non-null item.
	 */
	public int 
	nodeProp_getDependencyLevel(int lid, int pid) throws EditorException;
	
	/**
	 * Get the initial value of a fraction node property (the same value given 
	 * in {@link PropertyCreator#scratch_setFractionInitValue(float)}
	 * @param pid The ID of the fraction node property to use, should be from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs()}
	 * @return The intitial value which the fraction property was set to use.
	 * @throws EditorException Thrown if the PID is not valid, or if the 
	 * property is not a fraction property.
	 */
	public float 
	nodeProp_getFractionInitValue(int pid) throws EditorException;
	/**
	 * Identical to {@link PropertyViewer#nodeProp_getFractionInitValue(int)},
	 * but for layer properties.
	 * @param lid The node layer ID, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The property ID, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}
	 * @return The initial value the fractional property was set to use.
	 * @throws EditorException Thrown if the LID or PID is invalid, or if the 
	 * given property
	 * is not a fraction property.
	 */
	public float 
	nodeProp_getFractionInitValue(int lid, int pid) throws EditorException;
	
	/**
	 * Get the initial value on a boolean property
	 * @param pid The ID of the property
	 * @return The value the property was set to
	 * @throws EditorException Thrown id the PID is invalid or if the given
	 * property is not a boolean property.
	 */
	public boolean nodeProp_getBooleanInitValue(int pid) throws EditorException;
	
	/**
	 * Get the initial value on a boolean property
	 * @param lid The layer ID
	 * @param pid The property ID in the layer
	 * @return The value the property was set to
	 * @throws EditorException Thrown id the LID or PID is invalid, or if the
	 * given property is not a boolean property.
	 */
	public boolean 
	nodeProp_getBooleanInitValue(int lid, int pid) throws EditorException;
	
	/**
	 * Get a node property's description
	 * @param pid The ID of the node property, should be from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs()} 
	 * @return The description
	 * @throws EditorException Thrown if the ID does not point to a valid node 
	 * property.
	 */
	public String nodeProp_getDescription(int pid) throws EditorException;
	
	/**
	 * Get the description of a node property
	 * @param lid The ID of the layer, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}.
	 * @return The desctrption of the property.
	 * @throws EditorException Thrown if the layer or property ID does not point
	 *  to a non-null item.
	 */
	public String 
	nodeProp_getDescription(int lid, int pid) throws EditorException;
	
	/**
	 * Get a list of the pIDs for the given node property's dependencies.
	 * @param pid The ID of the node property, should be from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs()} 
	 * @return A list of PIDs added to the node property as dependencies
	 * @throws EditorException Thrown if the ID does not point to a valid node 
	 * property.
	 */
	public List<Integer> 
	nodeProp_getDependencyIDs(int pid) throws EditorException;
	
	/**
	 * Returns a list of node property IDs <i>within the base properties</i> 
	 * with a lower dependency level.  Currently, layer properties cannot be 
	 * used as dependencies.
	 * @param lid The layer ID of the property in question.
	 * @param pid The property ID of the property in question.
	 * @return A list of property IDs used as dependencies in the node property.
	 * @throws EditorException Thrown if the combination if layer and property 
	 * IDs does not point to a valid node property.
	 */
	public List<Integer> 
	nodeProp_getDependencyIDs(int lid, int pid) throws EditorException;
	
	/**
	 * Whether or not the given node property is ranged, which implies it has 
	 * range items and distribution items
	 * @param pid The Node Property ID to check.
	 * @return Whether or not the node property uses distributions.
	 * @throws EditorException Thrown if the PID does not point to a valid node 
	 * property.
	 */
	public boolean 
	nodeProp_isRangedProperty(int pid) throws EditorException;
	
	/**
	 * Check if the given layer property is ranged, meaning it has range items 
	 * and uses distributions
	 * @param lid The ID of the layer, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}
	 * @return If the property is ranged
	 * @throws EditorException Thrown if the layer or property ID does not point
	 *  to a non-null item.
	 */
	public boolean 
	nodeProp_isRangedProperty(int lid, int pid) throws EditorException;
	
	/**
	 * Check if the distribution for a property is uniform
	 * @param pid The property ID
	 * @return If the property's distribution is uniform
	 * @throws EditorException Thrown if the pid is invalid
	 */
	public boolean
	nodeProp_hasUniformDistribution(int pid) throws EditorException;
	
	/**
	 * Check if the distribution for a property is uniform
	 * @param lid The layer ID of the property
	 * @param pid The property ID
	 * @return If the property's distribution is uniform
	 * @throws EditorException Thrown if the layer or property ID is invalid
	 */
	public boolean 
	nodeProp_hasUniformDistribution(int lid, int pid) throws EditorException;
	
	/**
	 * Get the pathogen type of an Attachment Property
	 * @param pid The ID of the property
	 * @return The pathogen type of the property
	 * @throws EditorException Thrown if the ID is invalid, or if the property 
	 * is not an Attachment Property
	 */
	public String 
	nodeProp_getPathogenType(int pid) throws EditorException;
	
	/**
	 * Get the pathogen type of an Attachment Property in a layer
	 * @param lid The layer ID the property is in
	 * @param pid The ID of the Attachment Property
	 * @return The pathogen type of the property
	 * @throws EditorException Thrown if the layer/property ID combination is 
	 * invalid, or if the property is not an Attachment Property
	 */
	public String 
	nodeProp_getPathogenType(int lid, int pid) throws EditorException;
	
	/**
	 * Get a list of valid Conditional Distribution IDs for the given node 
	 * property.
	 * @param pid The ID of the node property.
	 * @return A list of Conditional Distribution IDs (cids)
	 * @throws EditorException Thrown if the node property does not exist, or if
	 * it does not use distributions.  Use 
	 * {@link PropertyViewer#nodeProp_isRangedProperty(int)} to check
	 * before calling this method.
	 */
	public List<Integer> 
	nodeProp_getConditionalDistributionIDs(int pid) throws EditorException;
	
	/**
	 * Get a list of conditional distribution IDs for the given layer property
	 * @param lid The layer ID of the property.
	 * @param pid The property ID.
	 * @return A list of conditional distribution IDs.
	 * @throws EditorException Thrown if the node property does not exist, or 
	 * if it does not use distributions.  Use 
	 * {@link PropertyViewer#nodeProp_isRangedProperty(int)} to check
	 * before calling this method.
	 */
	public List<Integer> 
	nodeProp_getConditionalDistributionIDs(int lid, int pid) 
			throws EditorException;
	
	/**
	 * Get the dependency conditions for the given conditional distribution
	 * @param pid The ID of the Node Property to check
	 * @param cid The ID of the conditional distribution, from 
	 * {@link PropertyViewer#nodeProp_getConditionalDistributionIDs(int)}
	 * @return A mapping of a property ID to a range ID within that property
	 * @throws EditorException Thrown if the node property does not exist, or 
	 * if it does not use distributions.  Use 
	 * {@link PropertyViewer#nodeProp_isRangedProperty(int)} to check
	 * before calling this method.  Also thrown if the cid does not point to a 
	 * conditional distribution.
	 */
	public Map<Integer, Integer> 
	nodeProp_getDistributionConditions(int pid, int cid) throws EditorException;
	
	/**
	 * Get the dependency conditions for a given conditional distribution.
	 * @param lid The layer ID
	 * @param pid The property ID
	 * @param cid The ID of the conditional distribution, from 
	 * {@link PropertyViewer#nodeProp_getConditionalDistributionIDs(int, int)}
	 * @return A map of property IDs from the <i>base node properties</i> 
	 * (layer properties currently cannot be used as dependencies) to a range 
	 * label within that property
	 * @throws EditorException Thrown if the combination of layer ID, 
	 * property ID, and conditional Distribution ID is invalid, or if the 
	 * node property is not ranged.  Using 
	 * {@link PropertyViewer#nodeProp_isRangedProperty(int)} to check before 
	 * calling this method will remove the risk of the last case.
	 */
	public Map<Integer, Integer> 
	nodeProp_getDistributionConditions(int lid, int pid, int cid) 
			throws EditorException;
	
	/**
	 * Get the probability map of the given conditional distribution.
	 * @param pid The ID of the node property to check.
	 * @param cid The ID of the conditional distribution, from 
	 * {@link PropertyViewer#nodeProp_getConditionalDistributionIDs(int)}
	 * @return A map of the range IDs in the given property to floating point 
	 * probabilities.
	 * @throws EditorException Thrown if the node property does not exist, or 
	 * if it does not use distributions.  Use 
	 * {@link PropertyViewer#nodeProp_isRangedProperty(int)} to check
	 * before calling this method.  Also thrown if the cid does not point to a 
	 * conditional distribution.
	 */
	public Map<Integer, Float> 
	nodeProp_getDistribution(int pid, int cid) throws EditorException;
	
	/**
	 * Get the dependency conditions for a given conditional distribution.
	 * @param lid The layer ID
	 * @param pid The property ID
	 * @param cid The ID of the conditional distribution, from 
	 * {@link PropertyViewer#nodeProp_getConditionalDistributionIDs(int, int)}
	 * @return A map of the range IDs in the given property to floating point 
	 * probabilities.
	 * @throws EditorException Thrown if the combination of layer ID, property 
	 * ID, and conditional Distribution ID is invalid, or if the node property 
	 * is not ranged.  Using 
	 * {@link PropertyViewer#nodeProp_isRangedProperty(int)}
	 * to check before calling this method will remove the risk of the last 
	 * case.
	 */
	public Map<Integer, Float> 
	nodeProp_getDistribution(int lid, int pid, int cid) throws EditorException;

	
	/**
	 * Get the probability map for the node property's default distribution.
	 * @param pid The ID of the node property to check.
	 * @return A map of range IDs from the given property to floating point 
	 * probabilities.
	 * @throws EditorException Thrown if the node property does not exist, or if
	 * it does not use distributions.  Use 
	 * {@link PropertyViewer#nodeProp_isRangedProperty(int)} to check before 
	 * calling this method. Also thrown if the default distribution has not been
	 * set, which should not be the case if the property was added through 
	 * {@link PropertyCreator#scratch_commit()}
	 */
	public Map<Integer, Float> 
	nodeProp_getDefaultDistribution(int pid) throws EditorException;
	
	/**
	 * Get the probability distribution for a node property's default 
	 * distribution
	 * @param lid The ID of the layer, from 
	 * {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from 
	 * {@link PropertyViewer#nodeProp_getPropertyIDs(int)}
	 * @return See {@link PropertyViewer#nodeProp_getDefaultDistribution(int)}
	 * @throws EditorException Thrown if the layer ID does not point to a layer,
	 *  or for the reasons listed in
	 * {@link PropertyViewer#nodeProp_getDefaultDistribution(int)}
	 */
	public Map<Integer, Float> 
	nodeProp_getDefaultDistribution(int lid, int pid) throws EditorException;
}
