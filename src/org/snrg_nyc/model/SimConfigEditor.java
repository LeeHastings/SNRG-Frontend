package org.snrg_nyc.model;

import java.util.Collection;

import org.snrg_nyc.util.ConstKeyMap;

/**
 * An interface for editing SimConfig files, which are basically maps containing
 * either Strings or more maps.
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
	 * Get the keys for the members of the SimConfig file
	 * @param confID The ID of the SimConfig file
	 * @return A collection of Strings representing the keys
	 * @throws EditorException Thrown if the Con
	 */
	public Collection<String>
	config_getKeys(int confID) throws EditorException;
	
	/**
	 * Check if the given key exists in the given SimConfig map
	 * @param confID The id of the SimConfig object
	 * @param key The key to check for
	 * @return If the given key is in the object
	 * @throws EditorException thrown if the confID is not valid.
	 */
	public boolean
	config_hasKey(int confID, String key) throws EditorException;
	
	/**
	 * Get a string value from the SimConfig object
	 * @param confID The ID of the configuration
	 * @param key A key within the SimConfig
	 * @return The value at that key in the SimConfig
	 * @throws EditorException Thrown if the ID or key is invalid, or if the 
	 * value at that key is a map, not a string.
	 */
	
	public String
	config_getString(int confID, String key) throws EditorException;
	
	/**
	 * Set a string value in the SimConfig object
	 * @param confID The ID of the configuration
	 * @param key A key within the SimConfig
	 * @param value The value to set at the given key
	 * @throws EditorException Thrown if the ID or key is invalid, or if the 
	 * value at that key is a map, not a string.
	 */
	public void 
	config_setString(int confID, String key, String value) 
			throws EditorException;
	
	/**
	 * Check if the value at this key is a map.
	 * @param confID The ID of the SimConfig item to check
	 * @param key The key in the SimConfig
	 * @return If the value pointed to by the key is a map
	 * @throws EditorException Thrown if the confID or key is invalid
	 */
	public boolean 
	config_isMap(int confID, String key) throws EditorException;
	
	/**
	 * Get an inner map from a SimConfig file.  Any changes to this map are 
	 * reflected in the SimConfig file.
	 * @param confID The ID of the SimConfig file
	 * @param key The key of the map
	 * @return An {@link ConstKeyMap} of Strings, backed by the SimConfig's 
	 * real map
	 * @throws EditorException Thrown if the confID or mapKey is invalid, or
	 * if mapKey points to a string, instead of a map.
	 */
	public ConstKeyMap<String, String>
	config_getMap(int confID, String key) throws EditorException;
	
	
}
