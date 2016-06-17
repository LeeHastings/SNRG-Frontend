package org.snrg_nyc.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class ConditionalDistribution extends Distribution{
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
