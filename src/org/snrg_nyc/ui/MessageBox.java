package org.snrg_nyc.ui;

import org.snrg_nyc.ui.components.UI_Message;

import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MessageBox extends VBox {
	public MessageBox(EditorPage editor){
		setPadding(new Insets(10,5,5,10));
		VBox messageBox = new VBox();
		messageBox.setMinHeight(80);
		
		ScrollPane messagePane = new ScrollPane();
		messagePane.setContent(messageBox);
		messagePane.setMaxHeight(100);
		messagePane.setMinHeight(messageBox.getMinHeight()+10);
		messagePane.setFitToWidth(true);
		messagePane.setPadding(new Insets(5));
		messagePane.setHbarPolicy(ScrollBarPolicy.NEVER);
		
		messageBox.heightProperty().addListener((prop, oldval, newval)->{
			messagePane.setVvalue((Double) newval);
		});

		getStyleClass().add("messages");
		messageBox.getStyleClass().add("message-text");
		
		Button messageClear = new Button("Clear Messages");
		messageClear.setOnMouseClicked(event-> 
				editor.messagesProperty().clear());

		getChildren().addAll(new Label("Messages:"), messagePane, messageClear);
		
		/*
		 * Get the messages in the editor, convert them to UI Text, and then
		 * insert them into the messages box.  If messages were removed,
		 * it simply deletes all the text elements and gets them again
		 */
		editor.messagesProperty().addListener(
			(Change<? extends UI_Message> c)-> {
				Text t;
				double w = messagePane.getWidth()-20;
				c.next();
				if(c.getRemovedSize() > 0){
					messageBox.getChildren().clear();
					for(UI_Message m : editor.messagesProperty()){
						t = m.getMessageUI();
						t.setWrappingWidth(w);
						messageBox.getChildren().add(t);
						System.out.println(m.getText());
					}
				} 
				else {
					for(UI_Message m : c.getAddedSubList()){
						t = m.getMessageUI();
						t.setWrappingWidth(w);
						messageBox.getChildren().add(t);
						System.out.println(m.getText());
					}
				}
			});
	}
}
