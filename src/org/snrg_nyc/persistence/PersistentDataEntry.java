package org.snrg_nyc.persistence;

import java.io.Serializable;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * A class to wrap {@link Transferable} objects into a format that
 * can be serialized and deserialized more effectively
 * 
 * @author Devin Hastings
 */
class PersistentDataEntry implements Serializable {
	
	static final long serialVersionUID = 1L;
	
	static class JsonAdapter implements JsonDeserializer<PersistentDataEntry>{
		@Override
		public PersistentDataEntry 
		deserialize(
				JsonElement js, 
				Type type, 
				JsonDeserializationContext context)
				throws JsonParseException 
		{
			
			JsonObject jsWrapper = js.getAsJsonObject();
			String name = jsWrapper.get("ExperimentName").getAsString();
			String className = jsWrapper.get("Type").getAsString();
			JsonObject objectjs = jsWrapper.get("Object").getAsJsonObject();
			
			try {
				Class<?> innerClass = Transferable.searchClasses(className);
				return new PersistentDataEntry(
						name, context.deserialize(objectjs, innerClass));
			} 
			catch (PersistenceException e) {
				throw new JsonNoClassException(e.getMessage());
			}
		}
		
	}
	
	@SerializedName("ExperimentName")
	private String expName;
	
	@SerializedName("Type")
	private String type;
	
	@SerializedName("ObjectID")
	private String id;
	
	@SerializedName("Object")
	private Transferable object;
	
	public PersistentDataEntry(String experimentName, Transferable object){
		expName = experimentName;
		type = object.getClass().getSimpleName();
		id = object.getObjectID();
		this.object = object;
		
		//Check if the object can be deserialized
		String packageName = object.getClass().getPackage().getName();
		boolean match = false;
		for(String s : Transferable.searchPackages){
			if(s.equals(packageName)){
				match = true;
			}
		}
		if(!match){
			System.err.println(
					"Warning: an object was added to a "
					+ "persistent data entry that cannot be deserialized: "
					+ object.getClass().getName());
		}
	}
	
	String getExperimentName() {
		return expName;
	}

	String getType() {
		return type;
	}
	
	String getID(){
		return id;
	}

	public Transferable getObject() {
		return object;
	}
}
