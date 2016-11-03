package org.snrg_nyc.model.properties;

import java.lang.reflect.Type;

import org.snrg_nyc.model.EditorException;

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
public class PropertyJsonAdapter
		implements JsonSerializer<NodeProperty>, JsonDeserializer<NodeProperty>
{
	//Passing strings as parameters multiple times is too error-prone for me
	final static String nameLabel = "PropertyName";
	final static String descLabel = "Description";
	//Two labels for backwards compatibility
	final static String depLabel = "DependencyLevel";
	final static String depLabel2 = "Dependency Level"; 
	final static String intRangesLabel = "IntegerRangeList";
	final static String enumValsLabel = "EnumValues";
	final static String initValLabel = "DisableRandom_UseInitValue";
	final static String distIDLabel = "DistributionID";
	final static String pathogenLabel = "PathogenType";
	
	private Class<?>[] propertyTypes;
	
	/**
	 * Create a {@link PropertyJsonAdapter} with the available property classes
	 * @param propTypes An array of classes that the property could be
	 * (they must all be subclasses of {@link NodeProperty}
	 */
	public PropertyJsonAdapter(Class<?>[] propTypes){
		propertyTypes = propTypes;
	}
	
	@Override
	public JsonElement 
	serialize(NodeProperty nodeProp, 
	          Type type, 
	          JsonSerializationContext context) 
	{
		JsonObject propertyJs = new JsonObject();
		JsonObject innerJs = new JsonObject();
		propertyJs.add(nodeProp.getClass().getSimpleName(), innerJs);
		
		try {
			innerJs.addProperty(nameLabel, nodeProp.getName());
		} catch (EditorException e1) {
			throw new JsonParseException("This property has no name!");
		}
		innerJs.addProperty(descLabel, nodeProp.getDescription());
		innerJs.addProperty(depLabel, nodeProp.getDependencyLevel());
		
		if(nodeProp instanceof IntegerRangeProperty){
			IntegerRangeProperty irp = (IntegerRangeProperty) nodeProp;
			
			JsonArray ranges = new JsonArray();
			innerJs.add(intRangesLabel, ranges);
			
			for(int i : irp.getSortedRangeIDs()){
				JsonObject rangeJs = new JsonObject();
				
				try {
					rangeJs.addProperty("RangeID", irp.getRangeLabel(i));
					rangeJs.addProperty("Min", irp.getRangeMin(i));
					rangeJs.addProperty("Max", irp.getRangeMax(i));
				} catch (EditorException e) {
					e.printStackTrace();
					throw new JsonParseException("Error while writing ranges");
				}
				
				ranges.add(rangeJs);
			}
		}
		else if(nodeProp instanceof BooleanProperty){
			if(nodeProp instanceof AttachmentProperty){
				innerJs.addProperty(pathogenLabel, 
						((AttachmentProperty)nodeProp).getPathogenName() );
			}
			else if(nodeProp instanceof ValueProperty) {
				innerJs.addProperty(
						initValLabel, 
						((BooleanProperty) nodeProp).getInitValue());
			}
		}
		else if(nodeProp instanceof EnumeratorProperty){
			EnumeratorProperty en = (EnumeratorProperty) nodeProp;
			
			JsonArray values = new JsonArray();
			innerJs.add(enumValsLabel, values);
			
			for(int i : en.getSortedRangeIDs()){
				try {
					values.add( new JsonPrimitive(en.getRangeLabel(i)) );
				} catch (EditorException e) {
					e.printStackTrace();
					throw new JsonParseException("Failed to write range");
				}
			}
		}
		else if(nodeProp instanceof FractionProperty){
			innerJs.addProperty(
					initValLabel, 
					((FractionProperty) nodeProp).getInitValue());
		}
		if(nodeProp.getDistributionID().equals("null")){
			innerJs.addProperty(distIDLabel, (String) null);
		}
		else {
			innerJs.addProperty(distIDLabel, nodeProp.getDistributionID());
		}
		return propertyJs;
	}

	@Override
	public NodeProperty 
	deserialize(JsonElement js, Type type, JsonDeserializationContext context)
			throws JsonParseException
	{
		JsonObject nodePropJs = js.getAsJsonObject();
		NodeProperty nodeProp = null;
		JsonObject innerJs = null;
		
		for(Class<?> propClass : propertyTypes){
			if(nodePropJs.has(propClass.getSimpleName())){
				innerJs = getJson(
						nodePropJs,propClass.getSimpleName())
						.getAsJsonObject();
				try {
					nodeProp = (NodeProperty) propClass.newInstance();
				} 
				catch (IllegalAccessException | InstantiationException e) {
					e.printStackTrace();
					throw new JsonParseException("Error while making class: "
							+e.toString());
				}
				break;
			}
		}
		if(nodeProp == null || innerJs == null){
			throw new JsonParseException(
					"The given property was not in the list of known"
					+ " property types: "+nodePropJs);
		}
		try {
			nodeProp.setName(getJson(innerJs,nameLabel).getAsString());
			nodeProp.setDescription(getJson(innerJs,descLabel).getAsString());
			if(innerJs.has(depLabel)){
				nodeProp.setDependencyLevel(innerJs.get(depLabel).getAsInt());
			}
			else {
				nodeProp.setDependencyLevel(
						getJson(innerJs,depLabel2).getAsInt());
			}
		} 
		catch (EditorException e) {
			e.printStackTrace();
			throw new JsonParseException("Failed to set property attributes");
		}
		
		if(nodeProp instanceof IntegerRangeProperty){
			IntegerRangeProperty irp = (IntegerRangeProperty) nodeProp;
			
			for(JsonElement rangeJs : 
				getJson(innerJs,intRangesLabel).getAsJsonArray())
			{
				JsonObject range = rangeJs.getAsJsonObject();
				int rid;
				try {
					rid = irp.addRange();
					
					irp.setRangeLabel(rid, 
							getJson(range,"RangeID").getAsString());
					irp.setRangeMin(rid, getJson(range,"Min").getAsInt());
					irp.setRangeMax(rid, getJson(range, "Max").getAsInt());
				} 
				catch (EditorException e) {
					e.printStackTrace();
					throw new JsonParseException("Error while reading ranges");
				}
			}
		}
		else if(nodeProp instanceof BooleanProperty){
			if(nodeProp instanceof AttachmentProperty){
				((AttachmentProperty) nodeProp).setPathogenName(
						getJson(innerJs, pathogenLabel).getAsString());
			}
			else if(nodeProp instanceof ValueProperty){
				((BooleanProperty) nodeProp).setInitValue(
						getJson(innerJs,initValLabel).getAsBoolean());
			}
		}
		else if(nodeProp instanceof EnumeratorProperty){
			EnumeratorProperty en = (EnumeratorProperty) nodeProp;
			
			for(JsonElement rangejs : 
				getJson(innerJs,enumValsLabel).getAsJsonArray())
			{
				int rid;
				try {
					rid = en.addRange();
					en.setRangeLabel(rid, rangejs.getAsString());
				} 
				catch (EditorException e) {
					e.printStackTrace();
					throw new JsonParseException("Error while reading ranges");
				}
			}
		}
		else if(nodeProp instanceof FractionProperty){
			((FractionProperty) nodeProp).setInitValue(
					getJson(innerJs,initValLabel).getAsFloat());
		}
		//If the distribution was null, the distIDLabel is not included
		if(innerJs.has(distIDLabel) 
		   && !innerJs.get(distIDLabel).isJsonNull())
		{ 
			String s = innerJs.get(distIDLabel).getAsString();
			if(s.equals("uniform")){
				nodeProp.useUniformDistribution();
			}
		}
		else if(!(nodeProp instanceof ValueProperty)){
			throw new JsonParseException(
					"The given property is missing a distribution: "
					+nodePropJs);
		}
		return nodeProp;
	}
	
	/**
	 * The same as {@link JsonObject#get(String)}, but it throws an exception
	 * if the tag is missing instead of returning null
	 * @param json The JsonObject to get data from
	 * @param label The key for the data in the JsonObject
	 * @return The data, if it exists
	 * @throws JsonParseException Thrown if the key does not exist.
	 */
	private JsonElement 
	getJson(JsonObject json, String label) throws JsonParseException {
		if(json.has(label)){
			return json.get(label);
		}
		else {
			throw new JsonParseException("No element with label '"
					+label+"': "+json);
		}
	}

}
