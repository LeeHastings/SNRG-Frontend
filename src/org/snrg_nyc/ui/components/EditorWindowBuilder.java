package org.snrg_nyc.ui.components;

import org.snrg_nyc.model.PropertiesEditor;

import javafx.stage.Stage;

public class EditorWindowBuilder {
	private boolean useLayers = true;
	private boolean showMessages = true;
	private boolean showToolbar = false;
	private boolean enablePathogens = false;
	
	public EditorWindowBuilder(){
		
	}
	public EditorWindow build(PropertiesEditor model, Stage initStage, String title){
		return new EditorWindow(model, initStage, title);
	}
}
