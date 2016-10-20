package org.snrg_nyc.model.internal;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.util.Either;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
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
			
			Map<String, SimConfig.Setting> map = new HashMap<>();
			
			for(Map.Entry<String, JsonElement> e : js.entrySet()){
				map.put(e.getKey(), deserializeSetting(e.getValue()));
			}
			
			return new SimConfig(fsm_ID, map);
		}
		catch(JsonSyntaxException e){
			System.out.println("Json object: " + json);
			throw e;
		} catch (EditorException e) {
			e.printStackTrace();
			throw new JsonParseException("Editor failure - "
			+e.getLocalizedMessage());
		}
		
	}

	@Override
	public JsonElement 
	serialize(SimConfig config, Type type, JsonSerializationContext context) {
		JsonObject js = new JsonObject();
		Map<String, SimConfig.Setting> data = config.data();
		
		for(String key : data.keySet()){
			try {
				js.add(key, serializeSetting(data.get(key)));
			} 
			catch (EditorException e) {
				e.printStackTrace();
				throw new JsonParseException("Could not serialize object '"
						+key+"': "+e.getMessage());
			}
		}
		return js;
	}
	
	private JsonElement
	serializeSetting(SimConfig.Setting setting) throws EditorException {
		if(setting.isMap()){
			JsonObject json = new JsonObject();
			
			for(Map.Entry<String,SimConfig.Setting>e : setting.getMap().entrySet()){
				json.add(e.getKey(), serializeSetting(e.getValue()));
			}
			return json;
		}
		else {
			return new JsonPrimitive(setting.getString());
		}
	}
	
	private SimConfig.Setting
	deserializeSetting(JsonElement json){
		if(json.isJsonPrimitive()){
			return new SimConfig.Setting(Either.left(json.getAsString()));
		}
		else {
			Map<String, SimConfig.Setting> map = new HashMap<>();
			JsonObject js = json.getAsJsonObject();
			for(Map.Entry<String, JsonElement> e : js.entrySet()){
				map.put(e.getKey(), deserializeSetting(e.getValue()));
			}
			return new SimConfig.Setting(Either.right(map));
		}
	}

}
