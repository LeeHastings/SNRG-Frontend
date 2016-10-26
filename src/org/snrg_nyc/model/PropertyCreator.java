package org.snrg_nyc.model;

import java.util.List;
import java.util.Map;

/**
 * The interface for building node properties.
 * Part of {@link PropertiesEditor}
 * <p>
 * There is a very particular sequence to the construction of a property.
 * <ul>
 *	<li>First, the property is constructed with 
 *      {@link PropertyCreator#scratch_new} or 
 *      {@link PropertyCreator#scratch_newInLayer}</li>
 *      
 *	<li>Then, if the property is ranged, the ranges are set using this sequence:
 *	  <ul>
 *		<li> Use {@link PropertyCreator#scratch_addRange} to make the range 
 *		</li>
 *		<li> Then, add attributes using 
 *		    {@link PropertyCreator#scratch_setRangeLabel}, 
 *		    {@link PropertyCreator#scratch_setRangeMin}, and
 *		    {@link PropertyCreator#scratch_setRangeMax}
 *		    (the last two begin for integer ranges) 
 *		</li>
 *	  </ul>
 *	  Otherwise, calling {@link PropertyCreator#scratch_setBooleanInitValue} 
 *	  for a BooleanProperty or 
 *	  {@link PropertyCreator#scratch_setFractionInitValue} for a 
 *	  FractionProperty will be enough to finish.
 *	</li>
 *	<li> If you intend for the property's distribution to depend on other 
 *	  properties, now is the time to add them as dependencies using
 *	  {@link PropertyCreator#scratch_addDependency}
 *	</li>
 *	<li>  Once all dependencies are added, you can start adding conditional
 *	  distributions using the method 
 *	  {@link PropertyCreator#scratch_addConditionalDistribution} for each 
 *	  condition in your distribution.
 *	</li>
 *	<li> Finally, you can set the default distribution with 
 *	  {@link PropertyCreator#scratch_setDefaultDistribution}
 *	</li>
 * </ul>
 * After the property is completed, it can be added to the internal structure
 * using {@link PropertyCreator#scratch_commit()}.  All exceptions should have 
 * informative messages.
 * </p>
 * @author Devin Hastings
 *
 */
public interface PropertyCreator {
	/*                                           *\
	|  ----- Scratch Node Property Methods -----  |
	\*                                           */
	
	
	/**Creates a new temporary property that can be manipulated by the UI. 
	 * Only one temporary property should be active at any time.
	 * 
	 * @param name Name of the property, must be unique (use 
	 * {@link PropertiesEditor#test_nodePropNameIsUnique(String)} 
	 * to check this)
	 * @param type Type of the property, call 
	 * {@link PropertiesEditor#getPropertyTypes()}
	 *  for a list of valid types
	 * @param description A brief description of the property
	 * @throws EditorException Thrown if the name is not unique, or 
	 * the type given is invalid.
	 */
	public void 
	scratch_new(String name, String type, String description) 
			throws EditorException;
	
	/** The same as 
	 * {@link PropertyCreator#scratch_new(String, String, String)},
	 * but the scratch property will be designed for a layer
	 * @param lid The ID of the layer to create the property for
	 * @throws EditorException Thrown if the layer ID is not valid, or id the
	 * scratch property's name is not unique
	 * @see PropertyCreator#scratch_new(String, String, String)
	 * @see PropertyCreator#scratch_commit()
	 */
	public void 
	scratch_newInLayer(int lid, String name, String type, String description) 
			throws EditorException;
	
	/**
	 * Delete the temporary node property
	 */
	public void 
	scratch_clear();
	
	/**
	 * Sets the dependency level, if it has not already been set.
	 * @param level Desired dependency level
	 * @throws EditorException Thrown if dependencies have been added to the 
	 * property.
	 */
	public void 
	scratch_setDependencyLevel(int level) throws EditorException;

	/**
	 * Create a range item for a property of any type
	 * @return A unique ID for the new range item
	 * @throws EditorException Thrown if the scratch property is not ranged
	 */
	public int 
	scratch_addRange() throws EditorException;
	
	/**
	 * Identical to {@link PropertyCreator#scratch_addRange()}, but it sets 
	 * the new label immediately.
	 * @param label The label for the range.  It must be unique.
	 * @return The range ID for the new range item.
	 * @throws EditorException Thrown if the scratch property is not ranged, 
	 * if the new label is a duplicate, or
	 * if distributions have already been added to the property (which depend 
	 * on the range labels).
	 */
	public int 
	scratch_addRange(String label) throws EditorException;

