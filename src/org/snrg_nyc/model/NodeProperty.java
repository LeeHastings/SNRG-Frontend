package org.snrg_nyc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The base class for all node properties
 * @author Devin
 *
 */
public abstract class NodeProperty implements Serializable {
	private static final long serialVersionUID = 1L;
	
	enum DistType{
		NULL,
		UNIFORM,
		UNIVARIAT
	}
	static class Distribution {
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

	static class ConditionalDistribution extends Distribution{
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
	
	protected String name;
	protected int dependencyLevel;
	protected String description;
	protected List<Integer> dependencies;
	protected DistType distType;
	
	public NodeProperty(){
		name = null;
		description = null;
		dependencyLevel = -1;
		dependencies = new ArrayList<Integer>();
	}
	public NodeProperty(String name, String description){
		this();
		setName(name);
		setDescription(description);
	}
	public void setName(String name){
		if(name == null || name == ""){
			throw new IllegalArgumentException("The name of a property cannot be empty or null");
		} 
		else {
			this.name = name;
		}
	}
	public void setDescription(String description){
		if(description == null || description.equals("")){
			throw new IllegalArgumentException("The description cannot be null or empty.");
		}
		else {
			this.description = description;
		}
	}
	public void setDependencyLevel(int dlvl){
		if(dlvl < 0){
			throw new IllegalArgumentException("The dependency level cannot be negative: "+dlvl);
		}
		else {
			dependencyLevel = dlvl;
		}
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public int getDependencyLevel() {
		return dependencyLevel;
	}
	/**
	 * Get this property's listed dependencies.
	 * @return A list of node property IDs representing the dependencies.
	 */
	public List<Integer> getDependencies(){
		return dependencies;
	}
	
	public void addDependency(int pid){
		if(dependencies.contains(pid)){
			throw new IllegalArgumentException(
					"Duplicate dependency ID '"+pid+"' in property '"+name+"'.");
		}
		else {
			dependencies.add(pid);
		}
	}
	
	public void removeDependency(int pid){
		if(!dependencies.contains(pid)){
			throw new IllegalArgumentException(
					"Tried to delete nonexistant dependency ID'"+pid+"' from property '"+name+"'.");
		}
		else {
			dependencies.remove(new Integer(pid));
		}
	}

	public boolean dependenciesAreSet(){
		return (dependencies != null && dependencies.size() > 0);
	}
	
	public void useUniformDistribution(){
		distType = DistType.UNIFORM;
		dependencyLevel = 0;
	}
	public DistType getDistributionType(){
		return distType;
	}
	
	public boolean dependsOn(int pid){
		return dependencies.contains(pid);
	}
	
	public String getDistributionID(){
		switch(distType){
		case UNIFORM:
			return "uniform";
		case NULL:
			return "null";
		case UNIVARIAT:
			return name.replace(' ', '_').toLowerCase()+"_unidist";
		default:
			return name.replace(' ', '_').toLowerCase()+"_dist";
		}
		
	}
}