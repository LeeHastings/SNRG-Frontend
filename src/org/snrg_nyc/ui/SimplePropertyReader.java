package org.snrg_nyc.ui;

import org.snrg_nyc.model.UIException;
import org.snrg_nyc.model.UI_Interface;

import javafx.beans.property.SimpleStringProperty;

public class SimplePropertyReader {
	UI_Interface ui;
	int propID;
	
	public SimplePropertyReader(UI_Interface ui, int pid){
		if(!ui.test_nodePropIDExists(pid)){
			throw new IllegalArgumentException("Invalid property ID: "+pid);
		}
		this.ui = ui;
		this.propID = pid;
	}
	
	public String getName() throws UIException{
		return ui.nodeProp_getName(propID);
	}
	public String getType() throws UIException{
		return ui.nodeProp_getType(propID);
	}
	public String getDescription() throws UIException{
		return ui.nodeProp_getDescription(propID);
	}
	public int getDependencyLevel() throws UIException{
		return ui.nodeProp_getDependencyLevel(propID);
	}
	public int getID(){
		return propID;
	}
	public String getSimpleType(){
		try {
			String type = getType();
			if(type.contains("Property")){
				type = type.substring(0, type.indexOf("Property"));
			}
			return type;
		}
		catch(Exception e){
			return ">ERROR<";
		}
		

	}
}
