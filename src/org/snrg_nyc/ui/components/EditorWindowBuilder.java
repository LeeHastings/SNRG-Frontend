package org.snrg_nyc.ui.components;

import org.snrg_nyc.model.PropertiesEditor;

import javafx.stage.Stage;

/**
 * A builder for creating instances of {@link EditorWindow}
 * with certain features included or removed.
 * This allows for more advanced construction
 * @author Devin Hastings
 *
 */
public class EditorWindowBuilder {
	private boolean enableLayers = false;
	private boolean enableToolbar = false;
	private boolean enablePathogens = false;
	private boolean enableEdges = false;
	
	public EditorWindowBuilder(){}
	
	public EditorWindowBuilder enableLayers(){
		enableLayers = true;
		return this;
	}
	public EditorWindowBuilder enableToolbar(){
		enableToolbar = true;
		return this;
	}
	public EditorWindowBuilder enablePathogens(){
		enablePathogens = true;
		return this;
	}
	public EditorWindowBuilder enableEdges(){
		enableEdges = true;
		return this;
	}
	
	public EditorWindow build(PropertiesEditor model, Stage initStage, String title){
		if(enableEdges && !enableLayers){
			throw new IllegalArgumentException("Cannot enable edges without layers!");
		}
		return new EditorWindow(model, initStage, title, 
				enableToolbar, enableLayers, enablePathogens, enableEdges);
	}
	
}
