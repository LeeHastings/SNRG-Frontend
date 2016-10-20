package org.snrg_nyc.ui.components;


import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * A small class for delivering and displaying messages from the 
 * {@link org.snrg_nyc.ui.EditorPage}
 * @author Devin Hastings
 *
 */
public class UI_Message {
	private String msg;
	private Color color;
	private Type msgType;
	
	private enum Type{
		Info,
		Warning,
		Error
	}
	private UI_Message(String message, Type messageType){
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
	
	public static UI_Message error(String message){
		return new UI_Message(message, Type.Error);
	}
	public static UI_Message info(String message){
		return new UI_Message(message, Type.Info);
	}
	public static UI_Message warning(String message){
		return new UI_Message(message, Type.Warning);
	}
	
	public Text 
	getMessageUI(){
		Text tx = new Text(msg);
		tx.setFill(color);
		return tx;
	}
	public String 
	getText(){
		return msgType.name()+": "+msg;
	}
	@Override
	public String
	toString(){
		return "UI_Message - "+getText();
	}
}
