package org.snrg_nyc.model;

import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.util.PropertyID;

public class LayerPropertyReader implements PropertyReader {
	PropertyID id;
	PropertiesEditor model;
	public LayerPropertyReader(PropertiesEditor model, int pid){
		id = new PropertyID(pid);
		this.model = model;
	}
	public LayerPropertyReader(PropertiesEditor model, int lid, int pid){
		id = new PropertyID(lid, pid);
		this.model = model;
	}
	public LayerPropertyReader(PropertiesEditor model,PropertyID id){
		this.id = id;
		this.model = model;
	}
	@Override
	public String name() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getName(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getName(id.pid());
		}
	}
	@Override
	public String description() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getDescription(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getDescription(id.pid());
		}
	}
	@Override
	public String type() throws EditorException {
		if(id.usesLayer()){
			 return model.nodeProp_getType(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getType(id.pid());
		}
	}
	@Override
	public int dependencyLevel() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getDependencyLevel(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getDependencyLevel(id.pid());
		}
	}
	@Override
	public boolean initBool() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getBooleanInitValue(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getBooleanInitValue(id.pid());
		}
	}
	@Override
	public float initFraction() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getFractionInitValue(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getFractionInitValue(id.pid());
		}
	}
	@Override
	public List<Integer> rangeIDs() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getRangeItemIDs(id.lid(),id.pid());
		}
		else {
			return model.nodeProp_getRangeItemIDs(id.pid());
		}
	}
	@Override
	public String rangeLabel(int rid) throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getRangeLabel(id.lid(), id.pid(), rid);
		}
		else {
			return model.nodeProp_getRangeLabel(id.pid(), rid);
		}
	}
	@Override
	public int rangeMin(int rid) throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getRangeMin(id.lid(), id.pid(), rid);
		}
		else {
			return model.nodeProp_getRangeMin(id.pid(), rid);
		}
	}
	@Override
	public int rangeMax(int rid) throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getRangeMax(id.lid(), id.pid(), rid);
		}
		else {
			return model.nodeProp_getRangeMax(id.pid(), rid);
		}
	}
	@Override
	public int pathogenID() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getPathogenID(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getPathogenID(id.pid());
		}
	}
	@Override
	public String pathogenType() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getPathogenType(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getPathogenType(id.pid());
		}
	}
	@Override
	public boolean isRanged() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_isRangedProperty(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_isRangedProperty(id.pid());
		}
	}
	@Override
	public boolean uniformDistribution() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_hasUniformDistribution(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_hasUniformDistribution(id.pid());
		}
	}
	@Override
	public List<Integer> distributionIDs() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getConditionalDistributionIDs(
					id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getConditionalDistributionIDs(
					id.pid());
		}
	}
	@Override
	public Map<Integer, Integer> 
	distributionConditions(int cid) throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getDistributionConditions(
					id.lid(), id.pid(), cid);
		}
		else {
			return model.nodeProp_getDistributionConditions(
					id.pid(), cid);
		}
	}
	@Override
	public Map<Integer, Float> 
	distributionMap(int cid) throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getDistribution(id.lid(), id.pid(), cid);
		}
		else {
			return model.nodeProp_getDistribution(id.pid(), cid);
		}
	}
	@Override
	public Map<Integer, Float> defaultDistribution() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getDefaultDistribution(
					id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getDefaultDistribution(
					id.pid());
		}
	}
	@Override
	public List<Integer> dependencies() throws EditorException {
		if(id.usesLayer()){
			return model.nodeProp_getDependencyIDs(id.lid(), id.pid());
		}
		else {
			return model.nodeProp_getDependencyIDs(id.pid());
		}
	}
}
