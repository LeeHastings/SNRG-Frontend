package org.snrg_nyc.model;

import java.util.List;

/**
 * The public-facing business logic for use by the user interface of an SNRG 
 * frontend.  This interface and any implementations of it should be the only 
 * classes used outside of the model packages.
 * @author Devin Hastings
 * @version 0.2
 */
public interface PropertiesEditor extends PropertyCreator, PropertyViewer {

	/**
	 * Save the properties to a persistent structure.  This does not 
	 * validate the data, only preserving the project in its current state.
	 * @param experimentName The name of the experiment under which to
	 *  save the data.
	 * @throws EditorException Thrown if there was some error while saving 
	 * (the message will likely have details)
	 */
	public void save(String experimentName) throws EditorException;
	
	/**
	 * Save the properties to the name currently in the experiment,
	 * @throws EditorException Thrown if there was a problem while saving
	 * @see  PropertiesEditor#save(String)
	 */
	public void save() throws EditorException;
	
	/**
	 * Load an experiment with the given name from the persistent structure.
	 * @param experimentName The name of the experiment (should be the same as when it was saved)
	 * @throws EditorException Thrown if there is no experiment with the given name, or if the 
	 * experiment saved there is somehow invalid.
	 */
	public void load(String experimentName) throws EditorException;
	
	/**
	 * Clear the experiment of all properties and settings, leaving the UI in its initial state
	 */
	public void clear();
	
	/**
	 * Return the names of experiments stored in the persistent data structure
	 * @return A list of names of experiments that can be used in {@link PropertiesEditor#load(String)}
	 */
	public List<String> getExperimentNames();
	
	/**
	 * If the given instance of {@link PropertiesEditor} can use layers.
	 * @return True if layers are allowed, otherwise false
	 */
	public boolean allowsLayers();
	
	/**
	 * A flag showing this editor contains the experiment information,
	 * which is accessed by methods beginning with "experiment_"
	 * @return True if experiment methods can be called, otherwise false.
	 */
	public boolean hasExperimentInfo();
	
	/**
	 * Get all the valid node property types
	 * @return A list of all valid Node Property types for use in 
	 * {@link PropertyCreator#scratch_new}
	 */
	public List<String> 
	getPropertyTypes();
	
	/*
	 * Experiment methods
	 */
	/**
	 * Get a description of the experiment
	 * @return The description of the experiment
	 * @throws EditorException thrown if this is not the main editor
	 */
	public String experiment_getDescription() throws EditorException;
	
	/**
	 * Set the description for the experiment
	 * @param desc The description
	 * @throws EditorException thrown if this is not the main editor
	 */
	public void experiment_setDescription(String desc) throws EditorException;
	
	/**
	 * Get the username attached to the experiment
	 * @return The username for this experiment
	 * @throws EditorException thrown if this is not the main editor
	 */
	public String experiment_getUserName() throws EditorException;
	
	/**
	 * Set the username for this experiment
	 * @param name The new username for this experiment
	 * @throws EditorException thrown if this is not the main editor
	 */
	public void experiment_setUserName(String name) throws EditorException;
	
	/**
	 * Get the name of the experiment 
	 * @return The experiment's name
	 * @throws EditorException thrown if this is not the main editor
	 */
	public String experiment_getName() throws EditorException;
	
	/**
	 * Set the name of the experiment.  This is used when 
	 * {@link PropertiesEditor#save()} is called
	 * @param name The new name for the experiment
	 * @throws EditorException thrown if this is not the main editor
	 */
	public void experiment_setName(String name) throws EditorException;
	
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
	 * Test if a property ID points to an existing node property
	 * @param pid The property ID to test
	 * @return If the pid points to an existing node property
	 */
	public boolean test_nodePropIDExists(int pid);
	
	/**
	 * Tests if a property ID points to a non-null property within a layer.
	 * @param lid The Layer ID to check in, should be from {@link PropertiesEditor#layer_getLayerIDs()}
	 * @param pid The property ID to check.
	 * @return If there is a non-null property that is linked to the give PID
	 * @throws EditorException Thrown if the layer ID is invalid.
	 */
	public boolean test_nodePropIDExists(int lid, int pid) throws EditorException;
	
