package org.snrg_nyc.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.UIException;
import org.snrg_nyc.model.UI_Interface;
import org.snrg_nyc.model.UI_Interface.DistributionType;

/**
 * A wrapper class for retrieving node information from the {@link UI_Interface}.
 * This is designed solely for reading node properties; it does not aid in
 * creating nodes and it doesn't handle any {@link UIException} thrown from the interface,
 * leaving that to the calling class.
 * @author Devin
 *
 */
class NodePropertyReader {
	private boolean useLayer;
	private int propID, layerID;
	private UI_Interface ui;
	/**
	 * Create a new node property reader, which assists in pulling data from node properties
	 * @param ui An instance of {@link UI_Interface} from which the reader will
	 * retrieve its information.
	 * @param pid The ID of the node property to attach this reader to
	 * @throws IllegalArgumentException Thrown if the property ID is not valid.
	 */
	public NodePropertyReader(UI_Interface ui, int pid) throws IllegalArgumentException{
		if(!ui.test_nodePropIDExists(pid)){
			throw new IllegalArgumentException("Invalid property ID for new property: "+pid);
		}
		propID = pid;
		useLayer = false;
		this.ui = ui;
	}
	/**
	 * The same as {@link NodePropertyReader#NodePropertyReader(UI_Interface, int)},
	 * but for layer objects.
	 * @param ui The instance of {@link UI_Interface} to read from.
	 * @param lid The layer ID to use
	 * @param pid The property ID to use
	 * @throws IllegalArgumentException Thrown if the combination of layer and property IDs
	 * does not point to a valid property.
	 */
	public NodePropertyReader(UI_Interface ui, int lid, int pid) throws IllegalArgumentException{
		try{
			if(!ui.test_nodePropIDExists(lid, pid)){
				throw new IllegalArgumentException("Invalid property ID for new property: "+pid);
			}
		}
		catch(UIException ue){
			throw new IllegalArgumentException("Invalid layer ID for new property: "+lid);
		}
		propID = pid;
		layerID = lid;
		useLayer = true;
		this.ui = ui;
	}
	/**
	 * @see UI_Interface#nodeProp_getName(int) 
	 * @see UI_Interface#nodeProp_getName(int, int)
	 */
	public String name() throws UIException{
		if(useLayer){
			return ui.nodeProp_getName(layerID, propID);
		}
		else {
			return ui.nodeProp_getName(propID);
		}
	}
	/**
	 * @see UI_Interface#nodeProp_getDescription(int)
	 * @see UI_Interface#nodeProp_getDescription(int, int)
	 */
	public String description() throws UIException{
		if(useLayer){
			return ui.nodeProp_getDescription(layerID, propID);
		}
		else {
			return ui.nodeProp_getDescription(propID);
		}
	}
	/**
	 * @see UI_Interface#nodeProp_getType(int)
	 * @see UI_Interface#nodeProp_getType(int, int)
	 */
	public String type() throws UIException{
		if(useLayer){
			return ui.nodeProp_getType(layerID, propID);
		}
		else {
			return ui.nodeProp_getType(propID);
		}
	}
	/**
	 * @see UI_Interface#nodeProp_getDependencyLevel(int)
	 * @see UI_Interface#nodeProp_getDependencyLevel(int, int)
	 */
	public int dependencyLevel() throws UIException{
		if(useLayer){
			return ui.nodeProp_getDependencyLevel(layerID, propID);
		}
		else {
			return ui.nodeProp_getDependencyLevel(propID);
		}
	}
	/**
	 * @see UI_Interface#nodeProp_getDistributionType(int)
	 * @see UI_Interface#nodeProp_getDistributionType(int, int)
	 */
	public DistributionType distributionType() throws UIException{
		if(useLayer){
			return ui.nodeProp_getDistributionType(layerID, propID);
		}
		else {
			return ui.nodeProp_getDistributionType(propID);
		}
	}
	/**
	 * @see UI_Interface#nodeProp_getDependencyIDs(int)
	 * @see UI_Interface#nodeProp_getDependencyIDs(int, int)
	 */
	public List<NodePropertyReader> dependencies() throws UIException{
		List<NodePropertyReader> ls = new ArrayList<>();
		for(int i : dependencyIDs()){
			ls.add(new NodePropertyReader(ui, i));
		} 
		return ls;
	}
	public NodePropertyReader dependency(int pid) throws UIException{
		if(useLayer){
			return new NodePropertyReader(ui, layerID, pid);
		}
		else {
			return new NodePropertyReader(ui, pid);
		}
	}
	public List<Integer> dependencyIDs() throws UIException{
		if(useLayer){
			return ui.nodeProp_getDependencyIDs(layerID, propID);
		}
		else {
			return ui.nodeProp_getDependencyIDs(propID);
		}
	}
	public List<Integer> rangeIDs() throws UIException{
		if(useLayer){
			return ui.nodeProp_getRangeItemIDs(layerID, propID);
		}
		else {
			return ui.nodeProp_getRangeItemIDs(propID);
		}
	}
	public String rangeLabel(int rid) throws UIException{
		if(useLayer){
			return ui.nodeProp_getRangeLabel(layerID, propID, rid);
		}
		else {
			return ui.nodeProp_getRangeLabel(propID, rid);
		}
	}
	public int rangeMax(int rid) throws UIException{
		if(useLayer){
			return ui.nodeProp_getRangeMax(layerID, propID, rid);
		}
		else {
			return ui.nodeProp_getRangeMax(propID, rid);
		}
	}
	public int rangeMin(int rid) throws UIException{
		if(useLayer){
			return ui.nodeProp_getRangeMin(layerID, propID, rid);
		}
		else {
			return ui.nodeProp_getRangeMin(propID, rid);
		}
	}
	public List<Integer> conditionalDistributionIDs() throws UIException{
		if(useLayer){
			return ui.nodeProp_getConditionalDistributionIDs(layerID, propID);
		}
		else {
			return ui.nodeProp_getConditionalDistributionIDs(propID);
		}
	}
	public Map<Integer, Integer> distributionConditions(int cid) throws UIException{
		if(useLayer){
			return ui.nodeProp_getDistributionConditions(layerID, propID, cid);
		}
		else {
			return ui.nodeProp_getDistributionConditions(propID, cid);
		}
	}
	public Map<Integer, Float> distributionMap(int cid) throws UIException{
		if(useLayer){
			return ui.nodeProp_getDistribution(layerID, propID, cid);
		}
		else {
			return ui.nodeProp_getDistribution(propID, cid);
		}
	}
	public Map<Integer, Float> defaultDistribution() throws UIException{
		if(useLayer){
			return ui.nodeProp_getDefaultDistribution(layerID, propID);
		}
		else {
			return ui.nodeProp_getDefaultDistribution(propID);
		}
	}
	public boolean isRanged() throws UIException{
		if(useLayer){
			return ui.nodeProp_isRangedProperty(layerID, propID);
		}
		else {
			return ui.nodeProp_isRangedProperty(propID);
		}
	}
	public float initValue() throws UIException{
		if(useLayer){
			return ui.nodeProp_getInitValue(layerID, propID);
		}
		else {
			return ui.nodeProp_getInitValue(propID);
		}
	}
}
