package org.snrg_nyc.ui.components;

import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.ui.EditorPage;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

public class DistributionTable extends TableView<Integer> {
	private Map<Integer, Float> probMap;
	private BooleanProperty readyProperty = new SimpleBooleanProperty();
	
	@SuppressWarnings("unchecked")
	public DistributionTable(EditorPage editor) throws EditorException{
		super();
		this.probMap = new HashMap<>();
		
		TableColumn<Integer, String> nameCol = new TableColumn<>("Range");
		TableColumn<Integer, String> probCol = new TableColumn<>("Probability");
		
		getColumns().addAll(nameCol, probCol);
		
		setEditable(true);
		probCol.setEditable(true);
		for(int i : editor.getModel().scratch_getRangeIDs()){
			probMap.put(i, null);
			this.getItems().add(i);
		}
		
		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		nameCol.setCellValueFactory(col ->{
			try {
				return new SimpleStringProperty(editor.getModel().scratch_getRangeLabel(col.getValue()));
			} catch (Exception e) {
				editor.sendError(e);
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
				setReady();
			}
		});
	}
	
	public Map<Integer, Float> getProbMap(){
		return probMap;
	}
	public void setPropMap(Map<Integer, Float> newMap){
		if(newMap.size() != probMap.size()){
			throw new IllegalArgumentException("The new probMap is of a different size!");
		}
		for(int rid : newMap.keySet()){
			if(!probMap.containsKey(rid)){
				throw new IllegalArgumentException("Unknown range ID: "+rid);
			}
			probMap.put(rid, newMap.get(rid));
		}
		setReady();
	}
	
	public BooleanProperty readyProperty(){
		return readyProperty;
	}
	
	public boolean isReady(){
		return readyProperty.get();
	}
	
	private void setReady(){
		for(Float f : probMap.values()){
			if(f == null){
				readyProperty.set(false);
				return;
			}
		}
		readyProperty.set(true);
	}
}
