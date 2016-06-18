package org.snrg_nyc.model;

import java.util.List;
import java.util.Map;

/**
 * The public-facing business logic for use by the user interface of an SNRG frontend.
 * This interface and any implementations of it should be the only public classes
 * in this package.
 * @author Devin Hastings
 * @version 0.2
 */
public interface UI_Interface {

	/**
	 * Save the properties to a persistent structure.  This does not validate the data,
	 * only preserving the project in its current state.
	 * @param experimentName The name of the experiment under which to save the data.
	 * @return The success of the operation.
	 */
	public void save(String experimentName);
	
	/*         *\
	 * Members *
	\*         */
	/**
	 * An enumeration of the possible distribution types in a node property.
	 * <ul>
	 * <li>Conditional: The common type of distribution, it is assumed a property will be of this type
	 * until specific methods are called.  Most methods involving default or conditional distributions 
	 * will throw an exception if the property is not of this distribution type.</li>
	 * <li>Uniform: The distribution type of a property after calling 
	 * {@link UI_Interface#scratch_useUniformDistribution()} </li>
	 * <li>Null: The distribution type of a property upon calling 
	 * {@link UI_Interface#scratch_setFractionInitValue(float)}</li>
	 * </ul>
	 * @author Devin Hastings
	 */
	public enum DistributionType{
		Conditional,
		Uniform,
		Null
	}
	
	/*                            *\
	 * Node Property test methods * 
	\*                            */
	
	/**
	 * Test if a given name is being used by another node property.
	 * @param name Node Property name
	 * @return Whether or not the name is available (and thus valid to use)
	 */
	public boolean test_nodePropNameIsUnique(String name);
	
	/**
	 * Identical to {@link UI_Interface#test_nodePropNameIsUnique(String)}, but for testing the name 
	 * against properties in a layer
	 * @param lid The layer ID to get properties from, should be from {@link UI_Interface#layer_getLayerIDs()}
	 * @param name The string name to test against other property names
	 * @return If the name is unique or not (returns false if there's a match).
	 * @throws UIException Thrown if the layer ID is invalid.
	 */
	public boolean test_nodePropNameIsUnique(int lid, String name) throws UIException;
	
	/**
	 * Test if a property ID points to an existing node property
	 * @param pid The property ID to test
	 * @return If the pid points to an existing node property
	 */
	public boolean test_nodePropIDExists(int pid);
	
	/**
	 * Tests if a property ID points to a non-null property within a layer.
	 * @param lid The Layer ID to check in, should be from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The property ID to check.
	 * @return If there is a non-null property that is linked to the give PID
	 * @throws UIException Thrown if the layer ID is invalid.
	 */
	public boolean test_nodePropIDExists(int lid, int pid) throws UIException;
	
	/**
	 * Test if the given layer ID points to a non-null layer
	 * @param lid The Layer ID to check
	 * @return If there is a layer that is linked to the given layer ID
	 */
	public boolean test_layerIDExists(int lid);
	
	
	/*                       *\
	 * Node Property Getters *
	\*                       */
	
	/**
	 * Get all the valid node property types
	 * @return A list of all valid Node Property types for use in {@link UI_Interface#scratch_new}
	 */
	public List<String> nodeProp_getTypes();
	
	/**
	 * Get the IDs of all the node properties, which are used by most methods beginning with the
	 * <i>nodeProp</i>prefix
	 * @return A list of node property IDs
	 */
	public List<Integer> nodeProp_getPropertyIDs();
	
	/**
	 * Get the node property IDs for a layer
	 * @param lid The ID of the layer to search in, should be from 
	 * {@link UI_Interface#layer_getLayerIDs()}
	 * @return A list of node property IDs for the layer
	 * @throws UIException Thrown if the layer ID is not valid
	 */
	public List<Integer> nodeProp_getPropertyIDs(int lid) throws UIException;
	
	/**
	 * Get the unique IDs for all ranges in a Ranged node property
	 * @param pid The ID of the node property, it must be a ranged property
	 * @return The IDs of the ranges in a node property
	 * @throws UIException Thrown if the pid is invalid, or if the property given is not ranged.
	 */
	public List<Integer> nodeProp_getRangeItemIDs(int pid) throws UIException;
	
	/**
	 * Get the unique IDs for all ranges in a Ranged node property within a layer
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}.
	 * It must also be a ranged property.
	 * @return The IDs of the range items in the property
	 * @throws UIException Thrown if the lid or pid is invalid, or if 
	 * the property linked to is not a ranged property
	 */
	public List<Integer> nodeProp_getRangeItemIDs(int lid, int pid) throws UIException;
	
