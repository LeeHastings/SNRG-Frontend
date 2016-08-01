package org.snrg_nyc.model;

import java.util.Map;

import org.snrg_nyc.model.internal.BooleanProperty;
import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.model.internal.EnumeratorProperty;
import org.snrg_nyc.model.internal.FractionProperty;
import org.snrg_nyc.model.internal.IntegerRangeProperty;

/**
 * A class for editing the properties in a pathogen.
 * The only public methods are those listed in {@link PropertiesEditor}.
 * @author Devin Hastings
 *
 */
class PathogenEditor extends PropertiesEditor_Impl{
	
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
	private NodeEditor parent; 
	private PathogenSettings pathSettings;
	/*         *\
	 * Methods *
	\*         */

	@Override
	protected Class<?>[] getPropertyClasses() {
		return pathogenPropertyTypes;
	}
	/**
	 * Create a new pathogen editor
	 * @param parent The {@link NodeEditor} this pathogen is attached to.
	 * @param pathogen The name of the pathogen
	 * @throws EditorException Thrown if the given parent is not an instance of a {@link NodeEditor}
	 */
	public PathogenEditor(NodeEditor parent, String pathogen) throws EditorException{
		super();
		this.parent = parent;
		
		pathSettings = new PathogenSettings(pathogen);
		
		pathSettings.setLayerAttributesList(layers);
		pathSettings.setPropertyDefinitionList(properties);
	}
	/**
	 * Create a pathogen Editor from a map of objects and settings
	 * @param parent
	 * @param settings
	 * @param objects
	 * @throws EditorException
	 */
	PathogenEditor(NodeEditor parent, PathogenSettings settings,
			Map<String, Transferable> objects) throws EditorException
	{
		super();
		this.parent = parent;
		pathSettings = settings;
		layers = settings.getLayerAttributesList();
		properties = settings.getPropertyDefinitionList();
		loadDistributions(objects);
	}
	public String getPathogen(){
		return pathSettings.getName();
	}
	@Override
	protected Map<String, Transferable> getSavedObjects() throws EditorException{
		Map<String, Transferable> map = super.getSavedObjects();
		map.put("pathogensettings_"+pathSettings.getName(), pathSettings);
		return map;
	}
	@Override
	public boolean test_nodePropNameIsUnique(String name) {
		if(parent == null){
			System.err.println("Pathogen Editor for "+getPathogen()+" has a null parent!");
		}
		return parent.test_nodePropNameIsUnique(name);
	}

}
	
	