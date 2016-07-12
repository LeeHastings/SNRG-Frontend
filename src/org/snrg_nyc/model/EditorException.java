package org.snrg_nyc.model;

/**
 * An internal class for exceptions specific to the UI Interface
 * @author Devin Hastings
 *
 */
public class EditorException extends Exception{
	private static final long serialVersionUID = 1L;
	public EditorException(String message){
		super(message);
	}
	public EditorException(){
		super();
	}
}
