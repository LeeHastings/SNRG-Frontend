package org.snrg_nyc.model.internal;

import java.util.ArrayList;
import java.util.List;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.persistence.Transferable;

/**
 * The base class for all node properties
 * @author Devin
 *
 */
public abstract class NodeProperty implements Transferable {
	private static final long serialVersionUID = 1L;
	
	public enum DistType{
		NULL,
		UNIFORM,
		UNIVARIAT
	}
	
	protected String name;
	protected int dependencyLevel;
	protected String description;
	protected List<Integer> dependencies;
	protected DistType distType;
	protected String errorMessage;
	
	public NodeProperty(){
		name = null;
		description = null;
		dependencyLevel = -1;
		dependencies = new ArrayList<Integer>();
		distType = DistType.NULL;
	}
	public NodeProperty(String name, String description) throws EditorException{
		this();
		errorMessage = "Error in property '"+name+"': ";
		setName(name);
		setDescription(description);
	}
	@Override
	public String getObjectID(){
		return this.name;
	}
	public void setName(String name) throws EditorException{
		if(name == null || name == ""){
			throw new EditorException(errorMessage+"The name of a property cannot be empty or null");
		} 
		else {
			this.name = name;
			errorMessage = "Error in property '"+name+"': ";
		}
	}
	public void setDescription(String description) throws EditorException{
		if(description == null || description.equals("")){
			throw new EditorException(errorMessage+"The description cannot be null or empty.");
		}
		else {
			this.description = description;
		}
	}
	public void setDependencyLevel(int dlvl) throws EditorException{
		if(dlvl < 0){
			throw new EditorException(errorMessage+"The dependency level cannot be negative: "+dlvl);
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
	
	public void addDependency(int pid) throws EditorException{
		if(dependencies.contains(pid)){
			throw new EditorException(
					errorMessage+"Duplicate dependency ID '"+pid+"' in property '"+name+"'.");
		}
		else {
			dependencies.add(pid);
		}
	}
	
	public void removeDependency(int pid) throws EditorException{
		if(!dependencies.contains(pid)){
			throw new EditorException(
					errorMessage+"Tried to delete nonexistant dependency ID'"+pid+"' from property '"+name+"'.");
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
	public void setDistributionType(DistType type){
		this.distType = type;
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
		default:
			return "unidist_"+name.replace(' ', '_').toLowerCase();
		}
		
	}
	String getBiDistID(){
		return "bidist_"+name.toLowerCase().replaceAll(" ", "_");
	}
}