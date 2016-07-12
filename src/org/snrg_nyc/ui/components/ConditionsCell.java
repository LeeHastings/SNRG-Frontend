package org.snrg_nyc.ui.components;

import java.util.Map.Entry;

import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.ui.EditorPage;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class ConditionsCell extends ListCell<Integer> {
	private EditorPage editor;
	
	public ConditionsCell(EditorPage e){
		editor = e;
		
		setOnDragDetected(event->{
			if(getItem() == null){
				return;
			}
			
			Dragboard dragbd = startDragAndDrop(TransferMode.MOVE);
			
			ClipboardContent content = new ClipboardContent();
			content.putString(Integer.toString( getItem() ));
			
			dragbd.setContent(content);
			event.consume();
		});
		
		setOnDragOver(event->{
			if(event.getGestureSource() != this
					&& event.getDragboard().hasString()
			){
				event.acceptTransferModes(TransferMode.MOVE);
			}
		});
		
		setOnDragEntered(event->{
			if(event.getGestureSource() != this
					&& event.getDragboard().hasString()
			){
				setOpacity(0.3);
			}
		});
		
		setOnDragExited(event->{
			if(event.getGestureSource() != this
					&& event.getDragboard().hasString()
			){
				setOpacity(1);
			}
		});
		
		setOnDragDropped(event->{
			if(getItem() == null){
				return;
			}
			Dragboard db = event.getDragboard();
			
			if(db.hasString()){
				int cid = Integer.parseInt(db.getString());
				ObservableList<Integer> items = getListView().getItems();
				
				int draggedId = items.indexOf(cid);
				int droppedId = items.indexOf(getItem());
				
				boolean up = draggedId > droppedId; //Dragged item was moved up list
				
				if(up){ //Push items down
					for(int i = draggedId; i > droppedId; i--){
						items.set(i, items.get(i-1));
					}
					items.set(droppedId, cid);
				}
				else { //Pull items up
					for(int i = draggedId; i < droppedId; i++){
						items.set(i, items.get(i+1));
					}
					items.set(droppedId, cid);
				}
				
			}
			
		});
	}
	
	@Override
	public void updateItem(Integer item, boolean empty){
		super.updateItem(item, empty);
		if(empty){
			setText(null);
			return;
		}
		String name = "Distribution: ";
		try{
			PropertiesEditor model = editor.getModel();
			for(Entry<Integer, Integer> pair :
				model.scratch_getDistributionCondition(item).entrySet() 
				){
				name += model.nodeProp_getName(pair.getKey())
				     +"["
				     + model.nodeProp_getRangeLabel(pair.getKey(), pair.getValue())
				     +"] ";
			}
		}
		catch (Exception e){
			editor.sendError(e);
			name = ">ERROR<";
		}
		setText(name);
	}
	
}
