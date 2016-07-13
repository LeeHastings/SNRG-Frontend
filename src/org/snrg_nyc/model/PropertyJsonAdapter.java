package org.snrg_nyc.model;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Json serializer/deserializer for {@link NodeProperty} and all its subclasses
 * @author Devin Hastings
 *
 */
class PropertyJsonAdapter implements JsonSerializer<NodeProperty>, JsonDeserializer<NodeProperty> {
	//Passing strings as parameters multiple times is too error-prone for me
	final static String nameLabel = "PropertyName";
	final static String descLabel = "Description";
	final static String depLabel = "Dependency Level";
	final static String intRangesLabel = "IntegerRangeList";
	final static String enumValsLabel = "EnumValues";
	final static String initValLabel = "DisableRandom_UseInitValue";
	final static String distIDLabel = "DistributionID";
	final static String pathogenLabel = "PathogenType";
	
	private Class<?>[] propertyTypes;
	
	public PropertyJsonAdapter(Class<?>[] propTypes){
		propertyTypes = propTypes;
	}
	
	@Override
	public JsonElement serialize(NodeProperty nodeProp, Type type, JsonSerializationContext context) {
		JsonObject propertyJs = new JsonObject();
		JsonObject innerJs = new JsonObject();
		propertyJs.add(nodeProp.getClass().getSimpleName(), innerJs);
		
		innerJs.addProperty(nameLabel, nodeProp.getName());
		innerJs.addProperty(descLabel, nodeProp.getDescription());
		innerJs.addProperty(depLabel, nodeProp.getDependencyLevel());
		
		if(nodeProp instanceof IntegerRangeProperty){
			IntegerRangeProperty irp = (IntegerRangeProperty) nodeProp;
			
			JsonArray ranges = new JsonArray();
			innerJs.add(intRangesLabel, ranges);
			
			for(int i : irp.getSortedRangeIDs()){
				JsonObject rangeJs = new JsonObject();
				
				rangeJs.addProperty("RangeID", irp.getRangeLabel(i));
				rangeJs.addProperty("Min", irp.getRangeMin(i));
				rangeJs.addProperty("Max", irp.getRangeMax(i));
				
				ranges.add(rangeJs);
			}
		}
		else if(nodeProp instanceof BooleanProperty){
			if(nodeProp instanceof AttachmentProperty){
				innerJs.addProperty(pathogenLabel, ((AttachmentProperty)nodeProp).getPathogen() );
			}
		}
		else if(nodeProp instanceof EnumeratorProperty){
			EnumeratorProperty en = (EnumeratorProperty) nodeProp;
			
			JsonArray values = new JsonArray();
			innerJs.add(enumValsLabel, values);
			
			for(int i : en.getSortedRangeIDs()){
				values.add( new JsonPrimitive(en.getRangeLabel(i)) );
			}
		}
		else if(nodeProp instanceof FractionProperty){
			innerJs.addProperty(
					initValLabel, 
					((FractionProperty) nodeProp).getInitValue());
		}
		innerJs.addProperty(distIDLabel, nodeProp.getDistributionID());
		return propertyJs;
	}

	@Override
	public NodeProperty deserialize(JsonElement js, Type type, JsonDeserializationContext context)
			throws JsonParseException 
	{
		JsonObject nodePropJs = js.getAsJsonObject();
		NodeProperty nodeProp = null;
		JsonObject innerJs = null;
		
		for(Class<?> propClass : propertyTypes){
			if(nodePropJs.has(propClass.getSimpleName())){
				innerJs = nodePropJs
						             .get(propClass.getSimpleName())
						             .getAsJsonObject();
				try {
					nodeProp = (NodeProperty) propClass.newInstance();
				} catch (Exception e) {
					throw new JsonParseException("Undefined class: "+propClass.getName());
				}
				break;
			}
		}
		if(nodeProp == null || innerJs == null){
			throw new JsonParseException(
					"The given property was not in the list of known property types: "+innerJs);
		}
		nodeProp.setName(innerJs.get(nameLabel).getAsString());
		nodeProp.setDescription(innerJs.get(descLabel).getAsString());
		nodeProp.setDependencyLevel(innerJs.get(depLabel).getAsInt());
		
		if(nodeProp instanceof IntegerRangeProperty){
			IntegerRangeProperty irp = (IntegerRangeProperty) nodeProp;
			
			for(JsonElement rangeJs : innerJs.get(intRangesLabel).getAsJsonArray()){
				JsonObject range = rangeJs.getAsJsonObject();
				int rid = irp.addRange();
				
				irp.setRangeLabel(rid, range.get("RangeID").getAsString());
				irp.setRangeMin(rid, range.get("Min").getAsInt());
				irp.setRangeMax(rid, range.get("Max").getAsInt());
			}
		}
		else if(nodeProp instanceof BooleanProperty){
			if(nodeProp instanceof AttachmentProperty){
				((AttachmentProperty) nodeProp).setPathogen(innerJs.get(pathogenLabel).getAsString());
			}
		}
		else if(nodeProp instanceof EnumeratorProperty){
			EnumeratorProperty en = (EnumeratorProperty) nodeProp;
			
			for(JsonElement rangejs : innerJs.get(enumValsLabel).getAsJsonArray()){
				int rid = en.addRange();
				en.setRangeLabel(rid, rangejs.getAsString());
			}
		}
		else if(nodeProp instanceof FractionProperty){
			((FractionProperty) nodeProp).setInitValue(
					innerJs.get(initValLabel).getAsFloat());
		}
		
		switch(innerJs.get(distIDLabel).getAsString()){
		case "null":
			if(!(nodeProp instanceof FractionProperty)){
				throw new IllegalStateException(
						"A null distribution on an invalid node property: "+nodeProp.getName());
			}
			break;
		case "uniform":
			nodeProp.useUniformDistribution();
			break;
		}
		return nodeProp;
	}

}
