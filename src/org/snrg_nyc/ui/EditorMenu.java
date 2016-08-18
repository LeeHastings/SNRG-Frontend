package org.snrg_nyc.ui;

import java.util.Optional;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.ui.components.LayerCell;
import org.snrg_nyc.ui.components.PropertyNameFactory;
import org.snrg_nyc.ui.components.PropertyTypeFactory;
import org.snrg_nyc.ui.components.UI_Message;
import org.snrg_nyc.util.PropertyID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A menu that interacts with an {@link EditorPage} and sends it
 * commands.
 * @author Devin Hastings
 *
 */
public class EditorMenu extends GridPane {
	private ObservableList<Optional<Integer>> layers = 
			FXCollections.observableArrayList();

	private BooleanProperty updated = new SimpleBooleanProperty(true);

	private ObservableList<PropertyID> properties = 
			FXCollections.observableArrayList();
	
	private Optional<Integer> currentLayer;
	
	private EditorPage editor;
	private int row = 0;
	private String baseTitle;
	
	private Text titleText;
	
	static final Font titleFont = Font.font("sans", FontWeight.LIGHT, 
			FontPosture.REGULAR, 20);
	
	private HBox layerBox;
	private Button newLayer;
	
	@SuppressWarnings("unchecked")
	public 
	EditorMenu(EditorPage editor, String title){
		//Basic setup
		super();
		baseTitle = title;
		this.editor = editor;
		setVgap(10);
		setPadding(new Insets(10));
		setMaxWidth(300);
		setPrefWidth(300);
		
		//One full-width column
		ColumnConstraints menuCol = new ColumnConstraints();
		menuCol.setPercentWidth(100);
		getColumnConstraints().add(menuCol);
		
		//Properties table
		titleText = new Text(title);
		titleText.setFont(titleFont);
		addItem(titleText);
		
		TableView<PropertyID> propertyTable = new TableView<>();
		propertyTable.setItems(properties);
		TableColumn<PropertyID, String> nameCol = new TableColumn<>("Name");
		TableColumn<PropertyID, String> typeCol = new TableColumn<>("Type");
		TableColumn<PropertyID, String> depCol = 
				new TableColumn<>("Dependency Level");
		
		nameCol.setCellValueFactory(new PropertyNameFactory(editor));
		typeCol.setCellValueFactory(new PropertyTypeFactory(editor));
		
		depCol.setCellValueFactory(col->{
			PropertyID id = col.getValue();
			try{
				if(id.usesLayer()){
					return new SimpleStringProperty(""+
							editor.model().nodeProp_getDependencyLevel(
									id.lid(), id.pid()) );
				}
				return new SimpleStringProperty(""+
						editor.model().nodeProp_getDependencyLevel(id.pid()));
			} 
			catch(Exception e){
				editor.sendError(e);
				return new SimpleStringProperty(">ERROR<");
			}
		});
		
		propertyTable.setColumnResizePolicy(
				TableView.CONSTRAINED_RESIZE_POLICY);
		propertyTable.setPrefHeight(200);
		propertyTable.getColumns().addAll(nameCol, typeCol, depCol);
		
		//Listen to if there is a selection
		propertyTable.getSelectionModel().selectedIndexProperty()
					.addListener((o, oldVal, newVal)->
		{
			if(newVal.intValue() != -1){
				PropertyID pid = propertyTable.getItems().get(
						newVal.intValue());
				editor.viewProperty(pid);
			}
		});
		
		propertyTable.focusedProperty().addListener((o, oldval, newval)->{
			if(!newval){
				propertyTable.getSelectionModel().clearSelection();
			}
		});
		
		currentLayer = Optional.empty();
		
		
		ComboBox<Optional<Integer>> layerSelect = new ComboBox<>();
		
		layerSelect.setCellFactory(lv->new LayerCell(editor));
		layerSelect.setButtonCell(new LayerCell(editor));
		layerSelect.setItems(layers);
		layers.add(Optional.empty());
		
		for(int i : editor.model().layer_getLayerIDs()){
			layers.add(Optional.of(i));
		}
		layerSelect.getSelectionModel().selectFirst();
		
		layerSelect.valueProperty().addListener((o, oldVal, newVal)->{
			if(newVal != null){
				currentLayer = newVal;
			}
			if(newVal != null && newVal.isPresent()){
				try {
					//A lot of work just to capitalize the layer!
					String name = editor.model().layer_getName(
							newVal.get())+" Properties";
					Character c = name.charAt(0);
					titleText.setText(Character.toUpperCase(c)+
							name.substring(1));
					
				} catch (Exception e1) {
					editor.sendError(e1);
				}
			} else {
				titleText.setText(baseTitle);
			}
			try{
				if(layers.contains(currentLayer)){
					updateProperties(currentLayer);
				}
				else {
					updateProperties(Optional.empty());
				}
			}
			catch(Exception e){
				editor.sendError(e);
			}
		});
		
		updated.addListener((o, oldval, newval)->{
			if(newval){
				if(currentLayer == null){
					editor.sendError(new Exception("Invalid null layer"));
				}
				layerSelect.setValue(currentLayer);
			}
		});
		
		HBox lsBox = new HBox();
		lsBox.setAlignment(Pos.CENTER);
		HBox.setHgrow(lsBox, Priority.ALWAYS);
		lsBox.getChildren().add(layerSelect);
		
		Label ll = new Label("View a Layer:");
		HBox.setHgrow(ll, Priority.ALWAYS);
		
		layerBox = new HBox();
		layerBox.setAlignment(Pos.CENTER_LEFT);
		layerBox.setPrefWidth(1000);
		layerBox.getChildren().addAll(ll, lsBox);
		addAllItems(layerBox);
		
		Button newProp = new Button("New Property");
		HBox.setHgrow(newProp, Priority.ALWAYS);

		HBox buttonBox = new HBox();
		
		newProp.setOnMouseClicked(event -> 
			editor.createProperty(currentLayer)
		);
		
		newLayer = new Button("New Layer");
		
		HBox lbox = new HBox(); //Box for padding button
		HBox.setHgrow(lbox, Priority.ALWAYS);
		lbox.setAlignment(Pos.BOTTOM_LEFT);
		lbox.getChildren().add(newLayer);

		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		newLayer.setOnMouseClicked(event->
			editor.createLayer()
		);
		buttonBox.getChildren().addAll(lbox, newProp);

		addAllItems(propertyTable, buttonBox);
		
		VBox messageBox = new VBox();
		messageBox.setMinHeight(80);
		
		ScrollPane messagePane = new ScrollPane();
		messagePane.setContent(messageBox);
		messagePane.setMaxHeight(100);
		messagePane.setMinHeight(messageBox.getMinHeight()+10);
		messagePane.setFitToWidth(true);
		messagePane.setPadding(new Insets(5));
		messagePane.setHbarPolicy(ScrollBarPolicy.NEVER);
		
		messageBox.heightProperty().addListener((prop, oldval, newval)->{
			messagePane.setVvalue((Double) newval);
		});
		
		Button messageClear = new Button("Clear Messages");
		messageClear.setOnMouseClicked(event-> 
				editor.messagesProperty().clear());

		addAllItems(new Label("Messages:"), messagePane, messageClear);
		
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
		editor.messagesProperty().addListener(
			new ListChangeListener<UI_Message>(){
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
		this.getStyleClass().add("menu");
		messagePane.getStyleClass().add("messages");
		messageBox.getStyleClass().add("message-text");
		
		try {
			updateAll();
		} catch (EditorException e) {
			editor.sendError(e);
		}
	}
	public void 
	addItem(Node n){
		add(n, 0, row++);
	}
	public void 
	addAllItems(Node ... nodes){
		for(Node n : nodes){
			addItem(n);
		}
	}
	/**
	 * Update the properties list to show the properties of a given layer, 
	 * or the properties in the main object.
	 * @param lid The {@link Optional} integer of the layer ID,
	 * empty to just update to node properties
	 * @throws EditorException Thrown if the layer ID is invalid.
	 */
	private void 
	updateProperties(Optional<Integer> lid) throws EditorException{
		if(lid == null){
			throw new IllegalArgumentException("The layer ID cannot be null");
		}
		properties.clear();
		if(lid.isPresent()){
			for(int i : editor.model().nodeProp_getPropertyIDs(lid.get())){
				properties.add(new PropertyID(lid.get(), i));
			}
		}
		else {
			for(int i : editor.model().nodeProp_getPropertyIDs()){
				properties.add(new PropertyID(i));
			}
		}
	}
	/**
	 * Update the properties and layers of the window to reflect the model.
	 * This is used when the model has changed, such as when loading a new 
	 * experiment.
	 * @throws EditorException Thrown if there was some problem while 
	 * retrieving data.
	 */
	public void 
	updateAll() throws EditorException{
		updated.set(false);
		//Disable layer selection options if the model does not support them
		layerBox.setDisable(!editor.model().allowsLayers());
		newLayer.setDisable(!editor.model().allowsLayers());
		
		layers.clear();
		layers.add(Optional.empty());
		for(int i : editor.model().layer_getLayerIDs()){
			layers.add(Optional.of(i));
		}
		if(!layers.contains(currentLayer)){
			currentLayer = Optional.empty();
		}
		updateProperties(currentLayer);
		updated.set(true);
	}
	
	public void
	setTitle(String title){
		baseTitle = title;
		if(!currentLayer.isPresent()){
			titleText.setText(baseTitle);
		}
	}

}
