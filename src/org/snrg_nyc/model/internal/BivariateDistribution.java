package org.snrg_nyc.model.internal;

import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.model.EditorException;

public class BivariateDistribution {
	protected ValuesListProperty<?> property;
	protected Map<Integer, Map<Integer, Float>> distribution = new HashMap<>();
	
	public BivariateDistribution(ValuesListProperty<?> property){
		this.property = property;
		for(int rid : property.getUnSortedRangeIDs()){
			Map<Integer, Float> ridDist = new HashMap<>();
			for(int rid2 : property.getUnSortedRangeIDs()){
				ridDist.put(rid2, null);
			}
			distribution.put(rid, ridDist);
		}
	}
	/**
	 * Check if all the values in the distribution are set.
	 * @return
	 */
	public boolean 
	ready(){
		for(Map<Integer, Float> map : distribution.values()){
			for(Float f : map.values()){
				if(f == null){
					return false;
				}
			}
		}
		return true;
	}
	
	public void
	setValue(int rid1, int rid2, float value) throws EditorException{
		if(!distribution.containsKey(rid1) ){
			throw new EditorException(
					"Unknown range ID in distribution: "+rid1);
		}
		else if(!distribution.containsKey(rid2)){
			throw new EditorException(
					"Unknown range ID in distribution: "+rid2);
		}
		distribution.get(rid1).put(rid2, value);
	}
	
	public float
	getValue(int rid1, int rid2){
		return distribution.get(rid1).get(rid2);
	}
	
	public ValuesListProperty<?> property(){
		return property;
	}
}
