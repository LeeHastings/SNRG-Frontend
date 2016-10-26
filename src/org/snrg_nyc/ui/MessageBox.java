package org.snrg_nyc.ui;

import org.snrg_nyc.util.Message;

import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
			(Change<? extends Message> c)-> {
				Text t;
				double w = messagePane.getWidth()-20;
				c.next();
				if(c.getRemovedSize() > 0){
					messageBox.getChildren().clear();
					for(Message m : editor.messagesProperty()){
						t = toUI(m);
						t.setWrappingWidth(w);
						messageBox.getChildren().add(t);
						System.out.println(m);
					}
				} 
				else {
					for(Message m : c.getAddedSubList()){
						t = toUI(m);
						t.setWrappingWidth(w);
						messageBox.getChildren().add(t);
						System.out.println(m);
					}
				}
			});
	}
	private Text toUI(Message m){
		Text t = new Text(m.text());
		Color color = Color.BLACK; //For future unknown types
		switch(m.type()){
		case INFO:
			color = Color.CADETBLUE;
			break;
		case WARNING:
			color = Color.FIREBRICK;
			break;
		case ERROR:
			color = Color.RED;
			break;
		}
		t.setFill(color);
		return t;
	}
}
