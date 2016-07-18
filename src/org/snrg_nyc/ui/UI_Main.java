package org.snrg_nyc.ui;

import java.util.Optional;

import org.snrg_nyc.model.NodeEditor;
import org.snrg_nyc.ui.components.LayerID;
import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class UI_Main extends Application{
	Alert quitAlert;
	String experimentName = null;

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
		
		quit.setOnAction(event-> quit(window.getStage()));
		
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
					window.sendInfo("The experiment was saved as "+expName.get());
				}
				catch (Exception e){
					window.sendError(e);
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
					window.sendInfo("The experiment was loaded as "+expName.get());
					window.updateProperties(null);
					window.getLayers().clear();
					window.getLayers().add(new LayerID());
					for(int i : window.getModel().layer_getLayerIDs()){
						window.getLayers().add(new LayerID(i));
					}
				}
			}
			catch (Exception e){
				window.sendError(e);
			}
		});
		window.getStage().setOnCloseRequest(event-> quit(window.getStage()) );
		window.show();
	}
	
	void quit(Stage stage){
		Optional<ButtonType> input = quitAlert.showAndWait();
		if(input.isPresent() && input.get() == ButtonType.OK){
			stage.close();
		}
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
