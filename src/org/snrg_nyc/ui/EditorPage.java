package org.snrg_nyc.ui;

import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.ui.EditorPage.Mode;
import org.snrg_nyc.ui.components.PropertyID;
import org.snrg_nyc.ui.components.UI_Message;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public abstract class EditorPage extends GridPane {
	
	public enum Mode{
		NEW_PROP,
		NEW_LAYER,
		VIEW_PROP,
		IDLE
	}

	protected PropertiesEditor ui;
	
	protected int pageNumber = 0;
	protected PropertyID propViewerID;
	protected Text title;
	protected Line hbar;
	protected Button nextBtn, cancel;
	
	protected final BooleanProperty advancePage = new SimpleBooleanProperty();
	protected final BooleanProperty finished = new SimpleBooleanProperty();
	protected final StringProperty layerName = new SimpleStringProperty();
	
	protected final Font titleFont = Font.font("sans", FontWeight.LIGHT, FontPosture.REGULAR, 20);
	protected Mode mode;

	protected final ListProperty<UI_Message> messages = new SimpleListProperty<UI_Message>();
	
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
	
	public abstract PropertiesEditor getModel();
	
	public abstract ListProperty<UI_Message> messages();
	public abstract BooleanProperty finished();
	public abstract BooleanProperty advancePage();
	public abstract StringProperty layerName();
	
	public abstract void viewProperty(PropertyID pid);
	public abstract void createProperty();
	public abstract void createLayer();
}
