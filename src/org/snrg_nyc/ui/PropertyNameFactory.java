package org.snrg_nyc.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

class PropertyNameFactory implements 
	Callback<TableColumn.CellDataFeatures<PropertyID, String>, ObservableValue<String>> {
	
	private EditorPage editor;
	
	public PropertyNameFactory(EditorPage e){
		editor = e;
	}
	
	@Override
	public ObservableValue<String> call(CellDataFeatures<PropertyID, String> cellData) {
		PropertyID id = cellData.getValue();
		try{
			String name;
			if(!id.usesLayer()){
				name = editor.ui.nodeProp_getName(id.pid());
			}
			else {
				name = editor.ui.nodeProp_getName(id.lid(),id.pid());
			}
			return new SimpleStringProperty(name);
		}
		catch(Exception e){
			editor.sendError(e);
			return new SimpleStringProperty(">ERROR<");
		}
	}
}
