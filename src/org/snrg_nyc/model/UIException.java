package org.snrg_nyc.model;

/**
 * An internal class for exceptions specific to the UI Interface
 * @author Devin
 *
 */
public class UIException extends Exception{
	private static final long serialVersionUID = 1L;
	public UIException(String message){
		super(message);
	}
	public UIException(){
		super();
	}
}
