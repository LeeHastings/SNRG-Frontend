package org.snrg_nyc.ui;

import java.util.Optional;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.NodeEditor;
import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.ui.components.ExperimentInfoWindow;
import org.snrg_nyc.ui.components.Fonts;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * The entry class for the SNRG frontend.
 * @author Devin Hastings
 *
 */
public class UI_Main extends Application{
	private PropertiesEditor model;
	private Stage stage;
	private Scene scene;

	private Alert quitAlert;

	private ListView<Integer> pathogensView = null;
	private ListView<Integer> edgesView = null;

	private EditorMenu propertiesMenu;
	private GridPane editorsMenu;
	
	private int editorsMenuRow = 0; //For automatic positioning in grid
	
	private EditorPage editor;
	
	private BorderPane window;

	@Override
	public void 
	start(Stage initStage){
		model = new NodeEditor();
		window = new BorderPane();
		stage = initStage;
		stage.setTitle("Node Settings Editor");
		editor = new EditorPage(this.model);		
		scene = new Scene(window, 1100, 600);

		//Style
		scene.getStylesheets().add(
				UI_Main.class.getResource("ui_style.css").toExternalForm());
		
		window.getStyleClass().add("main");
		
		ScrollPane editorPane = new ScrollPane();
		editorPane.setFitToHeight(true);
		editorPane.setFitToWidth(true);
		editorPane.setContent(editor);
		
		propertiesMenu = new EditorMenu(editor, "Node Properties");
		
		
		//Add a toolbar to the window
		MenuBar topMenu = new MenuBar();
		topMenu.setMinWidth(stage.getWidth());
		
		Menu fileM = new Menu("File");
		Menu editM = new Menu("Edit");
		topMenu.getMenus().addAll(fileM, editM);
		
		MenuItem newProject = new MenuItem("New");
		MenuItem save = new MenuItem("Save");
		MenuItem load = new MenuItem("Load");
		MenuItem quit = new MenuItem("Quit");
		fileM.getItems().addAll(newProject, save, load, quit);
		
		MenuItem expInfo = new MenuItem("Experiment Info");
		editM.getItems().addAll(expInfo);
		
		//The Quit menu
		quitAlert = new Alert(Alert.AlertType.CONFIRMATION);
		quitAlert.setTitle("Quit");
		quitAlert.setHeaderText("Are you sure you want to quit?");
		
		quit.setOnAction(event-> {
			if(shouldQuit()){
				stage.close();
				Platform.exit();
			}
		});
		
		//Open a new property
		Alert newAlert = new Alert(Alert.AlertType.CONFIRMATION);
		newAlert.setTitle("New Project");
		newAlert.setHeaderText("Do you want to open a new project?");
		newAlert.setContentText("Unsaved progress will be lost.");
		
		newProject.setOnAction(event->{
			Optional<ButtonType> input = newAlert.showAndWait();
			if( input.isPresent() && input.get() == ButtonType.OK){
				openNodeWindow();
				model.clear();
				update();
				try {
					propertiesMenu.updateAll();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		//The save menu
		TextInputDialog saveDialog = new TextInputDialog();
		saveDialog.setTitle("Save Experiment");
		saveDialog.setHeaderText("Name the experiment");
		saveDialog.setGraphic(null);
		
		save.setOnAction(event->{
			//Default to previously used name
			try {
				saveDialog.getEditor().setText(model.experiment_getName());
			} catch (Exception e1) {
				editor.sendError(e1);
			} 
			Optional<String> expName = saveDialog.showAndWait();
			if(expName.isPresent()){
				try{
					model.save(expName.get());
					editor.sendInfo("The experiment was saved as "
							+expName.get());
				}
				catch (Exception e){
					editor.sendError(e);
				}
			}
		});
		
		//Load menu, select from a valid list of experiments
		ChoiceDialog<String> loadDialog = new ChoiceDialog<>();
		loadDialog.setTitle("Load Experiment");
		loadDialog.setHeaderText("Select an experiment to load");
		loadDialog.setGraphic(null);
		
		load.setOnAction(event->{
			loadDialog.getItems().setAll(model.getExperimentNames());
			Optional<String> expName = loadDialog.showAndWait();
			try{
				if(expName.isPresent() ){
					openNodeWindow();
					model.load(expName.get());
					model.experiment_setName(expName.get());
					editor.sendInfo("The experiment was loaded as "
							+expName.get());
					editorPane.setContent(editor);
					update();
					propertiesMenu.updateAll();
				}
			}
			catch (Exception e){
				editor.sendError(e);
			}
		});
		
		//Edit experiment info
		expInfo.setOnAction(event->{
			ExperimentInfoWindow exp;
			try {
				exp = new ExperimentInfoWindow(model);
				exp.closeProperty().addListener((val, oldval, close)->{
					if(close){
						editorPane.setContent(editor);
					}
				});
				editorPane.setContent(exp);
			} catch (Exception e) {
				editor.sendError(e);
			}
		});
		
		stage.setOnCloseRequest(event->{
			if(!shouldQuit()){
				event.consume();
			}
			else {
				Platform.exit();
			}
		} );
		
		editorsMenu = null;
		editorsMenu = new GridPane();
		//Same appearance as leftMenu
		editorsMenu.getStyleClass().addAll(propertiesMenu.getStyleClass());
		editorsMenu.getColumnConstraints().addAll(
				propertiesMenu.getColumnConstraints());
		
		editorsMenu.setPrefWidth(propertiesMenu.getPrefWidth());
		editorsMenu.setMaxWidth(propertiesMenu.getMaxWidth()); 
		editorsMenu.setHgap(propertiesMenu.getHgap());
		editorsMenu.setVgap(propertiesMenu.getVgap());
		editorsMenu.setPadding(propertiesMenu.getPadding());
		
		pathogensView = new ListView<>();
		pathogensView.setPrefHeight(150);
		pathogensView.setCellFactory(col ->{
			return new ListCell<Integer>(){
				@Override
				public void updateItem(Integer item, boolean empty){
					super.updateItem(item, empty);
					if(item == null || empty){
						setText(null);
						return;
					}
					try {
						setText(model.pathogen_getName(item));
					} catch (EditorException e) {
						editor.sendError(e);
						setText(">ERROR<");
					}
				}
			};
		});
		Text pathText = new Text("Pathogens");
		pathText.setFont(Fonts.headFont);
		addAllToEditorsMenu(pathText, pathogensView);
		
		edgesView = new ListView<>();
		edgesView.setPrefHeight(150);
		edgesView.setCellFactory(lv ->{
			return new ListCell<Integer>(){
				@Override
				public void updateItem(Integer item, boolean empty){
					super.updateItem(item, empty);
					if(empty || item == null){
						setText(null);
					}
					else {
						try {
							setText(model.layer_getName(item));
						} catch (EditorException e) {
							setText(">ERROR<");
							editor.sendError(e);
						}
					}
				}
			};
		});
		Text edgeText = new Text("Edge Settings");
		edgeText.setFont(Fonts.headFont);
		addAllToEditorsMenu(edgeText, edgesView);
		
		Button returnToNodeProp = new Button("View Node Settings");
		returnToNodeProp.setOnMouseClicked(event-> openNodeWindow());
		addToEditorsMenu(returnToNodeProp);

		stage.setScene(scene);
		try {
			propertiesMenu.updateAll();
		} catch (EditorException e1) {
			editor.sendError(e1);
		}
		
		//Enable opening windows for pathogens
		pathogensView.getSelectionModel()
			.selectedItemProperty()
			.addListener((o, oldval, newval)-> 
			{
				if(newval != null){
					openPathogenWindow(newval);
				}
			});
		
		//Enable the same behavior for edge settings
		edgesView.getSelectionModel()
			.selectedItemProperty()
			.addListener((o, oldval, newval)->
			{
				if(newval != null){
					openEdgeWindow(newval);
				}
			});
		
		editor.addedLayerProperty().addListener((o, oldval, newval)->{
			if(newval){
				updateEdges();
			}
		});
		editor.addedPathogenProperty().addListener((o, oldval, newval)->{
			if(newval){
				updatePathogens();
			}
		});


		window.setTop(topMenu);
		HBox box = new HBox();
		HBox.setHgrow(editorPane, Priority.ALWAYS);
		box.getChildren().addAll(editorsMenu, propertiesMenu, editorPane);
		window.setCenter(box);
		window.setBottom(new MessageBox(editor));
		
		stage.show();
	}
	
	/**
	 * Change the editor to display the {@link PropertiesEditor}
	 * for a pathogen
	 * @param pathogenID The ID of the pathogen to edit
	 */
	void 
	openPathogenWindow(int pathogenID){
		try{
			editor.setModel(model.pathogen_getEditor(pathogenID));
			propertiesMenu.setTitle("Pathogen Properties");
			stage.setTitle("Pathogen Editor: "
					+model.pathogen_getName(pathogenID));
		}
		catch(EditorException e){
			editor.sendError(e);
		}
		pathogensView.getSelectionModel().clearSelection();
	}
	
	/**
	 * Change the editor to display the {@link PropertiesEditor}
	 * for an edge
	 * @param layerID The ID of the edge settings to edit (bound to a layer)
	 */
	public void 
	openEdgeWindow(int layerID){
		try {
			editor.setModel(model.layer_getEdgeEditor(layerID));
			propertiesMenu.setTitle("Edge Properties");
			stage.setTitle("Edge Editor: "+model.layer_getName(layerID));
		} 
		catch (EditorException e) {
			editor.sendError(e);
		}
		edgesView.getSelectionModel().clearSelection();
	}
	
	/**
	 * Change the editor to display the {@link PropertiesEditor}
	 * for the node settings
	 */
	public void
	openNodeWindow(){
		editor.setModel(model);
		propertiesMenu.setTitle("Node Properties");
		stage.setTitle("Node Settings Editor");
	}
	
	private void 
	addToEditorsMenu(Node n) {
		editorsMenu.add(n, 0, editorsMenuRow++);
	}

	private void 
	addAllToEditorsMenu(Node... nodes){
		for(Node n : nodes){
			addToEditorsMenu(n);
		}
	}
	/**
	 * Update the editors menu
	 */
	private void
	update(){
		updatePathogens();
		updateEdges();
	}
	/**
	 * Refresh the list of pathogens from the model
	 */
	private void updatePathogens(){
		try {
			pathogensView.getItems().setAll(model.pathogen_getPathogenIDs());
		} catch (EditorException e) {
			editor.sendError(e);
		}
	}
	/**
	 * Refresh the list of edges from the model
	 */
	private void updateEdges(){
		edgesView.getItems().setAll(model.layer_getLayerIDs());
	}
	
	/**
	 * Open a menu that asks if the user wants to quit.
	 * Return true if they accept, otherwise false.
	 * @return True if the program should exit, otherwise false.
	 */
	private boolean 
	shouldQuit(){
		Optional<ButtonType> input = quitAlert.showAndWait();
		return input.isPresent() && input.get() == ButtonType.OK;
	}
	
	public static void 
	main(String[] args){
		launch(args);
	}
}
