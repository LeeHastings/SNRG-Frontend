package org.snrg_nyc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.UI_Interface.DistributionType;

class EnumeratorProperty extends NodeProperty {
	protected List<String> values;
	protected List<ConditionalDistribution> conDistributions;
	protected Distribution defaultDist;
	
	public EnumeratorProperty(){
		super();
		values = new ArrayList<>();
		conDistributions = new ArrayList<>();
		defaultDist=null;
		distType = DistributionType.Conditional;
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
		if(distType != DistributionType.Conditional){
			throw new IllegalStateException("Cannot add conditional distributions to "
					+ "a distribution of type "+distType.toString());
		}
		int len = conDistributions.size();
		for(int cid = 0; cid < len; cid ++){
			if(conDistributions.get(cid) == null){
				conDistributions.set(cid, cd);
				return cid;
			}
		}
		conDistributions.add(cd);
		return len-1;
	}
	public void removeConditionalDistribution(int cid){
		assert_validCID(cid);
		conDistributions.set(cid, null);
	}
	List<ConditionalDistribution> getConditionalDistributions(){
		return conDistributions;
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
		distType = DistributionType.Uniform;
		defaultDist = null;
		conDistributions = null;
	}
	
	public List<Integer> getConditionalDistributionIDs(){
		if(distType != DistributionType.Conditional){
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
	/**
	 * Get the dependency conditions for a conditional distribution.
	 * @param cid The conditional distribution ID.
	 * @return The map of node property IDs to labels in the node property
	 * @throws IllegalArgumentException Thrown if there is no conditional distribution with the given cid
	 */
	public Map<Integer, Integer> getConDistributionConditions(int cid) throws IllegalArgumentException{
		if(distType != DistributionType.Conditional){
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
		if(distType != DistributionType.Conditional){
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
		if(distType != DistributionType.Conditional){
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
		if(distType != DistributionType.Conditional){
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
	
	@Override
	public void print(){
		super.print();
		for(int rid : getSortedRangeIDs()){
			printRangeItem(rid);
		}
		if(distType !=DistributionType.Conditional){
			System.out.println("Distribution: "+distType.toString());
		}
		else {
			for(int cid : getConditionalDistributionIDs()){
				System.out.println("Conditional Distribution "+cid);
				conDistributions.get(cid).print();
			}
			System.out.println("Default Distribution: ");
			if(defaultDist == null){
				System.out.println("\t<<NULL>>");
			}
			else {
				defaultDist.print();
			}
		}
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