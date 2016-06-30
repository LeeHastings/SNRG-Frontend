package org.snrg_nyc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.NodeProperty.ConditionalDistribution;
import org.snrg_nyc.model.NodeProperty.Distribution;

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
	static class DistributionList {
		@SerializedName("DistributionSampleList")
		List<ValuePair> values;
		DistributionList(List<ValuePair> vals){
			values = vals;
		}
	}
	static class ConditionalDistList extends DistributionList{
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
	List<DistributionList> distributions;
	
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

		
		for(int i : ep.getOrderedConditions()){
			List<Condition> conds = new ArrayList<>();
			List<ValuePair> pairs = new ArrayList<>();
			
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
						ep.getRangeLabel(probPair.getKey()), probPair.getValue())
						);
			}
			distributions.add(new ConditionalDistList(pairs, conds));
		}
		List<ValuePair> pairs = new ArrayList<>();
		for(Map.Entry<Integer, Float> keyPair : ep.getDefaultDistribution().entrySet()){
			pairs.add(new ValuePair(
					ep.getRangeLabel(keyPair.getKey()), keyPair.getValue())
					);
		}
		distributions.add(new DistributionList(pairs));
	}

	void addToProperty(UI_Interface ui, NodeProperty np) throws UIException{
		if(!np.getName().equals(propName)){
			throw new IllegalArgumentException("This distribution is for property '"+propName
					+"', tried to bind it to property '"+np.getName()+"'");
		}
		
		EnumeratorProperty ep;
		
		if(np instanceof EnumeratorProperty){
			ep = (EnumeratorProperty) np;
		}
		else {
			throw new IllegalArgumentException("The given property must be able to use distributions!");
		}
		ep.distType = NodeProperty.DistType.UNIVARIAT;
		
		Map<Integer, Integer> conds = new HashMap<>();
		Map<Integer, Float> rangeProbs = new HashMap<>();
		
		for(DistributionList dist : distributions){
			conds.clear();
			rangeProbs.clear();
			
			for(ValuePair pair : dist.values){
				Integer rid = ep.getRangeWithLabel(pair.Label);
				if(rid == null){
					throw new IllegalArgumentException("No range with label '"+pair.Label
							+"' in node property '"+ep.getName()+"'");
				}
				rangeProbs.put(rid, pair.Value);
			}
			if(dist instanceof ConditionalDistList){
				ConditionalDistList cDist = (ConditionalDistList) dist;
				for(Condition c : cDist.conditions){
					Integer pid = ui.search_nodePropWithName(c.Name);
					if(!ep.dependsOn(pid)){
						ep.addDependency(pid);
					}
					if(pid == null){
						throw new IllegalArgumentException(
								"There is no property with the given name: "+c.Name);
					}
					Integer rid = ui.search_rangeWithLabel(pid, c.Value);
					if(rid == null){
						throw new IllegalArgumentException(
								"There is no range named '"+c.Value+"'in the property '"
								+c.Name+"'");
					}
					conds.put(pid,rid);
				}
				ep.addConditionalDistribution(new ConditionalDistribution(conds, rangeProbs));
			}
			else {
				if(distributions.indexOf(dist) != distributions.size()-1){
					throw new IllegalArgumentException(
							"Found a default distribution before the end of the "
							+ "distribution list in Univariat Distribution "+name);
				}
				ep.setDefaultDistribution(new Distribution(rangeProbs));
			}
		}
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getName() {
		return name;
	}
	public String getPropName() {
		return propName;
	}
	public List<DistributionList> getDistributions() {
		return distributions;
	}
	
}