package org.snrg_nyc.model;

import java.util.Collection;

/**
 * An interface for editing SimConfig files, which are basically maps containing
 * either Strings or more maps (to an arbitrary depth).
 * 
 * @author Devin Hastings
 *
 */
public interface SimConfigEditor {
	
	/* TODO Implement these requirements:
	 * optional multiple (>= 0)  -> Intevention_*
	 * mandatory one (== 1)      -> InterventionConfig
	 * mandatory multiple (>= 1),-> 
	 * optional one (<= 1)
	 */
	/**
	 * Templates are predesigned files that provide an unpopulated SimConfig 
	 * file with the appropriate attributes.  This function provides a list of
	 * the templates' names
	 * @return A list of template names
	 * @throws EditorException Thrown if the editor doesn't support SimConfig 
	 * editing
	 */
	public Collection<String> 
	config_getTemplates() throws EditorException;
	
	/**
	 * Get the IDs of every SimConfig object
	 * @return The IDs of the SimConfig objects
	 * @throws EditorException Thrown if the editor doesn't support SimConfig 
	 * editing
	 */
	public Collection<Integer>
	config_getIDs() throws EditorException;
	
	/**
	 * Create a new SimConfig object for editing from the given template name
	 * @param template The template to use, from {@link #config_getTemplates()}
	 * @return The ID of the new SimConfig object, for use when editing
	 * @throws EditorException Thrown if the template is not valid
	 */
	public int
	config_newFromTemplate(String template) throws EditorException;
	
	/**
	 * Get the keys from the cSimConfig object, or a submap if keys are provided
	 * @param confID The ID of the Simconfig object
	 * @param keys The keys pointing to the submap, so internally it's 
	 * something like <code>map.get(keys[0]).get(keys[1]).get(...)</code>
	 * @return Return the keys in the given map
	 * @throws EditorException Thrown if the confID is invalid, or if the keys
	 * do not lead to a valid map.
	 */
	public Collection<String>
	config_getKeys(int confID, String ... keys) throws EditorException;
	
	/**
	 * Check if the given key exists in a SimConfig map
	 * @param confID The ID of the SimConfig object
	 * @param keys The keys to search for, in the style of
	 * <code>map.get(keys[0]).get(keys[1]).get(...)</code>
	 * @return If the given key (and submaps) exist
	 * @throws EditorException Thrown if the confID is invalid.
	 */
	public boolean
	config_hasKey(int confID, String ... keys) 
			throws EditorException;

	/**
	 * Get the string pointed to by the keys.
	 * @param confID The ID of the SimConfig object.
	 * @param keys The keys pointing to the submap, so internally it's 
	 * something like <code>map.get(keys[0]).get(keys[1]).get(...)</code>
	 * @return The String pointed to by the submaps
	 * @throws EditorException Thrown if the confID or keys are invalid, or if
	 * the object pointed to is actually a map.
	 */
	public String
	config_getString(int confID,String ... keys) 
			throws EditorException;

	/**
	 * Set the value of a string at a given location
	 * @param confID The ID of the SimConfig object
	 * @param value The new value of the item
	 * @param keys The keys in the submaps, something like 
	 * <code>map.get(keys[0]).get(keys[1]).get(...)</code>
	 * @throws EditorException Thrown if the confID or keys are invalid, or 
	 * if the value pointed to is a map, not a string.
	 */
	public void 
	config_setString(int confID, String value, String ... keys) 
			throws EditorException;

	/**
	 * Check if the given item is a map
	 * @param confID The ID of the SimConfig file
	 * @param keys The submap keys, in the style of 
	 * <code>map.get(keys[0]).get(keys[1]).get(...)</code>
	 * @return If the key is a map
	 * @throws EditorException Thrown if the confID or any of the keys are 
	 * invalid
	 */
	public boolean 
	config_isMap(int confID, String ... keys) 
			throws EditorException;
	
	/**
	 * Delete a SimConfig object
	 * @param confID The ID of the object
	 * @throws EditorException Thrown if the ID is invalid
	 */
	public void
	config_delete(int confID) throws EditorException;
	
}
