package org.snrg_nyc.model.editor;

import org.snrg_nyc.model.BooleanProperty;
import org.snrg_nyc.model.EnumeratorProperty;
import org.snrg_nyc.model.FractionProperty;
import org.snrg_nyc.model.IntegerRangeProperty;
import org.snrg_nyc.model.PropertiesEditor;


/**
 * A class for editing the properties in a pathogen.
 * The only public methods are those listed in {@link PropertiesEditor}.
 * @author Devin Hastings
 *
 */
public abstract class PathogenEditor implements PropertiesEditor{
	
	/*         *\
	 * Members *
	\*         */

	/** Node Property classes that can be created in the editor */
	static final Class<?>[] pathogenPropertyTypes = {
			EnumeratorProperty.class, 
			IntegerRangeProperty.class, 
			BooleanProperty.class,
			FractionProperty.class
		};
	
}
	
	