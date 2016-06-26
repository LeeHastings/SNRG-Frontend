package org.snrg_nyc.model;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.snrg_nyc.persistence.ExperimentDeserializer;
import org.snrg_nyc.persistence.ExperimentSerializer;
import org.snrg_nyc.persistence.MalformedSettingsException;
import org.snrg_nyc.persistence.PersistenceData;

/**
 * The only public class in this package, this is for use by any UI packages,
 * and the only public methods are those listed in {@link UI_Interface}.
 * @author Devin Hastings
 *
 */
class BusinessLogic implements UI_Interface {
	
	/*         *\
	 * Members *
	\*         */

	/** Node Property classes that can be created in the editor */
	private static final Class<?>[] nodePropertyTypes = {
			EnumeratorProperty.class, 
			IntegerRangeProperty.class, 
			BooleanProperty.class,
			FractionProperty.class
			};
	
	/** The temporary property used when creating new node properties */
	private NodeProperty scratchProperty;
	
	/**  A list of nullable node properties */
	private List<NodeProperty> nodeProperties;
	
	/** A list of node layers */
	private List<NodeLayer> nodeLayers;
	
	private Integer scratchLayerID;
	
	/*         *\
	 * Methods *
	\*         */
	
	public BusinessLogic(){
		nodeProperties = new ArrayList<>();
		nodeLayers = new ArrayList<>();
		scratchProperty = null;
		scratchLayerID = null;
	}
	
	/**
	 * Assert that the given node property ID points to a non-null node property.
	 * @param pid The node Property ID
	 * @throws UI_Interface.UIException Thrown if the given pid cannot be used to access a node property.
	 */
	private void assert_validPID(int pid) throws UIException{
		if(!test_nodePropIDExists(pid) ){
			throw new UIException("The given pid does not exist: "+pid);
		}
	}
	
	private void assert_validPID(int lid, int pid) throws UIException{
		assert_validLID(lid);
		if(!nodeLayers.get(lid).validPID(pid)){
			throw new UIException("No property of ID '"+pid+"' in layer '"+nodeLayers.get(lid).getName()+"'");
		}
	}
	
	private void assert_validLID(Integer lid) throws UIException {
		if(lid == null || lid < 0 || lid >= nodeLayers.size() || nodeLayers.get(lid) == null){
			throw new UIException("Invalid layer ID: "+lid);
		}
	}
	
