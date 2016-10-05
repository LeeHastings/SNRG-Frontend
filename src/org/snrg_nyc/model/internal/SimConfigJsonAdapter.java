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
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class SimConfigJsonAdapter 
implements JsonSerializer<SimConfig>, JsonDeserializer<SimConfig> {

	@Override
	public SimConfig 
	deserialize(JsonElement json, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		try{
			JsonObject js = json.getAsJsonObject();
			String fsm_ID = js.get("FSM_ID").getAsString();
			
			Map<String, Either<String, Map<String,String>>> data =
					new HashMap<>();
			
			for(Map.Entry<String, JsonElement> entry : js.entrySet()){
				if(entry.getValue().isJsonPrimitive()){
					data.put(entry.getKey(), 
							Either.left(entry.getValue().getAsString()) );
				}
				else if(entry.getValue().isJsonObject()) {
					Type t = new TypeToken<Map<String, String>>(){}.getType();
					data.put(entry.getKey(), Either.right(
							context.deserialize(entry.getValue(), t)));
				}
				else {
					throw new JsonParseException(
							"Unknown SimConfig data: "+entry.getValue());
				}
			}
			
			return new SimConfig(fsm_ID, data);
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
		Map<String, Either<String, Map<String, String>>> data = config.data();
		
		for(String key : data.keySet()){
			if(data.get(key).hasLeft()){
				js.addProperty(key, data.get(key).left());
			}
			else{
				js.add(key, context.serialize(data.get(key).right()));
			}
		}
		return js;
	}

}
