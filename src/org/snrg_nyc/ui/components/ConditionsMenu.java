package org.snrg_nyc.ui.components;

import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.ui.EditorPage;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;

public class ConditionsMenu extends GridPane{
	
	private int row;
	private BooleanProperty readyProperty;
	private EditorPage editor;
	Map<Integer, Integer> conditions;
	
	public ConditionsMenu(PropertiesEditor ui, EditorPage editor) throws EditorException{
		this.editor = editor;
		conditions = new HashMap<>();
		row=0;
		readyProperty = new SimpleBooleanProperty();
		setVgap(10);
		setHgap(5);
		
		for(int pid : ui.scratch_getDependencies()){
			CheckBox check = new CheckBox("Use "+ui.nodeProp_getName(pid));
			check.setId(pid+"_check");
			
			ComboBox<Integer> valueBox = new ComboBox<>();
			
			valueBox.setId(pid+"_value");
			
			add(check,    0, row);
			add(valueBox, 1, row);
			row ++;
			
			valueBox.disableProperty().bind(check.selectedProperty().not());
			
			valueBox.getItems().addAll(ui.nodeProp_getRangeItemIDs(pid));
			
			valueBox.setOnAction(e ->{ 
				conditions.put(pid, valueBox.getValue()); 
				updateReady();
			});
			
			valueBox.setCellFactory(lv -> {
				return new ListCell<Integer>(){
					@Override
					public void updateItem(Integer item, boolean empty){
						super.updateItem(item, empty);
						if(item == null || empty){
							setText("<empty>");
						}
						else {
							try {
								setText(ui.nodeProp_getRangeLabel(pid, item));
							} catch (EditorException e) {
								editor.sendError(e);
								setText(">ERROR<");
							}
						}
					}
				};
			});
			
			valueBox.setButtonCell( new ListCell<Integer>(){
				@Override
				public void updateItem(Integer item, boolean empty){
					super.updateItem(item, empty);
					if(item == null || empty){
						setText("<empty>");
					}
					else {
						try{
							setText(ui.nodeProp_getRangeLabel(pid, item));
						}
						catch(Exception e){
							editor.sendError(e);
							setText(">ERROR<");
						}
					}
				}
			});
			valueBox.valueProperty().addListener((o, old, newVal)->{
				conditions.put(pid, newVal);
				updateReady();
			});
			
			check.selectedProperty().addListener((o, old, selected)->{
				if(!selected){
					conditions.remove(pid);
				}
				else {
					conditions.put(pid, valueBox.getValue());
				}
				updateReady();
			});
		}
	}
	private void updateReady(){
		boolean ready = !conditions.isEmpty();
		if(ready){
			for(Integer i : conditions.values()){
				ready = ready && i!= null;
			}
		}
		readyProperty.set(ready);
	}
	public BooleanProperty readyProperty(){
		return readyProperty;
	}
	public int getRows(){
		return row+1;
	}
	public Map<Integer, Integer> getConditions(){
		return conditions;
	}
	public void setCondiditions(Map<Integer, Integer> newConds) throws EditorException{
		conditions.clear();
		
		for(int pid : newConds.keySet()){
			Node checkN = editor.lookup("#"+pid+"_check");
			Node valN= editor.lookup("#"+pid+"_value");
			
			if(checkN == null || valN == null){
				throw new IllegalArgumentException("Found an unknown property ID: "+pid);
			}
			if(!(checkN instanceof CheckBox) || !(valN instanceof ComboBox)){
				throw new IllegalArgumentException("Found menu items are of unknown types: "
						+checkN.getClass().getName()+", "+valN.getClass().getName());
			}
			CheckBox c = (CheckBox) checkN;
			@SuppressWarnings("unchecked")
			ComboBox<Integer> v = (ComboBox<Integer>) valN;
			
			c.setSelected(true);
			v.setValue(newConds.get(pid));
		}
		updateReady();
	}
}
