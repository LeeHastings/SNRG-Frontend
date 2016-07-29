package org.snrg_nyc.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.snrg_nyc.model.NodeEditor;
import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.ui.components.EditorWindow;
import org.snrg_nyc.ui.components.EditorWindowBuilder;

import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * The entry class for the SNRG frontend.
 * @author Devin Hastings
 *
 */
public class UI_Main extends Application{
	Alert quitAlert;
	String experimentName = null;
	List<Integer> pathogenWindows = new ArrayList<>();
	
	EditorWindow mainWindow;

	@Override
	public void start(Stage initStage){
		//Creating the main page
		mainWindow = new EditorWindowBuilder()
				     .enableToolbar(true)
				     .enablePathogens(true)
				     .build( new NodeEditor(), initStage, "Node Settings Editor");
		
		//Enable opening windows for pathogens
		mainWindow.pathogens().getSelectionModel()
		         .selectedItemProperty()
		         .addListener((o, oldval, newval)-> {
			if(newval != null){
				openPathogenWindow(newval);
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
				EditorWindow w = new EditorWindowBuilder()
					.build(
						mainWindow.model().pathogen_getEditor(pathogenID),
						new Stage(),
						"Pathogen Editor: "+mainWindow.model().pathogen_getName(pathogenID)
					);
				w.stage().setOnCloseRequest(event->{
					pathogenWindows.remove(pathogenWindows.indexOf(pathogenID));
					mainWindow.pathogens().getSelectionModel().clearSelection();
				});
				w.show();
			}
			catch(EditorException e){
				pathogenWindows.remove(pathogenWindows.indexOf(pathogenID));
				mainWindow.editor().sendError(e);
			}
		}
	}
}
