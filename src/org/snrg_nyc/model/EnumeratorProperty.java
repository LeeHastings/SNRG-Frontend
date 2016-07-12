package org.snrg_nyc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class EnumeratorProperty extends NodeProperty {
	private static final long serialVersionUID = 1L;
	
	protected List<String> values;
	protected List<ConditionalDistribution> conDistributions;
	protected List<Integer> condOrder;
	protected Distribution defaultDist;
	
	public EnumeratorProperty(){
		super();
		values = new ArrayList<>();
		conDistributions = new ArrayList<>();
		condOrder = new ArrayList<>();
		defaultDist=null;
		distType = DistType.UNIVARIAT;
	}
	public EnumeratorProperty(String name, String description){
		this();
		this.name = name;
		this.description = description;
	}
	public int addRange(){
		if(distributionsAreSet()){
			throw new IllegalStateException("Cannot edit range labels once distributions are set");
		}
		int len = values.size();
		//Add new ranges into empty spots if available
		for(int rid = 0; rid < len; rid++){
			if(values.get(rid) == null){
				values.set(rid, "");
				return rid;
			}
		}
		values.add("");
		return len;
	}
	public int addConditionalDistribution(ConditionalDistribution cd){
		int ID = -1;
		if(conDistributions == null){
			conDistributions = new ArrayList<>();
		}
		if(distType != DistType.UNIVARIAT){
			throw new IllegalStateException("Cannot add conditional distributions to "
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
			throw new IllegalArgumentException("The new conditions order "
					+ "does not have the right number of distributions!");
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
			throw new IllegalStateException("Cannot edit dependencies once distributions are set");
		}
		else {
			super.removeDependency(pid);
		}
	}
	
	public void removeRange(int rid){
		if(distributionsAreSet()){
			throw new IllegalStateException("Cannot edit range labels once distributions are set");
		}
		assert_validRID(rid);
		values.set(rid, null);
	}
	public Integer getRangeWithLabel(String label){
		for(String s : values){
			if(s != null && s.equals(label)){
				return values.indexOf(s);
			}
		}
		return null;
	}
	public String getRangeLabel(int rid){
		assert_validRID(rid);
		return values.get(rid);
	}
	public void setRangeLabel(int rid, String label){
		if(distributionsAreSet()){
			throw new IllegalStateException("Cannot edit range labels once distributions are set");
		}
		assert_validRID(rid);
		for(String s : values){
			if(s != null && label.equals(s)){
				throw new IllegalArgumentException("Duplicate Label: "+label);
			}
		}
		values.set(rid, label);
	}

	public List<Integer> getSortedRangeIDs(){
		List<Integer> rangeOrder = new ArrayList<>(getUnSortedRangeIDs());
		
		for(int i = 1; i < rangeOrder.size(); i++){
			int j = i;
			while( j > 0 && compareRanges(rangeOrder.get(j-1), rangeOrder.get(j)) >= 0 ){
				int r = rangeOrder.get(j);
				rangeOrder.set(j, rangeOrder.get(j-1));
				rangeOrder.set(j-1, r);
				j--;
			}
		}
		return rangeOrder;
	}
	
	public List<Integer> getUnSortedRangeIDs(){
		List<Integer> sorted = new ArrayList<>();
		int len  = values.size();
		for(int i = 0; i < len; i++){
			if(values.get(i) != null){
				sorted.add(i);
			}
		}
		return sorted;
	}
	@Override
	public void useUniformDistribution(){
		distType = DistType.UNIFORM;
		defaultDist = null;
		conDistributions = null;
	}
	
	public List<Integer> getConditionalDistributionIDs(){
		if(distType != DistType.UNIVARIAT){
			throw new IllegalStateException("No conditional distributions in a distribution"
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
			throw new IllegalStateException("There are no conditional distributions in a uniform distribution");
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
			throw new IllegalStateException("There are no conditional distributions in a distribution"
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
			throw new IllegalStateException("There is no default distribution on a distribution of type "
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
			throw new IllegalStateException("There is no default distribution on a distribution of type "
					+distType.toString());
		}
		if(defaultDist == null){
			throw new IllegalStateException("Default distribution was never set.!");
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
	public void printRangeItem(int rid) throws IllegalStateException{
		if(!rangeIsSet(rid)){
			throw new IllegalStateException("The range item with RID '"+rid+"' was not completely set!");
		}
		System.out.println("Range Item "+rid);
		System.out.println("\tLabel: "+getRangeLabel(rid));
	}

	public boolean distributionsAreSet(){
		return(defaultDist != null && conDistributions.size() > 0);
	}
	
	/**
	 * Check if the given Range Item ID is valid, throw an exception if it isn't.
	 * @param rid 
	 * @throws IllegalArgumentException
	 */
	protected void assert_validRID(int rid) throws IllegalArgumentException{
		if (!validRID(rid)){
			throw new IllegalArgumentException("Invalid Range Item ID: "+rid);
		}
	}
	public boolean validRID(int rid){
		return (rid >= 0 && rid < values.size() && values.get(rid) != null);
	}
	/**
	 * Check if the given Conditional Condition ID is valid, throw an exception if it isn't.
	 * @param cid
	 * @throws IllegalArgumentException
	 */
	protected void assert_validCID(int cid) throws IllegalArgumentException{
		if(!(cid >= 0 && cid <=conDistributions.size() && conDistributions.get(cid)!= null)){
			throw new IllegalArgumentException("Invalid Conditional Distribution ID: "+cid);
		}
	}
	
	/**
	 * An internal method to sort range values.
	 * @param rid1
	 * @param rid2
	 * @return See {@link Comparable#compareTo} for return value conventions.
	 */
	int compareRanges(int rid1, int rid2){
		return 0;
	}
}