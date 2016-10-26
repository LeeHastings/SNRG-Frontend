package org.snrg_nyc.persistence;

import java.util.List;
import java.util.Map;



/**
 * An interface for storing {@link Transferable} objects to some 
 * persistent data structure
 * @author Devin Hastings
 *
 */
public interface ExperimentSerializer {
	/**
	 * Store a group of objects into a persistent data structure.
	 * @param name The name of the experiment.
	 * @param dataEntries A map of string IDs to objects to be stored.
	 * @throws PersistenceException Thrown if there is some issue with
	 * saving the data
	 */
	public void 
	storeExperiment(String name, Map<String, Transferable> dataEntries) 
			throws PersistenceException;
	
	/**
	 * Load the data stored at the given experiment name
	 * @param name The experiment name to retrieve data from
	 * @return A map of unique IDs to generic objects that were stored with 
	 * {@link ExperimentSerializer#storeExperiment}
	 * @throws PersistenceException Thrown if there was some problem 
	 * retrieving the data
	 */
	public Map<String, Transferable> 
	loadExperiment(String name) throws PersistenceException;
	
	/**
	 * All experiments that can be loaded with 
	 * {@link ExperimentSerializer#loadExperiment}
	 * @return A list of available experiment names
	 */
	public List<String> 
	savedExperiments();
	
	/**
	 * Get the available templates for a given type
	 * @param type The type of the object to look for
	 * @return A list of valid templates for that type
	 */
	public List<String> 
	templates(Class<? extends Transferable> type);
	
	/**
	 * Load a specific object from the persistence templates
	 * @param name The name of the object
	 * @param type The type of the object
	 * @return An object of the class Type from the name given
	 * @throws PersistenceException Thrown if the object was not found or is 
	 * not of the given type.
	 */
	public <T extends Transferable> T
	loadFromTemplate(String name, Class<T> type) throws PersistenceException;
}
