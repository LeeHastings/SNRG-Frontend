package org.snrg_nyc.ui.components;

import org.snrg_nyc.model.PropertiesEditor;

import javafx.stage.Stage;

/**
 * A builder for creating instances of {@link EditorWindow}
 * with certain features included or removed.
 * This allows for more advanced construction
 * @author devin
 *
 */
public class EditorWindowBuilder {
	private boolean enableLayers = true;
	private boolean enableToolbar = false;
	private boolean enablePathogens = false;
	
	public EditorWindowBuilder(){}
	
	public EditorWindowBuilder enableLayers(boolean enable){
		enableLayers = enable;
		return this;
	}
	public EditorWindowBuilder enableToolbar(boolean show){
		enableToolbar = show;
		return this;
	}
	public EditorWindowBuilder enablePathogens(boolean enable){
		enablePathogens = enable;
		return this;
	}
	
	public EditorWindow build(PropertiesEditor model, Stage initStage, String title){
		return new EditorWindow(model, initStage, title, 
				enableToolbar, enableLayers, enablePathogens);
	}
	
}
