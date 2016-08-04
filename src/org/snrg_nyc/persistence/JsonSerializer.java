package org.snrg_nyc.persistence;

import java.util.Map;

import org.snrg_nyc.util.Transferable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

abstract class JsonSerializer implements ExperimentSerializer {

	private Gson gson;
	/**
	 * @param gBuilder A GsonBuilder with any settings that the user requires,
	 *  such as type adapters and style settings.
	 * Internally, the GsonBuilder is then provided with type adapters for internal classes and built.
	 */
	public JsonSerializer(GsonBuilder gBuilder){
		super();		
		gson = gBuilder
			   .registerTypeAdapter(
					   PersistentDataEntry.class, 
					   new PersistentDataEntry.JsonAdapter())
			   .create();
	}

	abstract protected void validateEnvironment(String name)
			throws PersistenceException;

	abstract protected void storeFile(String name, String data)
			throws PersistenceException;
	
	@Override
	public void 
	storeExperiment(String name, Map<String, Transferable> dataEntries) 
			throws PersistenceException 
	{
		validateEnvironment(name);
		
		for(String fileName : dataEntries.keySet()){
			PersistentDataEntry pde = new PersistentDataEntry(name, dataEntries.get(fileName));
			storeFile(fileName, gson.toJson(pde));
		}
		
	}

	protected Gson gson(){
		return gson;
	}
}
