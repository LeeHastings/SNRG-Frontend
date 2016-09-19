package org.snrg_nyc.model.properties;

import java.lang.reflect.Type;
import java.util.Map;

import org.snrg_nyc.model.internal.SimConfig;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class SimConfigJsonAdapter 
implements JsonSerializer<SimConfig>, JsonDeserializer<SimConfig> {

	@Override
	public SimConfig 
	deserialize(JsonElement json, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		Type t = new TypeToken<Map<String, String>>(){}.getType();
		Map<String, String> m = context.deserialize(json, t);
		return new SimConfig(m);
	}

	@Override
	public JsonElement 
	serialize(SimConfig config, Type type, JsonSerializationContext context) {
		return context.serialize(config.data());
	}

}
