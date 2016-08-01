package org.snrg_nyc.util;

/**
 * A wrapper for {@link Runnable} for use in lambdas.
 * <p>
 * One Executor to perform different actions based on outside parameters, 
 * and can then be used inside a lambda unlike a Runnable instance.
 * @author Devin Hastings
 *
 */
public class Executor {
	private Runnable action;
	public Executor(){}
	
	public void setAction(Runnable action){
		this.action = action;
	}
	public Runnable getAction(){
		return action;
	}
	public void run(){
		action.run();
	}
}