	/**
	 * Assert that the given {@link NodeProperty} can be cast to the given class, or 
	 * throw {@link UIException}
	 * @param p The {@link NodeProperty} to check.
	 * @param cls The class the {@link NodeProperty} should be an instance of.
	 * @throws UI_Interface.UIException Thrown if casting the give {@link NodeProperty} to the give class
	 * would fail.
	 */
	private void assert_nodeType(NodeProperty p, Class<? extends NodeProperty> cls) throws UIException{
		if(!cls.isAssignableFrom(p.getClass())){
			throw new UIException("The given property is of type "+
					p.getClass().getSimpleName()+", expected "+cls.getSimpleName());
		}
	}
	/**
	 * Assert that the scratch property is not null.
	 * @throws UI_Interface.UIException Thrown if the scratch property is null.
	 */
	private void assert_scratchExists() throws UIException{
		if(scratchProperty == null){
			throw new UIException("The scratch property is currently null.");
		}
	}
	/**
	 * Assert that a dependency condition map is valid for use in a {@link ConditionalDistribution} in the scratch property,
	 * otherwise it throws {@link UIException}.
	 * @param dependencyConditions A map of dependency conditions for a new {@link ConditionalDistribution}
	 * @throws UI_Interface.UIException Thrown if any of the integer IDs do not point to an existing node property, or if
	 * the String label mapped to that ID is not a label in the nodeProperty.
	 */
	private void assert_depConds(Map<Integer, Integer> dependencyConditions) throws UIException{
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		
		if(dependencyConditions.isEmpty()){
			throw new UIException("The set of dependency conditions cannot be empty!");
		}
		EnumeratorProperty enm = null;
		for(Integer pid: dependencyConditions.keySet()){
			if(pid == null || dependencyConditions.get(pid) == null){
				throw new UIException("Error in new dependency conditions: some values are null");
			}
			assert_validPID(pid);
			assert_nodeType(nodeProperties.get(pid), EnumeratorProperty.class);
			if(!scratchProperty.dependsOn(pid)){
				throw new UIException("Error while adding conditional distribution: tried to use property ID '"+pid+"'"
						+ " as a dependency, but it is not in the list of dependencies.");
			}
			enm = ((EnumeratorProperty) nodeProperties.get(pid));
			if(!enm.validRID(dependencyConditions.get(pid)) ){ //Property does not have the required rangeID
				String msg = String.format("Error while adding conditional distribution: range ID '%d' was not found in property '%s'",
						dependencyConditions.get(pid), enm.getName());
				throw new UIException(msg);
			}
		}
	}
	/**
	 * 
	 * Check if the probabilities map is valid, and throw an exception if it isn't.
	 * @param probabilities A map of probabilities for use in a node property distribution
	 * @throws UI_Interface.UIException Thrown if the distribution does not take each label in the scratch property
	 * and map it to a non-negative floating point value.  Also thrown if there are any extra labels in
	 * the map.
	 */
	private void assert_probMap(Map<Integer, Float> probabilities) throws UIException{
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		for(Entry<Integer, Float> entry : probabilities.entrySet()){
			if(entry.getValue() == null || entry.getKey() == null){
				throw new UIException("Error while adding distribution: there are null values");
			}
			if(entry.getValue() < 0){
				throw new UIException("Error while adding distribution: probabilities must be equal to "
						+ "or greater than zero, found pair ("+entry.getKey()+","+entry.getValue()+").");
			}
		}
		
		EnumeratorProperty ens = ((EnumeratorProperty) scratchProperty);
		List<Integer> rangeIDs = ens.getUnSortedRangeIDs();
		
		if(probabilities.size() > rangeIDs.size()){
			throw new UIException("Error while adding distribution: "
					+ "probability map has extra entries.");
			
		}
		for(int rid : rangeIDs){
			if(!probabilities.containsKey(rid)){
				throw new UIException("Error while adding distribution:"
						+ " probability map is missing the range ID for range '"+ens.getRangeLabel(rid)+"'.");
			}
		}
	}
	
	void assert_noConditionals() throws UIException{
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		if( ((EnumeratorProperty)scratchProperty).distributionsAreSet() ){
			throw new UIException("Tried to modify range labels of a property with distributions.");
		}
	}
	
	@SuppressWarnings("unused")
	private void print(){
		System.out.println("\nPrinting Node Layers");
		System.out.println("------------============------------");
		for(int lid : layer_getLayerIDs()){
			System.out.printf("Node Layer #%d:\n",lid);
			nodeLayers.get(lid).print();
		}
		System.out.println("\nPrinting Node Properties");
		System.out.println("------------============------------");
		if(scratchProperty != null){
			System.out.println("\nScratch Property:\n------------");
			scratchProperty.print();
		}
		for(Integer pid : nodeProp_getPropertyIDs()){
			System.out.println("\nNode Property #"+pid+":\n------------");
			nodeProperties.get(pid).print();
		}
	}
	
	/*                   *\
	 * Interface Methods *
	\*                   */
	@Override
	public void save(String experimentName) { //Currently prints all node properties
		//print();
		ExperimentSerializer es = new ExperimentSerializer(experimentName, this);
		es.saveStateToFiles();
	}
	
	@Override 
	public void load(String experimentName) throws UIException{
		clear();
		
		try {
			ExperimentDeserializer ds = new ExperimentDeserializer(this, experimentName);
			ds.loadFiles();
		} 
		catch (FileNotFoundException e) {
			throw new UIException("Missing file: "+e.getMessage());
		}
	}
	
	@Override
	public void clear(){
		scratch_clear();
		nodeProperties.clear();
		nodeLayers.clear();
	}
	
	@Override
	public List<String> getExperimentNames(){
		return PersistenceData.getFileNames();
	}

