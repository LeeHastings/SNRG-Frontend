package org.snrg_nyc.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.snrg_nyc.model.UIException;
import org.snrg_nyc.model.UI_Interface;

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
				
				if(makeProperty(depLevel, nodePropWrapper, null)){
					itr.remove();
				}
			}
			depLevel++;
		}
		JsonArray layers = obj.get("LayerAttributesList").getAsJsonArray();
		
		for(JsonElement js : layers){
			JsonObject layerJs = js.getAsJsonObject();
			
			assert_hasTag("Layer", layerJs, "LayerID");
			String layerID = layerJs.get("LayerID").getAsString();
			
			assert_hasTag("Layer "+layerID, layerJs, "PropertyDefinitionList");
			JsonArray layerProps = layerJs.get("PropertyDefinitionList").getAsJsonArray();
			
			try {
				int lid = ui.layer_new(layerID);
				for (JsonElement propjs : layerProps){
					JsonObject layerPropWrapper = propjs.getAsJsonObject();
					makeProperty(Integer.MAX_VALUE, layerPropWrapper, lid);
				}
			} catch (UIException e) {
				throw new MalformedSettingsException("Layer error: "+e.getMessage());
			}
		}
		
	}
	/**
	 * Make a scratch property
	 * @param nodePropWrapper The property wrapper (the type maps to the rest of the object)
	 * @param maxDepLvl The dependency level that the object must be equal to or less than 
	 * to be made
	 * @param layerID The layer to build the property in, or null for no layer
	 * @return If the property could be made at this time
	 * @throws MalformedSettingsException Thrown if a property has some invalid setting
	 */
	boolean makeProperty(int maxDepLvl, JsonObject nodePropWrapper, Integer layerID) 
			throws MalformedSettingsException
	{
		
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
		if(dependencylevel <= maxDepLvl){
			assert_hasTag(type, nodeProp, "PropertyName");
			String propName = nodeProp.get("PropertyName").getAsString();
			
			assert_hasTag(type+": "+propName, nodeProp, "Description");
			String propDesc = nodeProp.get("Description").getAsString();
			
			assert_hasTag(type+": "+propName, nodeProp, "DistributionID");
			String distID = nodeProp.get("DistributionID").getAsString();
			
			try{
				if(layerID == null){
					ui.scratch_new(propName, type, propDesc);
				}
				else {
					ui.scratch_newInLayer(layerID, propName, type, propDesc);
				}
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
			
			return true;
		}
		else {
			return false;
		}
	}
	
	void createDistribution(String distributionID) throws MalformedSettingsException, UIException{
		//First, this checks just enough of the file to see if it's the distribution we're looking for
		JsonObject distJs = null;
		String filename = null;
		for(Map.Entry<String, JsonElement> file : files.entrySet()){
			JsonObject jsFile = file.getValue().getAsJsonObject();
			
			//System.out.println("Searching "+file.getKey()+" for distribution "+distributionID);
			
			//Check if it's a distribution, and if it has the correct tag

			assert_tagIs("JSON File "+file.getKey(), jsFile, "ExperimentName", name);
			assert_hasTag("JSON File "+file.getKey(), jsFile, "Type");
			assert_hasTag("JSON File "+file.getKey(), jsFile, "Object");
			
			if(jsFile.get("Type").getAsString().equals("UnivariatDistribution")){
				JsonObject tempJs = jsFile.get("Object").getAsJsonObject();
				assert_hasTag("Json File "+file.getKey(), tempJs,"UnivariatDistributionID");
				
				if(tempJs.get("UnivariatDistributionID").getAsString().equals(distributionID)){
					distJs = tempJs;
					filename = file.getKey();
				}
			}
		}
		if(distJs == null){
			throw new MalformedSettingsException(
					"Did not find a file for the distribution '"+distributionID+"'");
		}
		//Now it validates the file fully
		assert_tagIs("Distribution "+filename, distJs, "BindToPropertyName", ui.scratch_getName());
		assert_hasTag("Distribution "+filename, distJs, "DependencyDistributionList");
		JsonArray distributions = distJs.get("DependencyDistributionList").getAsJsonArray();
		
		for(Iterator<JsonElement> itr = distributions.iterator(); itr.hasNext();){
			JsonObject js = itr.next().getAsJsonObject();
			assert_hasTag("Distribution "+filename, js, "DistributionSampleList");
			
			if(!js.has("PropertyDependencyList") && itr.hasNext()){
				throw new MalformedSettingsException(
					"Error in file "+filename+": unconditional distribution "
					+ "was found before the end of the distributions list");
			}
			if(!itr.hasNext() && js.has("PropertyDependencyList")){
				throw new MalformedSettingsException(
					"Error in file "+filename+": there was no unconditional "
					+ "distribution at the end of the distributions list");
			}
			
			Map<Integer, Integer> conditions = null;
			
			if(itr.hasNext()){ //Create a conditional Distribution
				conditions = new HashMap<>();
				for(JsonElement c : js.get("PropertyDependencyList").getAsJsonArray()){
					JsonObject condJs = c.getAsJsonObject();
					assert_hasTag("Distribution Condition"+filename, condJs, "Name");
					assert_hasTag("Distribution Condition"+filename, condJs, "Value");
					
					String propName =condJs.get("Name").getAsString();
					Integer pid = NodePropertyReader.getPIDfromName(ui, propName);
					
					if(pid == null){
						throw new MalformedSettingsException(
							"Error in file "+filename+": there is no property with the name"
							+ " '"+propName+"'");
					}
					if(!ui.scratch_getDependencies().contains(pid)){
						ui.scratch_addDependency(pid);
					}
					
					String propValue = condJs.get("Value").getAsString();
					Integer rid = NodePropertyReader.getRIDfromLabel(ui, pid, propValue);
					
					if(rid == null){
						throw new MalformedSettingsException(
							"Error in file "+filename+": there is no range named "
							+"'"+propValue+"' in property '"+propName+"'");
					}
					conditions.put(pid, rid);
				}
			}
			Map<Integer, Float> probMap = new HashMap<>();
			
			for(JsonElement p : js.get("DistributionSampleList").getAsJsonArray()){
				JsonObject probJs = p.getAsJsonObject();
				assert_hasTag("Distribution Probability "+filename, probJs, "Label");
				assert_hasTag("Distribution Probability"+filename, probJs, "Value");
				
				String label = probJs.get("Label").getAsString();
				Integer rid = NodePropertyReader.getRIDfromLabel(ui, label);
				if(rid == null){
					throw new MalformedSettingsException("Error in file "+filename
						+": no range label named '"+label+"' in property "
						+ui.scratch_getName());
				}
				probMap.put(rid, probJs.get("Value").getAsFloat());
			}
			if(conditions != null){
				ui.scratch_addConditionalDistribution(conditions, probMap);
			}
			else {
				ui.scratch_setDefaultDistribution(probMap);
			}
		}
	}
	
	JsonElement readJsFile(File readfile) throws FileNotFoundException{
		if(!Files.exists(saveDir) || !saveDir.toFile().isDirectory()){
			throw new FileNotFoundException("No experiment with name '"+name+"'");
		}
		FileReader reader = new FileReader(readfile);
		return new JsonParser().parse(reader);
	}
	/**
	 * @param name A name to show in the exception
	 * @param js The JSON object to check for the tag in
	 * @param tag The tag to find
	 * @throws MalformedSettingsException Thrown if the given tag is not present
	 */
	void assert_hasTag(String name, JsonObject js, String tag) throws MalformedSettingsException{
		if(!js.has(tag)){
			throw new MalformedSettingsException(
					String.format("JSON element '%s' is missing variable '%s':\n%s", name, tag, js.toString()));
		}
	}
	/**
	 * @param name The name to show in the exception
	 * @param js The JSON object to check
	 * @param tag The tag to search for and compare against
	 * @param value The value that the tag must be
	 * @throws MalformedSettingsException Thrown if the tag does not exists, or
	 * if its value did not match the value given
	 */
	void assert_tagIs(String name, JsonObject js, String tag, String value)throws MalformedSettingsException{
		assert_hasTag(name, js, tag);
		if(!js.get(tag).getAsString().equals(value)){
			throw new MalformedSettingsException(
					String.format("In JSON element '%s', expected variable '%s' to be '%s', but it was '%s':\n%s",
							name, tag, value, js.get(tag), js.toString()) );
		}
	}
}
