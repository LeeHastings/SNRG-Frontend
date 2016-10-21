package org.snrg_nyc.util;

/**
 * A class for sending information to and from different objects.
 * @author Devin Hastings
 *
 */
public class Message {
	public static interface MessageHandler{
		public void recieve(Message m);
	}
	public enum Type{
		INFO,
		WARNING, 
		ERROR
	}
	private String text;
	private Type type;
	private Message(String text, Type type){
		this.text = text;
		this.type = type;
	}
	
	public static Message info(String text){
		return new Message(text, Type.INFO);
	}
	public static Message warning(String text){
		return new Message(text, Type.WARNING);
	}
	public static Message error(String text){
		return new Message(text, Type.ERROR);
	}
	public Type type(){
		return type;
	}
	public String text(){
		return text;
	}
	public String toString(){
		return type.name()+": "+text;
	}
}
