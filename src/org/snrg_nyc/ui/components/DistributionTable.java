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
		
		probCol.setCellFactory(col->{
			EditorTableCell<Integer> cell = new EditorTableCell<>();
			cell.setOnEditCommit(event->{
				if(event.newText() == null){
					return;
				}
				try{
					float f = Float.parseFloat(event.newText());
					int id = event.cell().getTableView()
						      .getItems().get(event.cell().getIndex());
					probMap.put(id, f);
				}
				catch(NumberFormatException e){
					editor.sendError(e);
				}
				finally{
					setReady();
				}
			});
			return cell;
		});
		
		probCol.setCellValueFactory(col->{
			if(col.getValue() != null){
				Float f = probMap.get(col.getValue());
				return new SimpleStringProperty( f==null? null : f.toString() );
			}
			else {
				return null;
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
