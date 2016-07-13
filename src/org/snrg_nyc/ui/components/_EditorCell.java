package org.snrg_nyc.ui.components;

import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;

public class _EditorCell<T> extends TextFieldListCell<T> {
	boolean escPressed = false;
	
	public _EditorCell(StringConverter<T> converter){
		setConverter(converter);
		this.setOnKeyPressed(keypress->{
			if(keypress.getCode() == KeyCode.ESCAPE){
				escPressed = true;
			}
			else {
				escPressed = false;
			}
			keypress.consume();
		});
	}
	@Override
	public void cancelEdit(){
		if(escPressed){
			super.cancelEdit();
		}
		else {
			String t = ((TextField) getGraphic()).getText();
			if(getConverter() == null){
				throw new IllegalStateException("The StringConverter cannot be null!");
			}
			else {
				setGraphic(null);
				commitEdit(getConverter().fromString(t));
			}
		}
	}
	@Override
	public void updateItem(T item, boolean empty){
		super.updateItem(item, empty);
		if(empty || getItem() == null){
			return;
		}
		else {
			String text = getString();
			if(text == null || text.equals("")){
				setText("<empty>");
			}
			else {
				setText(text);
			}
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
