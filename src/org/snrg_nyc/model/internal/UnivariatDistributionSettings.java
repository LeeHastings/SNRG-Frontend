package org.snrg_nyc.model.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.model.properties.NodeProperty;
import org.snrg_nyc.model.properties.ValuesListProperty;
import org.snrg_nyc.model.properties.ValuesListProperty.ConditionalDistribution;
import org.snrg_nyc.model.properties.ValuesListProperty.Distribution;
import org.snrg_nyc.persistence.Transferable;

import com.google.gson.annotations.SerializedName;

/**
 * An intermediate class for use in JSON serialization of distributions
 * @author Devin Hastings
 *
 */
public class UnivariatDistributionSettings implements Transferable {
	private static final long serialVersionUID = 1L;
	static class Condition{
		String Name;
		String Value;
		
		Condition(String n, String v){
			Name = n;
			Value = v;
		}
	}
	static class ValuePair{
		String Label;
		Float Value;
		
		ValuePair(String l, Float v){
			Label = l;
			Value = v;
		}
	}
	public static class DistributionList {
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
	
	/**
	 * Create an object using data from a node property
	 * @param model The instance of {@link PropertiesEditor} to read data from.
	 * This should be the same editor that the NodeProperty is from
	 * @param np The {@link NodeProperty} to read the distribution from
	 * @throws EditorException Thrown if there was a problem reading
	 * data from the model.
	 */
	public 
	UnivariatDistributionSettings(PropertiesEditor model, NodeProperty np) 
			throws EditorException
	{
	
		if(np.getDistributionType() != NodeProperty.DistType.UNIVARIAT){
			throw new IllegalArgumentException(
					"Cannot make a univariat distribution for a property "
					+ "with a "+np.getDistributionType().name()
					+" distribution.");
		}
		
		propName = np.getName();
		name = np.getDistributionID();
		distributions = new ArrayList<>();
		ValuesListProperty<?> vlp = (ValuesListProperty<?>) np;

		
		for(int i : vlp.getOrderedConditions()){
			List<Condition> conds = new ArrayList<>();
			List<ValuePair> pairs = new ArrayList<>();
			
			for(Map.Entry<Integer, Integer> idPair : 
				vlp.getConDistributionConditions(i).entrySet()
			){
				conds.add(new Condition(
					model.nodeProp_getName(idPair.getKey()),
					model.nodeProp_getRangeLabel(
							idPair.getKey(), idPair.getValue())) 
					);
			}
			
			for(Map.Entry<Integer, Float> probPair : 
				vlp.getConDistributionProbMap(i).entrySet()
			){
				pairs.add(new ValuePair(
					vlp.getRangeLabel(probPair.getKey()), probPair.getValue())
					);
			}
			distributions.add(new ConditionalDistList(pairs, conds));
		}
		
		List<ValuePair> pairs = new ArrayList<>();
		
		for(Map.Entry<Integer, Float> keyPair : 
			vlp.getDefaultDistribution().entrySet())
		{
			pairs.add(new ValuePair(
					vlp.getRangeLabel(keyPair.getKey()), keyPair.getValue())
					);
		}
		distributions.add(new DistributionList(pairs));
	}
	
	/**
	 * Reattach this object with a {@link NodeProperty}, making its values
	 * consistent with those in the new {@link PropertiesEditor}
	 * @param model The {@link PropertiesEditor} to read data from
	 * @param np The {@link NodeProperty} to attach to and read from
	 * @throws EditorException Thrown if any of the data provided in this
	 * object is not consistent with the information in the node property
	 * or the model.
	 */
	public void 
	addToProperty(PropertiesEditor model, NodeProperty np) throws EditorException{
		if(!np.getName().equals(propName)){
			throw new IllegalArgumentException(
					"This distribution is for property '"+propName
					+"', tried to bind it to property '"+np.getName()+"'");
		}
		
		ValuesListProperty<?> vlp;
		
		if(np instanceof ValuesListProperty){
			vlp = (ValuesListProperty<?>) np;
		}
		else {
			throw new IllegalArgumentException(
					"The given property must be able to use distributions!");
		}
		vlp.setDistributionType(NodeProperty.DistType.UNIVARIAT);
		
		Map<Integer, Integer> conds = new HashMap<>();
		Map<Integer, Float> rangeProbs = new HashMap<>();
		
		for(DistributionList dist : distributions){
			conds.clear();
			rangeProbs.clear();
			
			for(ValuePair pair : dist.values){
				Integer rid = null;
				String l = pair.Label.toLowerCase();
				for(int rid2 : vlp.getUnSortedRangeIDs()){
					if(vlp.getRangeLabel(rid2).toLowerCase().equals(l)){
						rid = rid2;
					}
				}
				if(rid == null){
					throw new EditorException("No range with label '"
							+pair.Label
							+"' in node property '"+vlp.getName()+"'");
				}
				rangeProbs.put(rid, pair.Value);
			}
			if(dist instanceof ConditionalDistList){
				ConditionalDistList cDist = (ConditionalDistList) dist;
				for(Condition c : cDist.conditions){
					Integer pid = model.search_nodePropWithName(c.Name);
					if(!vlp.dependsOn(pid)){
						vlp.addDependency(pid);
					}
					if(pid == null){
						throw new IllegalArgumentException(
								"There is no property with the given name: "
								+c.Name);
					}
					Integer rid = model.search_rangeWithLabel(pid, c.Value);
					if(rid == null){
						throw new IllegalArgumentException(
								"There is no range named '"
								+c.Value+"'in the property '"
								+c.Name+"'");
					}
					conds.put(pid,rid);
				}
				vlp.addConditionalDistribution(
						new ConditionalDistribution(conds, rangeProbs));
			}
			else {
				if(distributions.indexOf(dist) != distributions.size()-1){
					throw new IllegalArgumentException(
							"Found a default distribution before the end of "
							+"the distribution list in Univariat Distribution "
							+ name);
				}
				vlp.setDefaultDistribution(new Distribution(rangeProbs));
			}
		}
	}
	public String 
	getName() {
		return name;
	}
	
	public String 
	getPropName() {
		return propName;
	}
	
	public List<DistributionList> 
	getDistributions() {
		return distributions;
	}

	@Override
	public String 
	getObjectID() {
		return name;
	}
	
}
