package org.snrg_nyc.ui;

import org.snrg_nyc.model.UI_Interface;
import org.snrg_nyc.model.UI_InterfaceFactory;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;


public class UI_Main extends Application{
	UI_Interface ui;
	Stage stage;
	Scene scene;
	EditorPage editor;
	TableView<SimplePropertyReader> properties;

	@Override
	public void start(Stage initStage){
		ui = new UI_InterfaceFactory().build();
		editor = new EditorPage(ui);
		
		stage = initStage;
		
		BorderPane mainPane = new BorderPane();
		scene = new Scene(mainPane, 800, 600);
		
		GridPane leftMenu = new GridPane();
		ColumnConstraints menuCol = new ColumnConstraints();
		menuCol.setPercentWidth(100);
		leftMenu.getColumnConstraints().add(menuCol);
		
		ToolBar topMenu = new ToolBar();
		topMenu.setPrefHeight(30);
		topMenu.setPadding(new Insets(0, 10, 0, 10));
		mainPane.setTop(topMenu);
		
		Button save = new Button("Save");
		save.setOnMouseClicked(event->{
			ui.save("test");
			editor.sendInfo("Sent info as 'test'");
		});
		
		topMenu.getItems().add(save);
		
		leftMenu.setVgap(10);
		leftMenu.setPadding(new Insets(10));
		leftMenu.setMaxWidth(280);
		leftMenu.setPrefWidth(280);
		
		Text title = new Text("Node Settings");
		title.setFont(Font.font("sans", FontWeight.LIGHT, FontPosture.REGULAR, 20));
		leftMenu.add(title, 0, 0);
		
		Button newPropButton = new Button("New Property");
		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		buttonBox.getChildren().add(newPropButton);
		leftMenu.add(buttonBox, 0, 3);
		
		properties = new TableView<>();
		TableColumn<SimplePropertyReader, String> nameCol = new TableColumn<>("Name");
		TableColumn<SimplePropertyReader, String> typeCol = new TableColumn<>("Type");
		TableColumn<SimplePropertyReader, Integer> depCol = new TableColumn<>("Dependency Level");
		
		nameCol.setCellValueFactory(new PropertyValueFactory<SimplePropertyReader, String>("name"));
		typeCol.setCellValueFactory(new PropertyValueFactory<SimplePropertyReader, String>("simpleType"));
		
		depCol.setCellValueFactory(new PropertyValueFactory<SimplePropertyReader, Integer>("dependencyLevel"));
		
		properties.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		properties.setPrefHeight(200);
		
		properties.getColumns().addAll(nameCol, typeCol, depCol);
		
		leftMenu.add(properties, 0, 2);
		
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
		
		newPropButton.setOnAction((event) -> {
			editor.startEditing();
		});
		
		editor.advancePage.addListener(e->{
			mainPane.setCenter(editor.page);
		});
		
		editor.finished.addListener(e -> {
			if(editor.finished.get()){ 
				try{
					int pid = ui.scratch_commitToNodeProperties();
					properties.getItems().add(new SimplePropertyReader(ui, pid));
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
				double w = messagePane.getWidth()-10;
				c.next();
				if(c.getRemovedSize() > 0){
					messageBox.getChildren().clear();
					for(UI_Message m : editor.messages){
						t = m.getMessageUI();
						t.setWrappingWidth(w);
						messageBox.getChildren().add(t);
					}
				} 
				else {
					System.out.println("Adding to list!");
					for(UI_Message m : c.getAddedSubList()){
						t = m.getMessageUI();
						t.setWrappingWidth(w);
						messageBox.getChildren().add(t);
					}
				}
			}
		});
		
		stage.setScene(scene);
		stage.show();
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
