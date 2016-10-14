package org.snrg_nyc.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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
	
	@Override
	public List<String>
	templates(Class<? extends Transferable> type){
		File p = FileSystem.templatePath.resolve(type.getSimpleName()).toFile();
		if(p.exists()){
			return Arrays.asList(p.list( (File f, String s)-> 
				s.endsWith(".json") && !f.isDirectory()
			));
		}
		else {
			return Collections.<String>emptyList();
		}
	}
	@Override
	public <T extends Transferable> T
	loadFromTemplate(String name, Class<T> type) throws PersistenceException{
		//Get the template file
		File f = FileSystem.templatePath.resolve(type.getSimpleName())
		                                .resolve( name + ".json").toFile();
		if(!f.exists()){ 
			throw new PersistenceException(
					"Could not find template file: "+f.getPath());
		}
		
		try {
			Reader r = new FileReader(f);
			T t = gson.fromJson(r, type);
			r.close();
			return t;
		} 
		catch(JsonSyntaxException | JsonIOException | IOException e){
			e.printStackTrace();
			throw new PersistenceException(
					"Loading template failed - "+e.getMessage());
		} 
	}
	
}
