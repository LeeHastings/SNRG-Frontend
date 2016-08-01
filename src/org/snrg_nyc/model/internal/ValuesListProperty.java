package org.snrg_nyc.model.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.snrg_nyc.util.AbstractFactory;


public abstract class ValuesListProperty<T extends ListValue> extends NodeProperty {
	private static final long serialVersionUID = 1L;
	
	public static class Distribution {
		protected Map<Integer, Float> probabilities;
		
		public Distribution(Map<Integer, Float> probabilities){
			this.probabilities = new HashMap<>(probabilities);
		}
		
		/** @return A copy of the probabilities map */
		public Map<Integer, Float> getProbabilities() {
			return new HashMap<>(probabilities);
		}
		
		public void print(){
			System.out.println("\tProbabilities");
			for(Entry<Integer, Float> p : probabilities.entrySet()){
				System.out.printf("\t\tValue: %d\tProbability: %.2f\n", p.getKey(), p.getValue());
			}
		}
	}

	public static class ConditionalDistribution extends Distribution{
		private Map<Integer, Integer> conditions;
		
		public ConditionalDistribution(Map<Integer, Integer> conditions, Map<Integer, Float> probabilities){
			super(probabilities);
			this.conditions = new HashMap<>(conditions);
		}
		
		/** @return A copy of the conditions map */
		public Map<Integer, Integer> getConditions() {
			return new HashMap<>(conditions);
		}
		
		@Override
		public void print(){
			System.out.println("\tConditions: ");
			for(Entry<Integer, Integer> c : conditions.entrySet()){
				System.out.printf("\t\tProp ID: %d\tRange ID: %d\n", c.getKey(), c.getValue());
			}
			super.print();
		}
	}
	protected List<T> values;
	protected List<Integer> ids;
	
	private AbstractFactory<T> valueFactory;
	private List<ConditionalDistribution> conDistributions;
	private List<Integer> condOrder;
	private Distribution defaultDist;
	
	public ValuesListProperty(String name, String desc, AbstractFactory<T> factory) {
		super(name, desc);
		init(factory);
	}

	public ValuesListProperty( AbstractFactory<T> factory) {
		super();
		init(factory);
	}
	
	private void init(AbstractFactory<T> factory){
		conDistributions = new ArrayList<>();
		condOrder = new ArrayList<>();
		defaultDist=null;
		distType = DistType.UNIVARIAT;
		values = new ArrayList<>();
		ids = new ArrayList<>();
		valueFactory = factory;
	}
	
	public boolean 
	distributionsAreSet(){
		return(defaultDist != null && conDistributions.size() > 0);
	}

	public boolean 
	validRID(int rid){
		return (rid >= 0 && rid < values.size() && values.get(rid) != null);
	}
	/**
	 * Check if the given Conditional Condition ID is valid, throw an exception if it isn't.
	 * @param cid
	 * @throws IllegalArgumentException
	 */
	protected void assert_validCID(int cid) throws IllegalArgumentException{
		if(!(cid >= 0 && cid <=conDistributions.size() && conDistributions.get(cid)!= null)){
			throw new IllegalArgumentException(errorMessage+
					"Invalid Conditional Distribution ID: "+cid);
		}
	}
	
	int compareRanges(int rid1, int rid2){
		return 0; //Never change them (sort by the order they're given)
	}
	/**
	 * Check if the given Range Item ID is valid, throw an exception if it isn't.
	 * @param rid 
	 * @throws IllegalArgumentException
	 */
	protected void assert_validRID(int rid) throws IllegalArgumentException{
		if (!validRID(rid)){
			throw new IllegalArgumentException(errorMessage+
					"Invalid Range Item ID: "+rid);
		}
	}
	
	public int 
	addRange(){
		int rid = -1;
		if(distributionsAreSet()){
			throw new IllegalStateException(errorMessage+
					"Cannot edit range labels once distributions are set");
		}
		int len = values.size();
		
		T newValue = valueFactory.build();
		for(int i = 0; i < len; i++){
			if(values.get(i) == null){
				values.set(i, newValue);
				rid = i;
			}
		}
		if(rid == -1){
			values.add(newValue);
			rid = len;
		}
		ids.add(rid);
		return rid;
	}
	public void 
	removeRange(int rid){
		if(distributionsAreSet()){
			throw new IllegalStateException(errorMessage+
					"Cannot edit range labels once distributions are set");
		}
		assert_validRID(rid);
		values.set(rid, null);
		ids.remove(rid);
	}
	public Integer getRangeWithLabel(String label){
		for(ListValue s : values){
			if(s != null && s.labelIs(label)){
				return values.indexOf(s);
			}
		}
		return null;
	}
	public String getRangeLabel(int rid){
		assert_validRID(rid);
		return values.get(rid).getLabel();
	}
	public void setRangeLabel(int rid, String label){
		if(label == null || label.length() == 0){
			throw new IllegalArgumentException(
					"Cannot set a range label to an empty string.");
		}
		if(distributionsAreSet()){
			throw new IllegalStateException(errorMessage+
					"Cannot edit range labels once distributions are set");
		}
		assert_validRID(rid);
		for(int rid2 : ids){
			if(rid2 != rid && values.get(rid2).getLabel().equals(label) ){
				throw new IllegalStateException("Duplicate range label: "
						+label);
			}
		}
		values.get(rid).setLabel(label);
	}

	public List<Integer> 
	getSortedRangeIDs(){
		List<Integer> rangeOrder = new ArrayList<>(ids);
		rangeOrder.sort((a, b)->compareRanges(a, b));
		return rangeOrder;
	}
	