	/**
	 * Get the label on a node property's range
	 * @param pid The ID of the node property, it must be a ranged property
	 * @param rid The ID of the range label, must be in {@link UI_Interface#nodeProp_getRangeItemIDs(int)}
	 * @return The label of the given range item
	 * @throws UIException Thrown if the pid or rid is invalid, or if the property given is not ranged.
	 */
	public String nodeProp_getRangeLabel(int pid, int rid) throws UIException;
	
	/**
	 * Get a label on a range item in a layer property
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}.
	 * It must also be a ranged property.
	 * @param rid The ID of the range item, from {@link UI_Interface#nodeProp_getRangeItemIDs(int, int)}
	 * @return The label of the range item
	 * @throws UIException Thrown if the lid, pid, or rid is invalid, or if 
	 * the property linked to is not a ranged property
	 */
	public String nodeProp_getRangeLabel(int lid, int pid, int rid) throws UIException;
	
	/**
	 * Get the upper bound of a node property's range
	 * @param pid The ID of the node property, it must be a ranged property
	 * @param rid The ID of the range, must be in {@link UI_Interface#nodeProp_getRangeItemIDs(int)}
	 * @return The upper bound of the range, as a numerical string
	 * @throws UIException Thrown if the pid or rid is invalid, or if the property given is not ranged.
	 */
	public int nodeProp_getRangeMax(int pid, int rid) throws UIException;
	
	/**
	 * Get a upper bound on a range item in a layer property
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}.
	 * It must also be a ranged property.
	 * @param rid The ID of the range item, from {@link UI_Interface#nodeProp_getRangeItemIDs(int, int)}
	 * @return The upper bound of the range item as a string
	 * @throws UIException Thrown if the lid, pid, or rid is invalid, or if 
	 * the property linked to is not a ranged property
	 */
	public int nodeProp_getRangeMax(int lid, int pid, int rid) throws UIException;
	
	/**
	 * Get the lower bound for a node property's range
	 * @param pid The ID of the node property, it must be a ranged property
	 * @param rid The ID of the range item in the property, must be in 
	 * {@link UI_Interface#nodeProp_getRangeItemIDs(int)}
	 * @return The lower bound of the range as a string
	 * @throws UIException Thrown if the pid or rid is invalid, or if the property given is not ranged.
	 */
	public int nodeProp_getRangeMin(int pid, int rid) throws UIException;
	
	/**
	 * Get the lower bound for the range item in a layer property
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}.
	 * It must also be a ranged property.
	 * @param rid The ID of the range item, from {@link UI_Interface#nodeProp_getRangeItemIDs(int, int)}
	 * @return The lower bound of the range item as a string
	 * @throws UIException Thrown if the lid, pid, or rid is invalid, or if 
	 * the property linked to is not a ranged property
	 */
	public int nodeProp_getRangeMin(int lid, int pid, int rid) throws UIException;
	
	/**
	 * Get the name of a node property.
	 * @param pid The ID of the node property, should be from {@link UI_Interface#nodeProp_getPropertyIDs()}
	 * @return The name of the property.
	 * @throws UIException Thrown if the ID does not point to a valid node property.
	 */
	public String nodeProp_getName(int pid) throws UIException;
	
	/**
	 * Return the name of a layer property
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}.
	 * @return The name of the layer property
	 * @throws UIException Thrown if the layer or property ID does not point to a non-null item.
	 */
	public String nodeProp_getName(int lid, int pid) throws UIException;
	
	/**
	 * Get the type of a node property.
	 * @param pid The ID of the node property, should be from {@link UI_Interface#nodeProp_getPropertyIDs()}
	 * @return The type of the property as a string.
	 * @throws UIException Thrown if the ID does not point to a valid node property.
	 */
	public String nodeProp_getType(int pid) throws UIException;
	
	/**
	 * Get the type of a layer property
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}.
	 * @return The type of the property, which is one of {@link UI_Interface#nodeProp_getTypes()}
	 * @throws UIException Thrown if the layer or property ID does not point to a non-null item.
	 */
	public String nodeProp_getType(int lid, int pid) throws UIException;
	
