package org.snrg_nyc.ui;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

class UI_Message {
	private String msg;
	private Color color;
	private Type msgType;
	
	enum Type{
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
