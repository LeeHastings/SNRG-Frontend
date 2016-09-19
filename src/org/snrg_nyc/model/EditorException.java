package org.snrg_nyc.model;

/**
 * An internal class for exceptions specific to the {@link PropertiesEditor}
 * and any classes in the internal model.  All instances of the exception
 * should have informative messages about their cause, as this class is a very
 * broad catch-all for issues while editing.
 * @author Devin Hastings
 *
 */
public class EditorException extends Exception{
	private static final long serialVersionUID = 1L;
	public EditorException(String message){
		super(message);
	}
}
