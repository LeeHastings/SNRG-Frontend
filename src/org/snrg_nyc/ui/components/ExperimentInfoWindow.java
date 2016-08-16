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
		
		Text statusText = new Text();
		statusText.setWrappingWidth(300);
		
		TextField nameText= new TextField(model.experiment_getName());
		
		TextArea descText = new TextArea(model.experiment_getDescription());
		descText.setPrefColumnCount(15);
		descText.setPrefRowCount(3);
		descText.setWrapText(true);
		
		TextField userText = new TextField(model.experiment_getUserName());
		
		Button close = new Button("Close");
		close.setOnMouseClicked(event-> closeProperty.set(true));
		
		Button apply = new Button("Apply");
		apply.setOnMouseClicked(event->{
			try {
				model.experiment_setDescription(descText.getText());
				model.experiment_setUserName(userText.getText());
				model.experiment_setName(nameText.getText());
				statusText.setText(String.format(
						"Update successful.  "
						+ "\nName: %s \nUsername: %s \nDescription: %s",
						model.experiment_getName(),
						model.experiment_getUserName(), 
						model.experiment_getDescription()
						));
			} 
			catch (EditorException e) {
				statusText.setText("An error occured: "+e.getMessage());
				e.printStackTrace();
			}
		});
		
		add(title, 0, 0, 3, 1);
		
		add(new Label("Name"), 0, 2);
		add(nameText, 2, 2);
		
		add(new Label("Description"), 0,3);
		add(descText, 2, 3, 1, 2);
		
		add(new Label("User Name"),0,5);
		add(userText, 2, 5);
		
		add(statusText, 0, 6, 3, 1);
		add(close, 0, 7);
		add(apply, 2, 7);
	}
	public ReadOnlyBooleanProperty
	closeProperty(){
		return closeProperty;
	}
}
