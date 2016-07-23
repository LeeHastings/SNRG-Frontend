package org.snrg_nyc.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.snrg_nyc.model.NodeEditor;
import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.ui.components.LayerID;
import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class UI_Main extends Application{
	Alert quitAlert;
	String experimentName = null;
	List<Integer> pathogenWindows = new ArrayList<>();
	ListView<Integer> pathogens = new ListView<>();

	@Override
	public void start(Stage initStage){
		EditorWindow window = new EditorWindow( new NodeEditor(), initStage, "Node Settings Editor");
		MenuBar topMenu = new MenuBar();
		window.setTop(topMenu);
		
		Menu fileM = new Menu("File");
		topMenu.getMenus().add(fileM);
		
		//Save dialog
		MenuItem save = new MenuItem("Save");
		MenuItem load = new MenuItem("Load");
		MenuItem quit = new MenuItem("Quit");
		fileM.getItems().addAll(quit, save, load);
		
		quitAlert = new Alert(Alert.AlertType.CONFIRMATION);
		quitAlert.setTitle("Quit");
		quitAlert.setHeaderText("Are you sure you want to quit?");
		
		quit.setOnAction(event-> {
			if(shouldQuit(window.getStage())){
				window.getStage().close();
			}
		});
		
		TextInputDialog saveDialog = new TextInputDialog();
		saveDialog.setTitle("Save Experiment");
		saveDialog.setHeaderText("Name the experiment");
		saveDialog.setGraphic(null);
		
		save.setOnAction(event->{
			saveDialog.getEditor().setText(experimentName);
			Optional<String> expName = saveDialog.showAndWait();
			if(expName.isPresent()){
				try{
					window.getModel().save(expName.get());
					window.editor().sendInfo("The experiment was saved as "+expName.get());
				}
				catch (Exception e){
					window.editor().sendError(e);
				}
			}
		});
		
		ChoiceDialog<String> loadDialog = new ChoiceDialog<>();
		loadDialog.setTitle("Load Experiment");
		loadDialog.setHeaderText("Select an experiment to load");
		loadDialog.setGraphic(null);
		
		load.setOnAction(event->{
			loadDialog.getItems().setAll(window.getModel().getExperimentNames());
			Optional<String> expName = loadDialog.showAndWait();
			try{
				if(expName.isPresent() ){
					experimentName = expName.get();
					window.getModel().load(expName.get());
					window.editor().sendInfo("The experiment was loaded as "+expName.get());
					window.updateProperties(null);
					window.getLayers().clear();
					window.getLayers().add(new LayerID());
					for(int i : window.getModel().layer_getLayerIDs()){
						window.getLayers().add(new LayerID(i));
					}
				}
			}
			catch (Exception e){
				window.editor().sendError(e);
			}
		});
		window.finishedProperty().addListener( (o, oldval, newval)->{
			pathogens.getItems().clear();
			try {
				pathogens.getItems().addAll(
						window.getModel().pathogen_getPathogenIDs());
			} catch (Exception e) {
				window.editor().sendError(e);
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
						setText(window.getModel().pathogen_getName(item));
					} catch (EditorException e) {
						window.editor().sendError(e);
						setText(">ERROR<");
					}
				}
			};
		});
		
		pathogens.getSelectionModel()
		         .selectedItemProperty()
		         .addListener((o, oldval, newval)->{
        	if(newval == null){
        		return;
        	}
			if(pathogenWindows.contains(newval)){
				window.editor().sendWarning("This pathogen window is already open");
			}
			else {
				pathogenWindows.add(newval);
				try{
					EditorWindow w = new EditorWindow(
						window.getModel().pathogen_getEditor(newval),
						new Stage(),
						"Pathogen Editor: "+window.getModel().pathogen_getName(newval)
					);
					w.getStage().setOnCloseRequest(event->{
						pathogenWindows.remove(pathogenWindows.indexOf(newval));
						pathogens.getSelectionModel().clearSelection();
					});
					w.show();
				}
				catch(EditorException e){
					pathogenWindows.remove(pathogenWindows.indexOf(newval));
					window.editor().sendError(e);
				}
			}
		});
		
		window.addAllToMenu(
				new Label("Pathogens"), 
				pathogens);
		
		window.getStage().setOnCloseRequest(event->{
			if(!shouldQuit(window.getStage())){
				event.consume();
			}
		} );
		window.show();
	}
	
	boolean shouldQuit(Stage stage){
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
}