	/**
	 * Get the dependency level of a node property.
	 * @param pid The ID of the node property, should be from {@link UI_Interface#nodeProp_getPropertyIDs()} 
	 * @return The dependency level, as an integer
	 * @throws UIException Thrown if the ID does not point to a valid node property.
	 */
	public int nodeProp_getDependencyLevel(int pid) throws UIException;
	
	/**
	 * Get the dependency level of a node property in a layer
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}.
	 * @return The dependency level of the given layer property.
	 * @throws UIException Thrown if the layer or property ID does not point to a non-null item.
	 */
	public int nodeProp_getDependencyLevel(int lid, int pid) throws UIException;
	
	/**
	 * Get the initial value of a fraction node property (the same value given in {@link UI_Interface#scratch_disableRandomUseInit}
	 * @param pid The ID of the fraction node property to use, should be from {@link UI_Interface#nodeProp_getPropertyIDs()}
	 * @return The intitial value which the fraction property was set to use.
	 * @throws UIException Thrown if the PID is not valid, or if the property is not a fraction property.
	 */
	public float nodeProp_getInitValue(int pid) throws UIException;
	/**
	 * Identical to {@link UI_Interface#nodeProp_getInitValue(int)}, but for layer properties.
	 * @param lid The node layer ID, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The property ID, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}
	 * @return The initial value the fractional property was set to use.
	 * @throws UIException Thrown if the LID or PID is invalid, or if the given property
	 * is not a fraction property.
	 */
	public float nodeProp_getInitValue(int lid, int pid) throws UIException;
	
	/**
	 * Get a node property's description
	 * @param pid The ID of the node property, should be from {@link UI_Interface#nodeProp_getPropertyIDs()} 
	 * @return The description
	 * @throws UIException Thrown if the ID does not point to a valid node property.
	 */
	public String nodeProp_getDescription(int pid) throws UIException;
	
	/**
	 * Get the description of a node property
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}.
	 * @return The desctrption of the property.
	 * @throws UIException Thrown if the layer or property ID does not point to a non-null item.
	 */
	public String nodeProp_getDescription(int lid, int pid) throws UIException;
	
	/**
	 * Get a list of the pIDs for the given node property's dependencies.
	 * @param pid The ID of the node property, should be from {@link UI_Interface#nodeProp_getPropertyIDs()} 
	 * @return A list of PIDs added to the node property as dependencies
	 * @throws UIException Thrown if the ID does not point to a valid node property.
	 */
	public List<Integer> nodeProp_getDependencyIDs(int pid) throws UIException;
	
	/**
	 * Returns a list of node property IDs <i>within the base properties</i> with a lower
	 * dependency level.  Currently, layer properties cannot be used as dependencies.
	 * @param lid The layer ID of the property in question.
	 * @param pid The property ID of the property in question.
	 * @return A list of property IDs used as dependencies in the node property.
	 * @throws UIException Thrown if the combination if layer and property IDs does not
	 * point to a valid node property.
	 */
	public List<Integer> nodeProp_getDependencyIDs(int lid, int pid) throws UIException;
	
	/**
	 * Whether or not the given node property is ranged, which implies it has range items 
	 * and distribution items
	 * @param pid The Node Property ID to check.
	 * @return Whether or not the node property uses distributions.
	 * @throws UIException Thrown if the PID does not point to a valid node property.
	 */
	public boolean nodeProp_isRangedProperty(int pid) throws UIException;
	
	/**
	 * Check if the given layer property is ranged, meaning it has range items and uses
	 * distributions
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}
	 * @return If the property is ranged
	 * @throws UIException Thrown if the layer or property ID does not point to a non-null item.
	 */
	public boolean nodeProp_isRangedProperty(int lid, int pid) throws UIException;
	
	/**
	 * Get the {@link DistributionType} of the property.
	 * @param pid The property ID of the property to check, from {@link UI_Interface#nodeProp_getPropertyIDs()}.
	 * @return A value from the {@link DistributionType} enumeration.
	 * @throws UIException Thrown if the PID does not point to a valid property.
	 */
	public DistributionType nodeProp_getDistributionType(int pid) throws UIException;
	/**
	 * Identical to {@link UI_Interface#nodeProp_getDistributionType(int)}, but for layer properties.
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}
	 * @return The {@link DistributionType} of a property.
	 * @throws UIException Thrown if the layer ID or property ID is invalid.
	 */
	public DistributionType nodeProp_getDistributionType(int lid, int pid) throws UIException;
	
