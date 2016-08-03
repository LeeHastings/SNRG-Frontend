package org.snrg_nyc.ui.components;

import org.snrg_nyc.util.PropertyID;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

/**
 * Get the name of a property from a {@link PropertyID}, 
 * automatically sending messages to the editor.
 * @author Devin Hastings
 *
 */
public class PropertyNameFactory implements 
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
				name = editor.getModel().nodeProp_getName(id.pid());
			}
			else {
				name = editor.getModel().nodeProp_getName(id.lid(),id.pid());
			}
			return new SimpleStringProperty(name);
		}
		catch(Exception e){
			editor.sendError(e);
			return new SimpleStringProperty(">ERROR<");
		}
	}
}
