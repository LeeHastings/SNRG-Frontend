package org.snrg_nyc.ui.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class EditorTableCell<T> extends TableCell<T, String> {
	
	public static class EditEvent<T>{
		private String newText;
		private String oldText;
		private EditorTableCell<T> cell;
		
		public String newText(){
			return newText;
		}
		public String oldText(){
			return oldText;
		}
		public EditorTableCell<T> cell(){
			return cell;
		}
	}
	public interface EditListener<T>{
		public void commitEdit(EditEvent<T> event);
	}
	
	private TextField textField;
	private EditListener<T> editListener = null;
	private BooleanProperty tabbedProperty = new SimpleBooleanProperty(false);

	public EditorTableCell(){
		super();
		
		textField = new TextField(getItem());
		textField.setMinWidth(this.getWidth()-this.getGraphicTextGap()*2);
		textField.focusedProperty().addListener((o2, oldval2, newval2)->{
			if(!newval2){
				commitEdit(textField.getText()); //Cancel if the text box has lost focus
			}
		});
		
		this.setOnKeyPressed(event->{
			if(event.getCode() == KeyCode.ESCAPE){
				cancelEdit();
			}
			else if(event.getCode() == KeyCode.ENTER){
				if(textField != null){
					commitEdit(textField.getText());
				}
			}
			else if(event.getCode() == KeyCode.TAB){
				tabbedProperty.set(true);
			}
			tabbedProperty.set(false);//Resets immediately
			event.consume();
		});
	}
	public void setOnEditCommit(EditListener<T> listener){
		this.editListener = listener;
	}
	
	@Override
	protected void updateItem(String item, boolean empty){
		super.updateItem(item, empty);
		if(empty){
			setText(null);
			setGraphic(null);
		}
		else if(isEditing()) {
            if (textField != null) {
                textField.setText(getItem());
            }
            setText(null);
            setGraphic(textField);
        } 
		else {
            setText(getItem());
            setGraphic(null);
        }
	}
	public void update(){
		updateItem(getItem(), isEmpty());
	}
	@Override
	public void startEdit(){
		if(!isEmpty()){
			super.startEdit();
			setText(null);
			setGraphic(textField);
			textField.requestFocus();
		}
	}
	/*
	 * super.commitEdit(String) breaks if you click on another row to commit the edit.
	 * So this has to do it itself
	 */
	@Override
	public void commitEdit(String t){
		EditEvent<T> event = new EditEvent<>();
		event.oldText = getItem();
		event.newText = t;
		event.cell = this;
		
		setText(t);
		setGraphic(null);
		if(editListener != null){
			editListener.commitEdit(event);
		}
		cancelEdit();
	}
}
