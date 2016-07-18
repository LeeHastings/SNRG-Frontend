package org.snrg_nyc.model;

/**
 * A class for editing the properties in a pathogen.
 * The only public methods are those listed in {@link PropertiesEditor}.
 * @author Devin Hastings
 *
 */
public class PathogenEditor extends PropertiesEditor_Impl{
	
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

	@Override
	public void load(String experimentName) throws EditorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int pathogen_create(String name) throws EditorException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public PropertiesEditor pathogen_getEditor(int pathID) throws EditorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?>[] getPropertyClasses() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
	
	