	@Override
	public boolean test_nodePropNameIsUnique(String name) {
		for(NodeProperty p : nodeProperties){
			if(p!= null && name.equals(p.getName())){
				return false;
			}
		}
		return true;
	}
	@Override
	public boolean test_nodePropIDExists(int pid){
		return (pid >= 0 && pid < nodeProperties.size() && nodeProperties.get(pid)!= null);
	}

	@Override
	public boolean test_nodePropNameIsUnique(int lid, String name) throws UIException {
		assert_validLID(lid);
		return nodeLayers.get(lid).nameIsUnique(name);
	}

	@Override
	public boolean test_nodePropIDExists(int lid, int pid) throws UIException {
		assert_validLID(lid);
		return nodeLayers.get(lid).validPID(pid);
	}

	@Override
	public boolean test_layerNameIsUnique(String name){
		for(NodeLayer l : nodeLayers){
			if(l.getName().equals(name)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean test_layerIDExists(int lid) {
		return(lid >= 0 && lid < nodeLayers.size() && nodeLayers.get(lid) != null);
	}
	
	@Override
	public List<String> nodeProp_getTypes() {
		ArrayList<String> li = new ArrayList<>();
		for(Class<?> c : nodePropertyTypes){
			li.add(c.getSimpleName());
		}
		return li;
	}
	
	@Override
	public List<Integer> nodeProp_getPropertyIDs() {
		ArrayList<Integer> ls = new ArrayList<>();
		for(int i = 0; i < nodeProperties.size(); i++){
			if(nodeProperties.get(i)!= null){
				ls.add(i);
			}
		}
		return ls;
	}

	@Override
	public List<Integer> nodeProp_getRangeItemIDs(int pid) throws UIException {
		assert_validPID(pid);
		assert_nodeType(nodeProperties.get(pid), EnumeratorProperty.class);
		return ((EnumeratorProperty) nodeProperties.get(pid)).getSortedRangeIDs();
	}

	@Override
	public String nodeProp_getRangeLabel(int pid, int rid) throws UIException {
		assert_validPID(pid);
		assert_nodeType(nodeProperties.get(pid), EnumeratorProperty.class);
		return ((EnumeratorProperty) nodeProperties.get(pid)).getRangeLabel(rid);
	}

	@Override
	public int nodeProp_getRangeMax(int pid, int rid) throws UIException {
		assert_validPID(pid);
		assert_nodeType(nodeProperties.get(pid), IntegerRangeProperty.class);
		return ((IntegerRangeProperty) nodeProperties.get(pid)).getRangeMax(rid);
	}

	@Override
	public int nodeProp_getRangeMin(int pid, int rid) throws UIException {
		assert_validPID(pid);
		assert_nodeType(nodeProperties.get(pid), IntegerRangeProperty.class);
		return ((IntegerRangeProperty) nodeProperties.get(pid)).getRangeMin(rid);
	}

	@Override
	public String nodeProp_getName(int pid) throws UIException {
		assert_validPID(pid);
		return nodeProperties.get(pid).getName();
	}

	@Override
	public String nodeProp_getType(int pid) throws UIException {
		assert_validPID(pid);
		return nodeProperties.get(pid).getClass().getSimpleName();
	}

	@Override
	public int nodeProp_getDependencyLevel(int pid) throws UIException {
		assert_validPID(pid);
		if(nodeProperties.get(pid).getDependencyLevel() < 0){
			throw new UIException("The dependency level of property '"+nodeProp_getName(pid)+"' was never set");
		}
		return nodeProperties.get(pid).getDependencyLevel();
	}
	
	@Override
	public String nodeProp_getDescription(int pid) throws UIException{
		assert_validPID(pid);
		return nodeProperties.get(pid).getDescription();
	}
	@Override 
	public List<Integer> nodeProp_getDependencyIDs(int pid) throws UIException{
		assert_validPID(pid);
		return nodeProperties.get(pid).getDependencies();
	}

	@Override
	public float nodeProp_getInitValue(int pid) throws UIException {
		assert_validPID(pid);
		assert_nodeType(nodeProperties.get(pid), FractionProperty.class);
		FractionProperty fp = (FractionProperty) nodeProperties.get(pid);
		if(!fp.hasInitValue()){
			throw new UIException("No initial value set for node property: "+fp.name);
		}
		return fp.getInitValue();
	}

	@Override
	public float nodeProp_getInitValue(int lid, int pid) throws UIException {
		assert_validPID(lid, pid);
		assert_nodeType(nodeLayers.get(lid).getProperty(pid), FractionProperty.class);
		FractionProperty fp = (FractionProperty) nodeLayers.get(lid).getProperty(pid);
		if(!fp.hasInitValue()){
			throw new UIException("No initial value for fraction property '"+pid+"' in layer '"+lid+"'");
		}
		return fp.getInitValue();
	}

	@Override
	public boolean nodeProp_isRangedProperty(int pid) throws UIException {
		assert_validPID(pid);
		return (nodeProperties.get(pid) instanceof EnumeratorProperty);
	}
	
	@Override
	public DistributionType nodeProp_getDistributionType(int pid) throws UIException {
		assert_validPID(pid);
		return nodeProperties.get(pid).getDistributionType();
	}

	@Override
	public DistributionType nodeProp_getDistributionType(int lid, int pid) throws UIException {
		assert_validLID(lid);
		return nodeLayers.get(lid).getProperty(pid).getDistributionType();
	}
	
	@Override
	public List<Integer> nodeProp_getConditionalDistributionIDs(int pid) throws UIException {
		assert_validPID(pid);
		assert_nodeType(nodeProperties.get(pid), EnumeratorProperty.class);
		return ((EnumeratorProperty) nodeProperties.get(pid)).getOrderedContitions();
	}

	@Override
	public Map<Integer, Integer> nodeProp_getDistributionConditions(int pid, int cid) throws UIException {
		assert_validPID(pid);
		assert_nodeType(nodeProperties.get(pid), EnumeratorProperty.class);
		return ((EnumeratorProperty) nodeProperties.get(pid)).getConDistributionConditions(cid);
	}

	@Override
	public Map<Integer, Float> nodeProp_getDistribution(int pid, int cid) throws UIException {
		assert_validPID(pid);
		assert_nodeType(nodeProperties.get(pid), EnumeratorProperty.class);
		return ((EnumeratorProperty) nodeProperties.get(pid)).getConDistributionProbMap(cid);
	}

	@Override
	public Map<Integer, Float> nodeProp_getDefaultDistribution(int pid) throws UIException {
		assert_validPID(pid);
		assert_nodeType(nodeProperties.get(pid), EnumeratorProperty.class);
		if(!((EnumeratorProperty) nodeProperties.get(pid)).hasDefaultDistribution()){
			throw new UIException("The default distribution for property '"+nodeProperties.get(pid)+"' was not set");
		}
		return ((EnumeratorProperty) nodeProperties.get(pid)).getDefaultDistribution();
	}
	
	@Override
	public List<Integer> nodeProp_getPropertyIDs(int lid) throws UIException {
		assert_validLID(lid);
		return nodeLayers.get(lid).getPropertyIDs();
	}

	@Override
	public List<Integer> nodeProp_getRangeItemIDs(int lid, int pid) throws UIException {
		assert_validPID(lid, pid);
		NodeProperty np = nodeLayers.get(lid).getProperty(pid);
		assert_nodeType(np, EnumeratorProperty.class);
		return ((EnumeratorProperty) np).getSortedRangeIDs();
	}

	@Override
	public String nodeProp_getRangeLabel(int lid, int pid, int rid) throws UIException {
		assert_validPID(lid, pid);
		NodeProperty np = nodeLayers.get(lid).getProperty(pid);
		assert_nodeType(np, EnumeratorProperty.class);
		return ((EnumeratorProperty) np).getRangeLabel(rid);
	}

	@Override
	public int nodeProp_getRangeMax(int lid, int pid, int rid) throws UIException {
		assert_validPID(lid, pid);
		NodeProperty np = nodeLayers.get(lid).getProperty(pid);
		assert_nodeType(np, IntegerRangeProperty.class);
		return ((IntegerRangeProperty) np).getRangeMax(rid);
	}

	@Override
	public int nodeProp_getRangeMin(int lid, int pid, int rid) throws UIException {
		assert_validPID(lid, pid);
		NodeProperty np = nodeLayers.get(lid).getProperty(pid);
		assert_nodeType(np, IntegerRangeProperty.class);
		return ((IntegerRangeProperty) np).getRangeMin(rid);
	}

	@Override
	public String nodeProp_getName(int lid, int pid) throws UIException {
		assert_validPID(lid, pid);
		return nodeLayers.get(lid).getProperty(pid).getName();
	}

	@Override
	public String nodeProp_getType(int lid, int pid) throws UIException {
		assert_validPID(lid, pid);
		return nodeLayers.get(lid).getProperty(pid).getClass().getSimpleName();
	}

	@Override
	public int nodeProp_getDependencyLevel(int lid, int pid) throws UIException {
		assert_validPID(lid, pid);
		return nodeLayers.get(lid).getProperty(pid).getDependencyLevel();
	}

	@Override
	public String nodeProp_getDescription(int lid, int pid) throws UIException {
		assert_validPID(lid, pid);
		return nodeLayers.get(lid).getProperty(pid).getDescription();
	}

	@Override
	public boolean nodeProp_isRangedProperty(int lid, int pid) throws UIException {
		assert_validPID(lid, pid);
		return nodeLayers.get(lid).getProperty(pid) instanceof EnumeratorProperty;
	}

	@Override
	public Map<Integer, Float> nodeProp_getDefaultDistribution(int lid, int pid) throws UIException {
		assert_validPID(lid, pid);
		NodeProperty np = nodeLayers.get(lid).getProperty(pid);
		assert_nodeType(np, EnumeratorProperty.class);
		return ((EnumeratorProperty) np).getDefaultDistribution();
	}
	
	@Override
	public List<Integer> nodeProp_getDependencyIDs(int lid, int pid) throws UIException {
		assert_validPID(lid, pid);
		return nodeLayers.get(lid).getProperty(pid).getDependencies();
	}

	@Override
	public List<Integer> nodeProp_getConditionalDistributionIDs(int lid, int pid) throws UIException {
		assert_validPID(lid, pid);
		NodeProperty np = nodeLayers.get(lid).getProperty(pid);
		assert_nodeType(np, EnumeratorProperty.class);
		return ((EnumeratorProperty) np).getOrderedContitions();
	}

	@Override
	public Map<Integer, Integer> nodeProp_getDistributionConditions(int lid, int pid, int cid) throws UIException {
		assert_validPID(lid, pid);
		NodeProperty np = nodeLayers.get(lid).getProperty(pid);
		assert_nodeType(np, EnumeratorProperty.class);
		return((EnumeratorProperty) np).getConDistributionConditions(cid);
	}

	@Override
	public Map<Integer, Float> nodeProp_getDistribution(int lid, int pid, int cid) throws UIException {
		assert_validPID(lid, pid);
		NodeProperty np = nodeLayers.get(lid).getProperty(pid);
		assert_nodeType(np, EnumeratorProperty.class);
		return ((EnumeratorProperty) np).getConDistributionProbMap(cid);
	}

	//Scratch Property Methods
	@Override
	public void scratch_new(String name, String type, String description) throws UIException {
		boolean validType = false;
		for(Class<?> nodeclass: nodePropertyTypes){
			if(type.equals(nodeclass.getSimpleName())){
				validType = true;
				try{
					//Use a factory method to build the scratch property
					Constructor<?> con = nodeclass.getConstructor(String.class, String.class);
					scratchProperty = (NodeProperty) con.newInstance(name, description);
				}
				catch(Exception e){
					throw new UIException(e.getMessage());
				}
				break;
			}
		}
		if(!validType){
			throw new UIException("Invalid type when creating scratch property: " + type);
		}
	}
	
	@Override
	public void scratch_newInLayer(int lid, String name, String type, String description) throws UIException {
		assert_validLID(lid);
		scratchLayerID = new Integer(lid);
		scratch_new(name, type, description);
	}
	
	@Override
	public Integer scratch_getLayerID() throws UIException {
		return scratchLayerID;
	}
	@Override
	public void scratch_clear() {
		scratchProperty = null;
		scratchLayerID = null;
	}

	@Override
	public void scratch_setDependencyLevel(int level) throws UIException {
		assert_scratchExists();
		if(scratchProperty instanceof EnumeratorProperty && ((EnumeratorProperty) scratchProperty).dependenciesAreSet()){
			throw new UIException("Cannot change dependency level once dependencies have been added.");
		}
		else {
			scratchProperty.setDependencyLevel(level);
		}

	}

	@Override
	public int scratch_addRange() throws UIException {
		assert_scratchExists();
		assert_noConditionals();
		return ((EnumeratorProperty) scratchProperty).addRange();
	}
	
	@Override
	public int scratch_addRange(String label) throws UIException{
		int rid = scratch_addRange();
		((EnumeratorProperty) scratchProperty).setRangeLabel(rid, label);
		return rid;
	}
	
	@Override
	public void scratch_removeRange(int rid) throws UIException {
		assert_scratchExists();
		assert_noConditionals();
		((EnumeratorProperty) scratchProperty).removeRange(rid);
	}
	
	@Override
	public List<Integer> scratch_getRangeIDs() throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		return ((EnumeratorProperty) scratchProperty).getSortedRangeIDs();
	}
	
	@Override
	public String scratch_getRangeLabel(int rid) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		return ((EnumeratorProperty) scratchProperty).getRangeLabel(rid);
	}

	@Override
	public void scratch_setRangeLabel(int rid, String label) throws UIException {
		assert_scratchExists();
		assert_noConditionals();
		((EnumeratorProperty) scratchProperty).setRangeLabel(rid, label);
	}

	@Override
	public void scratch_setRangeMin(int rid, int min) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, IntegerRangeProperty.class);
		try{
			((IntegerRangeProperty) scratchProperty).setRangeMin(rid, min);
		}
		catch(IllegalArgumentException e){
			throw new UIException(e.getMessage());
		}
	}

	@Override
	public void scratch_setRangeMax(int rid, int max) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, IntegerRangeProperty.class);
		try{
			((IntegerRangeProperty) scratchProperty).setRangeMax(rid, max);
		}
		catch(IllegalArgumentException e){
			throw new UIException(e.getMessage());
		}
	}
	
	@Override
	public boolean scratch_rangeIsSet(int rid) throws UIException{
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		EnumeratorProperty enm = ((EnumeratorProperty) scratchProperty);
		if(!enm.validRID(rid)){
			throw new UIException("Invalid range ID: "+rid);
		}
		else {
			return enm.rangeIsSet(rid);
		}
	}
	
	@Override
	public int scratch_getRangeMin(int rid) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, IntegerRangeProperty.class);
		IntegerRangeProperty irp = (IntegerRangeProperty)scratchProperty;
		
		if(!irp.validRID(rid)){
			throw new UIException("Invalid RID: "+rid);
		}
		return irp.getRangeMin(rid);
	}

	@Override
	public int scratch_getRangeMax(int rid) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, IntegerRangeProperty.class);
		IntegerRangeProperty irp = (IntegerRangeProperty)scratchProperty;
		
		if(!irp.validRID(rid)){
			throw new UIException("Invalid RID: "+rid);
		}
		return irp.getRangeMax(rid);
	}
	
	@Override
	public DistributionType scratch_getDistributionType() {
		return scratchProperty.getDistributionType();
	}

	@Override
	public float scratch_getInitValue() throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, FractionProperty.class);
		if( !((FractionProperty) scratchProperty).hasInitValue() ){
			throw new UIException("Initial value for the scratch property has not been set");
		}
		else {
			return ((FractionProperty) scratchProperty).getInitValue();
		}
	}

	@Override
	public void scratch_useUniformDistribution() throws UIException{
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		((EnumeratorProperty) scratchProperty).useUniformDistribution();
		scratchProperty.setDependencyLevel(0);
	}

	@Override
	public void scratch_setFractionInitValue(float init) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, FractionProperty.class);
		((FractionProperty) scratchProperty).setInitValue(init);
	}

	@Override
	public List<Integer> scratch_getPotentialDependencies() throws UIException {
		assert_scratchExists();
		if(scratchProperty.getDependencyLevel() < 0){
			throw new UIException("Cannot get dependencies for sratch property,"
					+ " as the dependency level has not been set");
		}
		List<Integer> deps = new ArrayList<>();
		for(int pid : nodeProp_getPropertyIDs()){
			if(nodeProperties.get(pid).getDependencyLevel() < scratchProperty.getDependencyLevel() 
					&& nodeProperties.get(pid) instanceof EnumeratorProperty){
				deps.add(pid);
			}
		}
		return deps;
	}

	@Override
	public void scratch_addDependency(int pid) throws UIException {
		assert_scratchExists();
		assert_validPID(pid);
		if(scratchProperty.getDependencyLevel() < 0){
			throw new UIException("Failed to add dependency to scratch property: "
					+ "scratch property's dependency level was not yet set.");
		}
		if(nodeProperties.get(pid).getDependencyLevel() >= scratchProperty.getDependencyLevel()){
			throw new UIException("Failed to add dependency to scratch property: "
					+ "requested property was not of a lower dependency level.");
		}
		else {
			scratchProperty.addDependency(pid);
		}

	}

	@Override
	public void scratch_removeDependency(int pid) throws UIException {
		assert_scratchExists();
		assert_validPID(pid);
		if(scratchProperty instanceof EnumeratorProperty && 
				((EnumeratorProperty) scratchProperty).distributionsAreSet()){
			throw new UIException("Cannot remove dependencies if distributions are set.");
		}
		else {
			scratchProperty.removeDependency(pid);
		}

	}

	@Override
	public List<Integer> scratch_getDependencies() throws UIException {
		assert_scratchExists();
		return scratchProperty.getDependencies();
	}

	@Override
	public int scratch_addConditionalDistribution(Map<Integer, Integer> dependencyConditions,
			Map<Integer, Float> probabilities) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		assert_depConds(dependencyConditions);
		assert_probMap(probabilities);
		
		return ((EnumeratorProperty) scratchProperty).addConditionalDistribution(
				new ConditionalDistribution(dependencyConditions, probabilities) );

	}

	@Override
	public void scratch_removeConditionalDistribution(int cid) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		((EnumeratorProperty) scratchProperty).removeConditionalDistribution(cid);
	}
	
	@Override
	public void scratch_clearDistributions(){
		
		try {
			assert_scratchExists();
			((EnumeratorProperty) scratchProperty).getConditionalDistributions().clear();
		} catch (UIException e) {
			//The only caught exception, since this has no negative side effects if it fails
			e.printStackTrace();
		}
	}

	@Override
	public void scratch_updateConditionalDistribution(int cid, Map<Integer, Integer> dependencyConditions,
			Map<Integer, Float> probabilities) throws UIException {
		
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		assert_depConds(dependencyConditions);
		assert_probMap(probabilities);
		((EnumeratorProperty) scratchProperty).setConditionalDistribution(
				cid, new ConditionalDistribution(dependencyConditions, probabilities));
	}

	@Override
	public void scratch_reorderConditionalDistributions(List<Integer> ordering) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		((EnumeratorProperty) scratchProperty).setConditionsOrder(ordering);
	}

	@Override
	public void scratch_setDefaultDistribution(Map<Integer, Float> distribution) throws UIException {
		assert_probMap(distribution);
		((EnumeratorProperty) scratchProperty).setDefaultDistribution(new Distribution(distribution));
	}

	@Override
	public int scratch_commitToNodeProperties() throws UIException {
		assert_scratchExists();
		List<NodeProperty> propertyList;
		//Add it to a layer if the scratchLayerID is not null
		if(scratchLayerID != null){
			assert_validLID(scratchLayerID);
			propertyList = nodeLayers.get(scratchLayerID).getProperties();
		}
		else {
			propertyList = nodeProperties;
		}
		for(NodeProperty np : propertyList){
			if(np != null && np.getName() == scratchProperty.getName()){
				throw new UIException("Scratch property has the same name as an existing property: "+np.getName());
			}
		}
		if(scratchProperty instanceof EnumeratorProperty){
			EnumeratorProperty ep = (EnumeratorProperty)scratchProperty;
			if( !ep.hasDefaultDistribution() && ep.getDistributionType() == DistributionType.Conditional ){
				throw new UIException("Tried to add a scratch property without a default distribution.");
			}
			if(ep instanceof IntegerRangeProperty){
				for (int i :ep.getUnSortedRangeIDs()){
					if( !ep.rangeIsSet(i) ){
						throw new UIException("Range '"+ep.getRangeLabel(i)+"' in the scratch property was not properly set");
					}
				}
			}
		}
		else if(scratchProperty instanceof FractionProperty){
			if ( !((FractionProperty) scratchProperty).hasInitValue() ){
				throw new UIException("Tried to add a fraction property without an initial value.");
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
	public String scratch_getName() throws UIException {
		assert_scratchExists();
		return scratchProperty.getName();
	}

	@Override
	public String scratch_getType()  throws UIException {
		assert_scratchExists();
		return scratchProperty.getClass().getSimpleName();
	}

	@Override
	public String scratch_getDescription() throws UIException {
		assert_scratchExists();
		return scratchProperty.getDescription();
	}

	@Override
	public int scratch_getDependencyLevel()  throws UIException {
		assert_scratchExists();
		return scratchProperty.getDependencyLevel();
	}

	@Override
	public List<Integer> scratch_getConditionalDistributionIDs() throws UIException  {
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		return ((EnumeratorProperty)scratchProperty).getConditionalDistributionIDs();
	}

	@Override
	public Map<Integer, Integer> scratch_getDistributionCondition(int cid) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		return ((EnumeratorProperty) scratchProperty).getConDistributionConditions(cid);
	}

	@Override
	public Map<Integer, Float> scratch_getDistribution(int cid) throws UIException {
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		return ((EnumeratorProperty) scratchProperty).getConDistributionProbMap(cid);
	}

	@Override
	public Map<Integer, Float> scratch_getDefaultDistribution() throws UIException  {
		assert_scratchExists();
		assert_nodeType(scratchProperty, EnumeratorProperty.class);
		return ((EnumeratorProperty) scratchProperty).getDefaultDistribution();
	}

	@Override
	public boolean scratch_isRangedProperty() throws UIException {
		assert_scratchExists();
		return (scratchProperty instanceof EnumeratorProperty);
	}

	/*                     *\
	 * Layer-based Methods *
	\*                     */

	@Override
	public int layer_new(String name) throws UIException {
		for(NodeLayer l : nodeLayers){
			if(l != null && l.getName().equals(name)){
				throw new UIException("Tried to add layer with duplicate name: "+name);
			}
		}
		nodeLayers.add(new NodeLayer(name));
		return nodeLayers.size()-1;
	}

	@Override
	public List<Integer> layer_getLayerIDs() {
		List<Integer> layerIDs = new ArrayList<>();
		for(int i = 0; i < nodeLayers.size(); i++){
			if(nodeLayers.get(i) != null){
				layerIDs.add(i);
			}
		}
		return layerIDs;
	}
	
	@Override
	public String layer_getName(int lid) throws UIException{
		assert_validLID(lid);
		return nodeLayers.get(lid).getName();
	}
	
	@Override
	public void layer_setName(int lid, String name) throws UIException{
		assert_validLID(lid);
		for(NodeLayer l : nodeLayers){
			if(l != null && l.getName().equals(name)){
				throw new UIException("A layer with the name '"+name+"' already exists");
			}
		}
		nodeLayers.get(lid).setName(name);
	}

}
