package org.snrg_nyc.ui.components;

import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class EditorListCell<T> extends ListCell<T>{
	StringConverter<T> converter = null;
	private TextField text;
	private boolean escPressed;
	
	public EditorListCell(StringConverter<T> converter){
		this.converter = converter;
	}
	public void setConverter(StringConverter<T> stringConverter) {
		converter = stringConverter;
	}
	@Override 
	public void startEdit(){
		super.startEdit();
		if(text == null){
			text = new TextField();
			text.setMinWidth(this.getWidth() - this.getGraphicTextGap()*2);
		}
		text.setText(this.getText());
		this.setGraphic(text);
		text.requestFocus();
	}
	@Override
	public void commitEdit(T newVal){
		super.commitEdit(newVal);
		if(converter == null){
			setText(getItem().toString());
		}
		else {
			setText(converter.toString(getItem()));
		}
		this.setGraphic(null);
	}
	
}