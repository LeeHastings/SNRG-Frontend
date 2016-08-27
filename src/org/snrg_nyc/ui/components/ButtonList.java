package org.snrg_nyc.ui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ButtonList<T> extends GridPane {
	public static interface LabelFactory<T>{
		public String toString(T item);
	}
	public static interface ClickListener<T>{
		public void onClick(T item);
	}
	private class ButtonItem extends HBox{
		Button b;
		T item;
		ButtonItem(T item){
			b = new Button(labelFactory.toString(item));
			HBox.setHgrow(b, Priority.ALWAYS);
			b.setMaxWidth(Double.MAX_VALUE);
			this.item = item;
			getChildren().add(b);
			b.setOnMouseClicked(event-> onClick(item));
			GridPane.setFillWidth(this, true);
		}
		void changeLabel(LabelFactory<T> factory){
			b.setText(factory.toString(item));
		}
	}
	
	private ObservableList<T> itemsProperty = FXCollections.observableArrayList();
	private List<ClickListener<T>> listeners = new ArrayList<>();
	private List<ButtonItem> buttons = new ArrayList<>();
	//Default label factory
	private LabelFactory<T> labelFactory = T::toString;
	
	private int row = 0;
	
	public ButtonList(){
		super();
		setVgap(10);
		setHgap(10);
		ColumnConstraints col = new ColumnConstraints();
		col.setHgrow(Priority.ALWAYS);
		getColumnConstraints().add(col);
		
		itemsProperty.addListener((Change<? extends T> change)-> {
			while(change.next()){
				if(change.wasRemoved()){
					buttons.clear();
					row =0;
					for(T item : itemsProperty){
						ButtonItem b = new ButtonItem(item);
						add(b, 0, row);
						buttons.add(b);
						row++;
					}
				}
				else{
					for(T item : change.getAddedSubList()){
						ButtonItem b = new ButtonItem(item);
						add(b, 0, row);
						buttons.add(b);
						row++;
					}
				}
			}
		});
	}
	public ButtonList(Collection<T> items){
		this();
		itemsProperty.setAll(items);
	}
	public void 
	addClickListener(ClickListener<T> listener){
		if(listener == null){
			throw new IllegalArgumentException("Listeners cannot be null!");
		}
		listeners.add(listener);
	}
	
	public void 
	setLabelFactory(LabelFactory<T> factory){
		if(factory == null){
			throw new IllegalArgumentException(
					"The Label Factory cannot be null!");
		}
		labelFactory = factory;
		for(ButtonItem b : buttons){
			b.changeLabel(factory);
		}
	}
	private void onClick(T item){
		for(ClickListener<T> l : listeners){
			l.onClick(item);
		}
	}
	
	public ObservableList<T> 
	getItems(){
		return itemsProperty;
	}
}
