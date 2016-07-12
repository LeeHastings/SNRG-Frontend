package org.snrg_nyc.ui.components;

import org.snrg_nyc.ui.EditorPage;

import javafx.scene.control.ListCell;

public class LayerCell extends ListCell<LayerID> {
	private EditorPage editor;
	public LayerCell(EditorPage editor){
		super();
		this.editor = editor;
	}
	@Override
	public void updateItem(LayerID item, boolean empty){
		super.updateItem(item, empty);
		if(!empty && item != null){
			if(!item.used()){
				setText("No Layer");
			}
			else {
				try{
					setText(editor.getModel().layer_getName(item.get()));
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
