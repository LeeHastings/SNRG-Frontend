package org.snrg_nyc.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.snrg_nyc.model.internal.AttachmentProperty;
import org.snrg_nyc.model.internal.BooleanProperty;
import org.snrg_nyc.model.internal.DistributionJsonAdapter;
import org.snrg_nyc.model.internal.FractionProperty;
import org.snrg_nyc.model.internal.IntegerRangeProperty;
import org.snrg_nyc.model.internal.NodeLayer;
import org.snrg_nyc.model.internal.NodeProperty;
import org.snrg_nyc.model.internal.NodeProperty.DistType;
import org.snrg_nyc.model.internal.PropertyJsonAdapter;
import org.snrg_nyc.model.internal.UnivariatDistribution;
import org.snrg_nyc.model.internal.ValueProperty;
import org.snrg_nyc.model.internal.ValuesListProperty;
import org.snrg_nyc.model.internal.ValuesListProperty.ConditionalDistribution;
import org.snrg_nyc.model.internal.ValuesListProperty.Distribution;
import org.snrg_nyc.persistence.ExperimentSerializer;
import org.snrg_nyc.persistence.PersistenceException;
import org.snrg_nyc.util.Transferable;
import org.snrg_nyc.persistence.JsonFileSerializer;

import com.google.gson.GsonBuilder;


/**
 * An editor class for creating node properties.
 * The only public methods are those listed in the {@link PropertiesEditor}.
 * @author Devin Hastings
 *
 */
abstract class PropertiesEditor_Impl implements PropertiesEditor {
	
	/*         *\
	 * Members *
	\*         */
	
	/** The temporary property used when creating new node properties */
	protected NodeProperty scratchProperty;
	
	/**  A list of nullable node properties */
	protected List<NodeProperty> properties;
	
	/** A list of node layers */
	protected List<NodeLayer> layers;
	
	protected Integer scratchLayerID;
	
	protected ExperimentSerializer serializer;
	
	/*         *\
	 * Methods *
	\*         */
	
	public PropertiesEditor_Impl(){
		properties = new ArrayList<>();
		layers = new ArrayList<>();
		
		scratchProperty = null;
		scratchLayerID = null;
		
		serializer = new JsonFileSerializer(jsonConfig());
	}
	
	protected GsonBuilder jsonConfig(){
		return new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(
                		UnivariatDistribution.DistributionList.class,
                		new DistributionJsonAdapter()
        		)
                .registerTypeAdapter(
                		NodeProperty.class, 
                		new PropertyJsonAdapter(getPropertyClasses())
        		);
	}
	
	protected abstract 
	Class<?>[] getPropertyClasses();
	
	protected Map<String, Transferable> 
	getSavedObjects() throws EditorException {
		Map<String, Transferable> e = new HashMap<>();
		
		for(NodeLayer l : layers){
			if(l == null){
				continue;
			}
			for(NodeProperty np : l.getProperties()){
				if(np != null && np instanceof ValuesListProperty
				   && ((ValuesListProperty<?>) np).getDistributionType() 
				      == NodeProperty.DistType.UNIVARIAT
				){
					UnivariatDistribution u = 
							new UnivariatDistribution(this,np);
					e.put(np.getDistributionID(), u);
				}
			}
		}
		for(NodeProperty np : properties){
			if(np != null && np instanceof ValuesListProperty
			   && ((ValuesListProperty<?>) np).getDistributionType() 
			      == NodeProperty.DistType.UNIVARIAT
			){
				UnivariatDistribution u = new UnivariatDistribution(this,np);
				e.put(np.getDistributionID(), u);
			}
		}
		return e;
	}
	
	protected void 
	loadDistributions(Map<String, Transferable> objects) 
			throws EditorException 
	{
		for(String key : objects.keySet()){
			
			if(objects.get(key) instanceof UnivariatDistribution){
				UnivariatDistribution uniD = 
						(UnivariatDistribution) objects.get(key);
				
				Integer pid = search_nodePropWithName(uniD.getPropName());
				Integer lid = null;
				if(pid == null){
					for(int l : layer_getLayerIDs()){
						pid = search_nodePropWithName(uniD.getPropName(), l);
						if(pid != null){
							lid = l;
							break;
						}
					}
					if(pid != null){
						NodeProperty np;
						
						if(lid == null){
							np = properties.get(pid);
						}
						else {
							np = layers.get(lid).getProperty(pid);
						}
						uniD.addToProperty(this, np);
					}
				}
				else {
					uniD.addToProperty(this, properties.get(pid));
				}
			}
		}
	}
	
	protected void 
	validateLoadedObjects() throws EditorException {
		for(NodeProperty p : properties){
			if(p instanceof ValuesListProperty){
				ValuesListProperty<?> en = (ValuesListProperty<?>)p;
				if(en.getDistributionType() == DistType.UNIVARIAT){
					en.getDefaultDistribution();
				}
			}
		}
	}
	
