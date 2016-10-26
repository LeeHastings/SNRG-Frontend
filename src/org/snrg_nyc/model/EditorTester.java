package org.snrg_nyc.model;


/**
 * An interface extending {@link PropertiesEditor} with some methods
 * that are useful for testing, such as changing the serializer.
 * @author Devin Hastings
 *
 */
public interface EditorTester extends PropertiesEditor {
	/**
	 * This sets if the {@link PropertiesEditor#save(String)} method
	 * will save normally or print to the console.
	 * @param print True to print to the console, false to save as usual
	 */
	public void 
	utest_setPrintMode(boolean print);
	
	/**
	 * Load an experiment name, and print debug messages
	 * @param name The experiment name to load from
	 * @throws EditorException Thrown if {@link PropertiesEditor#load(String)}
	 * throws an exception.
	 */
	public void 
	utest_loadWithMessages(String name) throws EditorException;
}
