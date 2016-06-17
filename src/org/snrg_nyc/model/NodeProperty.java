package org.snrg_nyc.model;

import java.util.ArrayList;
import java.util.List;

import org.snrg_nyc.model.UI_Interface.DistributionType;

/**
 * The base class for all node properties
 * @author Devin
 *
 */
abstract class NodeProperty {
	protected String name;
	protected int dependencyLevel;
	protected String description;
	protected List<Integer> dependencies;
	protected DistributionType distType;
	
	public NodeProperty(){
		name = null;
		description = null;
		dependencyLevel = -1;
		dependencies = new ArrayList<Integer>();
		distType = DistributionType.Null;
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
		distType = DistributionType.Uniform;
	}
	public DistributionType getDistributionType(){
		return distType;
	}
	
	public boolean dependsOn(int pid){
		return dependencies.contains(pid);
	}
	
	/**
	 * Print a description of the node property.
	 */
	public void print(){
		System.out.println("Name: "+name);
		System.out.println("Dependency Level: "+dependencyLevel);
		System.out.println("Type: "+this.getClass().getSimpleName());
		System.out.println("Description: "+this.description);
		System.out.print("Dependencies: ");
		if(dependencies.size() == 0){
			System.out.println("None");
		}
		for(int i: dependencies){
			if(dependencies.lastIndexOf(i)!=dependencies.size()-1){
				System.out.print("Property #"+i+", ");
			}
			else {
				System.out.print("Property #"+i+"\n");
			}
		}
	}
}