	/**
	 * Delete a range item from a node property of any type.
	 * @param rid The ID for the range item to be removed (obtained from 
	 * {@link PropertyCreator#scratch_addRange()})
	 * @throws EditorException Thrown if the id does not point to a range item, 
	 * or if distributions have already been added to the property (which depend
	 * on the range labels).
	 */
	public void 
	scratch_removeRange(int rid) throws EditorException;
	
	/**
	 * Add/Update the name of a Range item.
	 * @param rid The unique ID from {@link PropertyCreator#scratch_addRange()}
	 * @param label A unique label for the range item
	 * @throws EditorException Thrown if the id does not point to a range item,
	 * the name collides with another name, or if distributions have already 
	 * been added to the property (which depend on the range labels).
	 */
	public void 
	scratch_setRangeLabel(int rid, String label) throws EditorException;
	
	/**
	 * Add/Update the lower bound of an Integer Range property.
	 * @param rid The ID from {@link PropertyCreator#scratch_addRange()}
	 * @param min The new lower bound.  It cannot overlap with another range, 
	 * and must be smaller than the upper bound
	 * @throws EditorException Thrown if the id does not point to a range item, 
	 * if the given value overlaps with another range in the property, or
	 * if the value is greater than the upper bound of the range.
	 */
	public void 
	scratch_setRangeMin(int rid, int min) throws EditorException;
	
	/**
	 * Add/Update the upper bound for an Integer Range node property
	 * @param rid The ID from {@link PropertyCreator#scratch_addRange()}
	 * @param max The new upper bound.  Cannot overlap another range, and must 
	 * be greater than the lower bound
	 * @throws EditorException Thrown if the id does not point to a range item, 
	 * if the given value overlaps with another range in the property, or
	 * if the value is less than the lower bound of the range.
	 */
	public void 
	scratch_setRangeMax(int rid, int max) throws EditorException;
	
	/**
	 * Get all of the range label IDs
	 * @return A list of integers, sorted by the lower bound if a ranged 
	 * property, or alphabetically for enumerate properties.
	 * @throws EditorException Thrown if the scratch property is not a ranged 
	 * property, or if the scratch property is currently null, which means
	 * {@link PropertyCreator#scratch_new(String, String, String)} needs to be 
	 * called.
	 */
	public List<Integer> 
	scratch_getRangeIDs() throws EditorException;
	
	/**
	 * Get the label for a range item.
	 * @param rid The ID of the range item, as from 
	 * {@link PropertyCreator#scratch_getRangeIDs()}
	 * @return The label
	 * @throws EditorException Thrown if the scratch property is not a ranged 
	 * property or if the rid does not exist, or if the scratch property is 
	 * currently null, which means
	 * {@link PropertyCreator#scratch_new(String, String, String)} needs to be 
	 * called.
	 */
	public String 
	scratch_getRangeLabel(int rid) throws EditorException;
	
	/**
	 * Get the lower bound on a range in the scratch property
	 * @param rid The Range item ID of the range to view
	 * @return The lower bound of the given range item.
	 * @throws EditorException Thrown if the range ID is invalid, the scratch 
	 * property is not ranged, or the range item does not have a lower bound set
	 */
	public Integer 
	scratch_getRangeMin(int rid) throws EditorException;
	/**
	 * Get the upper bound on a range in the scratch property
	 * @param rid The range item ID of the range to view
	 * @return The upper bound of the range item in question
	 * @throws EditorException Thrown if the range ID is invalid, the scratch 
	 * property is not ranged, or id the range item does not have an upper bound
	 * set.
	 */
	public Integer 
	scratch_getRangeMax(int rid) throws EditorException;
	
	/**
	 * Check if a range item is set, meaning it has a property enumerate label 
	 * and valid bounds for a ranged property
	 * @param rid The Range ID in the scratch property
	 * @return If the range with the given ID is valid.
	 * @throws EditorException Thrown if the range ID is not valid.
	 */
	public boolean 
	scratch_rangeIsSet(int rid) throws EditorException;
	
