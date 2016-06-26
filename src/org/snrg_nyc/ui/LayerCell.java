package org.snrg_nyc.ui;

import javafx.scene.control.ListCell;

class LayerCell extends ListCell<LayerID> {
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
					setText(editor.ui.layer_getName(item.get()));
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
