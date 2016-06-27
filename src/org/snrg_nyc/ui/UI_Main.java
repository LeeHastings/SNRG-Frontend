package org.snrg_nyc.ui;

import java.util.Optional;

import org.snrg_nyc.model.UIException;
import org.snrg_nyc.model.UI_Interface;
import org.snrg_nyc.model.UI_InterfaceFactory;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.beans.property.*;


public class UI_Main extends Application{
	UI_Interface ui;
	Stage stage;
	Scene scene;
	Alert quitAlert;
	String experimentName = null;
	ObservableList<PropertyID> properties = FXCollections.observableArrayList();
	ObservableList<LayerID> layers = FXCollections.observableArrayList();

	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage initStage){
		ui = new UI_InterfaceFactory().build();
		EditorPage editor = new EditorPage(ui);
		editor.setPrefWidth(550);
		
		stage = initStage;
		stage.setTitle("Node Settings Editor");
		
		BorderPane mainPane = new BorderPane();
		scene = new Scene(mainPane, 900, 600);
		
		GridPane leftMenu = new GridPane();
		ColumnConstraints menuCol = new ColumnConstraints();
		menuCol.setPercentWidth(100);
		leftMenu.getColumnConstraints().add(menuCol);
		

		MenuBar topMenu = new MenuBar();
		mainPane.setTop(topMenu);
		
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
		
		quit.setOnAction(event-> quit());
		
		TextInputDialog saveDialog = new TextInputDialog();
		saveDialog.setTitle("Save Experiment");
		saveDialog.setHeaderText("Name the experiment");
		saveDialog.setGraphic(null);
		
		save.setOnAction(event->{
			saveDialog.getEditor().setText(experimentName);
			Optional<String> expName = saveDialog.showAndWait();
			if(expName.isPresent()){
				try{
					ui.save(expName.get());
					editor.sendInfo("The experiment was saved as "+expName.get());
				}
				catch (Exception e){
					editor.sendError(e);
				}
			}
		});
		
		ChoiceDialog<String> loadDialog = new ChoiceDialog<>();
		loadDialog.setTitle("Load Experiment");
		loadDialog.setHeaderText("Select an experiment to load");
		loadDialog.setGraphic(null);
		
		load.setOnAction(event->{
			loadDialog.getItems().setAll(ui.getExperimentNames());
			Optional<String> expName = loadDialog.showAndWait();
			try{
				if(expName.isPresent() ){
					experimentName = expName.get();
					ui.load(expName.get());
					editor.sendInfo("The experiment was loaded as "+expName.get());
					updateProperties(null);
					layers.clear();
					layers.add(new LayerID());
					for(int i : ui.layer_getLayerIDs()){
						layers.add(new LayerID(i));
					}
				}
			}
			catch (Exception e){
				editor.sendError(e);
			}
		});
		
		leftMenu.setVgap(10);
		leftMenu.setPadding(new Insets(10));
		leftMenu.setMaxWidth(300);
		leftMenu.setPrefWidth(300);
		
		Text title = new Text("Node Properties");
		title.setFont(Font.font("sans", FontWeight.LIGHT, FontPosture.REGULAR, 20));
		
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
				return new SimpleStringProperty(""+ui.nodeProp_getDependencyLevel(id.pid()));
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
		
		for(int i : ui.layer_getLayerIDs()){
			layers.add(new LayerID(i));
		}
		
		layerSelect.valueProperty().addListener((o, oldVal, newVal)->{
			if(newVal != null && newVal.used()){
				try {
					//A lot of work just to capitalize the layer!
					String name = ui.layer_getName(newVal.get())+" Properties";
					Character c = name.charAt(0);
					title.setText(Character.toUpperCase(c)+ name.substring(1));
					
				} catch (Exception e1) {
					editor.sendError(e1);
				}
			} else {
				title.setText("Node Properties");
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
					int lid = ui.layer_new(newVal);
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

		leftMenu.add(title,      0, 0);
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
		
		mainPane.setLeft(leftMenu);
		
		scene.getStylesheets().add(UI_Main.class.getResource("ui_style.css").toExternalForm());
		leftMenu.getStyleClass().add("menu");
		mainPane.getStyleClass().add("main");
		messagePane.getStyleClass().add("messages");
		messageBox.getStyleClass().add("message-text");
		
		editor.advancePage.addListener(e->{
			mainPane.setCenter(editor);
		});
		
		editor.finished.addListener(e -> {
			if(editor.finished.get()){ 
				try{
					ui.scratch_commitToNodeProperties();
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
		
		stage.setOnCloseRequest(event-> quit() );
		
		stage.setScene(scene);
		stage.show();
	}
	
	void updateProperties(LayerID lid) throws UIException{
		properties.clear();
		if(lid != null && lid.used()){
			for(int i : ui.nodeProp_getPropertyIDs(lid.get())){
				properties.add(new PropertyID(lid.get(), i));
			}
		}
		else {
			for(int i : ui.nodeProp_getPropertyIDs()){
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
	
	public static void main(String[] args){
		launch(args);
	}
}
