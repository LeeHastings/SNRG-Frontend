package org.snrg_nyc.persistence;

import java.io.Serializable;
import java.lang.reflect.Type;

import org.snrg_nyc.model.UI_Interface;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * A class to wrap Serializable objects 
 * into a format that can be encoded and decoded into the required Json format easily
 * 
 * @author Devin
 */
class PersistentDataEntry implements Serializable {
	/**
	 * A list of package names to search in for the simpleName of a class
	 */
	static String[] searchPackages = {
		UI_Interface.class.getPackage().getName(),
		ExperimentSerializer.class.getPackage().getName(),
		"java.lang",
		"java.util",
		"java.io"
	};
	
	static final long serialVersionUID = 1L;
	
	static class JsonAdapter implements JsonDeserializer<PersistentDataEntry>{
		@Override
		public PersistentDataEntry deserialize(JsonElement js, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			
			JsonObject jsWrapper = js.getAsJsonObject();
			String name = jsWrapper.get("ExperimentName").getAsString();
			String className = jsWrapper.get("Type").getAsString();
			JsonObject objectjs = jsWrapper.get("Object").getAsJsonObject();
			
			//Try to infer the type name 
			Class<?> innerClass = Object.class;
			for(String pkgName : searchPackages){
				try {
					innerClass = Class.forName(pkgName+"."+className);
				} catch (ClassNotFoundException e) {
					//That wasn't the package
				}
			}
			return new PersistentDataEntry(name, context.deserialize(objectjs, innerClass));
		}
		
	}
	
	@SerializedName("ExperimentName")
	private String name;
	
	@SerializedName("Type")
	private String type;
	
	@SerializedName("Object")
	private Serializable object;
	
	public PersistentDataEntry(String experimentName, Serializable object){
		name = experimentName;
		type = object.getClass().getSimpleName();
		this.object = object;
	}
	
	String getExperimentName() {
		return name;
	}

	String getType() {
		return type;
	}

	public Serializable getObject() {
		return object;
	}
}
