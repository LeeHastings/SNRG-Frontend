package org.snrg_nyc.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.NodeProperty;
import org.snrg_nyc.model.NodePropertyAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class SimpleJsonSerializer implements ExperimentSerializer {
	
	private Map<String, Map<String, String>> experiments = new HashMap<>();
	private Gson gson  = new GsonBuilder()
	                     .setPrettyPrinting()
	                     .disableHtmlEscaping()
	                     .registerTypeAdapter(NodeProperty.class, new NodePropertyAdapter())
	                     .registerTypeAdapter(PersistentDataEntry.class, new PersistentDataEntry.JsonAdapter())
	                     .create();
	
	
	@Override
	public void storeExperiment(String name, Map<String, Serializable> dataEntries) 
			throws PersistenceException 
	{
		PersistentDataEntry pData;
		Map<String, String> jsonData = new HashMap<>();
		for(String file : dataEntries.keySet()){
			pData = new PersistentDataEntry(name,dataEntries.get(file));
			System.out.printf("\n%s.json\n====================\n",file);
			System.out.println(gson.toJson(pData));
			
			jsonData.put(file, gson.toJson(pData));
		}
		experiments.put(name, jsonData);
	}

	@Override
	public Map<String, Serializable> loadExperiment(String name) throws PersistenceException {
		Map<String, Serializable> experiment = new HashMap<>();
		
		if(experiments.containsKey(name)){
			for(String key : experiments.get(name).keySet()){
				PersistentDataEntry pde = gson.fromJson(experiments.get(name).get(key), PersistentDataEntry.class);
				experiment.put(key, pde.getObject());
			}
		}
		else {
			throw new PersistenceException("Missing experiment: "+name);
		}
		return experiment;
	}

	@Override
	public List<String> savedExperiments() {
		return new ArrayList<String>(experiments.keySet());
	}


}
