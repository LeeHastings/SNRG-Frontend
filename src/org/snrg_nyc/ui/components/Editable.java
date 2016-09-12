package org.snrg_nyc.ui.components;

import javafx.util.Callback;

/**
 * An interface for custom editable content, such as table and list cells
 * @author devin
 *
 * @param <T> The type of the data stored in the cell.
 */
public interface Editable<T> {
	/**
	 * If the text field should have a different value factory than
	 * the unfocused cell, this factory is what you'd use.
	 * If this isn't called, the text field and regular cell should have the
	 * same text.
	 * @param factory The function used to populate the cell when editing
	 */
	public void
	setTextFieldFactory(Callback<T, String> factory);
}
