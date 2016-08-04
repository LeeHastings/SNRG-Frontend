package org.snrg_nyc.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.internal.AttachmentProperty;
import org.snrg_nyc.model.internal.BooleanProperty;
import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.model.internal.EnumeratorProperty;
import org.snrg_nyc.model.internal.FractionProperty;
import org.snrg_nyc.model.internal.IntegerRangeProperty;
import org.snrg_nyc.persistence.JsonExperimentPrinter;
import org.snrg_nyc.persistence.JsonFileSerializer;
import org.snrg_nyc.persistence.PersistenceException;
import org.snrg_nyc.util.Transferable;


/**
 * An editor class for creating node properties.
 * The only public methods should be those listed in {@link PropertiesEditor}.
 * @author Devin Hastings
 */
public class NodeEditor extends PropertiesEditor_Impl implements EditorTester {
	
	/*         *\
	 * Members *
	\*         */

	/** Node Property classes that can be created in the editor */
	static final Class<?>[] nodePropertyTypes = {
			EnumeratorProperty.class,
			IntegerRangeProperty.class,
			BooleanProperty.class,
			FractionProperty.class,
			AttachmentProperty.class
		};
	
	private NodeSettings nodeSettings = new NodeSettings();
	private List<PathogenEditor> pathogens;
	private List<EdgeEditor> edges;
 	
	/*         *\
	 * Methods *
	\*         */
	
	public 
	NodeEditor(){
		super();
		pathogens = new ArrayList<>();
		edges = new ArrayList<>();
		nodeSettings.setLayerAttributesList(layers);
		nodeSettings.setPropertyDefinitionList(properties);
	}
	
	private void 
	assert_validPathogenID(int pathID) throws EditorException{
		if(pathID < 0 || pathID >= pathogens.size() || pathogens.get(pathID) == null){
			throw new EditorException("Invalid pathogen ID: "+pathID);
		}
	}
	
	private int 
	pathogen_create(String name) throws EditorException {
		for(PathogenEditor p : pathogens){
			if(p != null && p.getPathogen().equals(name)){
				throw new EditorException("Duplicate pathogen name: "+name);
			}
		}
		pathogens.add(new PathogenEditor(this, name));
		return pathogens.size() - 1;
	}
	
