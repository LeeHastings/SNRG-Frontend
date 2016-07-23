package org.snrg_nyc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.internal.AttachmentProperty;
import org.snrg_nyc.model.internal.BooleanProperty;
import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.model.internal.EnumeratorProperty;
import org.snrg_nyc.model.internal.FractionProperty;
import org.snrg_nyc.model.internal.IntegerRangeProperty;


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
	
	private void assert_validPathogenID(int pathID) throws EditorException{
		if(pathID < 0 || pathID >= pathogens.size() || pathogens.get(pathID) == null){
			throw new EditorException("Invalid pathogen ID: "+pathID);
		}
	}
	
	private int pathogen_create(String name) throws EditorException {
		for(PathogenEditor p : pathogens){
			if(p != null && p.getPathogen().equals(name)){
				throw new EditorException("Duplicate pathogen name: "+name);
			}
		}
		pathogens.add(new PathogenEditor(this, name));
		return pathogens.size() - 1;
	}
	@Override
	public int scratch_commit() throws EditorException{
		if(scratchProperty instanceof AttachmentProperty){
			AttachmentProperty ap = (AttachmentProperty) scratchProperty;
			int pathID = pathogen_create(ap.getPathogenName());
			ap.setPathogenID(pathID);
		}
		return super.scratch_commit();
	}
	@Override
	protected Class<?>[] getPropertyClasses() {
		return nodePropertyTypes;
	}
	@Override
	public Map<String, Transferable > getSavedObjects() throws EditorException{
		Map<String, Transferable> map = super.getSavedObjects();
		map.put("nodesettings", nodeSettings);
		for(PathogenEditor p : pathogens){
			map.putAll(p.getSavedObjects());
		}
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
		Map<String, Transferable> objects = deserializeExperiment(experimentName);
		System.out.println("Found map:\n"+objects.toString());
		if(!objects.containsKey("nodesettings")){
			throw new EditorException("Tried to open an experiment without node settings!");
		}
		nodeSettings = (NodeSettings) objects.get("nodesettings");
		objects.remove("nodesettings");
		
		layers = nodeSettings.getLayerAttributesList();
		properties = nodeSettings.getPropertyDefinitionList();
		
		for(Transferable t : objects.values()){
			if(t instanceof PathogenSettings){
				PathogenEditor p = new PathogenEditor(this, (PathogenSettings) t, objects);
				pathogens.add(p);
			}
		}
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
	public PropertiesEditor pathogen_getEditor(int pathID) throws EditorException {
		assert_validPathogenID(pathID);
		return pathogens.get(pathID);
	}

	@Override
	public List<Integer> pathogen_getPathogenIDs() throws EditorException {
		List<Integer> ids = new ArrayList<>(pathogens.size());
		for(int i = 0; i < pathogens.size(); i++){
			if(pathogens.get(i) != null){
				ids.add(i);
			}
		}
		return ids;
	}

	@Override
	public String pathogen_getName(int pathID) throws EditorException {
		assert_validPathogenID(pathID);
		return pathogens.get(pathID).getPathogen();
	}

	@Override
	public int nodeProp_getPathogenID(int pid) throws EditorException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void scratch_setPathogenType(String type) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, AttachmentProperty.class);
		((AttachmentProperty) scratchProperty).setPathogenName(type);
	}

	@Override
	public String scratch_getPathogenType() throws EditorException {
		// TODO Auto-generated method stub
		return null;
	}


}