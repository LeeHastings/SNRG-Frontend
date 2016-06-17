package org.snrg_nyc.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.snrg_nyc.model.UIException;
import org.snrg_nyc.model.UI_Interface;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;
import javafx.scene.text.*;
import javafx.util.StringConverter;

public class EditorPage {

	private int pageNumber = 0;
	GridPane page = new GridPane();
	BooleanProperty advancePage = new SimpleBooleanProperty();
	BooleanProperty finished = new SimpleBooleanProperty();
	
	ListProperty<UI_Message> messages = new SimpleListProperty<UI_Message>();
	
	final UI_Interface ui;	
	
	final Font titleFont = Font.font("sans", FontWeight.LIGHT, FontPosture.REGULAR, 20);
	
	enum Mode{
		Editing,
		Viewing,
		Inactive
	}
	private Mode mode;
	
	public EditorPage(UI_Interface ui){
		this.ui = ui;
		mode = Mode.Inactive;

		page = new GridPane();
		page.setPrefWidth(800);
		page.setAlignment(Pos.TOP_LEFT);
		page.setVgap(10);
		page.setHgap(10);
		page.setPadding(new Insets(15));
		
		messages.set( FXCollections.observableArrayList( new ArrayList<>() ));
		
		advancePage.addListener((o, old, advance)->{
			if(advance){
				updatePage();
				advancePage.set(false);
			}
		});
		finished.addListener( (o, oldVal, newVal)->{
			if(newVal){
				pageNumber = 0;
				mode = Mode.Inactive;
				advancePage.set(true);
			}
		});
	}
	
	public void startEditing(){
		finished.set(false);
		pageNumber = 0;
		mode = Mode.Editing;
		advancePage.set(true);
	}
	
	public void sendError(Exception e){
		messages.add(new UI_Message("Error: "+e.getMessage(), UI_Message.Type.Error));
		e.printStackTrace();
	}
	
	public void sendWarning(String s){
		messages.add(new UI_Message(s, UI_Message.Type.Warning));
	}
	
	public void sendInfo(String s){
		messages.add(new UI_Message(s, UI_Message.Type.Info));
	}
	
	private void updatePage(){
		switch(mode){
		case Editing:
			updateEditorPage();
			break;
		default:
			page.getChildren().clear();
		}
	}
	@SuppressWarnings("unchecked")
	private void updateEditorPage(){
		page.getChildren().clear();
		Text title = new Text("New Node Property");
		title.setFont(titleFont);
		page.add(title, 0, 0, 4, 1);
		
		Line hbar = new Line();
		hbar.setStartX(20);
		hbar.setEndX(500);
		page.add(hbar, 0, 1, 5, 1);
		
		
		Button nextBtn = new Button("Next");
		nextBtn.setDisable(true);

		HBox nextBox = new HBox();
		nextBox.setAlignment(Pos.CENTER_RIGHT);
		nextBox.getChildren().add(nextBtn);
		page.add(nextBox, 4, 12);
		
		Button cancel = new Button("Cancel");
		page.add(cancel, 0, 12);
		
		cancel.setOnMouseClicked(event ->{
			ui.scratch_clear();
			pageNumber = 0;
			mode = Mode.Inactive;
			advancePage.set(true);
		});
		
		//Function to see if the next button should be enabled
		Runnable checkNext;
		
		if(pageNumber == 0){
			title.setText(title.getText()+" - Basic Properties");
			Label label = new Label("Name");
			page.add(label, 0, 2);
			
			TextField propName = new TextField();
			page.add(propName, 1, 2);
			
			label = new Label("Type");
			page.add(label, 0, 3);
			
			ComboBox<String> type = new ComboBox<>();
			type.getItems().addAll(ui.nodeProp_getTypes());
			page.add(type, 1, 3);
			
			label = new Label("Description");
			page.add(label, 0, 4);
			TextArea desc = new TextArea();
			desc.setPrefColumnCount(20);
			desc.setPrefRowCount(4);
			desc.setWrapText(true);
			
			page.add(desc, 1, 4, 1, 2);
			
			checkNext = () -> {
				nextBtn.setDisable(
						type.getValue() == null 
						|| propName.getText() == null
						|| propName.getText().equals("") 
						|| !ui.test_nodePropNameIsUnique(propName.getText())
						|| desc.getText() == null
						|| desc.getText().equals("")
				);
			};
			
			desc.textProperty().addListener(e -> checkNext.run() );
			type.setOnAction(e-> checkNext.run());
			propName.setOnAction(e->checkNext.run());
			nextBtn.setOnMouseClicked(event->{
				pageNumber ++;
				try {
					ui.scratch_new(propName.getText(), type.getValue(), desc.getText());
				} catch (Exception e1) {
					sendError(e1);
				}
				advancePage.set(true);
			});
		}
		
		else if(pageNumber == 1){
			String type = "None";
			try{
				type = ui.scratch_getType();
			} 
			catch(Exception e){
				sendError(e);
			}
			title.setText(title.getText()+" - "+type);
			
			CheckBox useUniform = new CheckBox("Use Uniform Distribution");

			Spinner<Integer> depLvl = new Spinner<>();
			depLvl.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100));
			
