package org.snrg_nyc.ui.components;

import org.snrg_nyc.model.PropertiesEditor;

import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class ExperimentInfoWindow extends GridPane {
	public ExperimentInfoWindow(PropertiesEditor model)throws UIException{
		if(!model.hasExperimentInfo()) {
			throw new UIException(
					"This editor does not have experiment info!");
		}
		Text title = new Text("Experiment Info");
		title.setFont(Fonts.titleFont);
		add(title, 0, 0, 3, 1);
	}
}
