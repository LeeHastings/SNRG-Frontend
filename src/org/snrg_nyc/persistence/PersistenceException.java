package org.snrg_nyc.persistence;

/**
 * A superclass for all persistence-based exceptions
 * @author devin
 *
 */
public class PersistenceException extends  Exception {
	private static final long serialVersionUID = 1L;
	
	public PersistenceException(){
		super();
	}
	public PersistenceException(String message){
		super(message);
	}
}
