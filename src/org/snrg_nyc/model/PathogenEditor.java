package org.snrg_nyc.model;

import java.io.Serializable;
import java.util.List;
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
public class PathogenEditor extends PropertiesEditor_Impl{
	
	/*         *\
	 * Members *
	\*         */

	/** Node Property classes that can be created in the editor */
	private static final Class<?>[] pathogenPropertyTypes = {
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
	
	public PathogenEditor(PropertiesEditor parent, String pathogen) throws EditorException{
		super();
		if(!(parent instanceof NodeEditor)){
			throw new EditorException("The given parent is not a Node Properties Editor!");
		}
		else {
			this.parent = (NodeEditor) parent;
		}
		pathSettings = new PathogenSettings(pathogen);
		
		pathSettings.setLayerAttributesList(layers);
		pathSettings.setPropertyDefinitionList(properties);
	}
	PathogenEditor(NodeEditor parent, PathogenSettings settings,
			Map<String, Transferable> objects) throws EditorException
	{
		super();
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
	public void save(String experimentName) throws EditorException{
		throw new EditorException("This should not be called directly by the pathogen editor!");
	}
	@Override
	public void load(String experimentName) throws EditorException {
		throw new EditorException("This should not be called directly by the pathogen editor!");
	}	

	@Override
	public boolean test_nodePropNameIsUnique(String name) {
		return parent.test_nodePropNameIsUnique(name);
	}

	@Override
	public PropertiesEditor pathogen_getEditor(int pathID) throws EditorException {
		throw new EditorException("Pathogens do not have internal pathogens.");
	}

	@Override
	public List<Integer> pathogen_getPathogenIDs() throws EditorException {
		throw new EditorException("Pathogens do not have internal pathogens.");
	}

	@Override
	public String pathogen_getName(int pathID) throws EditorException {
		throw new EditorException("Pathogens do not have internal pathogens.");
	}

	@Override
	public int nodeProp_getPathogenID(int pid) throws EditorException {
		throw new EditorException("Pathogens do not have internal pathogens.");
	}

	@Override
	public void scratch_setPathogenType(String type) throws EditorException {
		throw new EditorException("Pathogens do not have internal pathogens.");
		
	}

	@Override
	public String scratch_getPathogenType() throws EditorException {
		throw new EditorException("Pathogens do not have internal pathogens.");
	}

}
	
	