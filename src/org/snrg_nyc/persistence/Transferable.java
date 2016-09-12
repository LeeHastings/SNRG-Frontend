package org.snrg_nyc.persistence;

import java.io.Serializable;

import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.model.internal.NodeProperty;
import org.snrg_nyc.model.internal.UnivariatDistributionSettings;

/**
 * An interface that describes all objects that can be serialized and loaded
 * by an {@link org.snrg_nyc.persistence.ExperimentSerializer}
 * @author Devin Hastings
 */
public abstract class Transferable implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * An ID that describes the object, as specified in the file format
	 * @return A descriptive ID for the object.
	 */
	public abstract String getObjectID();
	
	/**
	 * The type of the object
	 * @return The named type of the object.  The default is the simple name
	 * of the object's class
	 */
	public String
	getType(){
		return getClass().getSimpleName();
	}
	/**
	 * A list of package names to search in for the simpleName of a class
	 */
	static String[] searchPackages = {
		NodeProperty.class.getPackage().getName(),
		PropertiesEditor.class.getPackage().getName(),
		ExperimentSerializer.class.getPackage().getName()
	};
	
	public static Class<?> 
	searchClasses(String type) throws PersistenceException{
		Class<?> innerClass = null;

		if(type.equals("UnivariatDistribution")) {
			innerClass = UnivariatDistributionSettings.class;
		}
		else {
			for(String pkgName : searchPackages){
				try {
					innerClass = Class.forName(pkgName+"."+type);
				} 
				catch (ClassNotFoundException e) {
					//That wasn't the package, ignore
					continue;
				}
			}
		}
		if(innerClass == null){
			throw new PersistenceException(
					"Could not find a class for the type "+type);
		}
		return innerClass;
	}

}
