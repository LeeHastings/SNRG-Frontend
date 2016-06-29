package org.snrg_nyc.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * An interface for storing generic objects to some persistent data structure
 * @author Devin Hastings
 *
 */
public interface ExperimentSerializer {
	/**
	 * Store a group of objects into a persistent data structure.
	 * @param name The name of the experiment.
	 * @param dataEntries A map of string IDs to objects to be stored.
	 * @throws PersistenceException Thrown if there is some issue with saving the data
	 */
	public void storeExperiment(String name, Map<String, Serializable> dataEntries) 
			throws PersistenceException;
	
	/**
	 * Load the data stored at the given experiment name
	 * @param name The experiment name to retrieve data from
	 * @return A map of unique IDs to generic objects that were stored with 
	 * {@link ExperimentSerializer#storeExperiment}
	 * @throws PersistenceException Thrown if there was some problem retrieving the data
	 */
	public Map<String, Serializable> loadExperiment(String name) throws PersistenceException;
	
	/**
	 * All experiments that can be loaded with {@ExperimentSerializer#loadExperiment}
	 * @return A list of the experiment names
	 */
	public List<String> savedExperiments();
}
