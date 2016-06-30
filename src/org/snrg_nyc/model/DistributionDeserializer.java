package org.snrg_nyc.model;

import java.lang.reflect.Type;

import org.snrg_nyc.model.UnivariatDistribution.ConditionalDistList;
import org.snrg_nyc.model.UnivariatDistribution.DistributionList;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

class DistributionDeserializer implements JsonDeserializer<DistributionList>{

	@Override
	public DistributionList deserialize(JsonElement js, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		if(js.getAsJsonObject().has("PropertyDependencyList")){
			return (ConditionalDistList) context.deserialize(js, ConditionalDistList.class);
		}
		else {
			Gson g = new Gson();
			return g.fromJson(js, DistributionList.class);
		}
	}


}
