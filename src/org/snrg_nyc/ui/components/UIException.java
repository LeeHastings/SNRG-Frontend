package org.snrg_nyc.ui.components;

/**
 * A checked exception specifically for UI errors, 
 * particularly for failing while being constructed
 * 
 * @author Devin Hastings
 *
 */
public class UIException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public UIException(String message){
		super(message);
	}
	
}
