package org.snrg_nyc.ui;


import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.model.UIException;
import org.snrg_nyc.model.UI_Interface;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;

public class ConditionsMenu extends GridPane{
	
	private int row;
	private BooleanProperty readyProperty;
	Map<Integer, Integer> conditions;
	
	public ConditionsMenu(UI_Interface ui, EditorPage editor) throws UIException{
		conditions = new HashMap<>();
		row=0;
		readyProperty = new SimpleBooleanProperty();
		setVgap(10);
		setHgap(5);
		BooleanBinding conditionsReady = 
				new SimpleBooleanProperty(false).or(new SimpleBooleanProperty(false));	
		
		for(int pid : ui.scratch_getDependencies()){
			CheckBox check = new CheckBox("Use "+ui.nodeProp_getName(pid));
			ComboBox<Integer> valueBox = new ComboBox<>();
			valueBox.setDisable(true);
			
			add(check,    0, row);
			add(valueBox, 1, row);
			row ++;
			
			BooleanProperty setCondition = new SimpleBooleanProperty(false);
			
			conditionsReady = conditionsReady
			                 .or(valueBox.disableProperty().not()
			                 .and(setCondition));

			valueBox.getItems().addAll(ui.nodeProp_getRangeItemIDs(pid));
			
			valueBox.setOnAction(e -> 
				conditions.put(pid, valueBox.getValue()) );
			
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
							} catch (UIException e) {
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
			valueBox.setOnAction(e ->{
				if(valueBox.getValue() != null){
					setCondition.set(true);
					conditions.put(pid, valueBox.getValue());
				}
			});
			
			check.setOnMouseClicked(e ->{
				valueBox.setDisable(!check.isSelected());
				if(!check.isSelected()){
					conditions.remove(pid);
				}
			});
		}
		readyProperty.bind(conditionsReady);
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
}