	protected Map<String, Transferable> 
	deserializeExperiment(String experimentName) throws EditorException{
		clear();
		Map<String, Transferable> e = null;
		try {
			e = serializer.loadExperiment(experimentName);
		} catch (PersistenceException e1) {
			throw new EditorException("Error while loading "
					+experimentName+": "+e1.getMessage());
		}
		return e;
	}
	
	/**
	 * Assert that the given node property ID points to a non-null node 
	 * property.
	 * @param pid The node Property ID
	 * @throws PropertiesEditor.EditorException Thrown if the given pid cannot 
	 * be used to access a node property.
	 */
	protected void 
	assert_validPID(int pid) throws EditorException{
		if(!test_nodePropIDExists(pid) ){
			throw new EditorException("The given pid does not exist: "+pid);
		}
	}
	
	protected void 
	assert_validPID(int lid, int pid) throws EditorException{
		assert_validLID(lid);
		if(!layers.get(lid).validPID(pid)){
			throw new EditorException("No property of ID '"+pid+"' in layer '"
					+layers.get(lid).getName()+"'");
		}
	}
	
	protected void 
	assert_validLID(Integer lid) throws EditorException {
		if(lid == null || lid < 0 || lid >= layers.size() 
				|| layers.get(lid) == null)
		{
			throw new EditorException("Invalid layer ID: "+lid);
		}
	}
	
	/**
	 * Assert that the given {@link NodeProperty} can be cast to the given 
	 * class, or throw {@link EditorException}
	 * @param p The {@link NodeProperty} to check.
	 * @param cls The class the {@link NodeProperty} should be an instance of.
	 * @throws PropertiesEditor.EditorException Thrown if casting the give 
	 * {@link NodeProperty} to the give class
	 * would fail.
	 */
	protected void 
	assert_nodeType(NodeProperty p, Class<? extends NodeProperty> cls) 
			throws EditorException
	{
		if(!cls.isAssignableFrom(p.getClass())){
			throw new EditorException("The given property is of type "+
					p.getClass().getSimpleName()+", expected "
					+cls.getSimpleName());
		}
	}
	/**
	 * Assert that the scratch property is not null.
	 * @throws PropertiesEditor.EditorException Thrown if the scratch property
	 *  is null.
	 */
	protected void 
	assert_scratchExists() throws EditorException {
		if(scratchProperty == null){
			throw new EditorException("The scratch property is currently null.");
		}
	}
	/**
	 * Assert that a dependency condition map is valid for use in a 
	 * {@link ConditionalDistribution} in the scratch property,
	 * otherwise it throws {@link EditorException}.
	 * @param dependencyConditions A map of dependency conditions for a new
	 *  {@link ConditionalDistribution}
	 * @throws PropertiesEditor.EditorException Thrown if any of the integer 
	 * IDs do not point to an existing node property, or if
	 * the String label mapped to that ID is not a label in the nodeProperty.
	 */
	private void 
	assert_depConds(Map<Integer, Integer> dependencyConditions) 
			throws EditorException
	{
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		
		if(!((ValuesListProperty<?>)scratchProperty).rangesAreSet()){
			throw new EditorException(
					"Not all ranges are set in this property");
		}
		
		if(dependencyConditions.isEmpty()){
			throw new EditorException(
					"The set of dependency conditions cannot be empty!");
		}
		ValuesListProperty<?> vlp = null;
		for(Integer pid: dependencyConditions.keySet()){
			if(pid == null || dependencyConditions.get(pid) == null){
				throw new EditorException("Error in new dependency conditions:"
						+ " some values are null");
			}
			assert_validPID(pid);
			assert_nodeType(properties.get(pid), ValuesListProperty.class);
			if(!scratchProperty.dependsOn(pid)){
				throw new EditorException(
						"Error while adding conditional distribution: "
						+ "tried to use property ID '"+pid+"' as a dependency,"
						+ " but it is not in the list of dependencies.");
			}
			vlp = ((ValuesListProperty<?>) properties.get(pid));
			
			if(!vlp.validRID(dependencyConditions.get(pid)) ){ 
				String msg = String.format(
						"Error while adding conditional distribution: "
						+ "range ID '%d' was not found in property '%s'",
						dependencyConditions.get(pid), vlp.getName());
				throw new EditorException(msg);
			}
		}
	}
	/**
	 * 
	 * Check if the probabilities map is valid, and throw an exception if 
	 * it isn't.
	 * @param probabilities A map of probabilities for use in a node 
	 * property distribution
	 * @throws PropertiesEditor.EditorException Thrown if the distribution
	 * does not take each label in the scratch property
	 * and map it to a non-negative floating point value.  Also thrown if
	 * there are any extra labels in the map.
	 */
	private void 
	assert_probMap(Map<Integer, Float> probabilities) throws EditorException{
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		
		if(!((ValuesListProperty<?>)scratchProperty).rangesAreSet()){
			throw new EditorException(
					"Not all ranges are set in this property");
		}
		for(Entry<Integer, Float> entry : probabilities.entrySet()){
			if(entry.getValue() == null || entry.getKey() == null){
				throw new EditorException("Error while adding distribution:"
						+ " there are null values");
			}
			if(entry.getValue() < 0){
				throw new EditorException("Error while adding distribution:"
						+ " probabilities must be equal to "
						+ "or greater than zero, found pair ("
						+ ""+entry.getKey()+","+entry.getValue()+").");
			}
		}
		
		ValuesListProperty<?> vlp = ((ValuesListProperty<?>) scratchProperty);
		List<Integer> rangeIDs = vlp.getUnSortedRangeIDs();
		
		if(probabilities.size() > rangeIDs.size()){
			throw new EditorException("Error while adding distribution: "
					+ "probability map has extra entries.");
			
		}
		for(int rid : rangeIDs){
			if(!probabilities.containsKey(rid)){
				throw new EditorException("Error while adding distribution:"
					+ " probability map is missing the range ID for range '"
						+vlp.getRangeLabel(rid)+"'.");
			}
		}
	}
	
