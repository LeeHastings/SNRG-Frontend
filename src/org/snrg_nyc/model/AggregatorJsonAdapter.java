package org.snrg_nyc.model;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

import org.snrg_nyc.model.AggregatorSettings.Op;
import org.snrg_nyc.model.internal.BivariateDistribution;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class AggregatorJsonAdapter implements JsonDeserializer<AggregatorSettings> {
	private NodeEditor model;
	private Collection<BivariateDistribution> distributions;
	public AggregatorJsonAdapter(
			NodeEditor model, 
			Collection<BivariateDistribution> distributions)
	{
		this.model = model;
		this.distributions = distributions;
	}
	@Override
	public AggregatorSettings deserialize(
			JsonElement json,
			Type type, 
			JsonDeserializationContext context)
			throws JsonParseException 
	{
		try{
			JsonObject js = json.getAsJsonObject();
			String layer = get(js, "BindToLayerID").getAsString();
			
			Integer layerID = null;
			for(int lid : model.layer_getLayerIDs()){
				if(model.layer_getName(lid).equals(layer)){
					layerID = lid;
				}
			}
			if(layerID == null){
				throw new JsonParseException("Unknown layer: "+layer);
			}
			
			AggregatorSettings agg = new AggregatorSettings(layer);
			String op = get(js, "UniversalMathOperation").getAsString();
			if(op.equals("sum")){
				agg.setOperation(Op.SUM);
			}
			else if(op.equals("product")){
				agg.setOperation(Op.MULTIPLY);
			}
			else{
				throw new JsonParseException("Unknown math operation: "+op);
			}
			JsonArray nodeProperties = 
					get(js, "NodeProperties").getAsJsonArray();
			
			for(JsonElement jj : nodeProperties){
				JsonObject propJs = jj.getAsJsonObject();
				String propName = get(propJs, "PropertyName").getAsString();
				Integer pid = model.search_nodePropWithName(propName);
				
				if(pid == null){
					throw new JsonParseException(
							"Missing property: "+propName);
				}
				String biDistID = 
						get(propJs, "BiDistributionID").getAsString();
				
				Iterator<BivariateDistribution> iter = distributions.iterator();
				BivariateDistribution bd = null;
				while(iter.hasNext()){
					BivariateDistribution b = iter.next();
					if(b.getId().equals(biDistID)){
						bd = b;
						iter.remove();
						break;
					}
				}
				if(bd == null){
					throw new JsonParseException(
							"Missing distribution for property '"+propName
							+"':"+biDistID);
				}
				agg.setNodePropertyDist(pid, bd);
			}
			for(JsonElement jj : get(js, "LayerAttributes").getAsJsonArray()){
				JsonObject propJs = jj.getAsJsonObject();
				String propName = get(propJs, "PropertyName").getAsString();
				Integer pid = model.search_nodePropWithName(propName, layerID);
				
				if(pid == null){
					throw new JsonParseException(
							"Missing property: "+propName);
				}
				String biDistID = 
						get(propJs, "BiDistributionID").getAsString();
				
				Iterator<BivariateDistribution> iter = 
						distributions.iterator();
				
				BivariateDistribution bd = null;
				while(iter.hasNext()){
					BivariateDistribution b = iter.next();
					if(b.getId().equals(biDistID)){
						bd = b;
						iter.remove();
						break;
					}
				}
				if(bd == null){
					throw new JsonParseException(
							"Missing distribution for property '"+propName
							+"':"+biDistID);
				}
				agg.setLayerDist(pid, bd);
			}
			for(JsonElement jj : get(js, "PathogensList").getAsJsonArray()){
				JsonObject pathJs = jj.getAsJsonObject();
			}
			
			return agg;
		}
		catch(EditorException e){
			throw new JsonParseException("Editor error - "+e.getMessage());
		}
	}
	
	public JsonElement 
	get(JsonObject js, String tag) throws JsonParseException{
		if(!js.has(tag)){
			throw new JsonParseException(
					"Json Element is missing tag '"+tag+"':"+js);
		}
		else {
			return js.get(tag);
		}
	}

}
