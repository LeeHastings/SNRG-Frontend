package org.snrg_nyc.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class Distribution {
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