	/**
	 * Get a list of valid Conditional Distribution IDs for the given node property.
	 * @param pid The ID of the node property.
	 * @return A list of Conditional Distribution IDs (cids)
	 * @throws UIException Thrown if the node property does not exist, or if it does not 
	 * use distributions.  Use {@link UI_Interface#nodeProp_isRangedProperty(int)} to check
	 * before calling this method.
	 */
	public List<Integer> nodeProp_getConditionalDistributionIDs(int pid) throws UIException;
	
	/**
	 * Get a list of conditional distribution IDs for the given layer property
	 * @param lid The layer ID of the property.
	 * @param pid The property ID.
	 * @return A list of conditional distribution IDs.
	 * @throws UIException Thrown if the node property does not exist, or if it does not 
	 * use distributions.  Use {@link UI_Interface#nodeProp_isRangedProperty(int)} to check
	 * before calling this method.
	 */
	public List<Integer> nodeProp_getConditionalDistributionIDs(int lid, int pid) throws UIException;
	
	/**
	 * Get the dependency conditions for the given conditional distribution
	 * @param pid The ID of the Node Property to check
	 * @param cid The ID of the conditional distribution, from {@link UI_Interface#nodeProp_getConditionalDistributionIDs(int)}
	 * @return A mapping of a property ID to a range ID within that property
	 * @throws UIException Thrown if the node property does not exist, or if it does not 
	 * use distributions.  Use {@link UI_Interface#nodeProp_isRangedProperty(int)} to check
	 * before calling this method.  Also thrown if the cid does not point to a conditional distribution.
	 */
	public Map<Integer, Integer> nodeProp_getDistributionConditions(int pid, int cid) throws UIException;
	
	/**
	 * Get the dependency conditions for a given conditional distribution.
	 * @param lid The layer ID
	 * @param pid The property ID
	 * @param cid The ID of the conditional distribution, from 
	 * {@link UI_Interface#nodeProp_getConditionalDistributionIDs(int, int)}
	 * @return A map of property IDs from the <i>base node properties</i> 
	 * (layer properties currently cannot be used as dependencies) to a range label within that property
	 * @throws UIException Thrown if the combination of layer ID, property ID, and conditional Distribution ID
	 * is invalid, or if the node property is not ranged.  Using {@link UI_Interface#nodeProp_isRangedProperty(int)}
	 * to check before calling this method will remove the risk of the last case.
	 */
	public Map<Integer, Integer> nodeProp_getDistributionConditions(int lid, int pid, int cid) throws UIException;
	
	/**
	 * Get the probability map of the given conditional distribution.
	 * @param pid The ID of the node property to check.
	 * @param cid The ID of the conditional distribution, from {@link UI_Interface#nodeProp_getConditionalDistributionIDs(int)}
	 * @return A map of the range IDs in the given property to floating point probabilities.
	 * @throws UIException Thrown if the node property does not exist, or if it does not 
	 * use distributions.  Use {@link UI_Interface#nodeProp_isRangedProperty(int)} to check
	 * before calling this method.  Also thrown if the cid does not point to a conditional distribution.
	 */
	public Map<Integer, Float> nodeProp_getDistribution(int pid, int cid) throws UIException;
	
	/**
	 * Get the dependency conditions for a given conditional distribution.
	 * @param lid The layer ID
	 * @param pid The property ID
	 * @param cid The ID of the conditional distribution, from 
	 * {@link UI_Interface#nodeProp_getConditionalDistributionIDs(int, int)}
	 * @return A map of the range IDs in the given property to floating point probabilities.
	 * @throws UIException Thrown if the combination of layer ID, property ID, and conditional Distribution ID
	 * is invalid, or if the node property is not ranged.  Using {@link UI_Interface#nodeProp_isRangedProperty(int)}
	 * to check before calling this method will remove the risk of the last case.
	 */
	public Map<Integer, Float> nodeProp_getDistribution(int lid, int pid, int cid) throws UIException;

	
	/**
	 * Get the probability map for the node property's default distribution.
	 * @param pid The ID of the node property to check.
	 * @return A map of range IDs from the given property to floating point probabilities.
	 * @throws UIException Thrown if the node property does not exist, or if it does not 
	 * use distributions.  Use {@link UI_Interface#nodeProp_isRangedProperty(int)} to check
	 * before calling this method. Also thrown if the default distribution has not been set, which
	 * should not be the case if the property was added through {@link UI_Interface#scratch_commitToNodeProperties()}
	 */
	public Map<Integer, Float> nodeProp_getDefaultDistribution(int pid) throws UIException;
	
