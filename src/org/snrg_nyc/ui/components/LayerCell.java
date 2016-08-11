package org.snrg_nyc.ui.components;

import java.util.Optional;

import org.snrg_nyc.ui.EditorPage;

import javafx.scene.control.ListCell;

public class LayerCell extends ListCell<Optional<Integer>> {
	private EditorPage editor;
	public LayerCell(EditorPage editor){
		super();
		this.editor = editor;
	}
	@Override
	public void updateItem(Optional<Integer> item, boolean empty){
		super.updateItem(item, empty);
		if(!empty && item != null){
			if(!item.isPresent()){
				setText("No Layer");
			}
			else {
				try{
					setText(editor.model().layer_getName(item.get()));
				}
				catch (Exception e){
					editor.sendError(e);
				}
			}
		}
		else {
			setText(null);
		}
	}
	
}