			if(!type.equals("FractionProperty")){
				page.add(useUniform, 0, 8, 3, 1);
				page.add(new Label("Dependency Level"), 0, 2);
				page.add(depLvl, 1, 2);
			}
			
			useUniform.setOnMouseClicked(event ->{
				if(useUniform.isSelected()){
					nextBtn.setText("Finish");
					depLvl.getValueFactory().setValue(0);
					depLvl.setDisable(true);
				}
				else {
					nextBtn.setText("Next");
					depLvl.setDisable(false);
				}
			});
			
			nextBtn.setOnMouseClicked(event->{
				try {
					if(useUniform.isSelected()){
						ui.scratch_useUniformDistribution();
						finished.set(true);
					}
					else {
						int dl = depLvl.getValue();
						ui.scratch_setDependencyLevel(dl);
						pageNumber = (dl > 0)? 2 : 4; //Skip dependencies and conditional distributions if the dependency level is 0
						advancePage.set(true);
					}
				} 
				catch (Exception e) {
					sendError(e);
				}
			});
			
			switch(type){
			case "EnumeratorProperty":
				ListView<Scratch_Range> values = new ListView<>();
				
				values.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
				values.setCellFactory(lv ->{
					TextFieldListCell<Scratch_Range> cell = new TextFieldListCell<>();
					cell.setConverter(new StringConverter<Scratch_Range>(){
						@Override
						public Scratch_Range fromString(String newVal) {
							Scratch_Range r = cell.getItem();
							try {
								r.setLabel(newVal);
							} catch (UIException e) {
								sendError(e);
							}
							return r;
						}
						@Override
						public String toString(Scratch_Range range) {
							try {
								return range.getLabel();
							} catch (UIException e) {
								sendError(e);
								return null;
							}
						}
					});
					return cell;
				});
				
				values.setPrefHeight(100);
				values.setPrefWidth(200);
				values.setEditable(true);

				Button add = new Button("Add Value");
				
				Button rmv = new Button("Remove Value");
				HBox rmvBox = new HBox();
				rmvBox.setAlignment(Pos.CENTER_RIGHT);
				rmvBox.getChildren().add(rmv);
				
				page.add(new Label("Enumerate Items"), 0, 3);
				page.add(values, 1, 3, 2, 3);
				
				page.add(add, 1, 6);
				page.add(rmvBox, 2, 6);
				
				checkNext = ()->{
					try {
						for(int i : ui.scratch_getRangeIDs()){
							if(ui.scratch_getRangeLabel(i).length() < 1){
								nextBtn.setDisable(true);
								return;
							}
						}
					} catch (Exception e) {
						sendError(e);
						nextBtn.setDisable(true);
					}
					nextBtn.setDisable(false);
				};
				
				values.setOnEditCommit(event->{
					checkNext.run();
				});
				
				add.setOnMouseClicked(event ->{
					try {
						int rid = ui.scratch_addRange();
						values.getItems().add(new Scratch_Range(ui, rid));
					} catch (Exception e) {
						sendError(e);
					}
				});
				
				rmv.setOnMouseClicked(event->{
					try{
						if(values.getSelectionModel().isEmpty()){
							sendWarning("Select a range to remove");
						}
						else {
							Scratch_Range r = values.getSelectionModel().getSelectedItem();
							int id = values.getSelectionModel().getSelectedIndex();
							r.delete();
							values.getItems().remove(id);
						}
					}
					catch(Exception e){
						sendError(e);
					}
				});
				
				
				
				break;
			case "IntegerRangeProperty":
				
				TableView<Scratch_Range> ranges = new TableView<>();
				TableColumn<Scratch_Range, String> labelCol = new TableColumn<>("Label");
				TableColumn<Scratch_Range, String> minCol = new TableColumn<>("Min");
				TableColumn<Scratch_Range, String> maxCol = new TableColumn<>("Max");
				ranges.setPrefHeight(200);
				ranges.setEditable(true);
				
				add = new Button("Add Range");
				rmv = new Button("Remove Range");
				rmvBox = new HBox();
				rmvBox.setAlignment(Pos.CENTER_RIGHT);
				rmvBox.getChildren().add(rmv);

				page.add(ranges, 1, 3, 2, 2);
				page.add(add, 1, 5);
				page.add(rmvBox, 2, 5);
				
				labelCol.setCellFactory(TextFieldTableCell.forTableColumn());
				minCol.setCellFactory(TextFieldTableCell.forTableColumn());
				maxCol.setCellFactory(TextFieldTableCell.forTableColumn());
				
				labelCol.setCellValueFactory((CellDataFeatures<Scratch_Range, String> data)->{
					try{
						return new SimpleStringProperty(data.getValue().getLabel());
					}
					catch (Exception e){
						sendError(e);
						return null;
					}
				});
				
				minCol.setCellValueFactory((CellDataFeatures<Scratch_Range, String> data) ->{
					try {
						return new SimpleStringProperty(data.getValue().getMin());
					} catch (Exception e) {
						sendError(e);
						return null;
					}
				});
				
				maxCol.setCellValueFactory((CellDataFeatures<Scratch_Range, String> data)->{
					try{
						return new SimpleStringProperty(data.getValue().getMax());
					}
					catch (Exception e){
						sendError(e);
						return null;
					}
				});
				
				ranges.getColumns().addAll(labelCol, minCol, maxCol);
				ranges.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
				
				labelCol.setEditable(true);
				minCol.setEditable(true);
				maxCol.setEditable(true);
				
				checkNext = ()->{
					try{
						for(int rid : ui.scratch_getRangeIDs()){
							if(!ui.scratch_rangeIsSet(rid)){
								nextBtn.setDisable(true);
								return;
							}
						}
						nextBtn.setDisable(false);
					}
					catch(Exception e){
						sendError(e);
					}
					
				};
				add.setOnMouseClicked(event->{
					try {
						int rid = ui.scratch_addRange();
						ranges.getItems().add(new Scratch_Range(ui, rid));
						ranges.refresh();
					} catch (Exception e) {
						sendError(e);
					}
				});
				
				rmv.setOnMouseClicked(event->{
					try {
						if(ranges.getSelectionModel().isEmpty()){
							sendWarning("Select a range to remove");
						}
						else {
							int id = ranges.getSelectionModel().getSelectedIndex();
							ranges.getItems().get(id).delete();
							ranges.getItems().remove(id);
						}
					}
					catch(Exception e){
						sendError(e);
					}
				});
				
				labelCol.setOnEditCommit((CellEditEvent<Scratch_Range, String> event)->{
					try {
						event.getRowValue().setLabel(event.getNewValue());
					} catch (Exception e) {
						sendError(e);
					}
					finally {
						checkNext.run();
					}
				});
				
				minCol.setOnEditCommit((CellEditEvent<Scratch_Range, String> event)->{
					try {
						if(event.getNewValue().equals("")){
							return;
						}
						else if(!event.getNewValue().matches("\\d+")){
							throw new NumberFormatException("Value '"+event.getNewValue()+"' is not an integer.");
						}
						else {
							int i = Integer.parseInt(event.getNewValue());
							event.getRowValue().setMin(i);
						}
					}
					catch (Exception e){
						sendError(e);
					}
					finally {
						checkNext.run();
					}
				});
				maxCol.setOnEditCommit((CellEditEvent<Scratch_Range, String> event)->{
					try {
						if(event.getNewValue().equals("")){
							return;
						}
						else if(!event.getNewValue().matches("\\d+")){
							throw new NumberFormatException("Value '"+event.getNewValue()+"' is not an integer.");
						}
						else {
							int i = Integer.parseInt(event.getNewValue());
							event.getRowValue().setMax(i);
						}
					}
					catch(Exception e){
						sendError(e);
					}
					finally {
						checkNext.run();
					}
				});
				
				break;
			case "BooleanProperty":
				nextBtn.setDisable(false);
				break;
			case "FractionProperty":
				HBox centering = new HBox();
				centering.setAlignment(Pos.CENTER);
				centering.setSpacing(10);
				
				TextField initVal = new TextField();
				nextBtn.setText("Finish");
				
				centering.getChildren().addAll(new Label("Value"), initVal);
				page.add(centering, 0, 4, 5, 3);
				
				initVal.textProperty().addListener((o, oldVal, newVal)->{
					if(!newVal.matches("\\d*\\.?\\d+")){
						nextBtn.setDisable(true);
						initVal.setText(newVal.replaceAll("[^0-9.]", ""));
					}
					else {
						nextBtn.setDisable(false);
					}
				});;
				
				nextBtn.setOnMouseClicked(e->{
					try{
						float f = Float.parseFloat(initVal.getText());
						ui.scratch_setFractionInitValue(f);
						finished.set(true);
					} 
					catch (Exception e1){
						sendError(e1);
					}
				});
				break;
			}
		}
		else if(pageNumber == 2){
			title.setText(title.getText() + " - Add Dependencies");
			TableView<SimplePropertyReader> potentialDependencies = new TableView<>();
			TableColumn<SimplePropertyReader, String> nameCol = new TableColumn<>("Name");
			TableColumn<SimplePropertyReader, String> depLvlCol = new TableColumn<>("Type");
			
			nameCol.setCellValueFactory(new PropertyValueFactory<SimplePropertyReader, String>("name"));
			depLvlCol.setCellValueFactory(new PropertyValueFactory<SimplePropertyReader, String>("simpleType"));
			
			TableView<SimplePropertyReader> scratchDependencies = new TableView<>();

			TableColumn<SimplePropertyReader, String> nameCol2 = new TableColumn<>("Name");
			TableColumn<SimplePropertyReader, String> depLvlCol2 = new TableColumn<>("Type");
			
			nameCol2.setCellValueFactory(new PropertyValueFactory<SimplePropertyReader, String>("name"));
			depLvlCol2.setCellValueFactory(new PropertyValueFactory<SimplePropertyReader, String>("simpleType"));
			
			
			potentialDependencies.getColumns().addAll(nameCol, depLvlCol);
			SimplePropertyReader spr;
			
			potentialDependencies.setPrefSize(180, 200);
			
			
			scratchDependencies.getColumns().addAll(nameCol2, depLvlCol2);
			
			scratchDependencies.setPrefWidth(potentialDependencies.getPrefWidth());
			scratchDependencies.setPrefHeight(potentialDependencies.getPrefHeight());
			
			scratchDependencies.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			potentialDependencies.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			
			try{
				for(int pid : ui.scratch_getPotentialDependencies()){
					spr = new SimplePropertyReader(ui, pid);
					potentialDependencies.getItems().add(spr);
				}
			}
			catch(Exception e){
				sendError(e);
			}
			
			HBox btnBox1 = new HBox();
			btnBox1.setAlignment(Pos.CENTER);
			HBox btnBox2 = new HBox();
			btnBox2.setAlignment(Pos.CENTER);
			
			Button addDep = new Button("Add >>");
			Button rmvDep = new Button("<< Remove");
			btnBox1.getChildren().add(addDep);
			btnBox2.getChildren().add(rmvDep);
			
			page.add(new Label("Available Dependencies"), 0, 2, 2, 1);
			page.add(new Label("Added Dependencies"), 3, 2, 2, 1);
			
			page.add(potentialDependencies, 0, 3, 2, 4);
			page.add(scratchDependencies, 3, 3, 2, 4);
			
			page.add(btnBox1, 2, 4);
			page.add(btnBox2, 2, 5);
			
			addDep.setOnMouseClicked(event->{
				if(potentialDependencies.getSelectionModel().isEmpty()){
					sendWarning("Select a dependency to add.");
				}
				else {
					int i = potentialDependencies.getSelectionModel().getSelectedIndex();
					SimplePropertyReader s = potentialDependencies.getItems().get(i);
					try{
						ui.scratch_addDependency(s.getID());
						scratchDependencies.getItems().add(s);
						potentialDependencies.getItems().remove(i);
					}
					catch(Exception e){
						sendError(e);
					}
				}
			});
			rmvDep.setOnMouseClicked(event->{
				if(scratchDependencies.getSelectionModel().isEmpty()){
					sendWarning("Select a dependency to remove.");
				}
				else {
					int i = scratchDependencies.getSelectionModel().getSelectedIndex();
					SimplePropertyReader s = scratchDependencies.getItems().get(i);
					try{
						ui.scratch_removeDependency(s.getID());
						scratchDependencies.getItems().remove(i);
						potentialDependencies.getItems().add(s);
					}
					catch (Exception e){
						sendError(e);
					}
				}
			});
			nextBtn.setOnMouseClicked(event->{
				try {
					pageNumber++;
					if(ui.scratch_getDependencies().isEmpty()){
						pageNumber = 4;
					}
					advancePage.set(true);
				} 
				catch (Exception e) {
					sendError(e);
				}
			});
			nextBtn.setDisable(false);
		}
		else if(pageNumber == 3){
			title.setText(title.getText()+" - Conditional Distributions");
			ListView<Integer> distributions = new ListView<>();
			distributions.setPrefHeight(100);
			distributions.setMaxWidth(hbar.getEndX());
			
			Button addDist = new Button("Add");
			Button rmvDist = new Button("Remove");
			
			final GridPane distCreator = new GridPane();
			final ScrollPane distMenu = new ScrollPane();
			final BorderPane distPane = new BorderPane();
			
			
			distCreator.setHgap(20);
			distCreator.setVgap(10);
			distCreator.setPadding(new Insets(10));

			distMenu.setFitToWidth(true);
			distMenu.setFitToHeight(true);
			distMenu.setMaxWidth(1000);
			distMenu.setMaxHeight(1000);
			
			distPane.setCenter(distMenu);
			distPane.setPrefHeight(200);
			distPane.setMaxWidth(hbar.getEndX());
			
			Line bar = new Line();
			bar.setStartX(0);
			bar.setEndX(hbar.getEndX());
			
			Map<Integer, Integer> conditions = new HashMap<>();
			Map<Integer, Float> probMap = new HashMap<>();
			
			page.add(distributions, 0, 3, 5, 2);
			page.add(addDist, 0, 5);
			page.add(rmvDist, 4, 5);
			page.add(bar, 0, 6, 5, 1);
			page.add(distPane, 0, 7, 5, 1);
			
			final BooleanProperty cleared = new SimpleBooleanProperty(true);
			
			distributions.setCellFactory(lv ->{
				ListCell<Integer> cell = new ListCell<>();
				String name = "Conditions "+cell.getItem()+": ";
				try{
					for(Entry<Integer, Integer> pair : ui.scratch_getDistributionCondition(cell.getItem()).entrySet() ){
						name+=  ui.nodeProp_getName( pair.getKey() )
								+ "["
								+ ui.nodeProp_getRangeLabel( pair.getKey(), pair.getValue() )
								+ "] ";
					}
					cell.setText(name);
				}
				catch (Exception e){
					sendError(e);
					cell.setText(">ERROR<");
				}
				return cell;
			});
			
			rmvDist.setOnMouseClicked(event->{
				if(distributions.getSelectionModel().isEmpty()){
					sendWarning("Select a distribution to remove");
				}
				else {
					int index = distributions.getSelectionModel().getSelectedIndex();
					try {
						ui.scratch_removeConditionalDistribution(distributions.getItems().get(index));
						distributions.getItems().remove(index);
					} 
					catch (Exception e) {
						sendError(e);
					}
					
				}
			});
			
			addDist.setOnMouseClicked(event->{
				if(!cleared.get()){
					return;
				}
				try {
					distCreator.getChildren().clear();
					conditions.clear();
					probMap.clear();
					
					distCreator.add(new Label("Conditions"), 0, 0, 2, 1);
					int row = 1;
					for(int pid : ui.scratch_getDependencies()){
						CheckBox check = new CheckBox("Use "+ui.nodeProp_getName(pid));
						ComboBox<Integer> valueBox = new ComboBox<>();
						valueBox.setDisable(true);
						
						distCreator.add(check, 0, row);
						distCreator.add(valueBox, 1, row);
						row ++;
						
						valueBox.setCellFactory(lv -> {
							return new ListCell<Integer>(){
								@Override
								public void updateItem(Integer item, boolean empty){
									super.updateItem(item, empty);
									if(item == null || empty){
										setText("<empty>");
									}
									else {
										try{
											setText(ui.nodeProp_getRangeLabel(pid, item));
										}
										catch(Exception e){
											sendError(e);
											setText(">ERROR<");
										}
									}
								}
							};
						});
						
						valueBox.setButtonCell( new ListCell<Integer>(){
							@Override
							public void updateItem(Integer item, boolean empty){
								super.updateItem(item, empty);
								if(item == null || empty){
									setText("<empty>");
								}
								else {
									try{
										setText(ui.nodeProp_getRangeLabel(pid, item));
									}
									catch(Exception e){
										sendError(e);
										setText(">ERROR<");
									}
								}
							}
						});
						
						valueBox.getItems().addAll(ui.nodeProp_getRangeItemIDs(pid));
						
						valueBox.setOnAction(e -> 
							conditions.put(pid, valueBox.getValue()) );
						
						check.setOnMouseClicked(e ->{
							valueBox.setDisable(!check.isSelected());
							if(!check.isSelected()){
								conditions.put(pid, valueBox.getValue());
							}
							else {
								conditions.remove(pid);
							}
						});
					}
					HBox buttonBar = new HBox();
					buttonBar.setPrefSize(1000, 25);
					
					HBox clearBox = new HBox();
					clearBox.setPrefWidth(1000);
					clearBox.setAlignment(Pos.CENTER_LEFT);
					Button clearBtn = new Button("Clear Distrubition");
					clearBox.getChildren().add(clearBtn);
					
					HBox finBox = new HBox();
					finBox.setPrefWidth(1000);
					finBox.setAlignment(Pos.CENTER_RIGHT);
					Button finBtn = new Button("Finish Distribution");
					finBox.getChildren().add(finBtn);
					
					buttonBar.getChildren().addAll(clearBox, finBox);
					distPane.setBottom(buttonBar);
					
					clearBtn.setOnMouseClicked(e ->{
						distMenu.setContent(null);
						distPane.setBottom(null);
						cleared.set(true);
					});
					
					distMenu.setContent(distCreator);
					cleared.set(false);
				}
				catch(Exception e){
					sendError(e);
				}
			});
			
		}
		else if(pageNumber == 4){ //Create the default distribution for the property
			title.setText(title.getText()+" - Default Distribution");
			TableView<Entry<Scratch_Range, Float>> distMap = new TableView<>();
			TableColumn<Entry<Scratch_Range, Float>, String> labelCol = new TableColumn<>("Range");
			TableColumn<Entry<Scratch_Range, Float>, String> probCol = new TableColumn<>("Probability");
			
			distMap.setPrefHeight(150);
			distMap.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			distMap.getColumns().addAll(labelCol, probCol);
			
			HBox centering = new HBox();
			centering.setAlignment(Pos.CENTER);
			centering.getChildren().add(distMap);
			
			page.add(centering, 0, 3, 5, 2);
			
			
			Map<Scratch_Range, Float> probMap = new HashMap<>();
			try {
				for(int i : ui.scratch_getRangeIDs()){
					probMap.put(new Scratch_Range(ui, i), null);
				}
			} 
			catch (UIException e) {
				sendError(e);
			}
			for(Entry<Scratch_Range, Float> pair : probMap.entrySet()){
				distMap.getItems().add(pair);
			}
			
			labelCol.setCellValueFactory(col -> {
				try {
					return new SimpleStringProperty(col.getValue().getKey().getLabel());
				} 
				catch (Exception e) {
					sendError(e);
					return new SimpleStringProperty(">ERROR<");
				}
			});
			
			probCol.setCellValueFactory(col-> {
				if(col.getValue().getValue() != null){
					return new SimpleStringProperty(col.getValue().getValue().toString());
				}
				else {
					return null;
				}
			});
			
			probCol.setCellFactory(TextFieldTableCell.forTableColumn());
			
			distMap.setEditable(true);
			probCol.setEditable(true);
			
			checkNext = () ->{ //Enabled once all values are set
				for(Float f : probMap.values()){
					if(f == null){
						nextBtn.setDisable(true);
						return;
					}
				}
				nextBtn.setDisable(false);
			};
			
			probCol.setOnEditCommit(event->{
				try{
					if(!event.getNewValue().matches("[0-9]*\\.?[0-9]+")){
						throw new NumberFormatException("Value '"
								+event.getNewValue()+"' is not a floating-point number");
					}
					event.getRowValue().setValue(Float.parseFloat(event.getNewValue()));
				}
				catch (Exception e){
					sendError(e);
				}
				finally{
					checkNext.run();
				}
			});
			
			
			
			nextBtn.setOnMouseClicked(event->{
				Map<Integer, Float> dist = new HashMap<>();
				for(Entry<Scratch_Range, Float> pair : probMap.entrySet()){
					dist.put(pair.getKey().rid, pair.getValue());
				}
				try {
					ui.scratch_setDefaultDistribution(dist);
					finished.set(true);
				} catch (Exception e) {
					sendError(e);
				}
			});
			nextBtn.setText("Finish");
		}
		else {
			sendError(new IllegalStateException("Invalid page number: "+pageNumber));
		}
	}
}
