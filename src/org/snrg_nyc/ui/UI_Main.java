package org.snrg_nyc.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.snrg_nyc.model.NodeEditor;
import org.snrg_nyc.model.internal.EditorException;
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
	ListView<Integer> pathogens = new ListView<>();
	
	EditorWindow mainWindow;

	@Override
	public void start(Stage initStage){
		//Creating the main page
		mainWindow = new EditorWindow( new NodeEditor(), initStage, "Node Settings Editor");
		
		//Add a toolbar to the window
		MenuBar topMenu = new MenuBar();
		mainWindow.setTop(topMenu);
		
		Menu fileM = new Menu("File");
		topMenu.getMenus().add(fileM);
		
		MenuItem save = new MenuItem("Save");
		MenuItem load = new MenuItem("Load");
		MenuItem quit = new MenuItem("Quit");
		fileM.getItems().addAll(quit, save, load);
		
		//The Quit menu
		quitAlert = new Alert(Alert.AlertType.CONFIRMATION);
		quitAlert.setTitle("Quit");
		quitAlert.setHeaderText("Are you sure you want to quit?");
		
		quit.setOnAction(event-> {
			if(shouldQuit()){
				mainWindow.stage().close();
			}
		});
		
		//The save menu
		TextInputDialog saveDialog = new TextInputDialog();
		saveDialog.setTitle("Save Experiment");
		saveDialog.setHeaderText("Name the experiment");
		saveDialog.setGraphic(null);
		
		save.setOnAction(event->{
			//Default to previously used name
			saveDialog.getEditor().setText(experimentName); 
			Optional<String> expName = saveDialog.showAndWait();
			if(expName.isPresent()){
				try{
					mainWindow.model().save(expName.get());
					mainWindow.editor().sendInfo("The experiment was saved as "+expName.get());
				}
				catch (Exception e){
					mainWindow.editor().sendError(e);
				}
			}
		});
		
		//Load menu, select from a valid list of experiments
		ChoiceDialog<String> loadDialog = new ChoiceDialog<>();
		loadDialog.setTitle("Load Experiment");
		loadDialog.setHeaderText("Select an experiment to load");
		loadDialog.setGraphic(null);
		
		load.setOnAction(event->{
			loadDialog.getItems().setAll(mainWindow.model().getExperimentNames());
			Optional<String> expName = loadDialog.showAndWait();
			try{
				if(expName.isPresent() ){
					experimentName = expName.get();
					mainWindow.model().load(expName.get());
					mainWindow.editor().sendInfo("The experiment was loaded as "+expName.get());
					mainWindow.updateAll();
					pathogens.getItems().setAll(mainWindow.model().pathogen_getPathogenIDs());
				}
			}
			catch (Exception e){
				mainWindow.editor().sendError(e);
			}
		});
		mainWindow.editor().finishedProperty().addListener( (o, oldval, newval)->{
			try {
				pathogens.getItems().setAll(
						mainWindow.model().pathogen_getPathogenIDs());
			} catch (Exception e) {
				mainWindow.editor().sendError(e);
			}
		});
		pathogens.setPrefHeight(100);
		pathogens.setCellFactory(col ->{
			return new ListCell<Integer>(){
				@Override
				public void updateItem(Integer item, boolean empty){
					super.updateItem(item, empty);
					if(item == null || empty){
						setText(null);
						return;
					}
					try {
						setText(mainWindow.model().pathogen_getName(item));
					} catch (EditorException e) {
						mainWindow.editor().sendError(e);
						setText(">ERROR<");
					}
				}
			};
		});
		//Open a pathogen window if a pathogen is selected in the list
		pathogens.getSelectionModel()
		         .selectedItemProperty()
		         .addListener((o, oldval, newval)-> {
			if(newval != null){
				openPathogenWindow(newval);
			}
		});
		
		mainWindow.addAllToMenu(
				new Label("Pathogens"), 
				pathogens);
		
		mainWindow.stage().setOnCloseRequest(event->{
			if(!shouldQuit()){
				event.consume();
			}
		} );
		mainWindow.show();
	}
	
	/**
	 * Open a menu that asks if the user wants to quit.
	 * Return true if they accept, otherwise false.
	 * @return
	 */
	boolean shouldQuit(){
		Optional<ButtonType> input = quitAlert.showAndWait();
		if(input.isPresent() && input.get() == ButtonType.OK){
			return true;
		}
		else {
			return false;
		}
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
				EditorWindow w = new EditorWindow(
					mainWindow.model().pathogen_getEditor(pathogenID),
					new Stage(),
					"Pathogen Editor: "+mainWindow.model().pathogen_getName(pathogenID)
				);
				w.stage().setOnCloseRequest(event->{
					pathogenWindows.remove(pathogenWindows.indexOf(pathogenID));
					pathogens.getSelectionModel().clearSelection();
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