	/**
	 * Set the distribution id to "uniform", removing the need for conditional 
	 * or default distributions,
	 * and set the dependency level to zero, overwriting any previous value.
	 * @throws EditorException Thrown if the scratch property is currently null,
	 *  which means
	 * {@link PropertyCreator#scratch_new(String, String, String)} needs to be 
	 * called.
	 */
	public void 
	scratch_useUniformDistribution() throws EditorException;
	
	/**
	 * Set the Pathogen Type for an Attachment node property
	 * @param type The type of the property
	 * @throws EditorException Thrown if the scratch property is not an 
	 * attachment property or if another attachment property with the given type
	 * exists, or if the name is otherwise invalid (empty, null, or 
	 * implementation-specific rules).
	 */
	public void 
	scratch_setPathogenType(String type) throws EditorException;
	
	/**
	 * Get the pathogen type of the scratch property
	 * @return The Pathogen Type set using 
	 * {@link PropertyCreator#scratch_setPathogenType(String)}
	 * @throws EditorException Thrown if the scratch property is not 
	 * an Attachment Property, or if the name has not been set
	 */
	public String 
	scratch_getPathogenType() throws EditorException;
	
	/**
	 * Set the initial value of a fraction node property.  
	 * Sets the dependency level to zero.
	 * @param init The static value to use.
	 * @throws EditorException Thrown if the scratch property is not a 
	 * Fractional Property, or if the scratch property is currently null, which 
	 * means {@link PropertyCreator#scratch_new(String, String, String)} needs 
	 * to be called.
	 */
	public void 
	scratch_setFractionInitValue(float init) throws EditorException;
	
	/**
	 * Get the value given to the scratch property after 
	 * {@link PropertyCreator#scratch_setFractionInitValue(float)} was called
	 * @return The initial value given to a fraction node property
	 * @throws EditorException Thrown if the scratch object is not a fraction 
	 * property, or if the intitial value has not been set.
	 */
	public float 
	scratch_getFractionInitValue() throws EditorException;
	
	/**
	 * Set the initial value for a boolean property
	 * @param init The value to use for the property
	 * @throws EditorException Thrown if the scratch property is not a boolean 
	 * property
	 */
	public void 
	scratch_setBooleanInitValue(boolean init) throws EditorException;
	
	/**
	 * Get the initial value for a boolean property
	 * @return The initial value
	 * @throws EditorException Thrown if the scratch property is not a boolean
	 *  property, or if the initial value was not set
	 */
	public boolean 
	scratch_getBooleanInitValue() throws EditorException;
	
	/**
	 * Get a list of node property IDs with dependency levels strictly less 
	 * than the scratch property's dependency level.
	 * <p> Note that the property IDs given are in the <i>base properties</i>.
	 * Layer properties cannot currently be used as dependencies.
	 * @return A list of integer property IDs
	 * @throws EditorException Thrown if the scratch property's dependency 
	 * level has not been set, or if the scratch property is currently null, 
	 * which means {@link PropertyCreator#scratch_new(String, String, String)}
	 * needs to be called.
	 */
	public List<Integer> 
	scratch_getPotentialDependencies() throws EditorException;
	
	/**
	 * Add a node property as a dependency to the scratch node property.
	 * <p> If the scratch property was created using 
	 * {@link PropertyCreator#scratch_newInLayer}, this method's
	 * behavior is currently undefined.
	 * @param pid The ID of the node property to be added as a dependency
	 * @throws EditorException Thrown if the given ID does not exist or is of a 
	 * higher dependency level, or if the scratch property is currently null.  
	 * If the scratch property 
	 */
	public void 
	scratch_addDependency(int pid) throws EditorException;
	
	/**
	 * Remove a property from the scratch property's dependencies
	 * @param pid The node property ID for the property to remove
	 * @throws EditorException Thrown if the property ID is not in the list of 
	 * dependencies for the scratch property, or if the distributions 
	 * have already been added.
	 */
	public void 
	scratch_removeDependency(int pid) throws EditorException;
	
