package org.snrg_nyc.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.LayerPropertyReader;
import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.model.PropertyID;
import org.snrg_nyc.model.PropertyReader;
import org.snrg_nyc.ui.components.ConditionsCell;
import org.snrg_nyc.ui.components.ConditionsMenu;
import org.snrg_nyc.ui.components.DistributionTable;
import org.snrg_nyc.ui.components.EditorListCell;
import org.snrg_nyc.ui.components.EditorTableCell;
import org.snrg_nyc.ui.components.EditorTableCell.EditListener;
import org.snrg_nyc.ui.components.Fonts;
import org.snrg_nyc.ui.components.LayerCell;
import org.snrg_nyc.ui.components.PropertyNameFactory;
import org.snrg_nyc.ui.components.PropertyTypeFactory;
import org.snrg_nyc.util.Executor;
import org.snrg_nyc.util.Message;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * A page for editing/viewing properties and layers.  It does
 * most of the work in the UI system.
 * @author Devin Hastings
 *
 */
public class EditorPage extends GridPane{
	
	public enum Mode {
		NEW_PROP,
		NEW_LAYER,
		VIEW_PROP,
		IDLE
	}
	private Mode mode;

	private PropertiesEditor model;
	
	private int pageNumber = 0;
	private PropertyID propViewerID;
	private Text title;
	private Line hbar;
	private Button nextBtn, cancel;
	
	private final BooleanProperty advancePage = new SimpleBooleanProperty();
	private final BooleanProperty finished = new SimpleBooleanProperty();
	private final BooleanProperty addedProperty = new SimpleBooleanProperty();
	private final BooleanProperty addedLayer = new SimpleBooleanProperty();
	private final BooleanProperty addedPathogen = new SimpleBooleanProperty();
	private final ListProperty<Message> messages = 
			new SimpleListProperty<Message>();

	final ObservableList<Optional<Integer>> layers =
			FXCollections.observableArrayList();
	
	private Optional<Integer> newPropLayerID;
	
	/**
	 * Create a new editor page that adds properties to the given
	 * {@link PropertiesEditor}
	 * @param ui The {@link PropertiesEditor} to interface with.
	 */
	public EditorPage(PropertiesEditor ui){
		this.model = ui;
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
		
		addedProperty.addListener( (o, oldVal, newVal)->{
			if(newVal){
				try {
					boolean attachment = 
							model.scratch_getType().equals("AttachmentProperty");
					model.scratch_commit();
					addedPathogen.set(attachment);
				} catch (Exception e) {
					sendError(e);
				}
				finished.set(true);
			}
			else {
				addedPathogen.set(false);
			}
		});
		addedLayer.addListener((o, oldval, newval)->{
			if(newval){
				finished.set(true);
			}
		});
		finished.addListener((o, oldval, newval)->{
			if(newval){
				pageNumber = 0;
				mode = Mode.IDLE;
				advancePage.set(true);
			}
		});
	}
	
	/*
	 * Handy wrappers for sending messages to the editor
	 */
	public void 
	sendMessage(Message m){
		if(m != null){
			messages.add(m);
		}
	}
	public void 
	sendError(Exception e){
		String message = e.getMessage();
		if(message == null){
			//Get something more informative
			message = e.getClass().getSimpleName();
		}
		messages.add(Message.error(e.getMessage()));
		e.printStackTrace();
	}
	
	public void 
	sendWarning(String s){
		messages.add(Message.warning(s));
	}
	
	public void 
	sendInfo(String s){
		messages.add(Message.info(s));
	}
	
	public void 
	createProperty(Optional<Integer> layerID){
		if(mode != Mode.IDLE && mode != Mode.VIEW_PROP){
			sendWarning("Cannot create a property "
					+ "while creating a layer/property!");
		}
		else {
			newPropLayerID = layerID;
			addedProperty.set(false);
			pageNumber = 0;
			mode = Mode.NEW_PROP;
			advancePage.set(true);
		}
	}
	public void 
	createProperty(){
		createProperty(Optional.empty());
	}
	
	public void 
	createLayer(){
		if(mode != Mode.IDLE && mode != Mode.VIEW_PROP){
			sendWarning(
					"Cannot create a layer while creating a layer/property!");
			return;
		}
		else {
			addedLayer.set(false);
			pageNumber = 0;
			mode = Mode.NEW_LAYER;
			advancePage.set(true);
		}
	}
	