	public List<Integer> getUnSortedRangeIDs(){
		return new ArrayList<>(ids);
	}
	
	@Override
	public void useUniformDistribution(){
		distType = DistType.UNIFORM;
		defaultDist = null;
		conDistributions = null;
	}
	
	public List<Integer> getConditionalDistributionIDs(){
		if(distType != DistType.UNIVARIAT){
			throw new IllegalStateException(errorMessage+
					"No conditional distributions in a distribution"
					+ "of type "+distType.toString());
		}
		List<Integer> ls = new ArrayList<>();
		for(int i = 0; i < conDistributions.size(); i++){
			if(conDistributions.get(i) != null){
				ls.add(i);
			}
		}
		return ls;
	}
	public List<Integer> getOrderedConditions(){
		return condOrder;
	}
	/**
	 * Get the dependency conditions for a conditional distribution.
	 * @param cid The conditional distribution ID.
	 * @return The map of node property IDs to labels in the node property
	 * @throws IllegalArgumentException Thrown if there is no conditional distribution with the given cid
	 */
	public Map<Integer, Integer> getConDistributionConditions(int cid) throws IllegalArgumentException{
		if(distType != DistType.UNIVARIAT){
			throw new IllegalStateException(errorMessage+
					"There are no conditional distributions in a uniform distribution");
		}
		assert_validCID(cid);
		return conDistributions.get(cid).getConditions();
	}
	/**
	 * Get the probabilities in a conditional distribution
	 * @param cid The conditional distribution
	 * @return A map of string labels (labels in this property) to floating point probabilities
	 * @throws IllegalArgumentException Thrown if there is no conditional distribution with the given cid
	 */
	public Map<Integer, Float> getConDistributionProbMap(int cid) throws IllegalArgumentException{
		if(distType != DistType.UNIVARIAT){
			throw new IllegalStateException(errorMessage+
					"There are no conditional distributions in a distribution"
					+ " of type "+distType.toString());
		}
		assert_validCID(cid);
		return conDistributions.get(cid).getProbabilities();
	}
	/**
	 * Check if the default distribution is set
	 * @return True if the default distribution is not null, false otherwise.
	 */
	public boolean hasDefaultDistribution(){
		return (defaultDist != null);
	}

	public boolean rangeIsSet(int rid){
		assert_validRID(rid);
		return (values.get(rid) != null);
	}
	public void setDefaultDistribution(Distribution distribution){
		if(distType != DistType.UNIVARIAT){
			throw new IllegalStateException(errorMessage+
					"There is no default distribution on a distribution of type "
					+distType.toString());
		}
		defaultDist = distribution;
	}
	/**
	 * Get the probabilities in the default distribution
	 * @return A map of this node property's range IDs to floating-point probabilities.
	 * @throws IllegalStateException Thrown if the default distribution has not been set 
	 * (as opposed to a {@link NullPointerException})
	 */
	public Map<Integer, Float> getDefaultDistribution() throws IllegalStateException{
		if(distType != DistType.UNIVARIAT){
			throw new IllegalStateException(errorMessage+
					"There is no default distribution on a distribution of type "
					+distType.toString());
		}
		if(defaultDist == null){
			throw new IllegalStateException(errorMessage+
					"Default distribution was never set.");
		}
		else {
			return defaultDist.getProbabilities();
		}
	}
	/**
	 * Search for a range ID based on a label
	 * @param label The String to search the range labels for
	 * @return The rid of the range, or -1 if not found.
	 */
	public int searchRangeIDs(String label){
		for(int i : getUnSortedRangeIDs()){
			if(label.equals(values.get(i) )){
				return i;
			}
		}
		return -1;
	}
	public int 
	addConditionalDistribution(ConditionalDistribution cd){
		int ID = -1;
		if(conDistributions == null){
			conDistributions = new ArrayList<>();
		}
		if(distType != DistType.UNIVARIAT){
			throw new IllegalStateException(errorMessage+
					"Cannot add conditional distributions to "
					+ "a distribution of type "+distType.toString());
		}
		int len = conDistributions.size();
		for(int cid = 0; cid < len; cid ++){
			if(conDistributions.get(cid) == null){
				conDistributions.set(cid, cd);
				ID = cid;
			}
		}
		if(ID == -1){
			conDistributions.add(cd);
			ID = len;
		}
		condOrder.add(ID);
		return ID;
	}
	public void removeConditionalDistribution(int cid){
		assert_validCID(cid);
		conDistributions.set(cid, null);
		condOrder.remove(condOrder.indexOf(cid));
	}
	public void setConditionalDistribution(int cid, ConditionalDistribution dist){
		assert_validCID(cid);
		conDistributions.set(cid, dist);
	}
	public List<ConditionalDistribution> getConditionalDistributions(){
		return conDistributions;
	}
	public void setConditionsOrder(List<Integer> order){
		if(condOrder.size() != order.size()){
			throw new IllegalArgumentException(errorMessage+
					"The new conditions order does not have "
					+ "the right number of distributions!");
		}
		for(int cid : order){
			if(!condOrder.contains(cid)){
				throw new IllegalArgumentException(
					"Unknown conditional distribution ID given: "+cid);
			}
		}
		condOrder = order;
	}
	
	@Override
	public void removeDependency(int pid){
		if(distributionsAreSet()){
			throw new IllegalStateException(errorMessage+
					"Cannot edit dependencies once distributions are set");
		}
		else {
			super.removeDependency(pid);
		}
	}
	
}
	