	/**
	 * Get all the added dependencies of the scratch object.
	 * @return A list of the node property IDs added as dependencies in the 
	 * scratch object.
	 * @throws EditorException Thrown if the scratch property is currently null,
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public List<Integer> 
	scratch_getDependencies() throws EditorException;
	
	/**
	 * Add a conditional Distribution to the current scratch property
	 * @param dependencyConditions A map of integer IDs, which must be one of 
	 * the IDs from {@link PropertyCreator#scratch_getDependencies}(); and a 
	 * range ID for a range in that property.
	 *  All pIDs are optional, but there must be exactly one range ID per pID.
	 * @param probabilities A map of all the range IDs in the scratch property
	 * to floating point values representing the probability of that range in 
	 * the distribution.  Each range ID must appear exactly once in the map.
	 * (these probabilities are relative, and there is no special requirements 
	 * for them).  Each label must be in the map exactly once.
	 * @return The ID of the new conditional Distribution
	 * @throws EditorException Thrown if the dependencyConditions or 
	 * probabilities aren't valid, or if the scratch property is currently null,
	 * which means {@link PropertyCreator#scratch_new(String, String, String)}
	 * needs to be called.
	 */
	public int scratch_addConditionalDistribution
		(Map<Integer, Integer> dependencyConditions, 
		 Map<Integer, Float> probabilities)
		throws EditorException;
	
	/**
	 * This removes a conditional distribution from the scratch property.
	 * @param cid The ID of the conditional distribution, as received from 
	 * {@link PropertyCreator#scratch_addConditionalDistribution(Map, Map)} or
	 * {@link PropertyCreator#scratch_getConditionalDistributionIDs()}
	 * @throws EditorException Thrown if the ID does not exist
	 * , or if the scratch property is currently null, which means
	 * {@link PropertyCreator#scratch_new(String, String, String)} needs to be 
	 * called.
	 */
	public void 
	scratch_removeConditionalDistribution(int cid) throws EditorException;
	
	/**
	 * Delete all conditional distributions and the default distribution.  
	 * This must be called if distributions have already been added, but the 
	 * user wants to change the range labels. <p>
	 * This does not throw an exception, since clearing a property without 
	 * conditional distributions is not a serious issue.
	 * @throws EditorException Thrown if the property does not exist, or is of t
	 * he wrong type
	 */
	public void 
	scratch_clearDistributions() throws EditorException;
	
	/**
	 * Replace the attributes of a conditional distribution with new 
	 * information.<p>
	 * 
	 * See {@link PropertyCreator#scratch_addConditionalDistribution} for the 
	 * requirements of dependencyConditions and probabilities.
	 * @param cid The ID of the conditional distribution, as received from 
	 * {@link PropertyCreator#scratch_addConditionalDistribution(Map, Map)} or
	 * {@link PropertyCreator#scratch_getConditionalDistributionIDs()}
	 * @param dependencyConditions See 
	 * {@link PropertyCreator#scratch_addConditionalDistribution}
	 * for requirements
	 * @param probabilities See 
	 * {@link PropertyCreator#scratch_addConditionalDistribution}
	 * for requirements
	 * @throws EditorException Thrown if the ID does not point to a 
	 * distribution, or the requirements for
	 * the parameters are not met, or if the scratch property is currently null,
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public void scratch_updateConditionalDistribution
		(int cid, Map<Integer, Integer> dependencyConditions, 
		 Map<Integer, Float> probabilities) 
		throws EditorException;
	
	/**
	 * Set the order in which the conditional distributions are to be checked.
	 * @param ordering A list of IDs of the conditional distributions in the 
	 * order they are to be checked
	 * @throws EditorException Thrown if the list does not contain each 
	 * conditional distribution ID exactly once, and only those IDs.
	 */
	public void 
	scratch_reorderConditionalDistributions(List<Integer> ordering) 
			throws EditorException;
	
	/**
	 * Set the default distribution of the scratch property.
	 * @param distribution A map of all the range IDs in the scratch property
	 * to floating point values representing the probability of that range in 
	 * the distribution.  Each range must appear exactly once in the map.
	 * @throws EditorException Thrown if the requirements for the distribution 
	 * are not met, or if the scratch property is currently null, which means
	 * {@link PropertyCreator#scratch_new(String, String, String)} needs to be 
	 * called.
	 */
	public void 
	scratch_setDefaultDistribution(Map<Integer, Float> distribution) 
			throws EditorException;
	
