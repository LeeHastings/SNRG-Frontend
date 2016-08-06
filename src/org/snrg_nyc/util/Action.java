package org.snrg_nyc.util;

/**
 * An interface similar to {@link Runnable}, but the function throws an
 * exception.
 * @author Devin Hastings
 *
 * @param <T> The type of exception thrown by the function;
 */
public interface Action<T extends Exception> {
	public void run() throws T;
}
