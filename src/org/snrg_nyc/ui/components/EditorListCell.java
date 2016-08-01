package org.snrg_nyc.ui.components;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;

public class EditorListCell<T> extends TextFieldListCell<T> {
	boolean escPressed = false;
	private Node graphic;
	
	public EditorListCell(StringConverter<T> converter){
		super(converter);
		this.setOnKeyPressed(keypress->{
			if(keypress.getCode() == KeyCode.ESCAPE){
				escPressed = true;
			}
			else {
				escPressed = false;
			}
			keypress.consume();
		});

		this.graphicProperty().addListener((o, oldval, newval)->{
			//Add a listener to the textfield if it hasn't been added already
			if(newval != null && (graphic == null || graphic != newval)){
				graphic = newval;
				graphic.focusedProperty().addListener((o2, oldval2, newval2)->{
					if(!newval2){
						cancelEdit(); //Cancel if the text box has lost focus
					}
				});
			}
		});
	}
	@Override
	public void cancelEdit(){
		if(escPressed){
			super.cancelEdit();
			escPressed = false;
		}
		else if(getGraphic() != null){
			String t = ((TextField) getGraphic()).getText();
			
			if(t == null || t.length() == 0){
				super.cancelEdit();
			}
			else if(getConverter() == null){
				throw new IllegalStateException("The StringConverter cannot be null!");
			}
			else {
				commitEdit(getConverter().fromString(t));
			}
		}
	}
	@Override
	public void updateItem(T item, boolean empty){
		super.updateItem(item, empty);
		if(empty){
			return;
		}
		else {
			setText(getString());
		}
	}
	private String getString(){
		if(getConverter() == null){
			return getItem().toString();
		}
		else{
			return getConverter().toString(getItem());
		}
	}
}
