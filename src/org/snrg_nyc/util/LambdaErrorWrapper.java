package org.snrg_nyc.util;


/**
 * A small utility for controlling errors in a lambda expression
 * @author Devin Hastings
 *
 * @param <T> The type of the error
 */
public class LambdaErrorWrapper<T extends Throwable> {
	private T exception = null;
	
	public LambdaErrorWrapper(){}
	public void 
	setError(T e){
		System.err.println("Caught lambda exception "+e.toString());
		this.exception = e;
	}
	public boolean 
	hasError(){
		return exception != null;
	}
	/**
	 * If there is an error set, throw that error
	 * @throws T The error set in the object
	 */
	public void 
	validate() throws T{
		if(exception != null){
			throw exception;
		}
	}
}
