package org.snrg_nyc.ui;

import java.util.Optional;

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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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

public class EditorWindow extends BorderPane {
	PropertiesEditor model;
	Stage stage;
	Scene scene;
	Alert quitAlert;
	
	EditorPage editor;
	
	ObservableList<PropertyID> properties = FXCollections.observableArrayList();
	ObservableList<LayerID> layers = FXCollections.observableArrayList();
	
	@SuppressWarnings("unchecked")
	public EditorWindow(PropertiesEditor model, Stage initStage, String title){
		this.model = model;
		stage = initStage;
		stage.setTitle(title);
		editor = new EditorPage(this.model);
		editor.setPrefWidth(500);
		
		scene = new Scene(this, 900, 600);
		
		GridPane leftMenu = new GridPane();
		ColumnConstraints menuCol = new ColumnConstraints();
		menuCol.setPercentWidth(100);
		leftMenu.getColumnConstraints().add(menuCol);
		
		leftMenu.setVgap(10);
		leftMenu.setPadding(new Insets(10));
		leftMenu.setMaxWidth(300);
		leftMenu.setPrefWidth(300);
		
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
		
		editor.layerName.addListener((o, oldVal, newVal)->{
			if(newVal!= null && !newVal.equals("")){
				try {
					int lid = this.model.layer_new(newVal);
					layers.add(new LayerID(lid));
				} catch (Exception e1) {
					editor.sendError(e1);
				}
				
			}
		});
		
		Button newProp = new Button("New Property");
		HBox.setHgrow(newProp, Priority.ALWAYS);
		
		Button newLayer = new Button("New Layer");
		
		HBox lbox = new HBox();
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

		leftMenu.add(titleText,      0, 0);
		leftMenu.add(layerBox,   0, 1);
		leftMenu.add(propertyTable, 0, 2);
		leftMenu.add(buttonBox,  0, 3);
		
		leftMenu.add(new Label("Messages:"), 0, 4);
		
		VBox messageBox = new VBox();
		messageBox.setMinHeight(80);
		
		ScrollPane messagePane = new ScrollPane();
		messagePane.setContent(messageBox);
		messagePane.setMaxHeight(200);
		messagePane.setMinHeight(messageBox.getMinHeight()+10);
		messagePane.setFitToWidth(true);
		messagePane.setPadding(new Insets(5));
		messagePane.setHbarPolicy(ScrollBarPolicy.NEVER);
		
		leftMenu.add(messagePane, 0, 5);
		
		this.setLeft(leftMenu);
		
		scene.getStylesheets().add(UI_Main.class.getResource("ui_style.css").toExternalForm());
		leftMenu.getStyleClass().add("menu");
		this.getStyleClass().add("main");
		messagePane.getStyleClass().add("messages");
		messageBox.getStyleClass().add("message-text");
		
		editor.advancePage.addListener(e->{
			this.setCenter(editor);
		});
		
		editor.finished.addListener((o, oldval, newval) -> {
			if(newval){ 
				try{
					this.model.scratch_commitToNodeProperties();
					updateProperties(layerSelect.getValue());
				}
				catch (Exception e1){
					editor.sendError(e1);
				}
			}
		});
		
		editor.messages.addListener(new ListChangeListener<UI_Message>(){
			@Override
			public void onChanged(Change<? extends UI_Message> c) {
				Text t;
				double w = messagePane.getWidth()-20;
				c.next();
				if(c.getRemovedSize() > 0){
					messageBox.getChildren().clear();
					for(UI_Message m : editor.messages){
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
	}
	void show(){
		stage.show();
	}
	void updateProperties(LayerID lid) throws EditorException{
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
	
	void quit(){
		Optional<ButtonType> input = quitAlert.showAndWait();
		if(input.isPresent() && input.get() == ButtonType.OK){
			stage.close();
		}
	}
	public Stage getStage(){
		return stage;
	}
	public PropertiesEditor getModel(){
		return model;
	}
	public void sendError(Exception e){
		editor.sendError(e);
	}
	public void sendInfo(String s){
		editor.sendInfo(s);
	}
	public ObservableList<LayerID> getLayers() {
		return layers;
	}
}