	/**
	 * Get the probability distribution for a node property's default distribution
	 * @param lid The ID of the layer, from {@link UI_Interface#layer_getLayerIDs()}
	 * @param pid The ID of the ranged node property, from {@link UI_Interface#nodeProp_getPropertyIDs(int)}
	 * @return See {@link UI_Interface#nodeProp_getDefaultDistribution(int)}
	 * @throws UIException Thrown if the layer ID does not point to a layer, or for the reasons listed in
	 * {@link UI_Interface#nodeProp_getDefalutDistribution(int)}
	 */
	public Map<Integer, Float> nodeProp_getDefaultDistribution(int lid, int pid) throws UIException;

	/*                                           *\
	|  ----- Scratch Node Property Methods -----  |
	\*                                           */
	
	
	/**Creates a new temporary property that can be manipulated by the UI. 
	 * Only one temporary property should be active at any time.
	 * 
	 * @param name Name of the property, must be unique (use 
	 * {@link UI_Interface#test_nodePropNameIsUnique(String)} to check this)
	 * @param type Type of the property, call {@link UI_Interface#nodeProp_getTypes()}
	 *  for a list of valid types
	 * @param description A brief description of the property
	 * @throws UIException Thrown if the name is not unique, or the type given is invalid.
	 */
	public void scratch_new(String name, String type, String description) throws UIException;
	
	/** The same as {@link UI_Interface#scratch_new(String, String, String)},
	 * but the scratch property will be designed for a layer
	 * @param lid The ID of the layer to create the property for
	 * @throws UIException Thrown if the layer ID is not valid, or id the scratch
	 *  property's name is not unique
	 * @see {@link UI_Interface#scratch_new(String, String, String)}
	 * {@link UI_Interface#scratch_commitToNodeProperties()}
	 */
	public void scratch_newInLayer(int lid, String name, String type, String description) throws UIException;
	
	/**
	 * Delete the temporary node property
	 */
	public void scratch_clear();
	
	/**
	 * Sets the dependency level, if it has not already been set.
	 * @param level Desired dependency level
	 * @throws UIException Thrown if dependencies have been added to the property.
	 */
	public void scratch_setDependencyLevel(int level) throws UIException;
	
	/**
	 * Create a range item for a property of any type
	 * @return A unique ID for the new range item
	 * @throws UIException Thrown if the scratch property is not ranged
	 */
	public int scratch_addRange() throws UIException;
	
	/**
	 * Identical to {@link UI_Interface#scratch_addRange()}, but it sets the new label immediately.
	 * @param label The label for the range.  It must be unique.
	 * @return The range ID for the new range item.
	 * @throws UIException Thrown if the scratch property is not ranged, if the new label is a duplicate, or
	 * if distributions have already been added to the property (which depend on the range labels).
	 */
	public int scratch_addRange(String label) throws UIException;

	/**
	 * Delete a range item from a node property of any type.
	 * @param rid The ID for the range item to be removed (obtained from {@link UI_Interface#scratch_addRange()})
	 * @throws UIException Thrown if the id does not point to a range item, or
	 * if distributions have already been added to the property (which depend on the range labels).
	 */
	public void scratch_removeRange(int rid) throws UIException;
	
	/**
	 * Add/Update the name of a Range item.
	 * @param rid The unique ID from {@link UI_Interface#scratch_addRange()}
	 * @param label A unique label for the range item
	 * @throws UIException Thrown if the id does not point to a range item, the 
	 * name collides with another name, or if distributions have already been 
	 * added to the property (which depend on the range labels).
	 */
	public void scratch_setRangeLabel(int rid, String label) throws UIException;
	
	/**
	 * Add/Update the lower bound of an Integer Range property.
	 * @param rid The ID from {@link UI_Interface#scratch_addRange()}
	 * @param min The new lower bound.  It cannot overlap with another range, and must be smaller
	 *  than the upper bound
	 * @throws UIException Thrown if the id does not point to a range item, if
	 * the given value overlaps with another range in the property, or
	 * if the value is greater than the upper bound of the range.
	 */
	public void scratch_setRangeMin(int rid, int min) throws UIException;
	
