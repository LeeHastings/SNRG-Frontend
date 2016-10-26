package org.snrg_nyc.ui.components;

import javafx.scene.control.TableCell;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TextField;
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
	private boolean escPressed, shiftPressed = false;
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
				else {
					commitEdit(textField.getText());
				}
			}
		});
		
		
		textField.setOnKeyPressed(event->{
			TablePosition<T,?> from = getTableView().getEditingCell();
			switch(event.getCode()){
			case ESCAPE:
				escPressed = true;
				cancelEdit();
				break;
			case SHIFT:
				shiftPressed = true;
				return;
			case UP:
				System.out.print("Up   : ");
				moveTo(from.getRow()-1, from.getColumn());
				break;
			case DOWN:
			case ENTER:
				System.out.print("Down : ");
				moveTo(from.getRow()+1, from.getColumn());
				break;
			case TAB:
				if(shiftPressed){
					System.out.print("Left : ");
					moveTo(from.getRow(), from.getColumn()-1);
				}
				else {
					System.out.print("Right: ");
					moveTo(from.getRow(), from.getColumn()+1);
				}
				break;
			default:
				return;
			}
			shiftPressed = false;
			TablePosition<T,?> to = getTableView().getEditingCell();
			if(to == null){
				System.err.printf("Stopped at (%d, %d)\n",
						from.getRow(), from.getColumn());
			}
			else {
				System.out.printf("Moved from (%d, %d) to (%d, %d)\n", 
						from.getRow(), from.getColumn(), 
						to.getRow(), to.getColumn());
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
	
	private boolean
	moveTo(int row, int col){
		
		if(row >= 0 && col >= 0 && row < getTableView().getItems().size() 
				&& col < getTableView().getColumns().size())
		{
			getTableView().edit(row, getTableView().getColumns().get(col));
			return true;
		}
		return false;
	}
}
