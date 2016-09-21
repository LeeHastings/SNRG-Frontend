package org.snrg_nyc.model.properties;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.model.internal.SimConfig;
import org.snrg_nyc.model.internal.SimConfig.ConfigData;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

public class SimConfigJsonAdapter 
implements JsonSerializer<SimConfig>, JsonDeserializer<SimConfig> {

	@Override
	public SimConfig 
	deserialize(JsonElement json, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		try{
			JsonObject js = json.getAsJsonObject();
			String fsm_ID = js.get("FSM_ID").getAsString();
			return new SimConfig(fsm_ID, getObjectMap(js));
		}
		catch(JsonSyntaxException e){
			System.out.println("Json object: " + json);
			throw e;
		}
		
	}

	@Override
	public JsonElement 
	serialize(SimConfig config, Type type, JsonSerializationContext context) {
		return getJsonMap(config.data());
	}
	
	private JsonObject
	getJsonMap(Map<String, ConfigData<?>> data){
		JsonObject js = new JsonObject();
		for(Map.Entry<String, ConfigData<?>> entry : data.entrySet()){
			Object cdata = entry.getValue().getData();
			if(cdata instanceof String){
				js.addProperty(entry.getKey(),(String) cdata); 
			}
			else if(cdata instanceof Map<?, ?>){
				js.add(entry.getKey(), getJsonMap(
						(Map<String, ConfigData<?>>) cdata));
			}
		}
		return js;
	}
	
	private Map<String, ConfigData<?>>
	getObjectMap(JsonObject json){
		Map<String, ConfigData<?>> data = new HashMap<>();
		
		for(Map.Entry<String, JsonElement> entry : json.entrySet()){
			if(entry.getValue().isJsonPrimitive()){
				
				ConfigData<String> d = new 
						ConfigData<String>( entry.getValue().getAsString() ) ;
				data.put(entry.getKey(),d);
				
			}
			else if(entry.getValue().isJsonObject()) {
				
				ConfigData<Map<String, ConfigData<?>>> d = 
						new ConfigData<>(getObjectMap(
								entry.getValue().getAsJsonObject()));
				
				data.put(entry.getKey(), d);
				
			}
			else {
				throw new JsonParseException(
						"Unknown SimConfig data: "+entry.getValue());
			}
		}
		return data;
	}

}