	/**
	 * Add/Update the upper bound for an Integer Range node property
	 * @param rid The ID from {@link UI_Interface#scratch_addRange()}
	 * @param max The new upper bound.  Cannot overlap another range, and must be greater than 
	 * the lower bound
	 * @throws UIException Thrown if the id does not point to a range item, if
	 * the given value overlaps with another range in the property, or
	 * if the value is less than the lower bound of the range.
	 */
	public void scratch_setRangeMax(int rid, int max) throws UIException;
	
	/**
	 * Get all of the range label IDs
	 * @return A list of integers, sorted by the lower bound if a ranged property,
	 * or alphabetically for enumerate properties.
	 * @throws UIException Thrown if the scratch property is not a ranged property
	 * , or if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public List<Integer> scratch_getRangeIDs() throws UIException;
	
	/**
	 * Get the label for a range item.
	 * @param rid The ID of the range item, as from {@link UI_Interface#scratch_getRangeIDs()}
	 * @return The label
	 * @throws UIException Thrown if the scratch property is not a ranged property or if the rid does not exist
	 * , or if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public String scratch_getRangeLabel(int rid) throws UIException;
	
	/**
	 * Get the lower bound on a range in the scratch property
	 * @param rid The Range item ID of the range to view
	 * @return The lower bound of the given range item.
	 * @throws UIException Thrown if the range ID is invalid, the scratch property
	 * is not ranged, or the range item does not have a lower bound set
	 */
	public int scratch_getRangeMin(int rid) throws UIException;
	/**
	 * Get the upper bound on a range in the scratch property
	 * @param rid The range item ID of the range to view
	 * @return The upper bound of the range item in question
	 * @throws UIException Thrown if the range ID is invalid, the scratch property is 
	 * not ranged, or id the range item does not have an upper bound set.
	 */
	public int scratch_getRangeMax(int rid) throws UIException;
	
	/**
	 * Check if a range item is set, meaning it has a property enumerate label and valid bounds 
	 * for a ranged property
	 * @param rid The Range ID in the scratch property
	 * @return If the range with the given ID is valid.
	 * @throws UIException Thrown if the range ID is not valid.
	 */
	public boolean scratch_rangeIsSet(int rid) throws UIException;
	
	/**
	 * Set the distribution id to "uniform", removing the need for conditional or default distributions,
	 * and set the dependency level to zero, overwriting any previous value.
	 * @throws UIException Thrown if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public void scratch_useUniformDistribution() throws UIException;
	
	/**
	 * Set the initial value of a fraction node property.  
	 * Sets the dependency level to zero.
	 * @param init The static value to use.
	 * @return The success of the operation.
	 * @throws UIException Thrown if the scratch property is not a Fractional Property
	 * , or if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public void scratch_setFractionInitValue(float init) throws UIException;
	
	/**
	 * Get the value given to the scratch property after 
	 * {@link UI_Interface#scratch_setFractionInitValue(float)} was called
	 * @return The initial value given to a fraction node property
	 * @throws UIException Thrown if the scratch object is not a fraction property, 
	 * or if the intitial value has not been set.
	 */
	public float scratch_getInitValue() throws UIException;
	
	/**
	 * Get a list of node property IDs with dependency levels strictly less than the scratch property's
	 * dependency level.
	 * <p> Note that the property IDs given are in the <i>base properties</i>.  Layer properties
	 * cannot currently be used as dependencies.
	 * @return A list of integer property IDs
	 * @throws UIException Thrown if the scratch property's dependency level has not been set
	 * , or if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public List<Integer> scratch_getPotentialDependencies() throws UIException;
	
	/**
	 * Add a node property as a dependency to the scratch node property.
	 * <p> If the scratch property was created using {@link UI_Interface#scratch_newInLayer}, this method's
	 * behavior is currently undefined.
	 * @param pid The ID of the node property to be added as a dependency
	 * @throws UIException Thrown if the given ID does not exist or is of a higher dependency level
	 * , or if the scratch property is currently null.  If the scratch property 
	 */
	public void scratch_addDependency(int pid) throws UIException;
	
	/**
	 * Remove a property from the scratch property's dependencies
	 * @param pid The node property ID for the property to remove
	 * @throws UIException Thrown if the property ID is not in the list of 
	 * dependencies for the scratch property, or if the distributions 
	 * have already been added.
	 */
	public void scratch_removeDependency(int pid) throws UIException;
	
