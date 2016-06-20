package org.snrg_nyc.persistence;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.snrg_nyc.model.UIException;
import org.snrg_nyc.model.UI_Interface;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ExperimentSerializer {
	private UI_Interface ui;
	private Map<String, JsonElement> files;
	private String name;
	private int distributions;
	
	public ExperimentSerializer(String experimentName, UI_Interface ui){
		name = experimentName;
		files = new HashMap<>();
		this.ui = ui;
		distributions = 0;
	}
	public void saveStateToFiles(){
		// Save NodeSettings and distributions
		JsonObject settings = new JsonObject();
		settings.addProperty("ExperimentName", name);
		settings.addProperty("Type", "NodeSettings");
		JsonObject obj = new JsonObject();
		JsonArray propList = new JsonArray();
		JsonArray layerList = new JsonArray();
		
		try {
			for(int pid : ui.nodeProp_getPropertyIDs()){
				JsonObject np = readNodeProperty(new NodePropertyReader(ui,pid));
				propList.add(np);
			};
			JsonArray layerProperties;
			for(int lid : ui.layer_getLayerIDs()){
				JsonObject layer = new JsonObject();
				layer.addProperty("LayerID", ui.layer_getName(lid));
				layerProperties = new JsonArray();
				for(int pid : ui.nodeProp_getPropertyIDs(lid)){
					JsonObject np = readNodeProperty(new NodePropertyReader(ui, lid, pid));
					layerProperties.add(np);
				}
				layer.add("PropertyDefinitionList", layerProperties);
				layerList.add(layer);
			}
			files.put("nodesettings", settings);
		}
		
		catch(UIException e){
			e.printStackTrace();
		}
		obj.add("PropertyDefinitionList", propList);
		obj.add("LayerAttributesList", layerList);
		settings.add("Object", obj);
		writeFiles();
	}
	
	JsonObject readNodeProperty(NodePropertyReader npr) throws UIException{
		JsonObject np = new JsonObject();
		np.addProperty("PropertyName", npr.name());
		np.addProperty("Description", npr.description());
		np.addProperty("Dependency Level", npr.dependencyLevel());
		
		switch(npr.type()){
		case("EnumeratorProperty"):
			JsonArray enums = new JsonArray();
			for(int rid : npr.rangeIDs()){
				enums.add(new JsonPrimitive(npr.rangeLabel(rid)));
			}
			np.add("EnumValues", enums);
			break;
		case("IntegerRangeProperty"):
			JsonArray ranges = new JsonArray();
			for(int rid: npr.rangeIDs()){
				JsonObject range = new JsonObject();
				
				range.addProperty("RangeID", npr.rangeLabel(rid));
				range.addProperty("Min", npr.rangeMin(rid));
				range.addProperty("Max", npr.rangeMax(rid));
				
				ranges.add(range);
			}
			np.add("IntegerRangeList", ranges);
			break;
		case("FractionProperty"):
			np.addProperty("DisableRandom_UseInitValue", npr.initValue());
			break;
		}
		np.addProperty("DistributionID", createUnivariatDistribution(npr));
		JsonObject nodeProp = new JsonObject();
		nodeProp.add(npr.type(), np);
		return nodeProp;
	}
	
	String createUnivariatDistribution(NodePropertyReader npr) throws UIException{
		switch(npr.distributionType()){
		case Null:
			return "null";
		case Uniform:
			return "uniform";
		default:
			String npName = npr.name().replaceAll("\\W", "").toLowerCase();
			String distID = npName+"_dist"+distributions;
			
			JsonObject js = new JsonObject();
			js.addProperty("ExperimentName", name);
			js.addProperty("Type", "UnivariatDistribution");
			
			JsonObject obj = new JsonObject();
			obj.addProperty("UnivariatDistributionID", distID);
			obj.addProperty("BindToPropertyName", npr.name());
			JsonArray depDistList = new JsonArray();
			
			for(int cid : npr.conditionalDistributionIDs()){
				JsonObject conDistribution = new JsonObject();
				JsonArray propDepList = new JsonArray();
				JsonObject condition;
				for(Entry<Integer, Integer> entry : npr.distributionConditions(cid).entrySet()){
					condition = new JsonObject();
					condition.addProperty("Name", ui.nodeProp_getName(entry.getKey()) );
					condition.addProperty("Value", ui.nodeProp_getRangeLabel(entry.getKey(), entry.getValue()) );
					propDepList.add(condition);
				}
				conDistribution.add("PropertyDependencyList", propDepList);
				
				JsonArray distSampleList = new JsonArray();
				JsonObject distribution;
				for(Entry<Integer, Float> entry : npr.distributionMap(cid).entrySet()){
					distribution = new JsonObject();
					distribution.addProperty("Label", npr.rangeLabel(entry.getKey()) );
					distribution.addProperty("Value", entry.getValue());
					distSampleList.add(distribution);
				}
				conDistribution.add("DistributionSampleList", distSampleList);
				depDistList.add(conDistribution);
			}
			JsonObject dist = new JsonObject();
			JsonArray defaultDistribution = new JsonArray();
			JsonObject distPair;
			for(Entry<Integer, Float> entry : npr.defaultDistribution().entrySet() ){
				distPair = new JsonObject();
				distPair.addProperty("Label", npr.rangeLabel(entry.getKey()));
				distPair.addProperty("Value", entry.getValue());
				defaultDistribution.add(distPair);
			}
			
			dist.add("DistributionSampleList", defaultDistribution);
			depDistList.add(dist);
			
			obj.add("DependencyDistributionList", depDistList);
			js.add("Object", obj);
			files.put("univariatdist"+(distributions++)+"_"+npName, js);
			return distID;
		}
	}
	
	void writeFiles(){
		Path saveDir = PersistenceData.saveLocation.resolve(name);
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.disableHtmlEscaping()
				.create();
		try{
			if(!Files.exists(saveDir)){
				Files.createDirectories(saveDir);
			}
			for(File f : saveDir.toFile().listFiles()){
				Files.delete(f.toPath());
			}
			for(Entry<String, JsonElement> file : files.entrySet()){
				Path output = saveDir.resolve(file.getKey()+".json" );
				Files.createFile(output);
				Files.write(output, gson.toJson(file.getValue()).getBytes(), StandardOpenOption.WRITE);
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

}