	void 
	assert_noConditionals() throws EditorException{
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		
		if( ((ValuesListProperty<?>)scratchProperty).distributionsAreSet() ){
			throw new EditorException("Tried to modify range labels of a "
					+ "property with distributions.");
		}
	}
	
	/**
	 * An internal method for checking if a property name is unique solely 
	 * against the internal properties of this editor (rather than its parent
	 *  or children editors)
	 * @param name The name to compare the other properties against
	 * @return True if the name is unique (there is no property with this name)
	 * , otherwise false.
	 */
	protected boolean 
	uniquePropName(String name){
		for(NodeProperty p : properties){
			if(p != null && p.getName().equals(name)){
				return false;
			}
		}
		for(NodeLayer l : layers){
			if(l != null){
				for(NodeProperty p : l.getProperties()){
					if(p != null && p.getName().equals(name)){
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/*                   *\
	 * Interface Methods *
	\*                   */
	
	@Override
	public void 
	clear(){
		scratch_clear();
		properties.clear();
		layers.clear();
	}
	
	@Override
	public List<String> 
	getExperimentNames(){
		return serializer.savedExperiments();
	}

	@Override
	public boolean 
	test_nodePropIDExists(int pid){
		return (pid >= 0 && pid < properties.size() 
				&& properties.get(pid)!= null);
	}

	@Override
	public boolean 
	test_nodePropIDExists(int lid, int pid) throws EditorException {
		assert_validLID(lid);
		return layers.get(lid).validPID(pid);
	}

	@Override
	public boolean 
	test_layerNameIsUnique(String name){
		for(NodeLayer l : layers){
			if(l.getName().equals(name)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean 
	test_layerIDExists(int lid) {
		return(lid >= 0 && lid < layers.size() 
				&& layers.get(lid) != null);
	}
	
	@Override
	public List<String> 
	getPropertyTypes() {
		ArrayList<String> li = new ArrayList<>();
		for(Class<?> c : getPropertyClasses()){
			li.add(c.getSimpleName());
		}
		return li;
	}
	
	@Override
	public List<Integer> 
	nodeProp_getPropertyIDs() {
		ArrayList<Integer> ls = new ArrayList<>();
		for(int i = 0; i < properties.size(); i++){
			if(properties.get(i)!= null){
				ls.add(i);
			}
		}
		return ls;
	}

	@Override
	public List<Integer> 
	nodeProp_getRangeItemIDs(int pid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), ValuesListProperty.class);
		return ((ValuesListProperty<?>) properties.get(pid)).getSortedRangeIDs();
	}

	@Override
	public String 
	nodeProp_getRangeLabel(int pid, int rid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), ValuesListProperty.class);
		return ((ValuesListProperty<?>) properties.get(pid)).getRangeLabel(rid);
	}

	@Override
	public int 
	nodeProp_getRangeMax(int pid, int rid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), IntegerRangeProperty.class);
		return ((IntegerRangeProperty) properties.get(pid)).getRangeMax(rid);
	}

	@Override
	public int 
	nodeProp_getRangeMin(int pid, int rid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), IntegerRangeProperty.class);
		return ((IntegerRangeProperty) properties.get(pid)).getRangeMin(rid);
	}

	@Override
	public String 
	nodeProp_getName(int pid) throws EditorException {
		assert_validPID(pid);
		return properties.get(pid).getName();
	}

	@Override
	public String 
	nodeProp_getType(int pid) throws EditorException {
		assert_validPID(pid);
		return properties.get(pid).getClass().getSimpleName();
	}

	@Override
	public int 
	nodeProp_getDependencyLevel(int pid) throws EditorException {
		assert_validPID(pid);
		if(properties.get(pid).getDependencyLevel() < 0){
			throw new EditorException("The dependency level of property '"
					+nodeProp_getName(pid)+"' was never set");
		}
		return properties.get(pid).getDependencyLevel();
	}
	
	@Override
	public String 
	nodeProp_getDescription(int pid) throws EditorException{
		assert_validPID(pid);
		return properties.get(pid).getDescription();
	}
	@Override 
	public List<Integer> 
	nodeProp_getDependencyIDs(int pid) throws EditorException{
		assert_validPID(pid);
		return properties.get(pid).getDependencies();
	}

