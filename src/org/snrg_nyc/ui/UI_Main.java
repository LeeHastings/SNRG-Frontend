package org.snrg_nyc.ui;

import java.util.List;
import java.util.Optional;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.NodeEditor;
import org.snrg_nyc.model.PropertiesEditor;

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
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
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
	private String experimentName = null;

	private ListView<Integer> pathogensView = null;
	private ListView<Integer> edgesView = null;

	private EditorMenu leftMenu;
	private GridPane rightMenu;
	
	private int rightMenuRow = 0; //For automatic positioning in grid
	
	private EditorPage editor;
	
	static final Font headFont = Font.font("sans", FontWeight.NORMAL, 
			FontPosture.REGULAR, 15);
	
	private BorderPane window;

	@Override
	public void 
	start(Stage initStage){
		model = new NodeEditor();
		window = new BorderPane();
		stage = initStage;
		stage.setTitle("Node Settings Editor");
		editor = new EditorPage(this.model);		
		scene = new Scene(window, 900, 600);

		//Style
		scene.getStylesheets().add(
				UI_Main.class.getResource("ui_style.css").toExternalForm());
		
		window.getStyleClass().add("main");
		
		ScrollPane editorPane = new ScrollPane();
		editorPane.setFitToHeight(true);
		editorPane.setFitToWidth(true);
		editorPane.setContent(editor);
		window.setCenter(editorPane);
		
		leftMenu = new EditorMenu(editor, "Node Properties");
		window.setLeft(leftMenu);
		
		
		//Add a toolbar to the window
		MenuBar topMenu = new MenuBar();
		window.setTop(topMenu);
		
		Menu fileM = new Menu("File");
		topMenu.getMenus().add(fileM);
		

		MenuItem newProject = new MenuItem("New");
		MenuItem save = new MenuItem("Save");
		MenuItem load = new MenuItem("Load");
		MenuItem quit = new MenuItem("Quit");
		fileM.getItems().addAll(newProject, save, load, quit);
		
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
				experimentName = "";
				try {
					leftMenu.updateAll();
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
			saveDialog.getEditor().setText(experimentName); 
			Optional<String> expName = saveDialog.showAndWait();
			if(expName.isPresent()){
				try{
					experimentName = expName.get();
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
					experimentName = expName.get();
					openNodeWindow();
					model.load(expName.get());
					editor.sendInfo("The experiment was loaded as "
							+expName.get());
					update();
					leftMenu.updateAll();
				}
			}
			catch (Exception e){
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
		
		rightMenu = null;
		rightMenu = new GridPane();
		//Same appearance as leftMenu
		rightMenu.getStyleClass().addAll(leftMenu.getStyleClass());
		rightMenu.getColumnConstraints().addAll(
				leftMenu.getColumnConstraints());
		
		rightMenu.setPrefWidth(leftMenu.getPrefWidth());
		rightMenu.setMaxWidth(leftMenu.getMaxWidth()); 
		rightMenu.setHgap(leftMenu.getHgap());
		rightMenu.setVgap(leftMenu.getVgap());
		rightMenu.setPadding(leftMenu.getPadding());
		
		stage.setWidth(window.getWidth()+rightMenu.getPrefWidth());
		
		window.setRight(rightMenu);
		
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
		pathText.setFont(headFont);
		addAllToRightMenu(pathText, pathogensView);
		
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
		edgeText.setFont(headFont);
		addAllToRightMenu(edgeText, edgesView);
		
		Button returnToNodeProp = new Button("View Node Settings");
		returnToNodeProp.setOnMouseClicked(event-> openNodeWindow());
		addToRightMenu(returnToNodeProp);

		editor.finishedProperty().addListener( (o, oldval, newval)->{
			update();
		});
		stage.setScene(scene);
		try {
			leftMenu.updateAll();
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
		
		stage.show();
	}
	
	/**
	 * Open a new window for a pathogen editor
	 * @param pathogenID The ID of the pathogen to edit
	 */
	void 
	openPathogenWindow(int pathogenID){
		try{
			editor.setModel(model.pathogen_getEditor(pathogenID));
			leftMenu.setTitle("Pathogen Properties");
			stage.setTitle("Pathogen Editor: "
					+model.pathogen_getName(pathogenID));
		}
		catch(EditorException e){
			editor.sendError(e);
		}
	}
	
	public void 
	openEdgeWindow(int layerID){
		try {
			editor.setModel(model.layer_getEdgeEditor(layerID));
			leftMenu.setTitle("Edge Properties");
			stage.setTitle("Edge Editor: "+model.layer_getName(layerID));
		} 
		catch (EditorException e) {
			editor.sendError(e);
		}
	}
	
	public void
	openNodeWindow(){
		editor.setModel(model);
		leftMenu.setTitle("Node Properties");
		stage.setTitle("Node Settings Editor");
	}
	
	private void 
	addToRightMenu(Node n) {
		rightMenu.add(n, 0, rightMenuRow++);
	}

	private void 
	addAllToRightMenu(Node... nodes){
		for(Node n : nodes){
			addToRightMenu(n);
		}
	}

	private void
	update(){
		try {
			pathogensView.getItems().setAll(
					model.pathogen_getPathogenIDs());
			List<Integer> lids = model.layer_getLayerIDs();
			edgesView.getItems().setAll(lids);
		} catch (Exception e) {
			editor.sendError(e);
		}
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
