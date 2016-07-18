package org.snrg_nyc.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.model.components.EditorException;
import org.snrg_nyc.ui.components.ConditionsCell;
import org.snrg_nyc.ui.components.ConditionsMenu;
import org.snrg_nyc.ui.components.DistributionTable;
import org.snrg_nyc.ui.components.LayerCell;
import org.snrg_nyc.ui.components.LayerID;
import org.snrg_nyc.ui.components.PropertyID;
import org.snrg_nyc.ui.components.PropertyNameFactory;
import org.snrg_nyc.ui.components.PropertyTypeFactory;
import org.snrg_nyc.ui.components.UI_Message;
import org.snrg_nyc.ui.components.EditorListCell;
import org.snrg_nyc.ui.components.EditorTableCell;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class EditorPage extends GridPane{
	
	public enum Mode {
		NEW_PROP,
		NEW_LAYER,
		VIEW_PROP,
		IDLE
	}
	protected Mode mode;

	protected PropertiesEditor ui;
	
	protected int pageNumber = 0;
	protected PropertyID propViewerID;
	protected Text title;
	protected Line hbar;
	protected Button nextBtn, cancel;
	
	protected final BooleanProperty advancePage = new SimpleBooleanProperty();
	protected final BooleanProperty finished = new SimpleBooleanProperty();
	protected final StringProperty layerName = new SimpleStringProperty();
	protected final ListProperty<UI_Message> messages = new SimpleListProperty<UI_Message>();
	
	protected static final Font titleFont = Font.font("sans", FontWeight.LIGHT, FontPosture.REGULAR, 20);
	
	public EditorPage(PropertiesEditor ui){
		this.ui = ui;
		mode = Mode.IDLE;
		setAlignment(Pos.TOP_LEFT);
		setVgap(10);
		setHgap(10);
		setPadding(new Insets(15));
		
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
				mode = Mode.IDLE;
				advancePage.set(true);
			}
		});
	}
	
	public void sendError(Exception e){
		messages.add(new UI_Message(e.getMessage(), UI_Message.Type.Error));
		e.printStackTrace();
	}
	
	public void sendWarning(String s){
		messages.add(new UI_Message(s, UI_Message.Type.Warning));
	}
	
	public void sendInfo(String s){
		messages.add(new UI_Message(s, UI_Message.Type.Info));
	}
	
	public void createProperty(){
		if(mode != Mode.IDLE){
			sendWarning("Cannot create a property while creating a layer/property!");
		}
		else {
			finished.set(false);
			pageNumber = 0;
			mode = Mode.NEW_PROP;
			advancePage.set(true);
		}
	}
	
	public void createLayer(){
		if(mode == Mode.NEW_PROP){
			sendWarning("Cannot create a layer while creating a property!");
		}
		else {
			finished.set(false);
			pageNumber = 0;
			mode = Mode.NEW_LAYER;
			advancePage.set(true);
		}
	}
	
	public void viewProperty(PropertyID pid){
		if(mode == Mode.NEW_PROP || mode == Mode.NEW_LAYER){
			sendWarning("You cannot view node properties while"
					+ " editing a property/layer!");
		}
		propViewerID = pid;
		finished.set(false);
		pageNumber = 0;
		mode = Mode.VIEW_PROP;
		advancePage.set(true);
	}
	
	protected void updatePage(){
		getChildren().clear();
		
		title = new Text();
		title.setFont(titleFont);
		add(title, 0,0,5,1);
		
		hbar = new Line();
		hbar.setStartX(20);
		hbar.setEndX(getPrefWidth());
		add(hbar, 0, 1, 5, 1);
		
		nextBtn = new Button("Next");
		nextBtn.setDisable(true);

		HBox nextBox = new HBox();
		nextBox.setAlignment(Pos.CENTER_RIGHT);
		nextBox.getChildren().add(nextBtn);
		add(nextBox, 4, 12);
		
		cancel = new Button("Cancel");
		add(cancel, 0, 12);
		
		cancel.setOnMouseClicked(event ->{
			ui.scratch_clear();
			pageNumber = 0;
			mode = Mode.IDLE;
			advancePage.set(true);
		});
		
		switch(mode){
		case NEW_PROP:
			newPropertyPage();
			break;
		case VIEW_PROP:
			propertyViewerPage();
			break;
		case NEW_LAYER:
			newLayerPage();
			break;
		default:
			getChildren().clear();
		}
	}
	
	private void newLayerPage(){
		title.setText("New Layer");

		TextField layerTx = new TextField();
		
		add(new Label("Layer Name"), 0, 2);
		add(layerTx, 1, 2);
		
		layerTx.textProperty().addListener((o, oldVal, newVal)->{
			nextBtn.setDisable(
				newVal == null
				|| newVal.equals("")
				|| !ui.test_layerNameIsUnique(newVal)
				);
		});
		
		nextBtn.setOnMouseClicked(event->{
			layerName.set(layerTx.getText());
			mode = Mode.IDLE;
			advancePage.set(true);
		});
	}
	
	@SuppressWarnings("unchecked")
	private void propertyViewerPage(){
		title.setText("Node Property Viewer");
		cancel.setText("Exit");
		PropertyID id = propViewerID;
		
		nextBtn.setOnMouseClicked(event->{
			pageNumber++;
			advancePage.set(true);
		});
		
		String propType = ">ERROR<";
		int depLvl = -1;
		String propName = ">ERROR<";
		try{
			if(id.usesLayer()){
				propType = ui.nodeProp_getType(id.lid(), id.pid());
				depLvl = ui.nodeProp_getDependencyLevel(id.lid(), id.pid());
				propName = ui.nodeProp_getName(id.lid(), id.pid());
			}
			else {
				propType = ui.nodeProp_getType(id.pid());
				depLvl = ui.nodeProp_getDependencyLevel(id.pid());
				propName = ui.nodeProp_getName(id.pid());
			}
		}
		catch(Exception e){
			sendError(e);
		}
		
		switch(pageNumber){
		case 0:
			
			try {
				if(id.usesLayer()){
					nextBtn.setDisable(!ui.nodeProp_isRangedProperty(id.lid(), id.pid()));
				}
				else {
					nextBtn.setDisable(!ui.nodeProp_isRangedProperty(id.pid()));
				}
			} catch (EditorException e1) {
				sendError(e1);
			}
			
			Text name, type, description, dependencyLevel;
			double w = 300;
			
			name = new Text(propName);
			name.setWrappingWidth(w);
			
			type=new Text(propType);
			type.setWrappingWidth(w);
			
			description=new Text();
			description.setWrappingWidth(w);
			try{
				if(id.usesLayer()){
					description.setText(ui.nodeProp_getDescription(id.lid(), id.pid()));
				}
				else {
					description.setText(ui.nodeProp_getDescription(id.pid()));
				}
			}
			catch(Exception e){
				sendError(e);
				description.setText(">ERROR<");
			}
			
			dependencyLevel=new Text();
			dependencyLevel.setWrappingWidth(w);
			try{
				if(id.usesLayer()){
					depLvl = ui.nodeProp_getDependencyLevel(id.lid(), id.pid());
				}
				else {
					depLvl = ui.nodeProp_getDependencyLevel(id.pid());
				}
				dependencyLevel.setText(Integer.toString(depLvl));
			}
			catch(Exception e){
				sendError(e);
				dependencyLevel.setText(">ERROR<");
			}
			
			add(new Label("Name"),             0, 2);
			add(new Label("Type"),             0, 3);
			add(new Label("Description"),      0, 4);
			add(new Label("Dependency Level"), 0, 5);
			
			add(name,            1, 2, 2, 1);
			add(type,            1, 3, 2, 1);
			add(description,     1, 4, 2, 1);
			add(dependencyLevel, 1, 5, 2, 1);
			
			if(depLvl > 0){
				Text dependencies = new Text();
				dependencies.setWrappingWidth(w);

				add(new Label("Dependencies"), 0, 6);
				add(dependencies, 1, 6, 2, 1);
				
				String depString = "";
				try{
					List<Integer> deps;
					if(id.usesLayer()){
						deps = ui.nodeProp_getDependencyIDs(id.lid(), id.pid());
					}
					else{
						deps = ui.nodeProp_getDependencyIDs(id.pid());
					}
					if(deps.size() == 0){
						depString = "(None)";
					}
					for(int i : deps){
						String space = 
							(deps.indexOf(i) == deps.size()-1) ? "" : ", ";
						depString += ui.nodeProp_getName(i)+space;
					}
				}
				catch(Exception e){
					depString=">ERROR<";
					sendError(e);
				}
				dependencies.setText(depString);
			}

			List<Integer> rangeIDs = null;
			if(!type.equals("FractionProperty")){
				try {
					if(id.usesLayer()){
						rangeIDs = ui.nodeProp_getRangeItemIDs(id.lid(),id.pid());
					}
					else {
						rangeIDs = ui.nodeProp_getRangeItemIDs(id.pid());
					}
				}
				catch(Exception e){
					sendError(e);
					rangeIDs = new ArrayList<>();
				}
			}
			
			
			switch(propType){
			case "AttachmentProperty":
				Text pathogenType = new Text();
				try {
					if(id.usesLayer()){
						pathogenType.setText(
								ui.nodeProp_getPathogenType(id.lid(), id.pid()));
					}
					else {
						pathogenType.setText(
								ui.nodeProp_getPathogenType(id.pid()));
					}
				}
				catch(EditorException e){
					pathogenType.setText(">ERROR<");
					sendError(e);
				}
				
				add(new Label("Pathogen Type"), 0, 7);
				break;
			case "EnumeratorProperty":
				ListView<String> enumValues = new ListView<>();
				enumValues.setPrefSize(100, 140);
				
				for(int rid : rangeIDs){
					try {
						if(id.usesLayer()){
							enumValues.getItems().add(
								ui.nodeProp_getRangeLabel(id.lid(), id.pid(), rid));
						}
						else {
							enumValues.getItems().add(
								ui.nodeProp_getRangeLabel(id.pid(), rid));
						}
						
					} catch (EditorException e) {
						sendError(e);
						enumValues.getItems().add(">ERROR<");
					}
				}
				add(new Label("Enum Values"), 0, 8);
				add(enumValues, 1, 8, 2, 2);
				break;
				
			case "IntegerRangeProperty":
				TableView<Integer> rangeItems = new TableView<>();
				TableColumn<Integer, String> labelCol = new TableColumn<>("Label");
				TableColumn<Integer, String> minCol = new TableColumn<>("Min");
				TableColumn<Integer, String> maxCol = new TableColumn<>("Max");
				
				rangeItems.setPrefHeight(140);
				rangeItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
				rangeItems.getColumns().addAll(labelCol, minCol, maxCol);
				
				labelCol.setCellValueFactory(col->{
					int rid = col.getValue();
					try{
						if(id.usesLayer()){
							return new SimpleStringProperty(
									ui.nodeProp_getRangeLabel(id.lid(), id.pid(), rid));
						}
						else {
							return new SimpleStringProperty(
									ui.nodeProp_getRangeLabel(id.pid(), rid));
						}
						
					}
					catch(Exception e){
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});
				minCol.setCellValueFactory(col->{
					int rid = col.getValue();
					try {
						int min;
						if(id.usesLayer()){
							min = ui.nodeProp_getRangeMin(id.lid(), id.pid(), rid);
						}
						else {
							min = ui.nodeProp_getRangeMin(id.pid(), rid);
						}
						return new SimpleStringProperty(Integer.toString(min));
					} catch (Exception e) {
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});
				maxCol.setCellValueFactory(col->{
					int rid = col.getValue();
					try{
						int max;
						if(id.usesLayer()){
							max = ui.nodeProp_getRangeMax(id.lid(), id.pid(), rid);
						}
						else {
							max = ui.nodeProp_getRangeMax(id.pid(), rid);
						}
						return new SimpleStringProperty(Integer.toString(max));
					}
					catch(Exception e){
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});
				
				try{
					for(int i : rangeIDs){
						rangeItems.getItems().add(i);
					}
				}
				catch(Exception e){
					sendError(e);
				}
				add(new Label("Range Items"), 0, 8);
				add(rangeItems, 1, 8, 2, 2);
				
				break;
			case "FractionProperty":
				add(new Label("Init Value"), 0, 8);
				String initVal;
				try{
					float init;
					if(id.usesLayer()){
						init = ui.nodeProp_getInitValue(id.lid(), id.pid());
					}
					else {
						init = ui.nodeProp_getInitValue(id.lid());
					}
					initVal = Float.toString(init);
				}
				catch(Exception e){
					sendError(e);
					initVal = ">ERROR<";
				}
				add(new Text(initVal), 1, 8);
				break;
			}
			
			break;
		case 1:
			title.setText(title.getText()+" - Distributions");
			
			nextBtn.setText("Finish");
			nextBtn.setDisable(false);
			
			nextBtn.setOnMouseClicked(event->{
				mode = Mode.IDLE;
				pageNumber = 0;
				advancePage.set(true);
			});
			
			boolean hasDistributions = false;
			final List<Integer> cids = new ArrayList<>();
			
			try {
				if(depLvl > 0){
					if(id.usesLayer()){
						cids.addAll(ui.nodeProp_getConditionalDistributionIDs(id.lid(), id.pid()) );
					}
					else {
						cids.addAll(ui.nodeProp_getConditionalDistributionIDs(id.pid()) );
					}
					hasDistributions = !cids.isEmpty();
				}
				else {
					hasDistributions = false;
				}
			} catch (EditorException e) {
				sendError(e);
			}
			
			int row = 2;
			if(hasDistributions){
				BorderPane distBPane = new BorderPane();
				GridPane distGPane = new GridPane();
				distGPane.setAlignment(Pos.CENTER);
				
				distBPane.getStyleClass().add("distribution-viewer");
				distBPane.setPadding(new Insets(5));

				distGPane.setPadding(new Insets(5));
				distGPane.setHgap(20);
				distGPane.setVgap(10);
				
				distBPane.setCenter(distGPane);
				
				//Conditional Distribution navigation
				final IntegerProperty indexProperty = new SimpleIntegerProperty(0);
				
				HBox buttonBar = new HBox();
				buttonBar.setAlignment(Pos.CENTER);
				buttonBar.setPrefWidth(1000);

				Button backBtn = new Button("<< Back");
				HBox backBox = new HBox();
				backBox.setAlignment(Pos.CENTER_LEFT);
				backBox.getChildren().add(backBtn);
				backBox.setPrefWidth(1000);
				
				Button fwdBtn = new Button("Next >>");
				HBox fwdBox = new HBox();
				fwdBox.setAlignment(Pos.CENTER_RIGHT);
				fwdBox.getChildren().add(fwdBtn);
				fwdBox.setPrefWidth(1000);
				
				Text condMessage = new Text();

				TableView<Entry<Integer, Integer>> conditions = new TableView<>();
				conditions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
				conditions.setPrefSize(200, 120);
				
				TableColumn<Entry<Integer, Integer>, String> depCol = new TableColumn<>("Dependency");
				TableColumn<Entry<Integer, Integer>, String> valCol = new TableColumn<>("Value");
				conditions.getColumns().addAll(depCol, valCol);
				
				depCol.setCellValueFactory(col->{
					int PID = col.getValue().getKey();
					try{
						return new SimpleStringProperty(
								ui.nodeProp_getName(PID));
					}
					catch(Exception e){
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});
				
				valCol.setCellValueFactory(col->{
					int PID = col.getValue().getKey();
					int RID = col.getValue().getValue();
					try{
						return new SimpleStringProperty(
								ui.nodeProp_getRangeLabel(PID, RID));
					}
					catch(Exception e){
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});

				TableView<Entry<Integer, Float>> distribution = new TableView<>();
				distribution.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
				distribution.setPrefSize(250, 120);
				
				TableColumn<Entry<Integer, Float>, String> rangeCol = new TableColumn<>("Range");
				TableColumn<Entry<Integer, Float>, Number> probCol = new TableColumn<>("Probability");
				
				distribution.getColumns().addAll(rangeCol, probCol);
				
				rangeCol.setCellValueFactory(col->{
					int rid = col.getValue().getKey();
					try{
						if(id.usesLayer()){
							return new SimpleStringProperty(
									ui.nodeProp_getRangeLabel(id.lid(),id.pid(), rid));
						}
						else {
							return new SimpleStringProperty(
									ui.nodeProp_getRangeLabel(id.pid(), rid));
						}
					}
					catch(Exception e){
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});
				probCol.setCellValueFactory(col->{
					return new SimpleFloatProperty(
							col.getValue().getValue());
				});
				distGPane.add(new Label("Conditions"),   0, 0);
				distGPane.add(new Label("Distribution"), 2, 0);
				distGPane.add(conditions,   0, 1);
				distGPane.add(distribution, 2, 1);
				
				Runnable updateDist = ()->{
					if(indexProperty.get() == 0){
						backBtn.setDisable(true);
					}
					else {
						backBtn.setDisable(false);
					}
					if(indexProperty.get()  >= cids.size()-1){
						fwdBtn.setDisable(true);
					}
					else {
						fwdBtn.setDisable(false);
					}
					int cid = cids.get(indexProperty.get() );
					
					conditions.getItems().clear();
					try {
						if(id.usesLayer()){
							conditions.getItems().addAll(
								ui.nodeProp_getDistributionConditions(
									id.lid(), id.pid(), cid).entrySet());
						}
						else {
							conditions.getItems().addAll(
								ui.nodeProp_getDistributionConditions(id.pid(), cid).entrySet());
						}
					} 
					catch (EditorException e) {
						sendError(e);
					}
					
					distribution.getItems().clear();
					try {
						if(id.usesLayer()){
							distribution.getItems().addAll(
								ui.nodeProp_getDistribution(id.lid(),id.pid(), cid).entrySet());
						}
						else {
							distribution.getItems().addAll(
									ui.nodeProp_getDistribution(id.pid(), cid).entrySet());
						}
					} catch (EditorException e) {
						sendError(e);
					}
					
					String msg = String.format(
							"Conditional Distribution [%d of %d]", 
							 indexProperty.get()+1, cids.size() );
					condMessage.setText(msg);
				};
				
				fwdBtn.setOnMouseClicked(event->{
					indexProperty.set(indexProperty.get()+1);
					updateDist.run();
				});
				backBtn.setOnMouseClicked(event->{
					indexProperty.set(indexProperty.get()-1);
					updateDist.run();
				});
				updateDist.run();
				
				buttonBar.getChildren().addAll(backBox, condMessage, fwdBox);
				distBPane.setBottom(buttonBar);
				
				add(new Label("Conditional Distributions"), 0, row++);
				add(distBPane, 0, row++, 5, 1);
				
				Line l = new Line();
				l.setEndX(200);
				
				add(l, 0, row++);
			}
			
			//Create default distribution table
			HBox centering = new HBox();
			centering.setAlignment(Pos.TOP_CENTER);
			
			TableView<Entry<Integer, Float>> distribution = new TableView<>();
			TableColumn<Entry<Integer, Float>, String> rangeCol = new TableColumn<>("Range");
			TableColumn<Entry<Integer, Float>, Number> probCol = new TableColumn<>("Probability");
			
			distribution.setPrefSize(250, 140);
			distribution.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			
			distribution.getColumns().addAll(rangeCol, probCol);
			
			rangeCol.setCellValueFactory(col->{
				int rid = col.getValue().getKey();
				try {
					if(id.usesLayer()){
						return new SimpleStringProperty(
								ui.nodeProp_getRangeLabel(id.lid(), id.pid(), rid));
					}
					else {
						return new SimpleStringProperty(
								ui.nodeProp_getRangeLabel(id.pid(), rid));
					}
				} 
				catch (Exception e) {
					sendError(e);
					return new SimpleStringProperty(">ERROR<");
				}
			});
			probCol.setCellValueFactory(col->{
				return new SimpleFloatProperty(
						col.getValue().getValue());
			});
			
			centering.getChildren().add(distribution);
			
			try {
				if(id.usesLayer()){
					distribution.getItems().addAll(
							ui.nodeProp_getDefaultDistribution(id.lid(), id.pid()).entrySet());
				}
				else {
					distribution.getItems().addAll(
							ui.nodeProp_getDefaultDistribution(id.pid()).entrySet());
				}
				
			} catch (EditorException e) {
				sendError(e);
			}
			add(new Label("Default Distribution"), 0, row++);
			add(centering, 0, row++, 5, 1);
			
			break;
		default:
			messages.add(new UI_Message("Unknown page number: "+pageNumber, UI_Message.Type.Error));
			break;
		}
	}

	private void newPropertyPage(){
		title.setText("New Node Property");
		
		//Function to see if the next button should be enabled
		Runnable checkNext;
		
		if(pageNumber == 0){
			title.setText(title.getText()+" - Basic Settings");
			
			TextField propName = new TextField();
			
			ComboBox<String> type = new ComboBox<>();
			type.getItems().addAll(ui.getPropertyTypes());
			
			TextArea desc = new TextArea();
			desc.setPrefColumnCount(20);
			desc.setPrefRowCount(4);
			desc.setWrapText(true);
			
			ComboBox<LayerID> layerSelect = new ComboBox<>();
			layerSelect.setCellFactory(lv-> new LayerCell(this) );
			layerSelect.setButtonCell(new LayerCell(this));
			
			layerSelect.getItems().add(new LayerID());
			for(int i: ui.layer_getLayerIDs()){
				layerSelect.getItems().add(new LayerID(i));
			}
			
			add(new Label("Name"), 0, 2);
			add(new Label("Type"), 0, 3);
			add(new Label("Description"), 0, 4);
			add(new Label("Layer"), 0, 6);
			
			add(propName, 1, 2);
			add(type, 1, 3);
			add(desc, 1, 4, 1, 2);
			add(layerSelect, 1, 6);
			
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
					if(layerSelect.getValue() != null && layerSelect.getValue().used()){
						ui.scratch_newInLayer(
								layerSelect.getValue().get(), 
								propName.getText(), 
								type.getValue(), 
								desc.getText());
					}
					else {
						ui.scratch_new(
								propName.getText(), 
								type.getValue(), 
								desc.getText());
					}
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
				add(useUniform, 0, 8, 3, 1);
				add(new Label("Dependency Level"), 0, 2);
				add(depLvl, 1, 2);
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
						//Skip dependencies and conditional distributions if the dependency level is 0
						pageNumber = (dl > 0)? 2 : 4; 
						advancePage.set(true);
					}
				} 
				catch (Exception e) {
					sendError(e);
				}
			});
			
			switch(type){
			case "EnumeratorProperty":
				ListView<Integer> values = new ListView<>();
				
				values.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
				values.setCellFactory(lv ->{
					EditorListCell<Integer> cell = new EditorListCell<>(null);
					cell.setConverter(new StringConverter<Integer>(){
						@Override
						public Integer fromString(String newVal) {
							int r = cell.getItem();
							try {
								ui.scratch_setRangeLabel(r, newVal);
							} catch (EditorException e) {
								sendError(e);
							}
							return r;
						}
						@Override
						public String toString(Integer rid) {
							try {
								return ui.scratch_getRangeLabel(rid);
							} catch (EditorException e) {
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
				
				add(new Label("Enumerate Items"), 0, 3);
				add(values, 1, 3, 2, 3);
				
				add(add, 1, 6);
				add(rmvBox, 2, 6);
				
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
						values.getItems().add(rid);
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
							int rid = values.getSelectionModel().getSelectedItem();
							int id = values.getSelectionModel().getSelectedIndex();
							ui.scratch_removeRange(rid);
							values.getItems().remove(id);
						}
					}
					catch(Exception e){
						sendError(e);
					}
				});
				
				
				
				break;
			case "IntegerRangeProperty":
				
				TableView<Integer> ranges = new TableView<>();
				TableColumn<Integer, String> labelCol = new TableColumn<>("Label");
				TableColumn<Integer, String> minCol = new TableColumn<>("Min");
				TableColumn<Integer, String> maxCol = new TableColumn<>("Max");
				ranges.setPrefHeight(200);
				ranges.setEditable(true);
				
				add = new Button("Add Range");
				rmv = new Button("Remove Range");
				rmvBox = new HBox();
				rmvBox.setAlignment(Pos.CENTER_RIGHT);
				rmvBox.getChildren().add(rmv);

				add(ranges, 1, 3, 2, 2);
				add(add, 1, 5);
				add(rmvBox, 2, 5);
				
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
				
				labelCol.setCellFactory(col -> {
					EditorTableCell<Integer> cell = new EditorTableCell<>();
					cell.setOnEditCommit((event)->{
						if(event.newText() == null){
							return;
						}
						try {
							int rid = event.cell().getTableView()
							     .getItems()
							     .get(event.cell().getIndex());
							ui.scratch_setRangeLabel(rid, event.newText());
						} 
						catch (Exception e1) {
							event.cell().cancelEdit();
							event.cell().update();
							sendError(e1);
						}
						finally{
							checkNext.run();
						}
					});
					return cell;
				});
				
				minCol.setCellFactory(col -> {
					EditorTableCell<Integer> cell = new EditorTableCell<>();
					cell.setOnEditCommit(event->{
						if(event.newText() == null){
							return;
						}
						try{
							int min = Integer.parseInt(event.newText());
							int rid = event.cell().getTableView()
							     .getItems()
							     .get(event.cell().getIndex());
							ui.scratch_setRangeMin(rid, min);
						}
						catch(Exception e){
							event.cell().cancelEdit();
							event.cell().update();
							sendError(e);
						}
						finally{
							checkNext.run();
						}
					});
					return cell;
				});
				
				maxCol.setCellFactory(col -> {
					EditorTableCell<Integer> cell = new EditorTableCell<>();
					cell.setOnEditCommit(event->{
						if(event.newText() == null){
							return;
						}
						try{
							int max = Integer.parseInt(event.newText());
							int rid = event.cell().getTableView()
							     .getItems()
							     .get(event.cell().getIndex());
							ui.scratch_setRangeMax(rid, max);
						}
						catch(Exception e){
							event.cell().cancelEdit();
							event.cell().update();
							sendError(e);
						}
						finally{
							checkNext.run();
						}
					});
					return cell;
				});
				
				labelCol.setCellValueFactory(data ->{
					try{
						String s = ui.scratch_getRangeLabel(data.getValue());
						if(s == null || s.equals("")){
							return new SimpleStringProperty("<empty>");
						}
						else {
							return new SimpleStringProperty(s);
						}
					}
					catch (Exception e){
						sendError(e);
						return null;
					}
				});
				
				minCol.setCellValueFactory(data ->{
					try {
						Integer i = ui.scratch_getRangeMin(data.getValue());
						return new SimpleStringProperty(i.toString());
					} catch (Exception e) {
						sendError(e);
						return null;
					}
				});
				
				maxCol.setCellValueFactory(data->{
					try{
						Integer i = ui.scratch_getRangeMax(data.getValue());
						return new SimpleStringProperty(i.toString());
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
				
				
				add.setOnMouseClicked(event->{
					try {
						int rid = ui.scratch_addRange();
						ranges.getItems().add(rid);
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
							int rid = ranges.getSelectionModel().getSelectedItem();
							int id = ranges.getSelectionModel().getSelectedIndex();
							ui.scratch_removeRange(rid);
							ranges.getItems().remove(id);
						}
					}
					catch(Exception e){
						sendError(e);
					}
				});
				
				break;
			case "BooleanProperty":
				nextBtn.setDisable(false);
				break;
			case "AttachmentProperty":
				TextField pathogenInput = new TextField();

				add(new Label("Pathogen Type"), 0, 3);
				add(pathogenInput,              1, 3);
				
				checkNext = () ->{
					nextBtn.setDisable(
							pathogenInput.getText() == null
							|| pathogenInput.getText().equals(""));
				};
				nextBtn.armedProperty().addListener((o, ov, nv)->{
					try {
						ui.scratch_setPathogenType(pathogenInput.getText());
					} catch (EditorException e1) {
						sendError(e1);
					}
				});
				pathogenInput.textProperty().addListener(event->{
					checkNext.run();
				});
				break;
			case "FractionProperty":
				HBox centering = new HBox();
				centering.setAlignment(Pos.CENTER);
				centering.setSpacing(10);
				
				TextField initVal = new TextField();
				nextBtn.setText("Finish");
				
				centering.getChildren().addAll(new Label("Value"), initVal);
				add(centering, 0, 4, 5, 3);
				
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
			TableView<PropertyID> potentialDependencies = new TableView<>();
			TableColumn<PropertyID, String> nameCol = new TableColumn<>("Name");
			TableColumn<PropertyID, String> typeCol = new TableColumn<>("Type");
			
			nameCol.setCellValueFactory(new PropertyNameFactory(this));
			typeCol.setCellValueFactory(new PropertyTypeFactory(this));
			
			TableView<PropertyID> scratchDependencies = new TableView<>();

			TableColumn<PropertyID, String> nameCol2 = new TableColumn<>("Name");
			TableColumn<PropertyID, String> typeCol2 = new TableColumn<>("Type");
			
			nameCol2.setCellValueFactory(new PropertyNameFactory(this));
			typeCol2.setCellValueFactory(new PropertyTypeFactory(this));
			
			
			potentialDependencies.getColumns().addAll(nameCol, typeCol);
			potentialDependencies.setPrefSize(220, 200);
			
			scratchDependencies.setPrefWidth(potentialDependencies.getPrefWidth());
			scratchDependencies.setPrefHeight(potentialDependencies.getPrefHeight());
			
			
			scratchDependencies.getColumns().addAll(nameCol2, typeCol2);
			
			
			scratchDependencies.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			potentialDependencies.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			
			try{
				for(int pid : ui.scratch_getPotentialDependencies()){
					potentialDependencies.getItems().add( new PropertyID(pid));
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
			
			add(new Label("Available Dependencies"), 0, 2, 2, 1);
			add(new Label("Added Dependencies"), 3, 2, 2, 1);
			
			add(potentialDependencies, 0, 3, 2, 4);
			add(scratchDependencies, 3, 3, 2, 4);
			
			add(btnBox1, 2, 4);
			add(btnBox2, 2, 5);
			
			addDep.setOnMouseClicked(event->{
				if(potentialDependencies.getSelectionModel().isEmpty()){
					sendWarning("Select a dependency to add.");
				}
				else {
					int i = potentialDependencies.getSelectionModel().getSelectedIndex();
					PropertyID pid = potentialDependencies.getItems().get(i);
					try{
						ui.scratch_addDependency(pid.pid());
						scratchDependencies.getItems().add(pid);
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
					PropertyID pid= scratchDependencies.getItems().get(i);
					try{
						ui.scratch_removeDependency(pid.pid());
						scratchDependencies.getItems().remove(i);
						potentialDependencies.getItems().add(pid);
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
			distributions.setItems(FXCollections.observableArrayList());
			
			distributions.setPrefHeight(100);
			distributions.setMaxWidth(hbar.getEndX());
			
			distributions.setCellFactory(lv -> new ConditionsCell(this));
			
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
			distPane.setMaxWidth(distributions.getMaxWidth());
			
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
			
			Line bar = new Line();
			bar.setStartX(0);
			bar.setEndX(hbar.getEndX());
			
			add(distributions, 0, 3, 5, 2);
			add(bar,           0, 6, 5, 1);
			add(distPane,      0, 7, 5, 1);
			add(addDist,       0, 5);
			add(rmvDist,       4, 5);
			
			final BooleanProperty cleared = new SimpleBooleanProperty(true);
			clearBtn.disableProperty().bind(cleared);
			finBtn.disableProperty().bind(cleared);
			
			cleared.addListener((obs, oldVal, newVal)->{
				if(newVal){
					distCreator.getChildren().clear();
					distributions.getSelectionModel().clearSelection();
				}
			});
			
			//You can advance if the page is cleared
			nextBtn.disableProperty().bind(cleared.not());
			nextBtn.setOnMouseClicked(event->{
				try {
					ui.scratch_reorderConditionalDistributions(distributions.getItems());
				} catch (Exception e) {
					sendError(e);
				}
				pageNumber++;
				advancePage.set(true);
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
			addDist.disableProperty().bind(cleared.not());
			
			final Runnable updateMenu = () ->{
				try {

					final DistributionTable distMap = new DistributionTable(this);
					distMap.setPrefSize(300, 250);
					distMap.setId("distMap");
					
					final ConditionsMenu condsMenu = new ConditionsMenu(ui, this);
					condsMenu.setPrefSize(300, 250);
					condsMenu.setId("condMenu");

					distCreator.add(new Label("Conditions"), 0, 0);
					distCreator.add(new Label("Probabilities"), 1, 0);
					distCreator.add(condsMenu, 0, 1);
					distCreator.add(distMap,  1, 1);
					
					finBtn.disableProperty().bind( 
							distMap.readyProperty().and(condsMenu.readyProperty()).not() );
					
					finBtn.setOnMouseClicked(e->{
						try{
							int cid = ui.scratch_addConditionalDistribution(
									condsMenu.getConditions(), distMap.getProbMap());
							distributions.getItems().add(cid);
							cleared.set(true);
						}
						catch(Exception err){
							sendError(err);
						}
					});
					
					clearBtn.setOnMouseClicked(e ->{
						cleared.set(true);
					});
					
					distMenu.setContent(distCreator);
					cleared.set(false);
				}
				catch(Exception e){
					sendError(e);
				}
			};
			
			distributions.getSelectionModel().
				selectedItemProperty().addListener((o, oldVal, cid)->{
					
				if(cleared.get() && cid != null){
					updateMenu.run();
					DistributionTable distMap = (DistributionTable) lookup("#distMap");
					ConditionsMenu condMenu = (ConditionsMenu) lookup("#condMenu");
					
					try{
						if(distMap == null || condMenu == null){
							throw new IllegalStateException("Missing distribution map/conditions menu!");
						}
						
						Map<Integer, Float> probMap = ui.scratch_getDistribution(cid);
						Map<Integer, Integer> conditions = ui.scratch_getDistributionCondition(cid);
						distMap.setPropMap(probMap);
						condMenu.setCondiditions(conditions);
						distMap.refresh();
					}
					catch(Exception e){
						sendError(e);
					}
					finBtn.setOnMouseClicked(event->{
						try {
							ui.scratch_updateConditionalDistribution(
									cid, condMenu.getConditions() , distMap.getProbMap());
							sendInfo("Updated conditional distribution "+cid);
							distributions.refresh();
							cleared.set(true);
						} catch (Exception e) {
							sendError(e);
						}
					});
				}
			});
			
			addDist.setOnMouseClicked(event->updateMenu.run());
			
		}
		else if(pageNumber == 4){
			title.setText(title.getText()+" - Default Distribution");
			
			try {
				final DistributionTable distMap = new DistributionTable(this);
				distMap.setPrefSize(300, 250);
				HBox centering = new HBox();
				centering.setAlignment(Pos.CENTER);
				centering.getChildren().add(distMap);

				add(centering, 0, 3, 5, 2);

				nextBtn.disableProperty().bind(distMap.readyProperty().not());


				nextBtn.setOnMouseClicked(event->{
					try {
						ui.scratch_setDefaultDistribution(distMap.getProbMap());
						finished.set(true);
					} 
					catch (Exception e) {
						sendError(e);
					}
				});
				nextBtn.setText("Finish");

			} 
			catch (EditorException e1) {
				sendError(e1);
			}
		}
		else {
			sendError(new IllegalStateException("Invalid page number: "+pageNumber));
		}
	}

	public PropertiesEditor getModel() {
		return ui;
	}
}