	@Override
	public float 
	nodeProp_getFractionInitValue(int pid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), FractionProperty.class);
		
		FractionProperty fp = (FractionProperty) properties.get(pid);
		if(!fp.hasInitValue()){
			throw new EditorException(
					"No initial value set for node property: "+fp.getName());
		}
		return fp.getInitValue();
	}

	@Override
	public float 
	nodeProp_getFractionInitValue(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		assert_nodeType(
				layers.get(lid).getProperty(pid), FractionProperty.class);
		FractionProperty fp = 
				(FractionProperty) layers.get(lid).getProperty(pid);
		
		if(!fp.hasInitValue()){
			throw new EditorException(
					"No initial value for fraction property '"
							+properties.get(pid).getName()+
							"' in layer '"+lid+"'");
		}
		return fp.getInitValue();
	}
	
	@Override
	public boolean 
	nodeProp_getBooleanInitValue(int pid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), BooleanProperty.class);
		
		BooleanProperty bp = (BooleanProperty) properties.get(pid);
		if(!bp.hasInitValue()){
			throw new EditorException(
					"No initial value for boolean property '"
							+properties.get(pid).getName()+"'" );
		}
		return bp.getInitValue();
	}

	@Override
	public boolean 
	nodeProp_getBooleanInitValue(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		assert_nodeType(
				layers.get(lid).getProperty(pid), BooleanProperty.class);
		BooleanProperty bp = 
				(BooleanProperty) layers.get(lid).getProperty(pid);
		
		if(!bp.hasInitValue()){
			throw new EditorException(
					"No initial value for boolean property '"+pid+
					"' in layer '"+lid+"'");
		}
		return bp.getInitValue();
	}
	
	@Override
	public String 
	nodeProp_getPathogenType(int pid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), AttachmentProperty.class);
		return ((AttachmentProperty) properties.get(pid)).getPathogenName();
	}

	@Override
	public String 
	nodeProp_getPathogenType(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		assert_nodeType(layers.get(lid).getProperty(pid),
				AttachmentProperty.class);
		
		return ((AttachmentProperty) layers.get(lid).getProperty(pid))
				.getPathogenName();
	}

	@Override
	public boolean 
	nodeProp_isRangedProperty(int pid) throws EditorException {
		assert_validPID(pid);
		return (properties.get(pid) instanceof ValuesListProperty);
	}
	
	@Override
	public List<Integer> 
	nodeProp_getConditionalDistributionIDs(int pid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), ValuesListProperty.class);
		
		return ((ValuesListProperty<?>) properties.get(pid))
				.getOrderedConditions();
	}

	@Override
	public Map<Integer, Integer> 
	nodeProp_getDistributionConditions(int pid,int cid) throws EditorException{
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), ValuesListProperty.class);
		return ((ValuesListProperty<?>) properties.get(pid))
				.getConDistributionConditions(cid);
	}

	@Override
	public Map<Integer, Float> 
	nodeProp_getDistribution(int pid, int cid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), ValuesListProperty.class);
		return ((ValuesListProperty<?>) properties.get(pid))
				.getConDistributionProbMap(cid);
	}

	@Override
	public Map<Integer, Float> 
	nodeProp_getDefaultDistribution(int pid) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), ValuesListProperty.class);
		
		if(!((ValuesListProperty<?>) properties.get(pid))
				.hasDefaultDistribution())
		{
			throw new EditorException("The default distribution for property '"
					+properties.get(pid).getName()+"' was not set");
		}
		return ((ValuesListProperty<?>) properties.get(pid))
				.getDefaultDistribution();
	}
	
	@Override
	public List<Integer> 
	nodeProp_getPropertyIDs(int lid) throws EditorException {
		assert_validLID(lid);
		return layers.get(lid).getPropertyIDs();
	}

	@Override
	public List<Integer> 
	nodeProp_getRangeItemIDs(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		NodeProperty np = layers.get(lid).getProperty(pid);
		assert_nodeType(np, ValuesListProperty.class);
		return ((ValuesListProperty<?>) np).getSortedRangeIDs();
	}

	@Override
	public String 
	nodeProp_getRangeLabel(int lid, int pid, int rid) throws EditorException {
		assert_validPID(lid, pid);
		NodeProperty np = layers.get(lid).getProperty(pid);
		assert_nodeType(np, ValuesListProperty.class);
		return ((ValuesListProperty<?>) np).getRangeLabel(rid);
	}

	@Override
	public int 
	nodeProp_getRangeMax(int lid, int pid, int rid) throws EditorException {
		assert_validPID(lid, pid);
		NodeProperty np = layers.get(lid).getProperty(pid);
		assert_nodeType(np, IntegerRangeProperty.class);
		return ((IntegerRangeProperty) np).getRangeMax(rid);
	}

	@Override
	public int 
	nodeProp_getRangeMin(int lid, int pid, int rid) throws EditorException {
		assert_validPID(lid, pid);
		NodeProperty np = layers.get(lid).getProperty(pid);
		assert_nodeType(np, IntegerRangeProperty.class);
		return ((IntegerRangeProperty) np).getRangeMin(rid);
	}

	@Override
	public String 
	nodeProp_getName(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		return layers.get(lid).getProperty(pid).getName();
	}

	@Override
	public String 
	nodeProp_getType(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		return layers.get(lid).getProperty(pid).getClass().getSimpleName();
	}

	@Override
	public int 
	nodeProp_getDependencyLevel(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		return layers.get(lid).getProperty(pid).getDependencyLevel();
	}

	@Override
	public String 
	nodeProp_getDescription(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		return layers.get(lid).getProperty(pid).getDescription();
	}

	@Override
	public boolean 
	nodeProp_isRangedProperty(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		return layers.get(lid).getProperty(pid) 
				instanceof ValuesListProperty;
	}

	@Override
	public Map<Integer, Float> 
	nodeProp_getDefaultDistribution(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		NodeProperty np = layers.get(lid).getProperty(pid);
		assert_nodeType(np, ValuesListProperty.class);
		return ((ValuesListProperty<?>) np).getDefaultDistribution();
	}
	
	@Override
	public List<Integer> 
	nodeProp_getDependencyIDs(int lid, int pid) throws EditorException {
		assert_validPID(lid, pid);
		return layers.get(lid).getProperty(pid).getDependencies();
	}

	@Override
	public List<Integer> 
	nodeProp_getConditionalDistributionIDs(int lid, int pid) 
			throws EditorException 
	{
		assert_validPID(lid, pid);
		NodeProperty np = layers.get(lid).getProperty(pid);
		assert_nodeType(np, ValuesListProperty.class);
		return ((ValuesListProperty<?>) np).getOrderedConditions();
	}

	@Override
	public Map<Integer, Integer> 
	nodeProp_getDistributionConditions(int lid, int pid, int cid) 
			throws EditorException 
	{
		assert_validPID(lid, pid);
		NodeProperty np = layers.get(lid).getProperty(pid);
		assert_nodeType(np, ValuesListProperty.class);
		return((ValuesListProperty<?>) np).getConDistributionConditions(cid);
	}

	@Override
	public Map<Integer, Float> 
	nodeProp_getDistribution(int lid, int pid, int cid) throws EditorException{
		assert_validPID(lid, pid);
		NodeProperty np = layers.get(lid).getProperty(pid);
		assert_nodeType(np, ValuesListProperty.class);
		return ((ValuesListProperty<?>) np).getConDistributionProbMap(cid);
	}
	
	@Override 
	public boolean 
	nodeProp_hasUniformDistribution(int pid) throws EditorException {
		assert_validPID(pid);
		return properties.get(pid).getDistributionType() == DistType.UNIFORM;
	}
	@Override
	public boolean 
	nodeProp_hasUniformDistribution(int lid, int pid) throws EditorException{
		assert_validPID(lid, pid);
		return layers.get(lid).getProperty(pid)
				.getDistributionType() == DistType.UNIFORM;
	}

	//Scratch Property Methods
	@Override
	public void 
	scratch_new(String name, String type, String description)
			throws EditorException 
	{
		boolean validType = false;
		if(!test_nodePropNameIsUnique(name)){
			throw new EditorException("Duplicate node property name: "+name);
		}
		for(Class<?> nodeclass: getPropertyClasses()){
			if(type.equals(nodeclass.getSimpleName())){
				validType = true;
				Constructor<?> con;
				try {
					con = nodeclass.getConstructor(
							String.class, String.class);
					scratchProperty = 
							(NodeProperty) con.newInstance(name, description);
				} catch (NoSuchMethodException 
						| SecurityException 
						| InstantiationException 
						| IllegalAccessException 
						| IllegalArgumentException 
						| InvocationTargetException e) 
				{
					// This should not happen under normal conditions
					e.printStackTrace();
					throw new EditorException("Failed to create scratch "
							+ "property - "+e.toString());
				}
			
				break;
			}
		}
		if(!validType){
			throw new EditorException(
					"Invalid type when creating scratch property: " + type);
		}
	}
	
	@Override
	public void 
	scratch_newInLayer(int lid, String name, String type, String description) 
			throws EditorException 
	{
		assert_validLID(lid);
		scratchLayerID = new Integer(lid);
		scratch_new(name, type, description);
	}
	
	@Override
	public Integer 
	scratch_getLayerID() throws EditorException {
		return scratchLayerID;
	}
	@Override
	public void 
	scratch_clear() {
		scratchProperty = null;
		scratchLayerID = null;
	}

	@Override
	public void 
	scratch_setDependencyLevel(int level) throws EditorException {
		assert_scratchExists();
		if(scratchProperty instanceof ValuesListProperty 
			&& ((ValuesListProperty<?>) scratchProperty).dependenciesAreSet())
		{
			throw new EditorException(
					"Cannot change dependency level once dependencies "
					+ "have been added.");
		}
		else {
			scratchProperty.setDependencyLevel(level);
		}

	}

	@Override
	public int 
	scratch_addRange() throws EditorException {
		assert_scratchExists();
		assert_noConditionals();
		return ((ValuesListProperty<?>) scratchProperty).addRange();
	}
	
	@Override
	public int 
	scratch_addRange(String label) throws EditorException{
		int rid = scratch_addRange();
		((ValuesListProperty<?>) scratchProperty).setRangeLabel(rid, label);
		return rid;
	}
	
	@Override
	public void 
	scratch_removeRange(int rid) throws EditorException {
		assert_scratchExists();
		assert_noConditionals();
		((ValuesListProperty<?>) scratchProperty).removeRange(rid);
	}
	
	@Override
	public List<Integer> 
	scratch_getRangeIDs() throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		return ((ValuesListProperty<?>) scratchProperty).getSortedRangeIDs();
	}
	
	@Override
	public String 
	scratch_getRangeLabel(int rid) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		return ((ValuesListProperty<?>) scratchProperty).getRangeLabel(rid);
	}

	@Override
	public void 
	scratch_setRangeLabel(int rid, String label) throws EditorException {
		assert_scratchExists();
		assert_noConditionals();
		((ValuesListProperty<?>) scratchProperty).setRangeLabel(rid, label);
	}

	@Override
	public void 
	scratch_setRangeMin(int rid, int min) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, IntegerRangeProperty.class);
		((IntegerRangeProperty) scratchProperty).setRangeMin(rid, min);
		
	}

	@Override
	public void 
	scratch_setRangeMax(int rid, int max) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, IntegerRangeProperty.class);
		((IntegerRangeProperty) scratchProperty).setRangeMax(rid, max);

	}
	
	@Override
	public boolean 
	scratch_rangeIsSet(int rid) throws EditorException{
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		ValuesListProperty<?> enm = ((ValuesListProperty<?>) scratchProperty);
		if(!enm.validRID(rid)){
			throw new EditorException("Invalid range ID: "+rid);
		}
		else {
			return enm.rangeIsSet(rid);
		}
	}
	
	@Override
	public int 
	scratch_getRangeMin(int rid) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, IntegerRangeProperty.class);
		IntegerRangeProperty irp = (IntegerRangeProperty)scratchProperty;
		
		if(!irp.validRID(rid)){
			throw new EditorException("Invalid RID: "+rid);
		}
		return irp.getRangeMin(rid);
	}

	@Override
	public int 
	scratch_getRangeMax(int rid) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, IntegerRangeProperty.class);
		IntegerRangeProperty irp = (IntegerRangeProperty)scratchProperty;
		
		if(!irp.validRID(rid)){
			throw new EditorException("Invalid RID: "+rid);
		}
		return irp.getRangeMax(rid);
	}

	@Override
	public float 
	scratch_getFractionInitValue() throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, FractionProperty.class);
		if( !((FractionProperty) scratchProperty).hasInitValue() ){
			throw new EditorException(
					"Initial value for the scratch property has not been set");
		}
		else {
			return ((FractionProperty) scratchProperty).getInitValue();
		}
	}
	@Override
	public boolean scratch_getBooleanInitValue() throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, BooleanProperty.class);
		if( !((BooleanProperty) scratchProperty).hasInitValue() ){
			throw new EditorException(
					"Initial value for the scratch property has not been set");
		}
		else {
			return ((BooleanProperty) scratchProperty).getInitValue();
		}
	}


	@Override
	public void 
	scratch_useUniformDistribution() throws EditorException{
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		((ValuesListProperty<?>) scratchProperty).useUniformDistribution();
		scratchProperty.setDependencyLevel(0);
	}

	@Override
	public void scratch_setBooleanInitValue(boolean init) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, BooleanProperty.class);
		((BooleanProperty) scratchProperty).setInitValue(init);
		
	}
	@Override
	public void 
	scratch_setFractionInitValue(float init) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, FractionProperty.class);
		((FractionProperty) scratchProperty).setInitValue(init);
	}

	@Override
	public List<Integer> 
	scratch_getPotentialDependencies() throws EditorException {
		assert_scratchExists();
		if(scratchProperty.getDependencyLevel() < 0){
			throw new EditorException("Cannot get dependencies for sratch "
					+ "property, as the dependency level has not been set");
		}
		List<Integer> deps = new ArrayList<>();
		for(int pid : nodeProp_getPropertyIDs()){
			if(properties.get(pid).getDependencyLevel() 
					< scratchProperty.getDependencyLevel() 
					&& properties.get(pid) instanceof ValuesListProperty){
				deps.add(pid);
			}
		}
		return deps;
	}

	@Override
	public void 
	scratch_addDependency(int pid) throws EditorException {
		assert_scratchExists();
		assert_validPID(pid);
		if(scratchProperty.getDependencyLevel() < 0){
			throw new EditorException("Failed to add dependency to scratch "
					+ "property: scratch property's dependency level was not "
					+ "yet set.");
		}
		if(properties.get(pid).getDependencyLevel() 
				>= scratchProperty.getDependencyLevel())
		{
			throw new EditorException(
					"Failed to add dependency to scratch property: "
					+ "requested property was not of a lower dependency level."
			);
		}
		else {
			scratchProperty.addDependency(pid);
		}

	}

	@Override
	public void 
	scratch_removeDependency(int pid) throws EditorException {
		assert_scratchExists();
		assert_validPID(pid);
		if(scratchProperty instanceof ValuesListProperty 
			&& ((ValuesListProperty<?>) scratchProperty).distributionsAreSet())
		{
			throw new EditorException(
					"Cannot remove dependencies if distributions are set.");
		}
		else {
			scratchProperty.removeDependency(pid);
		}

	}

	@Override
	public List<Integer> 
	scratch_getDependencies() throws EditorException {
		assert_scratchExists();
		return scratchProperty.getDependencies();
	}

	@Override
	public int 
	scratch_addConditionalDistribution(
			Map<Integer, Integer> dependencyConditions,
			Map<Integer, Float> probabilities
			) 
			throws EditorException 
	{
		assert_depConds(dependencyConditions);
		assert_probMap(probabilities);
		
		return ((ValuesListProperty<?>) scratchProperty)
				.addConditionalDistribution(
				new ConditionalDistribution(
						dependencyConditions, probabilities)
				);

	}

	@Override
	public void 
	scratch_removeConditionalDistribution(int cid) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		((ValuesListProperty<?>) scratchProperty)
		.removeConditionalDistribution(cid);
	}
	
	@Override
	public void 
	scratch_clearDistributions() throws EditorException{
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		((ValuesListProperty<?>) scratchProperty)
		.getConditionalDistributions().clear();
	}

	@Override
	public void 
	scratch_updateConditionalDistribution(
			int cid, 
			Map<Integer, Integer> dependencyConditions,
			Map<Integer, Float> probabilities
			) 
			throws EditorException 
	{	
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		assert_depConds(dependencyConditions);
		assert_probMap(probabilities);
		
		((ValuesListProperty<?>) scratchProperty)
		.setConditionalDistribution(
			cid, 
			new ConditionalDistribution(dependencyConditions, probabilities));
	}

	@Override
	public void 
	scratch_reorderConditionalDistributions(List<Integer> ordering)
			throws EditorException 
	{
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		((ValuesListProperty<?>) scratchProperty).setConditionsOrder(ordering);
	}

	@Override
	public void 
	scratch_setDefaultDistribution(Map<Integer, Float> distribution) 
			throws EditorException 
	{
		assert_probMap(distribution);
		((ValuesListProperty<?>) scratchProperty).setDefaultDistribution(
				new Distribution(distribution));
	}

	@Override
	public int 
	scratch_commit() throws EditorException {
		assert_scratchExists();
		List<NodeProperty> propertyList;
		//Add it to a layer if the scratchLayerID is not null
		if(scratchLayerID != null){
			assert_validLID(scratchLayerID);
			propertyList = layers.get(scratchLayerID).getProperties();
		}
		else {
			propertyList = properties;
		}
		for(NodeProperty np : propertyList){
			if(np != null && np.getName() == scratchProperty.getName()){
				throw new EditorException("Scratch property has the same name"
						+ " as an existing property: "+np.getName());
			}
		}
		if(scratchProperty instanceof ValuesListProperty){
			ValuesListProperty<?> vlp = (ValuesListProperty<?>)scratchProperty;
			
			if( !vlp.hasDefaultDistribution() && vlp.getDistributionType() 
					== NodeProperty.DistType.UNIVARIAT )
			{
				throw new EditorException("Tried to add a scratch property "
						+ "without a default distribution.");
			}
			if(!vlp.rangesAreSet()){
				throw new EditorException(
						"Not all ranges are set in this property");
			}
			
		}
		else if(scratchProperty instanceof ValueProperty){
			if ( !((ValueProperty<?>) scratchProperty).hasInitValue() ){
				throw new EditorException("Tried to add a fraction property "
						+ "without an initial value.");
			}
		}
		//Finally, add to the first available spot in the property list
		for(int i = 0; i < propertyList.size(); i++){
			if(propertyList.get(i) == null){
				propertyList.set(i, scratchProperty);
				scratch_clear();
				return i;
			}
		} 
		propertyList.add(scratchProperty);
		scratch_clear();
		return propertyList.size()-1;
	}

	@Override
	public String 
	scratch_getName() throws EditorException {
		assert_scratchExists();
		return scratchProperty.getName();
	}

	@Override
	public String 
	scratch_getType()  throws EditorException {
		assert_scratchExists();
		return scratchProperty.getClass().getSimpleName();
	}

	@Override
	public String 
	scratch_getDescription() throws EditorException {
		assert_scratchExists();
		return scratchProperty.getDescription();
	}

	@Override
	public int 
	scratch_getDependencyLevel() throws EditorException {
		assert_scratchExists();
		return scratchProperty.getDependencyLevel();
	}

	@Override
	public List<Integer> 
	scratch_getConditionalDistributionIDs() throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		return ((ValuesListProperty<?>)scratchProperty)
				.getConditionalDistributionIDs();
	}

	@Override
	public Map<Integer, Integer> 
	scratch_getDistributionCondition(int cid) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		return ((ValuesListProperty<?>) scratchProperty)
				.getConDistributionConditions(cid);
	}

	@Override
	public Map<Integer, Float> 
	scratch_getDistribution(int cid) throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		return ((ValuesListProperty<?>) scratchProperty)
				.getConDistributionProbMap(cid);
	}

	@Override
	public Map<Integer, Float> 
	scratch_getDefaultDistribution() throws EditorException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, ValuesListProperty.class);
		return ((ValuesListProperty<?>) scratchProperty).getDefaultDistribution();
	}

	@Override
	public boolean 
	scratch_isRangedProperty() throws EditorException {
		assert_scratchExists();
		return (scratchProperty instanceof ValuesListProperty);
	}

	/*                     *\
	 * Layer-based Methods *
	\*                     */

	@Override
	public int 
	layer_new(String name) throws EditorException {
		if(name == null || name.length() == 0){
			throw new EditorException(
					"The new layer name cannot be null or empty");
		}
		for(NodeLayer l : layers){
			if(l != null && l.getName().equals(name)){
				throw new EditorException(
						"Tried to add layer with duplicate name: "+name);
			}
		}
		layers.add(new NodeLayer(name));
		return layers.size()-1;
	}

	@Override
	public List<Integer> 
	layer_getLayerIDs() {
		List<Integer> layerIDs = new ArrayList<>();
		for(int i = 0; i < layers.size(); i++){
			if(layers.get(i) != null){
				layerIDs.add(i);
			}
		}
		return layerIDs;
	}
	
	@Override
	public String 
	layer_getName(int lid) throws EditorException{
		assert_validLID(lid);
		return layers.get(lid).getName();
	}
	
	@Override
	public void 
	layer_setName(int lid, String name) throws EditorException{
		assert_validLID(lid);
		for(NodeLayer l : layers){
			if(l != null && l.getName().equals(name)){
				throw new EditorException("A layer with the name '"+name
						+"' already exists");
			}
		}
		layers.get(lid).setName(name);
	}

	@Override
	public Integer 
	search_nodePropWithName(String name) {
		for(int i : nodeProp_getPropertyIDs()){
			if(properties.get(i).getName().equals(name)){
				return i;
			}
		}
		return null;
	}
	
	@Override
	public Integer 
	search_nodePropWithName(String name, int lid) throws EditorException {
		assert_validLID(lid);
		for(int i : layers.get(lid).getPropertyIDs()){
			if(layers.get(lid).getProperty(i).getName().equals(name)){
				return i;
			}
		}
		return null;
	}

	@Override
	public Integer 
	search_rangeWithLabel(int pid, String label) throws EditorException {
		assert_validPID(pid);
		assert_nodeType(properties.get(pid), ValuesListProperty.class);
		
		ValuesListProperty<?> en = (ValuesListProperty<?>) properties.get(pid);
		
		for(int i:en.getUnSortedRangeIDs()){
			if(en.getRangeLabel(i).equals(label)){
				return i;
			}
		}
		return null;
	}

	/*
	 * Unsupported Methods
	 * (These should be overwritten where required
	 */
	private final String 
	noPathogensMsg = "This Editor does not have internal pathogens.";
	
	@Override
	public void 
	save(String experimentName) throws EditorException{
		throw new EditorException(
				"This should not be called directly by this editor!");
	}
	@Override
	public void 
	load(String experimentName) throws EditorException {
		throw new EditorException(
				"This should not be called directly by this editor!");
	}
	
	@Override
	public PropertiesEditor 
	pathogen_getEditor(int pathID) throws EditorException {
		throw new EditorException(noPathogensMsg);
	}

	@Override
	public List<Integer> 
	pathogen_getPathogenIDs() throws EditorException {
		throw new EditorException(noPathogensMsg);
	}
	@Override
	public int nodeProp_getPathogenID(int lid, int pid) throws EditorException {
		throw new EditorException(noPathogensMsg);
	}

	@Override
	public String 
	pathogen_getName(int pathID) throws EditorException {
		throw new EditorException(noPathogensMsg);
	}

	@Override
	public int 
	nodeProp_getPathogenID(int pid) throws EditorException {
		throw new EditorException(noPathogensMsg);
	}

	@Override
	public void 
	scratch_setPathogenType(String type) throws EditorException {
		throw new EditorException(noPathogensMsg);
		
	}

	@Override
	public String 
	scratch_getPathogenType() throws EditorException {
		throw new EditorException(noPathogensMsg);
	}
	
	@Override
	public PropertiesEditor 
	layer_getEdgeEditor(int lid) throws EditorException {
		throw new EditorException(noPathogensMsg);
	}

}
