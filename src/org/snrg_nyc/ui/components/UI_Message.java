package org.snrg_nyc.ui.components;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * A small class for delivering and displaying messages on the {@link org.snrg_nyc.ui.EditorWindow} 
 * @author Devin Hastings
 *
 */
public class UI_Message {
	private String msg;
	private Color color;
	private Type msgType;
	
	public enum Type{
		Info,
		Warning,
		Error
	}
	public UI_Message(String message, Type messageType){
		msg = message != null? message : ">ERROR NO MESSAGE<";
		msgType = messageType;
		
		switch(msgType){ 
		case Info:
			color = Color.CADETBLUE;
			break;
		case Warning:
			color = Color.FIREBRICK;
			break;
		case Error:
			color = Color.RED;
			break;
		}
	}
	public Text getMessageUI(){
		Text tx = new Text(msg);
		tx.setFill(color);
		return tx;
	}
	public String getText(){
		return msgType.name()+": "+msg;
	}
}