	@Override
	public void 
	save(String experimentName) throws EditorException {
		Map<String, Transferable> e = getSavedObjects();
		
		try {
			serializer.storeExperiment(experimentName, e);
		} catch (PersistenceException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public int 
	scratch_commit() throws EditorException{
		if(scratchProperty instanceof AttachmentProperty){
			AttachmentProperty ap = (AttachmentProperty) scratchProperty;
			int pathID = pathogen_create(ap.getPathogenName());
			ap.setPathogenID(pathID);
		}
		return super.scratch_commit();
	}
	
	@Override
	protected Class<?>[] 
	getPropertyClasses() {
		return nodePropertyTypes;
	}
	
	@Override
	public Map<String, Transferable > 
	getSavedObjects() throws EditorException{
		Map<String, Transferable> map = super.getSavedObjects();
		map.put("nodesettings", nodeSettings);
		for(PathogenEditor p : pathogens){
			map.putAll(p.getSavedObjects());
		}
		for(EdgeEditor e : edges){
			map.putAll(e.getSavedObjects());
		}
		return map;
	}
	
	@Override 
	public void 
	clear(){
		super.clear();
		for(PathogenEditor p : pathogens){
			if(p != null){
				p.clear();
			}
		}
		pathogens.clear();
	}
	
	@Override
	public void 
	load(String experimentName) throws EditorException {
		Map<String, Transferable> objects = deserializeExperiment(experimentName);
		//System.out.println("Found map:\n"+objects.toString());
		
		if(!objects.containsKey("nodesettings")){
			throw new EditorException("Tried to open an experiment without node settings!");
		}
		nodeSettings = (NodeSettings) objects.get("nodesettings");
		objects.remove("nodesettings");
		
		layers = nodeSettings.getLayerAttributesList();
		properties = nodeSettings.getPropertyDefinitionList();
		
		loadDistributions(objects);
		
		Iterator<String> it = objects.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			Transferable object = objects.get(key);
			if(object instanceof PathogenSettings){
				PathogenSettings settings = (PathogenSettings) object;
				it.remove();
				PathogenEditor p = new PathogenEditor(this, settings, objects);
				pathogens.add(p);
			}
			else if(object instanceof EdgeSettings){
				EdgeSettings settings = (EdgeSettings) object;
				it.remove();

				String layer = settings.getLayerName();
				int idx = -1;
				for(int i = 0; i < layers.size(); i++){
					if(layers.get(i).getName().equals(layer)){
						idx = i;
					}
				}
				if(idx == -1){
					throw new EditorException("Unknown layer in edge settings: "+layer);
				}
				//Edges and layers indexes must match up
				//TODO: Consider using hashmap instead?
				while(idx >= edges.size()){
					edges.add(null);
				}
				edges.set(idx, new EdgeEditor(this, settings, objects));
			}
			
		}
		validateLoadedObjects();
		for(PathogenEditor p : pathogens){
			p.validateLoadedObjects();
		}
	}

	@Override
	public boolean 
	test_nodePropNameIsUnique(String name) {
		for(PathogenEditor path : pathogens){
			if(path != null && !path.uniquePropName(name)){
				return false;
			}
		}
		for(EdgeEditor edge : edges){
			if(edge != null && !edge.uniquePropName(name)){
				return false;
			}
		}
		return uniquePropName(name);
	}

	@Override
	public PropertiesEditor 
	pathogen_getEditor(int pathID) throws EditorException {
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
	public String 
	pathogen_getName(int pathID) throws EditorException {
		assert_validPathogenID(pathID);
		return pathogens.get(pathID).getPathogen();
	}

	@Override
	public int 
	nodeProp_getPathogenID(int pid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), AttachmentProperty.class);
		return ((AttachmentProperty) properties.get(pid)).getPathogenID();
	}
	
	@Override
	public int 
	nodeProp_getPathogenID(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		assert_nodeType(
				layers.get(lid).getProperty(pid), AttachmentProperty.class);
		
		return ((AttachmentProperty) layers.get(lid).getProperty(pid))
				.getPathogenID();
	}
	@Override
	public void 
	scratch_setPathogenType(String type) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, AttachmentProperty.class);
		((AttachmentProperty) scratchProperty).setPathogenName(type);
	}
	@Override
	public String 
	scratch_getPathogenType() throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, AttachmentProperty.class);
		return ((AttachmentProperty) scratchProperty).getPathogenName();
	}
	@Override
	public int layer_new(String name) throws EditorException {
		int lid = super.layer_new(name);
		
		if(lid == edges.size()){
			edges.add(new EdgeEditor(this, layers.get(lid)));
		}
		else if(lid < edges.size()){
			edges.set(lid, new EdgeEditor(this, layers.get(lid)));
		}
		else {
			layers.set(lid, null);
			throw new EditorException("A layer was improperly added to the node settings, "
					+ "and now a new layer cannot be properly added.");
		}
		return lid;
	}
	@Override
	public PropertiesEditor layer_getEdgeEditor(int lid) throws EditorException{
		assert_validLID(lid);
		if(lid >= edges.size()){
			throw new EditorException("Layer ID is outside of edge settings bounds: "+lid);
		}
		return edges.get(lid);
	}

	/*
	 * EditorTester methods
	 */
	
	@Override
	public void utest_setPrintMode(boolean print) {
		if(print){
			this.serializer = new JsonExperimentPrinter(jsonConfig());
		}
		else {
			this.serializer = new JsonFileSerializer(jsonConfig());
		}
	}
}
