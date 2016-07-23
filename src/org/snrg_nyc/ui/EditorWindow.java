package org.snrg_nyc.ui;

import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.ui.components.LayerCell;
import org.snrg_nyc.ui.components.LayerID;
import org.snrg_nyc.ui.components.PropertyID;
import org.snrg_nyc.ui.components.PropertyNameFactory;
import org.snrg_nyc.ui.components.PropertyTypeFactory;
import org.snrg_nyc.ui.components.UI_Message;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
 * The class is a border pane, with the left side listing properties and layers, 
 * and the center containing an {@link EditorPage} object.
 * Feel free to use the top, right, and bottom for extra content.
 * 
 * @author Devin Hastings
 */
public class EditorWindow extends BorderPane {
	private PropertiesEditor model;
	private Stage stage;
	private Scene scene;
	
	/**
	 * A single column {@link GridPane} (which provides more flexibility
	 *  than an {@link HBox}), which contains a list of the properties and
	 *  a box for messages from the editor.
	 */
	private GridPane leftMenu;
	
	private int menuRow = 0;
	
	private EditorPage editor;
	
	private ObservableList<PropertyID> properties = FXCollections.observableArrayList();
	private ObservableList<LayerID> layers = FXCollections.observableArrayList();
	
	/**
	 * Create a new window that edits the model in the given {@link PropertiesEditor}.
	 * @param model The {@link PropertiesEditor} to interface with.
	 * @param initStage The javaFX {@link Stage} to show the window on.
	 * @param title The title of the new window.
	 */
	@SuppressWarnings("unchecked")
	public EditorWindow(PropertiesEditor model, Stage initStage, String title){
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
		titleText.setFont(Font.font("sans", FontWeight.LIGHT, FontPosture.REGULAR, 20));
		
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
		
		ComboBox<LayerID> layerSelect = new ComboBox<>();
		
		layerSelect.setCellFactory(lv->new LayerCell(editor));
		layerSelect.setButtonCell(new LayerCell(editor));
		layerSelect.setItems(layers);
		
		layers.add(new LayerID());
		
		for(int i : this.model.layer_getLayerIDs()){
			layers.add(new LayerID(i));
		}
		
		layerSelect.valueProperty().addListener((o, oldVal, newVal)->{
			if(newVal != null && newVal.used()){
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
		
		Button newProp = new Button("New Property");
		HBox.setHgrow(newProp, Priority.ALWAYS);
		
		Button newLayer = new Button("New Layer");
		
		HBox lbox = new HBox(); //Box for padding button
		HBox.setHgrow(lbox, Priority.ALWAYS);
		lbox.setAlignment(Pos.BOTTOM_LEFT);
		lbox.getChildren().add(newLayer);

		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		buttonBox.getChildren().addAll(lbox, newProp);
		
		newProp.setOnMouseClicked(event -> 
			editor.createProperty()
		);
		newLayer.setOnMouseClicked(event->
			editor.createLayer()
		);
		
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

		addAllToMenu(titleText,
				     layerBox,
				     propertyTable,
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
		

		leftMenu.add(new Label("Messages:"), 0, 6);
		leftMenu.add(messagePane, 0, 7);
		
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
		
		stage.setScene(scene);
		try {
			updateProperties(null);
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
	private void updateProperties(LayerID lid) throws EditorException{
		properties.clear();
		if(lid != null && lid.used()){
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
		layers.add(new LayerID());
		for(int i : model.layer_getLayerIDs()){
			layers.add(new LayerID(i));
		}
	}
	
	/**
	 * Add a node to the left hand menu of the window.
	 * The menu is organized from top to bottom in the order 
	 * the nodes are added.
	 * @param n The Node to add to the menu.
	 */
	public void addToMenu(Node n){
		leftMenu.add(n, 0, menuRow++);
	}
	/**
	 * {@link EditorWindow#addToMenu(Node)}, but for a collection of nodes.
	 * The nodes will be added to the menu in the order they're given.
	 * @param nodes The nodes to add, from top to bottom, in the order they're given
	 */
	public void addAllToMenu(Node... nodes){
		for(Node n : nodes){
			addToMenu(n);
		}
	}

	public void show(){
		stage.show();
	}
	
	//Getters
	public Stage stage(){
		return stage;
	}
	public Scene scene(){
		return scene;
	}
	public PropertiesEditor model(){
		return model;
	}
	public EditorPage editor(){
		return editor;
	}
}
