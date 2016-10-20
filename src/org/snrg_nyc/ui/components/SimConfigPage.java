package org.snrg_nyc.ui.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;

import java.util.Arrays;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.SimConfigEditor;
import org.snrg_nyc.ui.EditorPage;
import org.snrg_nyc.util.Executor;

/**
 * A UI page for editing SimConfig settings.
 * @author Devin Hastings
 *
 */
public class SimConfigPage extends ScrollPane {
	private SimConfigEditor config;
	private EditorPage editor;
	private GridPane grid = new GridPane();
		
	private BooleanProperty closeProperty = new SimpleBooleanProperty(false);
	
	private final Executor onNext = new Executor();
	private final Executor onClose = new Executor();
	private BooleanProperty readyProperty = new SimpleBooleanProperty(false);
	
	//The row of the grid
	private int row;
	
	public SimConfigPage(EditorPage editor){
		this.editor = editor;
		this.config = editor.model();
		grid.setVgap(editor.getVgap());
		grid.setHgap(editor.getHgap());
		grid.setPadding(editor.getInsets());
		
		setTitle("Select Configuration");

		CheckBox newconf = new CheckBox();
		grid.add(new Label("New Configuration"), 0, row);
		grid.add(newconf, 1, row++);
		
		Label tLabel = new Label("Templates:");
		ComboBox<String> templates = new ComboBox<>();
		
		grid.add(tLabel, 0, row);
		grid.add(templates, 1, row++);
		try {
			templates.getItems().setAll(config.config_getTemplates());
		} catch (EditorException e) {
			editor.sendError(e);
		}
		BooleanProperty textReady = new SimpleBooleanProperty(false);
		templates.valueProperty().addListener((val,o,string)->{
			textReady.set(string != null && string.length() > 0);
		});
		
		Label cLabel=  new Label("Configurations");
		ListView<Integer> configs = new ListView<>();
		configs.setMaxHeight(200);
		configs.setMaxWidth(300);
		grid.add(cLabel, 0, row);
		grid.add(configs, 1, row++);
		
		try {
			configs.getItems().setAll(config.config_getIDs());
		} 
		catch (EditorException e1) {
			editor.sendError(e1);
		}
		
		configs.setCellFactory(data->{
			ListCell<Integer> c = new ListCell<Integer>(){
				@Override
				public void updateItem(Integer item, boolean empty){
					super.updateItem(item, empty);
					if(!empty){
						try {
							setText(config.config_getString(item,"FSM_ID"));
						} 
						catch (EditorException e) {
							setText(">ERROR<");
							editor.sendError(e);
						}
					}
					else {
						setText(null);
					}
				}
			};
			return c;
		});
		
		BooleanProperty selReady = new SimpleBooleanProperty(false);
		configs.getSelectionModel()
		       .selectedItemProperty()
		       .addListener((v,o,s)->
		{
			selReady.set(s != null);
		});
		
		templates.disableProperty().bind(newconf.selectedProperty().not());
		tLabel.disableProperty().bind(templates.disableProperty());
		
		configs.disableProperty().bind(newconf.selectedProperty());
		cLabel.disableProperty().bind(configs.disableProperty());
		
		readyProperty.bind(
				newconf.selectedProperty()
				.and(textReady)
				.or(selReady));
		
		onNext.setAction(()->{
			try {
				if(newconf.isSelected()){
					newConfig(templates.getValue());
				}
				else {
					this.existingConfig(
							configs.getSelectionModel().getSelectedItem());
				}
			} catch (Exception e) {
				editor.sendError(e);
			}
		});
		onClose.setAction(()->{
			return;
		});
		
		setButtons("Next");
		
		setContent(grid);
	}
	
	private void
	existingConfig(int confID) throws EditorException{
		grid.getChildren().clear();
		setTitle(config.config_getString(confID, "FSM_ID"));
		viewConfig(confID);
		onNext.setAction(()->{
			closeProperty.set(true);
		});
		setButtons("Finish");
	}
	
