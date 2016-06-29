package org.snrg_nyc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.gson.annotations.SerializedName;

/**
 * An intermediate class for use in JSON serialization of distributions
 * @author Devin Hastings
 *
 */
class UnivariatDistribution implements Serializable {
	private static final long serialVersionUID = 1L;
	class Condition{
		String Name;
		String Value;
		
		Condition(String n, String v){
			Name = n;
			Value = v;
		}
	}
	class ValuePair{
		String Label;
		Float Value;
		
		ValuePair(String l, Float v){
			Label = l;
			Value = v;
		}
	}
	class DistributionList {
		@SerializedName("DistributionSampleList")
		List<ValuePair> values;
		DistributionList(List<ValuePair> vals){
			values = vals;
		}
	}
	class ConditionalDistList extends DistributionList{
		@SerializedName("PropertyDependencyList")
		List<Condition> conditions;
		
		ConditionalDistList(List<ValuePair> vals, List<Condition> conds) {
			super(vals);
			conditions = conds;
		}
	}
	@SerializedName("UnivariatDistributionID")
	private String name;
	
	@SerializedName("BindToPropertyName")
	private String propName;
	
	@SerializedName("DependencyDistributionList")
	private List<DistributionList> distributions;
	
	UnivariatDistribution(UI_Interface ui, NodeProperty np) throws UIException{
	
		if(np.getDistributionType() != NodeProperty.DistType.UNIVARIAT){
			throw new IllegalArgumentException(
					"Cannot make a univariat distribution for a property with a "
					+np.getDistributionType().name()+" distribution.");
		}
		propName = np.getName();
		name = np.getDistributionID();
		distributions = new ArrayList<>();
		EnumeratorProperty ep = (EnumeratorProperty) np;

		List<Condition> conds = new ArrayList<>();
		List<ValuePair> pairs = new ArrayList<>();
		for(int i : ep.getOrderedContitions()){
			conds.clear();
			pairs.clear();
			for(Map.Entry<Integer, Integer> idPair : 
				ep.getConDistributionConditions(i).entrySet()
			){
				conds.add(new Condition(
						ui.nodeProp_getName(idPair.getKey()),
						ui.nodeProp_getRangeLabel(idPair.getKey(), idPair.getValue())) 
						);
			}
			for(Map.Entry<Integer, Float> probPair : 
				ep.getConDistributionProbMap(i).entrySet()
			){
				pairs.add(new ValuePair(
						ep.getRangeLabel(probPair.getKey()),probPair.getValue())
						);
			}
			distributions.add(new ConditionalDistList(pairs, conds));
		}
		
		pairs.clear();
		for(Map.Entry<Integer, Float> keyPair : ep.getDefaultDistribution().entrySet()){
			pairs.add(new ValuePair(
					ep.getRangeLabel(keyPair.getKey()), keyPair.getValue())
					);
		}
		distributions.add(new DistributionList(pairs));
	}
	String getDistributionID(){
		return name;
	}
	
	NodeProperty addToProperty(UI_Interface ui, NodeProperty np){
		EnumeratorProperty ep;
		if(np instanceof EnumeratorProperty){
			ep = (EnumeratorProperty) np;
		}
		else {
			throw new IllegalArgumentException("The given property must be able to use distributions!");
		}
		Map<Integer, Integer> conds = new HashMap<>();
		Map<Integer, Float> rangeProbs = new HashMap<>();
		
		for(DistributionList dist : distributions){
			conds.clear();
			rangeProbs.clear();
			
			if(dist instanceof ConditionalDistList){
				ConditionalDistList cDist = (ConditionalDistList) dist;
				for(Condition c : cDist.conditions){
				}
			}
		}
		return np;
	}
}
