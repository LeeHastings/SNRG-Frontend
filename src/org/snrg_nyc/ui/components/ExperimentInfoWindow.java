package org.snrg_nyc.ui.components;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.PropertiesEditor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class ExperimentInfoWindow extends GridPane {
	private BooleanProperty closeProperty = new SimpleBooleanProperty(false);
	
	public ExperimentInfoWindow(PropertiesEditor model)
			throws UIException, EditorException
	{
		if(!model.hasExperimentInfo()) {
			throw new UIException(
					"This editor does not have experiment info!");
		}
		setPrefSize(400, 250);
		setPadding(new Insets(15));
		setHgap(10);
		setVgap(10);
		
		Text title = new Text("Experiment Info");
		title.setFont(Fonts.titleFont);
		
		TextArea descText = new TextArea(model.experiment_getDescription());
		descText.setPrefColumnCount(15);
		descText.setPrefRowCount(4);
		descText.setWrapText(true);
		
		TextField userText = new TextField(model.experiment_getUserName());
		
		Button close = new Button("Close");
		close.setOnMouseClicked(event-> closeProperty.set(true));
		
		Button apply = new Button("Apply");
		apply.setOnMouseClicked(event->{
			try {
				model.experiment_setDescription(descText.getText());
				model.experiment_setUserName(userText.getText());
			} 
			catch (EditorException e) {
				add(new Text("An error occured: "+e.getMessage()), 1, 0, 1, 3);
				e.printStackTrace();
			}
		});
		
		add(title, 0, 0, 3, 1);
		add(new Label("Description"), 0,2);
		add(descText, 2, 2, 1, 2);
		add(new Label("User Name"),0,4);
		add(userText, 2, 4);
		add(close, 0, 6);
		add(apply, 2, 6);
	}
	public ReadOnlyBooleanProperty
	closeProperty(){
		return closeProperty;
	}
}