	/**
	 * Get all the added dependencies of the scratch object.
	 * @return A list of the node property IDs added as dependencies in the scratch object.
	 * @throws UIException Thrown if the scratch property is currently null, which means
	 *  {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public List<Integer> scratch_getDependencies() throws UIException;

	/**
	 * Get the {@link DistributionType} of the scratch property
	 * @return A value from the {@link DistributionType} enumeration
	 */
	public DistributionType scratch_getDistributionType();
	
	/**
	 * Add a conditional Distribution to the current scratch property
	 * @param dependencyConditions A map of integer IDs, which must be one of the IDs from 
	 * {@link UI_Interface#scratch_getDependencies}(); and a range ID for a range in that property.
	 *  All pIDs are optional, but there must be exactly one range ID per pID.
	 * @param probabilities A map of all the range IDs in the scratch property
	 * to floating point values representing the probability of that range in the distribution.  Each 
	 * range ID must appear exactly once in the map.
	 * (these probabilities are relative, and there is no special requirements for them).  Each label
	 * must be in the map exactly once.
	 * @return The ID of the new conditional Distribution
	 * @throws UIException Thrown if the dependencyConditions or probabilities aren't valid
	 * , or if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public int scratch_addConditionalDistribution
		(Map<Integer, Integer> dependencyConditions, Map<Integer, Float> probabilities)
		throws UIException;
	
	/**
	 * This removes a conditional distribution from the scratch property.
	 * @param cid The ID of the conditional distribution, as received from 
	 * {@link UI_Interface#scratch_addConditionalDistribution(Map, Map)} or
	 * {@link UI_Interface#scratch_getConditionalDistributionIDs()}
	 * @throws UIException Thrown if the ID does not exist
	 * , or if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public void scratch_removeConditionalDistribution(int cid) throws UIException;
	
	/**
	 * Delete all conditional distributions and the default distribution.  This must be called
	 * if distributions have already been added, but the user wants to change the range labels.
	 */
	public void scratch_clearDistributions();
	
	/**
	 * Replace the attributes of a conditional distribution with new information.  
	 * <p>
	 * See {@link UI_Interface#scratch_addConditionalDistribution} for the requirements 
	 * of dependencyConditions and probabilities.
	 * @param cid The ID of the conditional distribution, as received from 
	 * {@link UI_Interface#scratch_addConditionalDistribution(Map, Map)} or
	 * {@link UI_Interface#scratch_getConditionalDistributionIDs()}
	 * @param dependencyConditions See {@link UI_Interface#scratch_addConditionalDistribution}
	 * for requirements
	 * @param probabilities See {@link UI_Interface#scratch_addConditionalDistribution}
	 * for requirements
	 * @throws UIException Thrown if the ID does not point to a distribution, or the requirements for
	 * the parameters are not met
	 * , or if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public void scratch_updateConditionalDistribution
		(int cid, Map<Integer, Integer> dependencyConditions, Map<Integer, Float> probabilities) 
		throws UIException;
	
	/**
	 * Set the order in which the conditional distributions are to be checked.
	 * @param ordering A list of IDs of the conditional distributions in the order they are to be checked
	 * @throws UIException Thrown if the list does not contain each conditional distribution ID exactly
	 * once, and only those IDs.
	 */
	public void scratch_reorderConditionalDistributions(List<Integer> ordering) throws UIException;
	
	/**
	 * Set the default distribution of the scratch property.
	 * @param distribution A map of all the range IDs in the scratch property
	 * to floating point values representing the probability of that range in the distribution.  Each 
	 * range must appear exactly once in the map.
	 * @throws UIException Thrown if the requirements for the distribution are not met
	 * , or if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public void scratch_setDefaultDistribution(Map<Integer, Float> distribution) throws UIException;
	
	/**
	 * Finalize the scratch property and add it to the structure of node properties.  In its current state, 
	 * this clears the scratch property automatically, and there is no way to edit or remove an added node 
	 * property, so it is important to make sure the property is how the user wants it before committing.
	 * 
	 * <p>
	 * If the scratch property was created using {@link UI_Interface#scratch_newInLayer}, it will
	 * be attached to the layer using the layer ID that was used there. Call {@link UI_Interface#scratch_getLayerID()}
	 * to see what layer it will be attached to.
	 *  
	 * @return The node property ID of the new property, which is also available through 
	 * {@link UI_Interface#nodeProp_getPropertyIDs()}
	 * 
	 * @throws UIException There are several reasons that this exception may be thrown, here is a list of examples:
	 * <ul>
	 * <li>The scratch property is null, which means {@link UI_Interface#scratch_new(String, String, String)} was never called</li>
	 * <li>The scratch property has the same name as another node property</li>
	 * <li>The scratch property is ranged, but does not have a default distribution set.</li>
	 * <li>The property was created using {@link UI_Interface#scratch_newInLayer(int, String, String, String)}, and the
	 * layer linked to the given ID was removed, which should not be possible from this interface.</li>
	 * </ul>
	 * And various other messages.  {@link UIException#getMessage()} should yield more information.
	 */
	public int scratch_commitToNodeProperties() throws UIException;
	
