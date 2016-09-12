package org.snrg_nyc.ui.components;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

/**
 * A custom table cell for editing text.
 * <i><b>Note: this cell has non-standard behavior.</b></i>  The only way
 * to execute code upon editing the cell is through 
 * {@link EditorTableCell#setOnEditCommit(EditListener)}, due to incompatible
 * behavior when using the standard listener system.
 * @author Devin Hastings
 *
 * @param <T> The type of object in the table
 */
public class EditorTableCell<T> 
extends TableCell<T, String> implements Editable<T> {
	
	/**
	 * A custom event system, because the default breaks when clicking
	 * on a cell in a different row to commit the changes.
	 * @author Devin Hastings
	 *
	 * @param <T> The type of object in the table
	 */
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
	/**
	 * The event listener called when an edit is commited.  This works
	 * differently from the usual system, because this is done on a 
	 * cell-by-cell basis (which admittedly may not be the most efficient),
	 * but the usual system simply does not work if the row that was edited
	 * is no longer in focus (which was the desired behavior)
	 * @author Devin Hastings
	 *
	 * @param <T> Type of object in the table
	 */
	public interface EditListener<T>{
		public void commitEdit(EditEvent<T> event);
	}
	
	private TextField textField;
	private EditListener<T> editListener = null;
	private boolean escPressed = false;
	private boolean entPressed = false;
	private Callback<T, String> textFactory;

	public EditorTableCell(){
		super();
		textFactory = item-> getTableColumn()
				            .getCellObservableValue(getIndex())
				            .getValue();
		
		textField = new TextField(getItem());
		textField.setMinWidth(this.getWidth()-this.getGraphicTextGap()*2);
		textField.focusedProperty().addListener((o2, oldval2, focused)->{
			if(!focused){
				if(escPressed){
					escPressed = false;
				}
				else if(entPressed){
					entPressed = false;
				}
				else {
					commitEdit(textField.getText());
				}
			}
		});
		
		textField.setOnKeyPressed(event->{
			if(event.getCode() == KeyCode.ESCAPE){
				escPressed = true;
				cancelEdit();
			}
			else if(event.getCode() == KeyCode.ENTER){
				entPressed = true;
				if(textField != null){
					commitEdit(textField.getText());
				}
			}
			else if(event.getCode() == KeyCode.TAB){
				this.getTableView().getSelectionModel().selectRightCell();
				if(textField!= null){
					commitEdit(textField.getText());
				}
			}
			else{
				return;
			}
			event.consume();
		});
	}
	/**
	 * Set the listener for commiting an edit.  There is only one listener.
	 * @param listener The {@link EditListener} to execute when an edit is 
	 * committed.
	 */
	public void 
	setOnEditCommit(EditListener<T> listener){
		this.editListener = listener;
	}
	
	@Override
	protected void 
	updateItem(String item, boolean empty){
		super.updateItem(item, empty);
		if(empty){
			setText(null);
			setGraphic(null);
		}
		else if(isEditing()) {
            setText(null);
            setGraphic(textField);
        } 
		else {
            setGraphic(null);
            setText(
            		getTableColumn()
            		.getCellObservableValue(getIndex())
            		.getValue());
        }
	}
	public void 
	update(){
		updateItem(getItem(), isEmpty());
	}
	
	@Override
	public void 
	startEdit(){
		if(!isEmpty()){
			super.startEdit();
			setText(null);
			textField.setText(textFactory.call(
					getTableView().getItems().get(getIndex())
					));
			
			setGraphic(textField);
			textField.requestFocus();
		}
	}
	/**
	 * {@link TableCell#commitEdit(Object)} breaks if you click on another 
	 * row to commit the edit, because it calls something in the TableView that
	 * requires the row to still be focused.  So this method uses a custom 
	 * system.
	 */
	@Override
	public void 
	commitEdit(String t){
		EditEvent<T> event = new EditEvent<>();
		event.oldText = getItem();
		event.newText = t;
		event.cell = this;
		
		if(editListener != null){
			editListener.commitEdit(event);
		}
		cancelEdit();
	}
	@Override
	public void
	cancelEdit(){
		super.cancelEdit();
		update();
	}
	@Override
	public void
	setTextFieldFactory(Callback<T, String> factory){
		if(factory == null){
			throw new IllegalArgumentException("A factory cannot be null!");
		}
		this.textFactory = factory;
	}
}
