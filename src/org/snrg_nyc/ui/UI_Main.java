package org.snrg_nyc.ui;

import java.util.ArrayList;
import java.util.List;

import org.snrg_nyc.model.NodeEditor;
import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.ui.components.EditorWindow;
import org.snrg_nyc.ui.components.EditorWindowBuilder;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The entry class for the SNRG frontend.
 * @author Devin Hastings
 *
 */
public class UI_Main extends Application{
	List<Integer> pathogenWindows = new ArrayList<>();
	List<Integer> edgeWindows = new ArrayList<>();
	EditorWindow mainWindow;

	@Override
	public void start(Stage initStage){
		//Creating the main page
		mainWindow = new EditorWindowBuilder()
				     .enableToolbar()
				     .enableLayers()
				     .enablePathogens()
				     .enableEdges()
				     .build( new NodeEditor(), initStage, "Node Settings Editor");
		
		//Enable opening windows for pathogens
		mainWindow.pathogensView().getSelectionModel()
			.selectedItemProperty()
			.addListener((o, oldval, newval)-> 
			{
				if(newval != null){
					openPathogenWindow(newval);
				}
			});
		
		//Enable the same behavior for edge settings
		mainWindow.edgesView().getSelectionModel()
			.selectedItemProperty()
			.addListener((o, oldval, newval)->
			{
				if(newval != null){
					openEdgeWindow(newval);
				}
			});
		
		mainWindow.show();
	}
	
	public static void main(String[] args){
		launch(args);
	}
	
	/**
	 * Open a new window for a pathogen editor
	 * @param pathogenID The ID of the pathogen to edit
	 */
	void openPathogenWindow(int pathogenID){
		if(pathogenWindows.contains(pathogenID)){
			mainWindow.editor().sendWarning("This pathogen window is already open");
		}
		else {
			pathogenWindows.add(pathogenID);
			try{
				EditorWindow w = 
					new EditorWindowBuilder()
					.enableLayers()
					.build(
						mainWindow.model().pathogen_getEditor(pathogenID),
						new Stage(),
						"Pathogen Editor: "+mainWindow.model().pathogen_getName(pathogenID)
					);
				w.stage().setOnCloseRequest(event->{
					pathogenWindows.remove(pathogenWindows.indexOf(pathogenID));
					mainWindow.pathogensView().getSelectionModel().clearSelection();
				});
				w.show();
			}
			catch(EditorException e){
				pathogenWindows.remove(pathogenWindows.indexOf(pathogenID));
				mainWindow.editor().sendError(e);
			}
		}
	}
	
	public void openEdgeWindow(int layerID){
		if(edgeWindows.contains(layerID)){
			mainWindow.editor().sendWarning(
					"There is already an edge settings window open for this layer");
		}
		else {
			edgeWindows.add(layerID);
			try {
				EditorWindow w = 
					new EditorWindowBuilder()
					.build(
						mainWindow.model().layer_getEdgeEditor(layerID),
						new Stage(),
						"Edge Editor: "+mainWindow.model().layer_getName(layerID)
					);
				
				w.stage().setOnCloseRequest(event->{
					edgeWindows.remove(edgeWindows.indexOf(layerID));
					mainWindow.edgesView().getSelectionModel().clearSelection();
				});
				w.show();
			} catch (EditorException e) {
				mainWindow.editor().sendError(e);
			}
		}
	}
}
