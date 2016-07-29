package org.snrg_nyc.ui.components;

import java.util.Optional;

import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.ui.EditorPage;
import org.snrg_nyc.ui.UI_Main;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * This class is a UI interface for a {@link PropertiesEditor} instance.
 * The class is a border pane, with the left side listing properties and UI messages, 
 * the right containing pathogens and edge settings (if enabled),
 * and the center containing an {@link EditorPage} object.
 * 
 * @author Devin Hastings
 */
public class EditorWindow extends BorderPane {
	private PropertiesEditor model;
	private Stage stage;
	private Scene scene;

	private Alert quitAlert;
	private String experimentName = null;

	private ListView<Integer> pathogensView = null;
	private ListView<Integer> edgesView = null;

	private GridPane leftMenu;
	private GridPane rightMenu;
	
	private int leftMenuRow = 0; //Row for items in leftMenu
	private int rightMenuRow = 0; //Row for items in rightMenu
	
	private EditorPage editor;
	
	private ObservableList<PropertyID> properties = FXCollections.observableArrayList();
	private ObservableList<Optional<Integer>> layers = FXCollections.observableArrayList();
	
	static final Font titleFont = Font.font("sans", FontWeight.LIGHT, FontPosture.REGULAR, 20);
	static final Font headFont = Font.font("sans", FontWeight.NORMAL, FontPosture.REGULAR, 15);
	
