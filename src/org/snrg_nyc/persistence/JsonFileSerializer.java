package org.snrg_nyc.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.util.Transferable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonFileSerializer implements ExperimentSerializer {
	public static final Path savePath = Paths.get("save_data");
	private Gson gson;
	
	static {
		if(!Files.exists(savePath)){
			savePath.toFile().mkdir();
		}
	}
	
	/**
	 * @param gBuilder A GsonBuilder with any settings that the user requires,
	 *  such as type adapters and style settings.
	 * Internally, the GsonBuilder is then provided with type adapters for internal classes and built.
	 */
	public JsonFileSerializer(GsonBuilder gBuilder){
		super();		
		gson = gBuilder
			   .registerTypeAdapter(
					   PersistentDataEntry.class, 
					   new PersistentDataEntry.JsonAdapter())
			   .create();
	}
	@Override
	public void storeExperiment(String name, Map<String, Transferable> dataEntries) throws PersistenceException {
		Path saveDir = savePath.resolve(name);

		System.out.println("Saving to "+saveDir.toString());
		
		if(!Files.exists(saveDir)){
			saveDir.toFile().mkdir();
		}
		else { //Delete old files in directory
			try {
				Files.list(saveDir).forEach( f ->{
					try {
						Files.delete(f);
					} 
					catch (Exception e) { //Get out of the loop!
						throw new RuntimeException(e.getLocalizedMessage());
					}
				});
			} 
			catch (IOException e) {
				throw new PersistenceException("Error while getting files: "+e.getLocalizedMessage());
			}
		}
		
		for(String fileName : dataEntries.keySet()){
			Writer w = null;
			try {
				File f = saveDir.resolve(fileName+".json").toFile();
				w = new FileWriter(f);
			} 
			catch (IOException e) {
				throw new PersistenceException("Error while creating new file: "+e.getLocalizedMessage());
			}
			
			try {
				PersistentDataEntry pde = new PersistentDataEntry(name, dataEntries.get(fileName));
				w.write(gson.toJson(pde) );
			} catch (IOException e) {
				throw new PersistenceException("Error while writing file: "+e.getLocalizedMessage());
			}
			finally {
				try {
					w.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(w != null){
				try {
					w.close();
				} catch (IOException e) {
					throw new PersistenceException("Error while closing file: "+e.getLocalizedMessage());
				}
			}
		}
		
	}

	@Override
	public Map<String, Transferable> loadExperiment(String name) throws PersistenceException {
		Path saveDir = savePath.resolve(name);
		if(!Files.exists(saveDir)){
			throw new PersistenceException("No experiment with name: "+name);
		}
		Map<String, Transferable> loaded = new HashMap<>();
		
		try { 
			Files.list(saveDir).forEach(p ->{
				if(!Files.isDirectory(p) && p.getFileName().toString().endsWith(".json")){
					
					String fileName = p.getFileName().toString().replaceAll(".json", "");
					try {
						Reader r = new FileReader(p.toFile());
						JsonElement js = new JsonParser().parse(r);
						
						PersistentDataEntry pde = gson.fromJson(js, PersistentDataEntry.class);
						Transferable obj = pde.getObject();
						
						loaded.put(fileName, obj);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("In "
								+fileName+": "+e.toString());
					}
				}
			});
		} catch (Exception e) {
			throw new PersistenceException("Error while reading files: "+e.getLocalizedMessage());
		}
		
		return loaded;
	}

	@Override
	public List<String> savedExperiments() {
		List<String> experimentNames = new ArrayList<>();
		try {
			Files.list(savePath).forEach( p ->{
				if(Files.isDirectory(p)){
					experimentNames.add(p.getFileName().toString());
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return experimentNames;
	}

}
