package org.snrg_nyc.persistence;

import java.io.Serializable;
import java.lang.reflect.Type;

import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.model.internal.NodeProperty;
import org.snrg_nyc.model.internal.UnivariatDistribution;
import org.snrg_nyc.util.Transferable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * A class to wrap {@link Transferable} objects into a format that
 *  can be serialized and deserialized more effectively
 * 
 * @author Devin Hastings
 */
class PersistentDataEntry implements Serializable {
	/**
	 * A list of package names to search in for the simpleName of a class
	 */
	static String[] searchPackages = {
		NodeProperty.class.getPackage().getName(),
		PropertiesEditor.class.getPackage().getName(),
		ExperimentSerializer.class.getPackage().getName()
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
			
			//Try to infer the class name, use a generic Object otherwise
			Class<?> innerClass = null;
			for(String pkgName : searchPackages){
				try {
					innerClass = Class.forName(pkgName+"."+className);
				} catch (ClassNotFoundException e) {
					//That wasn't the package, ignore
					continue;
				}
			}
			//Special case compatibility settings
			if(className.equals("UnivariatDistributionSettings")){
				innerClass = UnivariatDistribution.class;
			}
			if(innerClass == null){
				throw new JsonParseException("Unknown object type: "+className);
			}
			return new PersistentDataEntry(name, context.deserialize(objectjs, innerClass));
		}
		
	}
	
	@SerializedName("ExperimentName")
	private String name;
	
	@SerializedName("Type")
	private String type;
	
	@SerializedName("ObjectID")
	private String id;
	
	@SerializedName("Object")
	private Transferable object;
	
	public PersistentDataEntry(String experimentName, Transferable object){
		name = experimentName;
		type = object.getClass().getSimpleName();
		id = object.getObjectID();
		this.object = object;
		
		//Check if the object can be deserialized
		String packageName = object.getClass().getPackage().getName();
		boolean match = false;
		for(String s : searchPackages){
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
		return name;
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