	/**
	 * Test if a name for a layer is taken
	 * @param name The name to check
	 * @return True if the name can be used for a new layer 
	 * (as there is no other layer with this name), false otherwise
	 */
	public boolean test_layerNameIsUnique(String name);
	
	/**
	 * Test if the given layer ID points to a non-null layer
	 * @param lid The Layer ID to check
	 * @return If there is a layer that is linked to the given layer ID
	 */
	public boolean test_layerIDExists(int lid);
	
	/*                *\
	 * Search methods *
    \*                */
	
	/**
	 * Search for a property with the given name
	 * @param name The name of the property
	 * @return The ID of a property with the given name, or null if there is no match
	 */
	public Integer search_nodePropWithName(String name);
	
	/**
	 * Search for a property with the given name within a layer
	 * @param name The name of the property
	 * @param lid The layer to search in
	 * @return The property ID of the matching property, or null if there was no match
	 * @throws EditorException Thrown if the Layer ID is invalid
	 */
	public Integer search_nodePropWithName(String name, int lid) throws EditorException;
	
	/**
	 * Search for a range in the given property with the given label
	 * @param pid The ID of the property to search in
	 * @param label The label of the range to search for
	 * @return The range ID of a range with the requested label, or null if there was no match
	 * @throws EditorException Thrown if the property ID is invalid
	 */
	public Integer search_rangeWithLabel(int pid, String label) throws EditorException;

	
	/*                          *\
	 * --- Pathogen Methods --- * 
	\*                          */
	/**
	 * Get all valid pathogen IDs.
	 * @return Pathogen IDs for valid pathogens
	 * @throws EditorException Thrown if you are editing a 
	 * pathogen, which does not have embedded pathogens.
	 */
	public List<Integer> pathogen_getPathogenIDs() throws EditorException;
	
	/**
	 * Get the name of a pathogen
	 * @param pathID The pathogen ID, from 
	 * {@link PropertiesEditor#pathogen_getPathogenIDs()}
	 * @return The name of the given pathogen
	 * @throws EditorException Thrown if the editor does not support pathogens,
	 * or if the pathogen ID is invalid
	 */
	public String pathogen_getName(int pathID) throws EditorException;
	
	/**
	 * Get a PropertiesEditor for the given pathogen in order to add properties
	 * @param pathID The ID of the pathogen, which is from 
	 * {@link PropertiesEditor#pathogen_getPathogenIDs()}
	 * @return A PropertiesEditor that is tied to the pathogen
	 * @throws EditorException Thrown if the given ID does not exist.
	 */
	public PropertiesEditor 
	pathogen_getEditor(int pathID) throws EditorException;
	
	/*               *\
	 * Layer Methods *
	\*               */
	
	/**
	 * Create a new layer
	 * @param name The name for the new layer.
	 * @return An integer ID for the new layer.
	 * @throws EditorException Thrown if a layer with that name already exists.
	 */
	public int layer_new(String name) throws EditorException;
	
	/**
	 * Get the available IDs for node layers.
	 * @return All IDs that point to non-null layer items.
	 */
	public List<Integer> layer_getLayerIDs();
	
	/**
	 * Get the name of a layer
	 * @param lid The ID, from {@link PropertiesEditor#layer_getLayerIDs()}
	 * @return The name of the layer linked to the give ID
	 * @throws EditorException Thrown if the layer ID does not point to a layer
	 */
	public String layer_getName(int lid) throws EditorException;
	
	/**
	 * Change the name of a layer
	 * @param lid The ID of the layer to rename.
	 * @param name The new name of the layer.
	 * @throws EditorException Thrown if the layer ID is invalid, or
	 * if a layer with the given name already exists.
	 */
	public void layer_setName(int lid, String name) throws EditorException;
	
	/**
	 * Get the {@link PropertiesEditor} for the edge settings on the given layer.
	 * This allows the user to edit the properties of the edge settings.
	 * <p>
	 * These are different from the layer properties in the node settings, which
	 * are edited from this instance of the PropertiesEditor.
	 * @param lid The ID of the layer to edit settings for
	 * @throws EditorException Thrown if the layer ID is invalid.
	 * @return A new {@link PropertiesEditor} linked to a layer's edge settings.
	 */
	public PropertiesEditor layer_getEdgeEditor(int lid) throws EditorException;

}

