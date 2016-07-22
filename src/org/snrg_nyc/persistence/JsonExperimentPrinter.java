package org.snrg_nyc.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.Transferable;

import com.google.gson.Gson;

/**
 * Simple class to test JSON serialization.  Prints directly to the console.
 * @author Devin Hastings
 *
 */
@Deprecated
public class JsonExperimentPrinter implements ExperimentSerializer {
	
	private Map<String, Map<String, String>> experiments = new HashMap<>();
	private Gson gson;
	
	public JsonExperimentPrinter(Gson g){
		super();
		gson = g;
	}
	@Override
	public void storeExperiment(String name, Map<String, Transferable> dataEntries) 
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
	public Map<String, Transferable> loadExperiment(String name) throws PersistenceException {
		Map<String, Transferable> experiment = new HashMap<>();
		
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
