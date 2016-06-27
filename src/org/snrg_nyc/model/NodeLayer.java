package org.snrg_nyc.model;

import java.util.ArrayList;
import java.util.List;

class NodeLayer {
	private String name;
	/**
	 * A nullable list of node properties
	 */
	private List<NodeProperty> layerAttributes;
	public NodeLayer(String name){
		this.name = name;
		layerAttributes = new ArrayList<>();
	}
	public String getName(){
		return name;
	}
	public void setName(String name){
		if(name == null || name.equals("")){
			throw new IllegalArgumentException("The new layer name cannot be empty or null");
		}
		else {
			this.name = name;
		}
	}
	/**
	 * Add a {@link NodeProperty} to the layer
	 * @param np The {@link NodeProperty} to add
	 * @return The ID of the new property in the layer
	 * @throws IllegalArgumentException Thrown if a property with the same name exists in the layer.
	 */
	public int addProperty(NodeProperty np) throws IllegalArgumentException{
		//Check for duplicates
		if(!nameIsUnique(np.getName())){
			String msg = String.format("Error when adding property '%s' to layer "
					+ "'%s': a property with this name already exists.", np.getName(), name);
			throw new IllegalArgumentException(msg);
		}
		//The list is nullable when objects are removed, use one of those spots instead
		for(int i = 0;  i < layerAttributes.size();i++ ){
			if(layerAttributes.get(i) == null){
				layerAttributes.set(i, np);
			}
		}
		layerAttributes.add(np);
		return layerAttributes.indexOf(np);
	}
	public List<Integer> getPropertyIDs(){
		List<Integer> ids = new ArrayList<>();
		for(int i = 0; i < layerAttributes.size(); i++){
			if(layerAttributes.get(i) != null){
				ids.add(i);
			}
		}
		return ids;
	}
	
	public NodeProperty getProperty(int pid){
		assert_validPID(pid);
		return layerAttributes.get(pid);
	}

	public List<NodeProperty> getProperties(){
		return layerAttributes;
	}
	public void removeProperty(int pid){
		assert_validPID(pid);
		layerAttributes.set(pid, null);
	}
	/**
	 * Assert that the given property ID points to a non-null node property in the layer.
	 * @param pid The node property ID that must be valid.
	 * @throws IllegalArgumentException Thrown if the property ID given is not valid.
	 */
	private void assert_validPID(int pid) throws IllegalArgumentException{
		if(!validPID(pid)){
			throw new IllegalArgumentException("Tried to get a property from layer '"+ name
					+"' with an invalid property ID: "+pid);
		}
	}
	/**
	 * If the given property ID points to a non-null property in the layer
	 * @param pid The PID to check
	 * @return If it exists and is non-null
	 */
	public boolean validPID(int pid){
		return (pid >= 0 && pid < layerAttributes.size() && layerAttributes.get(pid) != null);
	}
	
	public boolean nameIsUnique(String name){
		for(NodeProperty p : layerAttributes){
			if(p != null && p.getName().equals(name)){
				return false;
			}
		}
		return true;
	}
	public void print(){
		System.out.println("Name: "+name);
		System.out.println("Properties:\n---------------");
		for(int pid : getPropertyIDs()){
			layerAttributes.get(pid).print();
		}
	}
}