	/**
	 * Open the property viewer mode for a property with the given ID
	 * @param pid The ID of the property to view.
	 */
	public void 
	viewProperty(PropertyID pid){
		if(mode == Mode.NEW_PROP || mode == Mode.NEW_LAYER){
			sendWarning("You cannot view node properties while"
					+ " editing a property/layer!");
			return;
		}
		propViewerID = pid;
		finished.set(false);
		pageNumber = 0;
		mode = Mode.VIEW_PROP;
		advancePage.set(true);
	}
	
	private void 
	updatePage(){
		finished.set(false);
		getChildren().clear();
		title = new Text();
		title.setFont(Fonts.titleFont);
		add(title, 0,0,5,1);
		
		hbar = new Line();
		hbar.setStartX(20);
		hbar.setEndX(500);
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
			model.scratch_clear();
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
	
	private void 
	newLayerPage(){
		title.setText("New Layer");

		TextField layerTx = new TextField();
		
		add(new Label("Layer Name"), 0, 2);
		add(layerTx, 1, 2);
		
		layerTx.textProperty().addListener((o, oldVal, newVal)->{
			nextBtn.setDisable(
				newVal == null
				|| newVal.equals("")
				|| !model.test_layerNameIsUnique(newVal)
				);
		});
		
		nextBtn.setText("Finish");
		
		nextBtn.setOnMouseClicked(event->{
			try {
				int lid = model.layer_new(layerTx.getText());
				layers.add(Optional.of(lid));
				addedLayer.set(true);
			} catch (Exception e) {
				sendError(e);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private void 
	propertyViewerPage(){
		title.setText("Property Viewer");
		cancel.setText("Exit");
		PropertyReader prop = new LayerPropertyReader(model,propViewerID);
		
		nextBtn.setOnMouseClicked(event->{
			pageNumber++;
			advancePage.set(true);
		});
		
		String propType = ">ERROR<";
		int depLvl = -1;
		String propName = ">ERROR<";
		boolean uniformDist = false;
		
		try{
			propType = prop.type();
			depLvl = prop.dependencyLevel();
			propName = prop.name();
			uniformDist = prop.uniformDistribution();
		}
		catch(Exception e){
			sendError(e);
		}
		
		switch(pageNumber){
		case 0:
			
			try {
				nextBtn.setDisable(!prop.isRanged());
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
				description.setText(prop.description());
			}
			catch(Exception e){
				sendError(e);
				description.setText(">ERROR<");
			}
			
			dependencyLevel=new Text();
			dependencyLevel.setWrappingWidth(w);
			try{
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
			
			add(name,            1, 2);
			add(type,            1, 3);
			add(description,     1, 4);
			add(dependencyLevel, 1, 5);
			
			if(uniformDist){
				add(new Label("Distribution"), 0, 6);
				add(new Text("uniform"),       1, 6);
				
				nextBtn.setDisable(true);
			}
			else if(depLvl > 0){
				Text dependencies = new Text();
				dependencies.setWrappingWidth(w);

				add(new Label("Dependencies"), 0, 6);
				add(dependencies,              1, 6);
				
				String depString = "";
				try{
					List<Integer> deps = prop.dependencies();
					if(deps.size() == 0){
						depString = "(None)";
					}
					for(int i : deps){
						String space = 
							(deps.indexOf(i) == deps.size()-1) ? "" : ", ";
						depString += model.nodeProp_getName(i)+space;
					}
				}
				catch(Exception e){
					depString=">ERROR<";
					sendError(e);
				}
				dependencies.setText(depString);
			}

			List<Integer> rangeIDs;
			
			switch(propType){
			case "AttachmentProperty":
				Text pathogenType = new Text();

				add(new Label("Pathogen Type"), 0, 7);
				add(pathogenType,               1, 7);
				try {
					pathogenType.setText(prop.pathogenType());
				}
				catch(EditorException e){
					pathogenType.setText(">ERROR<");
					sendError(e);
				}
				break;
			case "EnumeratorProperty":
				try {
					rangeIDs = prop.rangeIDs();
				}
				catch(Exception e){
					sendError(e);
					rangeIDs = new ArrayList<>();
				}
				ListView<String> enumValues = new ListView<>();
				enumValues.setPrefSize(100, 140);
				
				for(int rid : rangeIDs){
					try {
						enumValues.getItems().add(prop.rangeLabel(rid));
					} 
					catch (EditorException e) {
						sendError(e);
						enumValues.getItems().add(">ERROR<");
					}
				}
				add(new Label("Enum Values"), 0, 8);
				add(enumValues,               1, 8, 3, 2);
				break;
				
			case "IntegerRangeProperty":
				try {
					rangeIDs = prop.rangeIDs();
				}
				catch(Exception e){
					sendError(e);
					rangeIDs = new ArrayList<>();
				}
				TableView<Integer> rangeItems = new TableView<>();
				TableColumn<Integer, String> labelCol = 
						new TableColumn<>("Label");
				TableColumn<Integer, String> minCol = new TableColumn<>("Min");
				TableColumn<Integer, String> maxCol = new TableColumn<>("Max");
				
				rangeItems.setPrefHeight(140);
				rangeItems.setColumnResizePolicy(
						TableView.CONSTRAINED_RESIZE_POLICY);
				rangeItems.getColumns().addAll(labelCol, minCol, maxCol);
				
				labelCol.setCellValueFactory(col->{
					int rid = col.getValue();
					try{
						return new SimpleStringProperty(prop.rangeLabel(rid));
					}
					catch(Exception e){
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});
				minCol.setCellValueFactory(col->{
					int rid = col.getValue();
					try {
						int min = prop.rangeMin(rid);
						return new SimpleStringProperty(Integer.toString(min));
					} catch (Exception e) {
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});
				maxCol.setCellValueFactory(col->{
					int rid = col.getValue();
					try{
						int max = prop.rangeMax(rid);
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
				add(rangeItems,               1, 8, 3, 2);
				
				break;
			case "BooleanProperty": 
			case "FractionProperty":
				add(new Label("Init Value"), 0, 8);
				String initVal = null;
				try{
					if(propType.equals("BooleanProperty")){
						initVal = Boolean.toString(prop.initBool());
					}
					else if(propType.equals("FractionProperty")){
						initVal = Float.toString(prop.initFraction());
					}
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
					cids.addAll(prop.distributionIDs());
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
				distBPane.setMaxWidth(500);
				distBPane.setMinHeight(150);
				
				//Conditional Distribution navigation
				final IntegerProperty indexProperty = 
						new SimpleIntegerProperty(0);
				
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

				TableView<Entry<Integer, Integer>> conditions =
						new TableView<>();
				
				conditions.setColumnResizePolicy(
						TableView.CONSTRAINED_RESIZE_POLICY);
				
				conditions.setPrefSize(200, 120);
				
				TableColumn<Entry<Integer, Integer>, String> depCol = 
						new TableColumn<>("Dependency");
				
				TableColumn<Entry<Integer, Integer>, String> valCol =
						new TableColumn<>("Value");
				
				conditions.getColumns().addAll(depCol, valCol);
				
				depCol.setCellValueFactory(col->{
					int PID = col.getValue().getKey();
					try{
						return new SimpleStringProperty(
								model.nodeProp_getName(PID));
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
								model.nodeProp_getRangeLabel(PID, RID));
					}
					catch(Exception e){
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});

				TableView<Entry<Integer, Float>> distribution =
						new TableView<>();
				
				distribution.setColumnResizePolicy(
						TableView.CONSTRAINED_RESIZE_POLICY);
				
				distribution.setPrefSize(250, 120);
				
				TableColumn<Entry<Integer, Float>, String> rangeCol = 
						new TableColumn<>("Range");
				
				TableColumn<Entry<Integer, Float>, Number> probCol = 
						new TableColumn<>("Probability");
				
				distribution.getColumns().addAll(rangeCol, probCol);
				
				rangeCol.setCellValueFactory(col->{
					int rid = col.getValue().getKey();
					try{
						return new SimpleStringProperty(prop.rangeLabel(rid));
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
						conditions.getItems().addAll(
								prop.distributionConditions(cid).entrySet());
					} 
					catch (EditorException e) {
						sendError(e);
					}
					
					distribution.getItems().clear();
					try {
						distribution.getItems().addAll(
								prop.distributionMap(cid).entrySet());
					} 
					catch (EditorException e) {
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
			TableColumn<Entry<Integer, Float>, String> rangeCol =
					new TableColumn<>("Range");
			
			TableColumn<Entry<Integer, Float>, Number> probCol = 
					new TableColumn<>("Probability");
			
			distribution.setMinSize(250, 140);
			distribution.setColumnResizePolicy(
					TableView.CONSTRAINED_RESIZE_POLICY);
			
			distribution.getColumns().addAll(rangeCol, probCol);
			
			rangeCol.setCellValueFactory(col->{
				int rid = col.getValue().getKey();
				try {
					return new SimpleStringProperty(prop.rangeLabel(rid));
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
				distribution.getItems().addAll(
						prop.defaultDistribution().entrySet());
			} 
			catch (EditorException e) {
				sendError(e);
			}
			add(new Label("Default Distribution"), 0, row++);
			add(centering, 0, row+=2, 5, 2);
			
			break;
		default:
			sendError(new IllegalStateException(
					"Illegal page number: "+pageNumber));
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private void 
	newPropertyPage(){
		title.setText("New Property");
		
		//Function to see if the next button should be enabled
		Runnable checkNext;
		
		if(pageNumber == 0){
			title.setText(title.getText()+" - Basic Settings");
			
			TextField propName = new TextField();
			
			ComboBox<String> type = new ComboBox<>();
			type.getItems().addAll(model.getPropertyTypes());
			
			TextArea desc = new TextArea();
			desc.setPrefColumnCount(20);
			desc.setPrefRowCount(4);
			desc.setWrapText(true);
			
			ComboBox<Optional<Integer>> layerSelect = new ComboBox<>();
			layerSelect.setCellFactory(lv-> new LayerCell(this) );
			layerSelect.setButtonCell(new LayerCell(this));
			
			layerSelect.getItems().add(Optional.empty());
			for(int i: model.layer_getLayerIDs()){
				layerSelect.getItems().add(Optional.of(i));
			}
			layerSelect.getSelectionModel().select(newPropLayerID);
			layerSelect.setDisable(!model.allowsLayers());
			
			add(new Label("Name"), 0, 2);
			add(new Label("Layer"), 0, 3);
			add(new Label("Type"), 0, 4);
			add(new Label("Description"), 0, 5);
			
			add(propName, 1, 2);
			add(layerSelect, 1, 3);
			add(type, 1, 4);
			add(desc, 1, 5, 1, 2);
			
			checkNext = () -> {
				if(!model.test_nodePropNameIsUnique(propName.getText())){
					sendWarning("Property name already exists: "
								+propName.getText());
					nextBtn.setDisable(true);
				}
				else {
					nextBtn.setDisable(
							type.getValue() == null 
							|| propName.getText() == null
							|| propName.getText().length() == 0 
							|| desc.getText() == null
							|| desc.getText().length() == 0
					);
				}
			};
			
			desc.textProperty().addListener(    e -> checkNext.run());
			propName.textProperty().addListener(e -> checkNext.run());
			type.setOnAction(e -> checkNext.run());
			
			nextBtn.setOnMouseClicked(event->{
				pageNumber ++;
				try {
					if(layerSelect.getValue() != null 
					   && layerSelect.getValue().isPresent())
					{
						model.scratch_newInLayer(
								layerSelect.getValue().get(), 
								propName.getText(), 
								type.getValue(), 
								desc.getText());
					}
					else {
						model.scratch_new(
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
				type = model.scratch_getType();
			} 
			catch(Exception e){
				sendError(e);
			}
			title.setText(title.getText()+" - "+type);
			
			CheckBox useUniform = new CheckBox("Use Uniform Distribution");

			Spinner<Integer> depLvl = new Spinner<>();
			depLvl.setValueFactory(
					new SpinnerValueFactory.IntegerSpinnerValueFactory(0,100));
			
			try {
				if(model.scratch_isRangedProperty()){
					add(useUniform, 0, 8, 3, 1);
					add(new Label("Dependency Level"), 0, 2);
					add(depLvl, 1, 2);
				}
			} catch (EditorException e2) {
				sendError(e2);
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
						model.scratch_useUniformDistribution();
						addedProperty.set(true);
					}
					else {
						int dl = depLvl.getValue();
						model.scratch_setDependencyLevel(dl);
						//Skip dependencies and conditional distributions
						//if the dependency level is 0
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
				
				values.getSelectionModel().setSelectionMode(
						SelectionMode.SINGLE);
				final Callback<Integer, String> valuesFactory = 
					rid ->{
						try {
							return model.scratch_getRangeLabel(rid);
						} catch (EditorException e) {
							sendError(e);
							return ">ERROR<";
						}
					};
				values.setCellFactory(lv ->{
					EditorListCell<Integer> cell = new EditorListCell<>(null);
					cell.setConverter(new StringConverter<Integer>(){
						@Override
						public Integer fromString(String newVal) {
							int r = cell.getItem();
							if(newVal == null || newVal.length() == 0 
									|| newVal.equals("<empty>"))
							{
								return r; //cancel edit if the cell is empty
							}
							try {
								model.scratch_setRangeLabel(r, newVal);
							} 
							catch (EditorException e) {
								sendError(e);
							}
							return r;
						}
						@Override
						public String toString(Integer rid) {
							String s = valuesFactory.call(rid);
							if(s == null || s.length() == 0){
								return "<empty>";
							}
							else{
								return s;
							}
						}
					});
					cell.setTextFieldFactory(valuesFactory);
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
						for(int i : model.scratch_getRangeIDs()){
							String label = model.scratch_getRangeLabel(i);
							if(label == null || label.length() ==0){
								nextBtn.setDisable(true);
								return;
							}
						}
					} 
					catch (Exception e) {
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
						int rid = model.scratch_addRange();
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
							int index = values.getSelectionModel()
									       .getSelectedIndex();
							int rid = values.getItems().get(index);
							model.scratch_removeRange(rid);
							values.getItems().remove(index);
						}
					}
					catch(Exception e){
						sendError(e);
					}
				});
				
				
				
				break;
			case "IntegerRangeProperty":
				
				TableView<Integer> ranges = new TableView<>();
				TableColumn<Integer, String> labelCol = 
						new TableColumn<>("Label");
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
						for(int rid : model.scratch_getRangeIDs()){
							if(!model.scratch_rangeIsSet(rid)){
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
				
				final EditListener<Integer> labelEditL = (event)->{
					try {
						int rid = event.cell()
							.getTableView()
							.getItems()
							.get(event.cell().getIndex());
						model.scratch_setRangeLabel(rid, event.newText());
					} 
					catch (Exception e1) {
						event.cell().cancelEdit();
						event.cell().update();
						sendError(e1);
					}
					finally{
						checkNext.run();
					}
				};
				final Callback<Integer, String> labelFactory =
				(item)->{
					try {
						return model.scratch_getRangeLabel(item);
					}
					catch(EditorException e){
						sendError(e);
						return ">ERROR<";
					}
				};
				
				labelCol.setCellFactory(col -> {
					EditorTableCell<Integer> cell = new EditorTableCell<>();
					cell.setOnEditCommit(labelEditL);
					cell.setTextFieldFactory(labelFactory);
					return cell;
				});
				
				labelCol.setCellValueFactory(data ->{
					String s = labelFactory.call(data.getValue());
					if(s == null || s.length() == 0){
						s = "<empty>";
					}
					return new SimpleStringProperty(s);
				});
				
				final EditListener<Integer> minListener = 
					event->{
						if(event.newText() == null 
						   || event.newText().length() == 0)
						{
							return;
						}
						try{
							int min = Integer.parseInt(event.newText());
							int rid = event.cell().getTableView()
							     .getItems()
							     .get(event.cell().getIndex());
							model.scratch_setRangeMin(rid, min);
						}
						catch(Exception e){
							event.cell().cancelEdit();
							event.cell().update();
							sendError(e);
						}
						finally{
							checkNext.run();
						}
					};
				minCol.setCellFactory(col -> {
					EditorTableCell<Integer> cell = new EditorTableCell<>();
					cell.setOnEditCommit(minListener);
					return cell;
				});

				minCol.setCellValueFactory(data ->{
					try {
						Integer i = 
								model.scratch_getRangeMin(data.getValue());
						if(i == null){
							return new SimpleStringProperty(null);
						}
						else {
							return new SimpleStringProperty(i.toString());
						}
					} catch (Exception e) {
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});
				
				final EditListener<Integer> maxListener = 
					event->{
						if(event.newText() == null 
						   || event.newText().length() == 0)
						{
							return;
						}
						try{
							int max = Integer.parseInt(event.newText());
							int rid = event.cell().getTableView()
							     .getItems()
							     .get(event.cell().getIndex());
							model.scratch_setRangeMax(rid, max);
						}
						catch(Exception e){
							event.cell().cancelEdit();
							event.cell().update();
							sendError(e);
						}
						finally{
							checkNext.run();
						}
					};
					
				maxCol.setCellFactory(col -> {
					EditorTableCell<Integer> cell = new EditorTableCell<>();
					cell.setOnEditCommit(maxListener);
					return cell;
				});
				
				maxCol.setCellValueFactory(data->{
					try{
						Integer i = 
								model.scratch_getRangeMax(data.getValue());
						if(i == null){
							return new SimpleStringProperty(null);
						}
						else {
							return new SimpleStringProperty(i.toString());
						}
					}
					catch (Exception e){
						sendError(e);
						return new SimpleStringProperty(">ERROR<");
					}
				});
				
				ranges.getColumns().addAll(labelCol, minCol, maxCol);
				ranges.setColumnResizePolicy(
						TableView.CONSTRAINED_RESIZE_POLICY);
				
				labelCol.setEditable(true);
				minCol.setEditable(true);
				maxCol.setEditable(true);
				
				
				add.setOnMouseClicked(event->{
					try {
						int rid = model.scratch_addRange();
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
							int index = ranges.getSelectionModel()
									          .getSelectedIndex();
							int rid = ranges.getItems().get(index);
							model.scratch_removeRange(rid);
							ranges.getItems().remove(index);
						}
					}
					catch(Exception e){
						sendError(e);
					}
				});
				
				break;
			case "BooleanRangeProperty":
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
						model.scratch_setPathogenType(pathogenInput.getText());
					} catch (EditorException e1) {
						sendError(e1);
					}
				});
				pathogenInput.textProperty().addListener(event->{
					checkNext.run();
				});
				break;
			case "BooleanProperty":
			case "FractionProperty":
				Node initVal = null;
				final Executor update = new Executor();
				
				if(type.equals("FractionProperty")){
					TextField initFloat = new TextField();
					initFloat.textProperty().addListener((o, oldVal, newVal)->{
						if(!newVal.matches("\\d*\\.?\\d+")){
							nextBtn.setDisable(true);
							initFloat.setText(
									newVal.replaceAll("[^0-9.]", ""));
						}
						else {
							nextBtn.setDisable(false);
						}
					});
					update.setAction( ()->{
						float f = Float.parseFloat(initFloat.getText());
						try {
							model.scratch_setFractionInitValue(f);
						} catch (EditorException e1) {
							sendError(e1);
						}
					});
					initVal = initFloat;
				}
				else if(type.equals("BooleanProperty")){
					ComboBox<Boolean> valSelect = new ComboBox<>();
					valSelect.getItems().addAll(true, false);
					valSelect.setOnAction(event->{
						if(valSelect.getValue() != null){
							nextBtn.setDisable(false);
						}
					});
					update.setAction(()->{
						boolean b = valSelect.getValue();
						try{
							model.scratch_setBooleanInitValue(b);
						}
						catch(EditorException e){
							sendError(e);
						}
					});
					initVal = valSelect;
				}

				nextBtn.setText("Finish");
				
				add(new Label("Value"), 0, 4);
				add(initVal, 1, 4);
				
				nextBtn.setOnMouseClicked(e->{
					try{
						update.run();
						addedProperty.set(true);
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
			TableColumn<PropertyID, String> nameCol = 
					new TableColumn<>("Name");
			TableColumn<PropertyID, String> typeCol = 
					new TableColumn<>("Type");
			
			nameCol.setCellValueFactory(new PropertyNameFactory(this));
			typeCol.setCellValueFactory(new PropertyTypeFactory(this));
			
			TableView<PropertyID> scratchDependencies = new TableView<>();

			TableColumn<PropertyID, String> nameCol2 = 
					new TableColumn<>("Name");
			TableColumn<PropertyID, String> typeCol2 = 
					new TableColumn<>("Type");
			
			nameCol2.setCellValueFactory(new PropertyNameFactory(this));
			typeCol2.setCellValueFactory(new PropertyTypeFactory(this));
			
			
			potentialDependencies.getColumns().addAll(nameCol, typeCol);
			potentialDependencies.setPrefSize(220, 200);
			
			scratchDependencies.setPrefWidth(
					potentialDependencies.getPrefWidth());
			
			scratchDependencies.setPrefHeight(
					potentialDependencies.getPrefHeight());
			
			
			scratchDependencies.getColumns().addAll(nameCol2, typeCol2);
			
			
			scratchDependencies.setColumnResizePolicy(
					TableView.CONSTRAINED_RESIZE_POLICY);
			potentialDependencies.setColumnResizePolicy(
					TableView.CONSTRAINED_RESIZE_POLICY);
			
			try{
				for(int pid : model.scratch_getPotentialDependencies()){
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
					int i = potentialDependencies.getSelectionModel()
							                     .getSelectedIndex();
					PropertyID pid = potentialDependencies.getItems().get(i);
					try{
						model.scratch_addDependency(pid.pid());
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
					int i = scratchDependencies.getSelectionModel()
							                   .getSelectedIndex();
					PropertyID pid= scratchDependencies.getItems().get(i);
					try{
						model.scratch_removeDependency(pid.pid());
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
					if(model.scratch_getDependencies().isEmpty()){
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
					model.scratch_reorderConditionalDistributions(
							distributions.getItems());
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
					int index = distributions.getSelectionModel()
							                 .getSelectedIndex();
					try {
						model.scratch_removeConditionalDistribution(
								distributions.getItems().get(index));
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

					final DistributionTable distMap = 
							new DistributionTable(this);
					distMap.setPrefSize(300, 250);
					distMap.setId("distMap");
					
					final ConditionsMenu condsMenu = 
							new ConditionsMenu(model, this);
					condsMenu.setPrefSize(300, 250);
					condsMenu.setId("condMenu");

					distCreator.add(new Label("Conditions"), 0, 0);
					distCreator.add(new Label("Probabilities"), 1, 0);
					distCreator.add(condsMenu, 0, 1);
					distCreator.add(distMap,  1, 1);
					
					finBtn.disableProperty().bind( 
							distMap.readyProperty()
							       .and(condsMenu.readyProperty()).not() );
					
					finBtn.setOnMouseClicked(e->{
						try{
							int cid = model.scratch_addConditionalDistribution(
									condsMenu.getConditions(), 
									distMap.getProbMap());
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
					DistributionTable distMap = 
							(DistributionTable) lookup("#distMap");
					ConditionsMenu condMenu = 
							(ConditionsMenu) lookup("#condMenu");
					
					try{
						if(distMap == null || condMenu == null){
							throw new IllegalStateException("Missing "
									+ "distribution map/conditions menu!");
						}
						
						Map<Integer, Float> probMap = 
								model.scratch_getDistribution(cid);
						Map<Integer, Integer> conditions = 
								model.scratch_getDistributionCondition(cid);
						distMap.setPropMap(probMap);
						condMenu.setCondiditions(conditions);
						distMap.refresh();
					}
					catch(Exception e){
						sendError(e);
					}
					finBtn.setOnMouseClicked(event->{
						try {
							model.scratch_updateConditionalDistribution(
									cid, condMenu.getConditions() , 
									distMap.getProbMap());
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
						model.scratch_setDefaultDistribution(
								distMap.getProbMap());
						addedProperty.set(true);
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
			sendError(new IllegalStateException("Invalid page number: "
					                            +pageNumber));
		}
	}
	
	/**
	 * Get the internal {@link PropertiesEditor} this 
	 * page interfaces with.
	 * @return The {@link PropertiesEditor} currently used by the editor
	 */
	public PropertiesEditor 
	model() {
		return model;
	}

	public void setModel(PropertiesEditor model){
		this.model = model;
		finished.set(true);
	}
	/**
	 * If the editor page has finished editing a property 
	 * or layer, this is set to true.
	 * @return The finished property, in read only form.
	 */
	public ReadOnlyBooleanProperty 
	finishedProperty(){
		return finished;
	}
	/**
	 * If the editor is moving to another page, this is set to true
	 * @return A read only version of the advance page Property.
	 */
	public ReadOnlyBooleanProperty 
	advancePageProperty(){
		return advancePage;
	}
	/**
	 * The messages the editor has received
	 * @return A read only copy of the editor messages
	 */
	public ReadOnlyListProperty<Message> 
	messagesProperty(){
		return messages;
	}
	/**
	 * A property for if a layer was added
	 * @return The property denoting if a layer was added
	 */
	public ReadOnlyBooleanProperty
	addedLayerProperty(){
		return addedLayer;
	}
	public ReadOnlyBooleanProperty
	addedPathogenProperty(){
		return addedPathogen;
	}
	public void 
	requireWidth(double w){
		setPrefWidth(w);
		setMaxWidth(w);
		setMinWidth(w);
	}
}
