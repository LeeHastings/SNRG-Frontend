package org.snrg_nyc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.components.AttachmentProperty;
import org.snrg_nyc.model.components.BooleanProperty;
import org.snrg_nyc.model.components.EditorException;
import org.snrg_nyc.model.components.EnumeratorProperty;
import org.snrg_nyc.model.components.FractionProperty;
import org.snrg_nyc.model.components.IntegerRangeProperty;


/**
 * An editor class for creating node properties.
 * The only public methods are those listed in the {@link PropertiesEditor}.
 * @author Devin Hastings
 *
 */
public class NodeEditor extends PropertiesEditor_Impl {
	
	/*         *\
	 * Members *
	\*         */

	/** Node Property classes that can be created in the editor */
	private static final Class<?>[] nodePropertyTypes = {
			EnumeratorProperty.class,
			IntegerRangeProperty.class,
			BooleanProperty.class,
			FractionProperty.class,
			AttachmentProperty.class
		};
	
	private NodeSettings nodeSettings = new NodeSettings();
	private List<PathogenEditor> pathogens;
	
	/*         *\
	 * Methods *
	\*         */
	
	public NodeEditor(){
		super();
		pathogens = new ArrayList<>();
		nodeSettings.setLayerAttributesList(layers);
		nodeSettings.setPropertyDefinitionList(properties);
	}
	
	@Override
	protected Class<?>[] getPropertyClasses() {
		return nodePropertyTypes;
	}
	@Override
	public Map<String, Serializable> getSavedObjects() throws EditorException{
		Map<String, Serializable> map = super.getSavedObjects();
		map.put("nodesettings", nodeSettings);
		return map;
	}
	@Override 
	public void clear(){
		super.clear();
		for(PathogenEditor p : pathogens){
			if(p != null){
				p.clear();
			}
		}
		pathogens.clear();
	}
	
	@Override
	public void load(String experimentName) throws EditorException {
		Map<String, Serializable> objects = deserializeExperiment(experimentName);
		System.out.println("Found map:\n"+objects.toString());
		if(!objects.containsKey("nodesettings")){
			throw new EditorException("Tried to open an experiment without node settings!");
		}
		nodeSettings = (NodeSettings) objects.get("nodesettings");
		objects.remove("nodesettings");
		
		layers = nodeSettings.getLayerAttributesList();
		properties = nodeSettings.getPropertyDefinitionList();
		
		loadDistributions(objects);
	}

	@Override
	public boolean test_nodePropNameIsUnique(String name) {
		for(PathogenEditor path : pathogens){
			if(path != null && !path.uniquePropName(name)){
				return false;
			}
		}
		return uniquePropName(name);
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
	public List<Integer> pathogen_getPathogenIDs() throws EditorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String pathogen_getName(int pathID) throws EditorException {
		// TODO Auto-generated method stub
		return null;
	}


}
