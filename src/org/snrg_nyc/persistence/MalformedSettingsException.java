package org.snrg_nyc.persistence;

/**
 * An exception thrown when the configuration files for a property are somehow
 * malformed
 * @author Devin
 *
 */
public class MalformedSettingsException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public MalformedSettingsException(String name) {
		super(name);
	}
	public MalformedSettingsException(){
		super();
	}

}
