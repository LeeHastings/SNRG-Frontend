package org.snrg_nyc.ui;

import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.ui.components.PropertyID;
import org.snrg_nyc.ui.components.UI_Message;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public interface EditorPage {
	
	public enum Mode{
		NEW_PROP,
		NEW_LAYER,
		VIEW_PROP,
		IDLE
	}
	
	public void sendError(Exception e);
	public void sendInfo(String s);
	public void sendWarning(String s);
	
	public PropertiesEditor getModel();
	
	public Node lookup(String string);
	
	public ListProperty<UI_Message> messages();
	public BooleanProperty finished();
	public BooleanProperty advancePage();
	public StringProperty layerName();
	
	public Region asRegion();
	
	public void viewProperty(PropertyID pid);
	public void createProperty();
	public void createLayer();
}
