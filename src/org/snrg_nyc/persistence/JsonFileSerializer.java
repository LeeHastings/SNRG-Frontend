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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonFileSerializer extends JsonSerializer {
	public static final Path savePath = Paths.get("save_data");
	private Path saveDir;
	
	static {
		if(!Files.exists(savePath)){
			savePath.toFile().mkdir();
		}
	}
	
	public JsonFileSerializer(GsonBuilder gBuilder) {
		super(gBuilder);
	}
	

	@Override
	public Map<String, Transferable> 
	loadExperiment(String name) throws PersistenceException {
		saveDir = savePath.resolve(name);
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
						
						PersistentDataEntry pde = gson().fromJson(js, PersistentDataEntry.class);
						Transferable obj = pde.getObject();
						
						loaded.put(fileName, obj);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("In "
								+fileName+": "+e.toString());
					}
				}
			});
		}
		catch (Exception e) {
			throw new PersistenceException("Error while reading files: "+e.getLocalizedMessage());
		}
		
		return loaded;
	}

	@Override
	public List<String> 
	savedExperiments() {
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

	@Override
	protected void 
	validateEnvironment(String name) throws PersistenceException {
		saveDir = savePath.resolve(name);

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
				e.printStackTrace();
				throw new PersistenceException("Error while getting files: "
						+e.getLocalizedMessage());
			}
		}
	}

	@Override
	protected void 
	storeFile(String name, String data) throws PersistenceException {
		Writer w = null;
		try {
			File f = saveDir.resolve(name+".json").toFile();
			w = new FileWriter(f);
		} 
		catch (IOException e) {
			throw new PersistenceException("Error while creating new file: "
					+e.getLocalizedMessage());
		}
		
		try {
			w.write(data);
		} catch (IOException e) {
			throw new PersistenceException("Error while writing file: "
					+e.getLocalizedMessage());
		}
		finally {
			try {
				w.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new PersistenceException("Error while closing file: "
						+e.getLocalizedMessage());
			}
		}
		
		if(w != null){
			try {
				w.close();
			} catch (IOException e) {
				throw new PersistenceException("Error while closing file: "
						+e.getLocalizedMessage());
			}
		}
		
	}

}