	/**
	 * Create a new window that edits the model in the given {@link PropertiesEditor}.
	 * @param model The {@link PropertiesEditor} to interface with.
	 * @param initStage The javaFX {@link Stage} to show the window on.
	 * @param title The title of the new window.
	 */
	@SuppressWarnings("unchecked")
	EditorWindow(PropertiesEditor model, Stage initStage, String title, 
			boolean enableToolbar, boolean enableLayers, boolean enablePathogens, boolean enableEdges)
	{
		this.model = model;
		stage = initStage;
		stage.setTitle(title);
		editor = new EditorPage(this.model);
		
		editor.setPrefWidth(500);
		scene = new Scene(this, 900, 600);
		
		leftMenu = new GridPane();
		leftMenu.setVgap(10);
		leftMenu.setPadding(new Insets(10));
		leftMenu.setMaxWidth(300);
		leftMenu.setPrefWidth(300);

		ColumnConstraints menuCol = new ColumnConstraints();
		menuCol.setPercentWidth(100);
		leftMenu.getColumnConstraints().add(menuCol);
		
		Text titleText = new Text("Node Properties");
		titleText.setFont(titleFont);
		addToLeftMenu(titleText);
		
		TableView<PropertyID> propertyTable = new TableView<>();
		propertyTable.setItems(properties);
		TableColumn<PropertyID, String> nameCol = new TableColumn<>("Name");
		TableColumn<PropertyID, String> typeCol = new TableColumn<>("Type");
		TableColumn<PropertyID, String> depCol = new TableColumn<>("Dependency Level");
		
		nameCol.setCellValueFactory(new PropertyNameFactory(editor));
		typeCol.setCellValueFactory(new PropertyTypeFactory(editor));
		
		depCol.setCellValueFactory(col->{
			PropertyID id = col.getValue();
			try{
				if(id.usesLayer()){
					return new SimpleStringProperty(
							""+this.model.nodeProp_getDependencyLevel(id.lid(), id.pid()) );
				}
				return new SimpleStringProperty(""+this.model.nodeProp_getDependencyLevel(id.pid()));
			} 
			catch(Exception e){
				editor.sendError(e);
				return new SimpleStringProperty(">ERROR<");
			}
		});
		
		propertyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		propertyTable.setPrefHeight(200);
		propertyTable.getColumns().addAll(nameCol, typeCol, depCol);
		
		//Listen to if there is a selection
		propertyTable.getSelectionModel().selectedIndexProperty().addListener((o, oldVal, newVal)->{
			if(newVal.intValue() != -1){
				PropertyID pid = propertyTable.getItems().get(newVal.intValue());
				editor.viewProperty(pid);
			}
		});
		propertyTable.focusedProperty().addListener((o, oldval, newval)->{
			if(!newval){
				propertyTable.getSelectionModel().clearSelection();
			}
		});
		
		if(enableLayers){
			ComboBox<Optional<Integer>> layerSelect = new ComboBox<>();
			
			layerSelect.setCellFactory(lv->new LayerCell(editor));
			layerSelect.setButtonCell(new LayerCell(editor));
			layerSelect.setItems(layers);
			
			layers.add(Optional.empty());
			
			for(int i : this.model.layer_getLayerIDs()){
				layers.add(Optional.of(i));
			}
			
			layerSelect.valueProperty().addListener((o, oldVal, newVal)->{
				if(newVal != null && newVal.isPresent()){
					try {
						//A lot of work just to capitalize the layer!
						String name = this.model.layer_getName(newVal.get())+" Properties";
						Character c = name.charAt(0);
						titleText.setText(Character.toUpperCase(c)+ name.substring(1));
						
					} catch (Exception e1) {
						editor.sendError(e1);
					}
				} else {
					titleText.setText("Node Properties");
				}
				try{
					updateProperties(newVal);
				}
				catch(Exception e){
					editor.sendError(e);
				}
			});
			
			HBox lsBox = new HBox();
			lsBox.setAlignment(Pos.CENTER);
			HBox.setHgrow(lsBox, Priority.ALWAYS);
			lsBox.getChildren().add(layerSelect);
			
			Label ll = new Label("View a Layer:");
			HBox.setHgrow(ll, Priority.ALWAYS);
			
			HBox layerBox = new HBox();
			layerBox.setAlignment(Pos.CENTER_LEFT);
			layerBox.setPrefWidth(1000);
			layerBox.getChildren().addAll(ll, lsBox);
			addToLeftMenu(layerBox);
		}
		
		
		Button newProp = new Button("New Property");
		HBox.setHgrow(newProp, Priority.ALWAYS);

		HBox buttonBox = new HBox();
		
		newProp.setOnMouseClicked(event -> 
			editor.createProperty()
		);
		
		if(enableLayers){
			Button newLayer = new Button("New Layer");
			
			HBox lbox = new HBox(); //Box for padding button
			HBox.setHgrow(lbox, Priority.ALWAYS);
			lbox.setAlignment(Pos.BOTTOM_LEFT);
			lbox.getChildren().add(newLayer);

			buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
			buttonBox.getChildren().addAll(lbox, newProp);
			newLayer.setOnMouseClicked(event->
				editor.createLayer()
			);
		}
		else {
			buttonBox.getChildren().add(newProp);
		}

		addAllToLeftMenu(propertyTable,
				     buttonBox);
		
		VBox messageBox = new VBox();
		messageBox.setMinHeight(80);
		
		ScrollPane messagePane = new ScrollPane();
		messagePane.setContent(messageBox);
		messagePane.setMaxHeight(200);
		messagePane.setMinHeight(messageBox.getMinHeight()+10);
		messagePane.setFitToWidth(true);
		messagePane.setPadding(new Insets(5));
		messagePane.setHbarPolicy(ScrollBarPolicy.NEVER);
		

		leftMenu.add(new Label("Messages:"), 0, 7);
		leftMenu.add(messagePane, 0, 8);
		
		this.setLeft(leftMenu);
		
		scene.getStylesheets().add(UI_Main.class.getResource("ui_style.css").toExternalForm());
		leftMenu.getStyleClass().add("menu");
		this.getStyleClass().add("main");
		messagePane.getStyleClass().add("messages");
		messageBox.getStyleClass().add("message-text");
		
		editor.advancePageProperty().addListener(event->
			setCenter(editor)
		);
		
		editor.finishedProperty().addListener((o, oldval, newval) -> {
			try {
				updateAll();
			} catch (Exception e) {
				editor.sendError(e);
			}
		});
		
		/*
		 * Get the messages in the editor, convert them to UI Text, and then
		 * insert them into the messages box.  If messages were removed,
		 * it simply deletes all the text elements and gets them again
		 */
		editor.messagesProperty().addListener(new ListChangeListener<UI_Message>(){
			@Override
			public void onChanged(Change<? extends UI_Message> c) {
				Text t;
				double w = messagePane.getWidth()-20;
				c.next();
				if(c.getRemovedSize() > 0){
					messageBox.getChildren().clear();
					for(UI_Message m : editor.messagesProperty()){
						t = m.getMessageUI();
						t.setWrappingWidth(w);
						messageBox.getChildren().add(t);
						System.out.println(m.getText());
					}
				} 
				else {
					for(UI_Message m : c.getAddedSubList()){
						t = m.getMessageUI();
						t.setWrappingWidth(w);
						messageBox.getChildren().add(t);
						System.out.println(m.getText());
					}
				}
			}
		});
		
		
		if(enableToolbar){
			//Add a toolbar to the window
			MenuBar topMenu = new MenuBar();
			setTop(topMenu);
			
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
					stage.close();
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
						model.save(expName.get());
						editor().sendInfo("The experiment was saved as "+expName.get());
					}
					catch (Exception e){
						editor().sendError(e);
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
						model.load(expName.get());
						editor.sendInfo("The experiment was loaded as "+expName.get());
						updateAll();
						pathogensView.getItems().setAll(model.pathogen_getPathogenIDs());
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
			} );
		} //Endif for toolbar
		
		
		rightMenu = null;
		if(enableEdges || enablePathogens){
			rightMenu = new GridPane();
			
			rightMenu.getStyleClass().addAll(leftMenu.getStyleClass());
			rightMenu.getColumnConstraints().addAll(leftMenu.getColumnConstraints());
			
			rightMenu.setPrefWidth(leftMenu.getPrefWidth());
			rightMenu.setMaxWidth(leftMenu.getMaxWidth());
			rightMenu.setHgap(leftMenu.getHgap());
			rightMenu.setVgap(leftMenu.getVgap());
			rightMenu.setPadding(leftMenu.getPadding());
			
			stage.setWidth(getWidth()+rightMenu.getPrefWidth());
		}
		setRight(rightMenu);
		
		if(enablePathogens){
			pathogensView = new ListView<>();
			editor.finishedProperty().addListener( (o, oldval, newval)->{
				try {
					pathogensView.getItems().setAll(
							model.pathogen_getPathogenIDs());
				} catch (Exception e) {
					editor.sendError(e);
				}
			});
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
							setText(model().pathogen_getName(item));
						} catch (EditorException e) {
							editor().sendError(e);
							setText(">ERROR<");
						}
					}
				};
			});
			Text pathText = new Text("Pathogens");
			pathText.setFont(headFont);
			addAllToRightMenu(pathText, pathogensView);
		} 
		//End if for pathogens
		
		if(enableEdges){
			edgesView = new ListView<>();
			edgesView.setPrefHeight(150);
			editor.finishedProperty().addListener(event->{
				edgesView.getItems().setAll(model.layer_getLayerIDs());
			});
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
		}
		
		stage.setScene(scene);
		try {
			updateAll();
		} catch (EditorException e1) {
			editor.sendError(e1);
		}
	}
	/**
	 * Update the properties list to show the properties of a given layer, 
	 * or the properties in the main object.
	 * @param lid The {@link LayerID} of the layer to get properties from,
	 * or null to read from the main list of properties
	 * @throws EditorException Thrown if the layer ID is invalid.
	 */
	private void updateProperties(Optional<Integer> lid) throws EditorException{
		properties.clear();
		if(lid != null && lid.isPresent()){
			for(int i : this.model.nodeProp_getPropertyIDs(lid.get())){
				properties.add(new PropertyID(lid.get(), i));
			}
		}
		else {
			for(int i : this.model.nodeProp_getPropertyIDs()){
				properties.add(new PropertyID(i));
			}
		}
	}
	/**
	 * Update the properties and layers of the window to reflect the model.
	 * This is used when the model has changed, such as when loading a new experiment.
	 * @throws EditorException Thrown if there was some problem while retrieving data.
	 */
	public void updateAll() throws EditorException{
		updateProperties(null);
		layers.clear();
		layers.add(Optional.empty());
		for(int i : model.layer_getLayerIDs()){
			layers.add(Optional.of(i));
		}
	}
	
	/**
	 * Add a node to the left hand menu of the window.
	 * The menu is organized from top to bottom in the order 
	 * the nodes are added.
	 * @param n The Node to add to the menu.
	 */
	public void addToLeftMenu(Node n){
		leftMenu.add(n, 0, leftMenuRow++);
	}
	/**
	 * {@link EditorWindow#addToLeftMenu(Node)}, but for a collection of nodes.
	 * The nodes will be added to the menu in the order they're given.
	 * @param nodes The nodes to add, from top to bottom, in the order they're given
	 */
	public void addAllToLeftMenu(Node... nodes){
		for(Node n : nodes){
			addToLeftMenu(n);
		}
	}
	
	private void addToRightMenu(Node n){
		if(rightMenu == null){
			throw new IllegalStateException(
					"Tried to add items to a disabled right menu!");
		}
		rightMenu.add(n, 0, rightMenuRow++);
	}

	private void addAllToRightMenu(Node... nodes){
		for(Node n : nodes){
			addToRightMenu(n);
		}
	}

	public void show(){
		stage.show();
	}
	
	//Getters
	public ListView<Integer> pathogensView(){
		return pathogensView;
	}
	public ListView<Integer> edgesView(){
		return edgesView;
	}
	public Stage stage(){
		return stage;
	}
	public PropertiesEditor model(){
		return model;
	}
	public EditorPage editor(){
		return editor;
	}
	
	/**
	 * Open a menu that asks if the user wants to quit.
	 * Return true if they accept, otherwise false.
	 * @return If the program should exit.
	 */
	private boolean shouldQuit(){
		Optional<ButtonType> input = quitAlert.showAndWait();
		return input.isPresent() && input.get() == ButtonType.OK;
	}
	
}