	private void 
	newConfig(String template) throws EditorException {
		int confID = config.config_newFromTemplate(template);
		editor.sendInfo("Created new SimConfig with ID: "+confID);
		grid.getChildren().clear();
		setTitle("New "+ template);
		
		viewConfig(confID);
		
		onClose.setAction(()->{
			try {
				editor.sendInfo("Deleting Simconfig "+confID);
				config.config_delete(confID);
			} catch (Exception e) {
				editor.sendError(e);
			}
		});
		onNext.setAction(()->{
			closeProperty.set(true);
		});
		setButtons("Finish");
	}
	
	/**
	 * Put the information from a {@link SimConfig} object up on the page.
	 * @param confID The ID of the config object
	 * @throws EditorException Thrown if it's somehow not valid 
	 * (likely a bad ID)
	 */
	private void
	viewConfig(int confID) throws EditorException{
		for(String key : config.config_getKeys(confID)){
			if(config.config_isMap(confID, key)){
				Label l = new Label(key);
				l.setFont(Fonts.boldReg);
				
				HBox lbox = new HBox();
				lbox.setPadding(new Insets(5, 0, 0, 0));
				lbox.setAlignment(Pos.TOP_LEFT);
				lbox.getChildren().add(l);
				grid.add(lbox, 0, row);
				grid.add(getSubMap(confID), 1, row++);
			}
			else {
				grid.add(new Label(key), 0, row);
				TextField text = 
						new TextField(config.config_getString(confID, key));
				text.setMaxWidth(150);
				text.setOnAction(event->{
					try {
						config.config_setString(confID, text.getText(), key);
					} catch (Exception e) {
						editor.sendError(e);
					}
				});
				grid.add(text, 1, row++);
			}
		}
	}
	
	private Node
	getSubMap(int confID, String...keys) throws EditorException{
		int subrow = 0;
		GridPane subPane = new GridPane();
		subPane.setVgap(10);
		subPane.setHgap(10);
		
		for(String key : config.config_getKeys(confID, keys)) {
			
			String[] nextkeys = Arrays.copyOf(keys, keys.length+1);
			nextkeys[keys.length]=key;
			if(config.config_isMap(confID, nextkeys)){
				Label l = new Label(key);
				l.setFont(Fonts.boldReg);
				
				HBox lbox = new HBox();
				lbox.setPadding(new Insets(5, 0, 0, 0));
				lbox.setAlignment(Pos.TOP_LEFT);
				lbox.getChildren().add(l);
				subPane.add(lbox, 0, subrow);
				
				subPane.add(getSubMap(confID, nextkeys), 1, subrow++);
			}
			else {
				subPane.add(new Label(key), 0, subrow);
				TextField text = 
						new TextField(
								config.config_getString(confID, nextkeys));
				text.setMaxWidth(150);
				text.maxWidth(100);
				text.setOnAction(event->{
					try {
						config.config_setString(
								confID, text.getText(), nextkeys);
					} catch (Exception e) {
						editor.sendError(e);
					}
				});
				subPane.add(text, 1	, subrow++);
			}
		}
		return subPane;
	}

	private void 
	setTitle(String title){
		row = 0;
		Label l = new Label("SimConfig Editor - "+title);
		l.setFont(Fonts.titleFont);
		
		grid.add(l, 0, row++, 2, 1);
		
		Line bar = new Line();
		bar.setStartX(0);
		bar.setStartY(0);
		bar.setEndX(500);
		bar.setEndY(0);
		grid.add(bar, 0, row++, 2, 1);
		
	}
	
	private void 
	setButtons(String nextName){
		Button cancel = new Button("Cancel");
		cancel.setOnAction(e -> closeProperty.set(true));
		
		Button next = new Button(nextName);
		next.setOnAction(e->onNext.run());
		
		next.disableProperty().bind(readyProperty.not());
		HBox nbox = new HBox();
		
		nbox.setAlignment(Pos.CENTER_RIGHT);
		nbox.getChildren().add(next);
		
		grid.add(cancel, 0, row+3);
		grid.add(nbox, 1, row+3);
		
	}
	
	public ReadOnlyBooleanProperty 
	closeProperty(){
		return closeProperty;
	}
}
