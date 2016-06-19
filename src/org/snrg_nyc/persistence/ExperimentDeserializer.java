package org.snrg_nyc.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.snrg_nyc.model.UIException;
import org.snrg_nyc.model.UI_Interface;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ExperimentDeserializer {
	UI_Interface ui;
	Path saveDir;
	String name;
	Map<String, JsonElement> files;
	
	/**
	 * A deserializer for reading the json files from an experiment back into the system
	 * @param ui The {@link UI_Interface} to read the data into
	 * @param name The name of the experiment to read load
	 * @throws FileNotFoundException Thrown if the name given is not in the directory
	 * for saved files, or if it is not a directory.
	 */
	public ExperimentDeserializer(UI_Interface ui, String name) throws FileNotFoundException{
		this.ui = ui;
		this.name = name;
		saveDir = PersistenceData.saveLocation.resolve(name);
		files = new HashMap<>();
		
		//Read all the json files in the directory
		File[] filenames = saveDir.toFile().listFiles();
		for(int i = 0; i < filenames.length; i++){
			if(filenames[i].getName().endsWith(".json")){
				files.put(filenames[i].getName(), readJsFile(filenames[i]));
			}
		}
	}
	public void loadFiles() throws MalformedSettingsException {
		if(!files.containsKey("nodesettings.json")){
			throw new MalformedSettingsException("Missing node settings file");
		}
		
		JsonObject nodejs = files.get("nodesettings.json").getAsJsonObject();
		String ns = "Node Settings";
		assert_tagIs(ns, nodejs, "ExperimentName", name);
		assert_tagIs(ns, nodejs, "Type", "NodeSettings");
		assert_hasTag(ns, nodejs, "Object");
		
		JsonObject obj = nodejs.get("Object").getAsJsonObject();
		assert_hasTag(ns+" Object", obj, "PropertyDefinitionList");
		assert_hasTag(ns+" Object", obj, "LayerAttributesList");
		
		//Create the properties, one at a time, in order of dependency levels
		JsonArray properties = obj.get("PropertyDefinitionList").getAsJsonArray();
		int depLevel = 0;
		while(properties.size() > 0){
			for( Iterator<JsonElement> itr = properties.iterator(); itr.hasNext(); ){
				JsonObject nodePropWrapper = itr.next().getAsJsonObject();
				
				if(nodePropWrapper.entrySet().size() != 1){
					throw new MalformedSettingsException("Invalid node property: "+nodePropWrapper);
				}
				String type = null;
				JsonObject nodeProp = null;
				
				for(String t : ui.nodeProp_getTypes()){
					if(nodePropWrapper.has(t)){
						type = t;
						nodeProp = nodePropWrapper.get(t).getAsJsonObject();
						break;
					}
				}
				if(type == null || nodeProp == null){
					throw new MalformedSettingsException(
							"Unknown node property type: "+nodePropWrapper);
				}
				
				assert_hasTag(type, nodeProp, "Dependency Level");
				int dependencylevel = nodeProp.get("Dependency Level").getAsInt();
				
				//It's a shame so much work has to be done before checking the dependency level
				if(dependencylevel == depLevel){
					assert_hasTag(type, nodeProp, "PropertyName");
					String propName = nodeProp.get("PropertyName").getAsString();
					
					assert_hasTag(type+": "+propName, nodeProp, "Description");
					String propDesc = nodeProp.get("Description").getAsString();
					
					assert_hasTag(type+": "+propName, nodeProp, "DistributionID");
					String distID = nodeProp.get("DistributionID").getAsString();
					
					try{
						ui.scratch_new(propName, type, propDesc);
						ui.scratch_setDependencyLevel(dependencylevel);
						
						switch(type){
						case "EnumeratorProperty":
							assert_hasTag(type+": "+propName, nodeProp, "EnumValues");
							JsonArray enums = nodeProp.get("EnumValues").getAsJsonArray();
							
							for(JsonElement val : enums){
								ui.scratch_addRange(val.getAsString());
							}
							break;
							
						case "IntegerRangeProperty": //Add our ranges
							assert_hasTag(type+": "+propName, nodeProp, "IntegerRangeList");
							JsonArray ranges = nodeProp.get("IntegerRangeList").getAsJsonArray();
							
							for(JsonElement val : ranges){
								JsonObject range = val.getAsJsonObject();
								assert_hasTag(type+": "+propName, range, "RangeID");
								assert_hasTag(type+": "+propName, range, "Min");
								assert_hasTag(type+": "+propName, range, "Max");
								
								String label = range.get("RangeID").getAsString();
								int min = range.get("Min").getAsInt();
								int max = range.get("Max").getAsInt();
								
								int rid = ui.scratch_addRange(label);
								ui.scratch_setRangeMin(rid, min);
								ui.scratch_setRangeMax(rid, max);
							}
							break;
							
						case "FractionProperty":
							assert_hasTag(type+": "+propName, nodeProp, "DisableRandom_UseInitValue");
							float initVal = nodeProp.get("DisableRandom_UseInitValue").getAsFloat();
							ui.scratch_setFractionInitValue(initVal);
							break;
						}
						switch(distID){
						case "uniform":
							ui.scratch_useUniformDistribution();
							break;
						case "null":
							if(!type.equals("FractionProperty")){
								throw new MalformedSettingsException(
										String.format("Bad setting on %s '%s', only FractionProperty"
										+ "can have a null distribution.", type, propName));
							}
							break;
						default:
							createDistribution(distID);
							break;
						}
						ui.scratch_commitToNodeProperties();
					}
					catch(UIException e){
						throw new MalformedSettingsException("New Property error: "+e.getMessage());
					}
					
					itr.remove();
				}
			}
			depLevel++;
		}
		
	}
	
	void createDistribution(String distributionID) throws MalformedSettingsException, UIException{
		//TODO Implement this method
		ui.scratch_useUniformDistribution();
	}
	
	JsonElement readJsFile(File readfile) throws FileNotFoundException{
		if(!Files.exists(saveDir) || !saveDir.toFile().isDirectory()){
			throw new FileNotFoundException("No experiment with name '"+name+"'");
		}
		FileReader reader = new FileReader(readfile);
		return new JsonParser().parse(reader);
	}
	
	void assert_hasTag(String name, JsonObject js, String tag) throws MalformedSettingsException{
		if(!js.has(tag)){
			throw new MalformedSettingsException(
					String.format("JSON element '%s' is missing variable '%s'", name, tag));
		}
	}
	
	void assert_tagIs(String name, JsonObject js, String tag, String value)throws MalformedSettingsException{
		assert_hasTag(name, js, tag);
		if(!js.get(tag).getAsString().equals(value)){
			throw new MalformedSettingsException(
					String.format("In JSON element '%s', expected variable '%s' to be '%s', but it was '%s'",
							name, tag, value, js.get(tag)) );
		}
	}
}
