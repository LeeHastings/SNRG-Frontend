package org.snrg_nyc.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

class PropertyTypeFactory implements 
	Callback<TableColumn.CellDataFeatures<PropertyID, String>, ObservableValue<String>> {
	
	private EditorPage editor;
	
	public PropertyTypeFactory(EditorPage e){
		editor = e;
	}
	
	@Override
	public ObservableValue<String> call(CellDataFeatures<PropertyID, String> cellData) {
		PropertyID id = cellData.getValue();
		try{
			String type;
			if(!id.usesLayer()){
				type = editor.ui.nodeProp_getType(id.pid());
			}
			else {
				type = editor.ui.nodeProp_getType(id.lid(), id.pid());
			}
			
			if(type.contains("Property")){
				type = type.substring(0, type.indexOf("Property"));
			}
			return new SimpleStringProperty(type);
		}
		catch(Exception e){
			editor.sendError(e);
			return new SimpleStringProperty(">ERROR<");
		}
	}
}