	/**
	 * @return The name of the scratch property
	 * @throws UIException Thrown if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public String scratch_getName() throws UIException;
	
	/**
	 * @return The type of the scratch property
	 * @throws UIException Thrown if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public String scratch_getType() throws UIException;
	
	/**
	 * @return The description of the scratch property
	 * @throws UIException Thrown if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public String scratch_getDescription() throws UIException;
	
	/**
	 * @return The dependency level, or -1 if it has not been set.
	 * @throws UIException Thrown if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public int scratch_getDependencyLevel() throws UIException;
	
	/**
	 * Get a list of unique IDs, in order of evaluation, for the conditional 
	 * distributions of the scratch property.
	 * @return The list of IDs for the conditional distributions.
	 * @throws UIException Thrown if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public List<Integer> scratch_getConditionalDistributionIDs() throws UIException;
	
	/**
	 * Get the conditions to be met for a conditional distribution to be used.
	 * @param cid The ID of the conditional distribution, obtained from 
	 * {@link UI_Interface#scratch_getConditionalDistributionIDs()}
	 * @return A map in which each pair is a node property ID and the ID of the range
	 * it must be for the condition to be met.
	 * @throws UIException Thrown when the ID given does not point to a valid conditional distribution,
	 * or if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public Map<Integer, Integer> scratch_getDistributionCondition(int cid) throws UIException;
	
	/**
	 * Get the distribution of probabilities in a conditional distribution.
	 * @param cid The ID of the conditional distribution, as from 
	 * {@link UI_Interface#scratch_getConditionalDistributionIDs()}
	 * @return A map of Range IDs in the scratch property to its probability in the 
	 * conditional distribution.
	 * @throws UIException Thrown if the ID given does not point to a valid conditional distribution,
	 * or if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public Map<Integer, Float> scratch_getDistribution(int cid) throws UIException;
	
	/**
	 * Return the default distribution as a map, similar to
	 *  {@link UI_Interface#scratch_getDistribution(int)} .
	 * @return A map of the range IDs in the scratch property to their probabilities 
	 * in the distribution.
	 * @throws UIException Thrown if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public Map<Integer, Float> scratch_getDefaultDistribution() throws UIException;
	
	/**
	 * Check if the scratch property can use distributions (regardless of the number of distributions).
	 * @return If the scratch property is a ranged or enumerator property.
	 * @throws UIException Thrown if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public boolean scratch_isRangedProperty() throws UIException;
	
	/**
	 * Get the layer ID of the layer that the scratch property is attached to, if
	 * there is one.
	 * @return The layer ID, or null if the scratch property is not attached to a
	 * layer.
	 * @throws UIException Thrown if the scratch property is currently null, which means
	 * {@link UI_Interface#scratch_new(String, String, String)} needs to be called.
	 */
	public Integer scratch_getLayerID() throws UIException;
	
	/*               *\
	 * Layer Methods *
	\*               */
	
	/**
	 * Create a new layer
	 * @param name The name for the new layer.
	 * @return An integer ID for the new layer.
	 * @throws UIException Thrown if a layer with that name already exists.
	 */
	public int layer_new(String name) throws UIException;
	
	/**
	 * Get the available IDs for node layers.
	 * @return All IDs that point to non-null layer items.
	 */
	public List<Integer> layer_getLayerIDs();
	
	/**
	 * Get the name of a layer
	 * @param lid The ID, from {@link UI_Interface#layer_getLayerIDs()}
	 * @return The name of the layer linked to the give ID
	 * @throws UIException Thrown if the layer ID does not point to a layer
	 */
	public String layer_getName(int lid) throws UIException;
	
	/**
	 * Change the name of a layer
	 * @param lid The ID of the layer to rename.
	 * @param name The new name of the layer.
	 * @throws UIException Thrown if the layer ID is invalid, or
	 * if a layer with the given name already exists.
	 */
	public void layer_setName(int lid, String name) throws UIException;

	
}