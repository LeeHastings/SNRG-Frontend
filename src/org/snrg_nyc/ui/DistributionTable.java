package org.snrg_nyc.ui;

import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.model.UIException;
import org.snrg_nyc.model.UI_Interface;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

class DistributionTable extends TableView<Integer> {
	private Map<Integer, Float> probMap;
	private BooleanProperty readyProperty = new SimpleBooleanProperty();
	
	public DistributionTable(UI_Interface ui, EditorPage editor) throws UIException{
		super();
		readyProperty.set(false);

		this.probMap = new HashMap<>();
		
		TableColumn<Integer, String> nameCol = new TableColumn<>("Range");
		TableColumn<Integer, String> probCol = new TableColumn<>("Probability");
		
		getColumns().addAll(nameCol, probCol);
		
		setEditable(true);
		probCol.setEditable(true);
		for(int i : ui.scratch_getRangeIDs()){
			probMap.put(i, null);
			this.getItems().add(i);
		}
		
		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		nameCol.setCellValueFactory(col ->{
			try {
				return new SimpleStringProperty(ui.scratch_getRangeLabel(col.getValue()));
			} catch (Exception e) {
				return new SimpleStringProperty(">ERROR<");
			}
		});
		probCol.setCellFactory(TextFieldTableCell.forTableColumn());
		probCol.setCellValueFactory(col->{
			if(col.getValue() != null){
				Float f = probMap.get(col.getValue());
				return new SimpleStringProperty( f==null? null : f.toString() );
			}
			else {
				return null;
			}
		});
		probCol.setOnEditCommit(event ->{
			try{
				if(!event.getNewValue().matches("[0-9]*\\.?[0-9]+")){
					throw new NumberFormatException("Value '"
							+event.getNewValue()+"' is not a floating-point number");
				}
				probMap.put( event.getRowValue(), Float.parseFloat(event.getNewValue()) );
			}
			catch (Exception e){
				editor.sendError(e);
			}
			finally{
				for(Float f : probMap.values()){
					if(f == null){
						readyProperty.set(false);
						return;
					}
				}
				readyProperty.set(true);
			}
		});

		
		
	}
	public Map<Integer, Float> getProbMap(){
		return probMap;
	}
	public BooleanProperty readyProperty(){
		return readyProperty;
	}
	public boolean isReady(){
		return readyProperty.get();
	}
}
