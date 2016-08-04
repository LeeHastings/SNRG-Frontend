package org.snrg_nyc.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.util.Transferable;

import com.google.gson.GsonBuilder;

/**
 * Simple class to test JSON serialization.  Prints directly to the console.
 * @author Devin Hastings
 *
 */
public class JsonExperimentPrinter extends JsonSerializer {
	private Map<String, Map<String, Transferable>> experiments;
	
	public JsonExperimentPrinter(GsonBuilder gBuilder) {
		super(gBuilder);
		experiments = new HashMap<>();
	}

	@Override
	public Map<String, Transferable> loadExperiment(String name) throws PersistenceException {
		if(!experiments.containsKey(name)){
			throw new PersistenceException("No experiment with name: "+name);
		}
		else {
			return experiments.get(name);
		}
	}

	@Override
	public List<String> 
	savedExperiments() {
		return new ArrayList<String>(experiments.keySet());
	}

	@Override
	protected void 
	validateEnvironment(String name) throws PersistenceException {
		if(experiments == null){
			experiments = new HashMap<>();
		}
		if(gson() == null){
			throw new PersistenceException("The Gson adapter is null!");
		}
	}
	@Override
	public void 
	storeExperiment(String name, Map<String, Transferable> objects)
			throws PersistenceException
	{
		super.storeExperiment(name, objects);
		experiments.put(name, objects);
	}

	@Override
	protected void storeFile(String name, String data) throws PersistenceException {
		System.out.printf("\n%s\n================\n%s", name, data);
	}



}