	/**
	 * Finalize the scratch property and add it to the structure of node 
	 * properties.  In its current state, this clears the scratch property 
	 * automatically, and there is no way to edit or remove an added node 
	 * property, so it is important to make sure the property is how the 
	 * user wants it before committing.
	 * 
	 * <p>
	 * If the scratch property was created using 
	 * {@link PropertyCreator#scratch_newInLayer}, it will
	 * be attached to the layer using the layer ID that was used there.
	 *  Call {@link PropertyCreator#scratch_getLayerID()}
	 * to see what layer it will be attached to.
	 *  
	 * @return The node property ID of the new property, which is also available
	 * through {@link PropertyViewer#nodeProp_getPropertyIDs()}
	 * 
	 * @throws EditorException There are several reasons that this exception 
	 * may be thrown, here is a list of examples:
	 * <ul>
	 * <li>The scratch property is null, which means 
	 * {@link PropertyCreator#scratch_new(String, String, String)}
	 *  was never called</li>
	 * <li>The scratch property has the same name as another node property</li>
	 * <li>The scratch property is ranged, but does not have a default 
	 * distribution set.</li>
	 * <li>The property was created using 
	 * {@link PropertyCreator#scratch_newInLayer(int, String, String, String)},
	 *  and the layer linked to the given ID was removed, which should not be
	 *   possible from this interface.</li>
	 * </ul>
	 * And various other messages.  {@link EditorException#getMessage()} 
	 * should yield more information.
	 */
	public int 
	scratch_commit() throws EditorException;
	
	/**
	 * @return The name of the scratch property
	 * @throws EditorException Thrown if the scratch property is currently null,
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public String 
	scratch_getName() throws EditorException;
	
	/**
	 * @return The type of the scratch property
	 * @throws EditorException Thrown if the scratch property is currently null,
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public String 
	scratch_getType() throws EditorException;
	
	/**
	 * @return The description of the scratch property
	 * @throws EditorException Thrown if the scratch property is currently null,
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public String 
	scratch_getDescription() throws EditorException;
	
	/**
	 * @return The dependency level, or -1 if it has not been set.
	 * @throws EditorException Thrown if the scratch property is currently null,
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public int 
	scratch_getDependencyLevel() throws EditorException;
	
	/**
	 * Get a list of unique IDs, in order of evaluation, for the conditional 
	 * distributions of the scratch property.
	 * @return The list of IDs for the conditional distributions.
	 * @throws EditorException Thrown if the scratch property is currently null,
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public List<Integer> 
	scratch_getConditionalDistributionIDs() throws EditorException;
	
	/**
	 * Get the conditions to be met for a conditional distribution to be used.
	 * @param cid The ID of the conditional distribution, obtained from 
	 * {@link PropertyCreator#scratch_getConditionalDistributionIDs()}
	 * @return A map in which each pair is a node property ID and the ID of the 
	 * range it must be for the condition to be met.
	 * @throws EditorException Thrown when the ID given does not point to a 
	 * valid conditional distribution, or if the scratch property is currently 
	 * null, which means
	 * {@link PropertyCreator#scratch_new(String, String, String)} needs to be 
	 * called.
	 */
	public Map<Integer, Integer> 
	scratch_getDistributionCondition(int cid) throws EditorException;
	
	/**
	 * Get the distribution of probabilities in a conditional distribution.
	 * @param cid The ID of the conditional distribution, as from 
	 * {@link PropertyCreator#scratch_getConditionalDistributionIDs()}
	 * @return A map of Range IDs in the scratch property to its probability in 
	 * the conditional distribution.
	 * @throws EditorException Thrown if the ID given does not point to a valid 
	 * conditional distribution, or if the scratch property is currently null, 
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public Map<Integer, Float> 
	scratch_getDistribution(int cid) throws EditorException;
	
	/**
	 * Return the default distribution as a map, similar to
	 *  {@link PropertyCreator#scratch_getDistribution(int)} .
	 * @return A map of the range IDs in the scratch property to their 
	 * probabilities in the distribution.
	 * @throws EditorException Thrown if the scratch property is currently null,
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public Map<Integer, Float> 
	scratch_getDefaultDistribution() throws EditorException;
	
	/**
	 * Check if the scratch property can use distributions (regardless of the 
	 * number of distributions).
	 * @return If the scratch property is a ranged or enumerator property.
	 * @throws EditorException Thrown if the scratch property is currently null,
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public boolean 
	scratch_isRangedProperty() throws EditorException;
	
	/**
	 * Get the layer ID of the layer that the scratch property is attached to, 
	 * if there is one.
	 * @return The layer ID, or null if the scratch property is not attached to 
	 * a layer.
	 * @throws EditorException Thrown if the scratch property is currently null, 
	 * which means {@link PropertyCreator#scratch_new(String, String, String)} 
	 * needs to be called.
	 */
	public Integer 
	scratch_getLayerID() throws EditorException;
}
