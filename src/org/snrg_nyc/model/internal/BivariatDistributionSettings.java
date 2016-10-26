package org.snrg_nyc.model.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.properties.ValuesListProperty;
import org.snrg_nyc.persistence.Transferable;

import com.google.gson.annotations.SerializedName;

public class BivariatDistributionSettings implements Transferable {
	private static final long serialVersionUID = 1L;

	@SerializedName("BivariatDistributionID")
	private String distID;
	
	@SerializedName("BindToPropertyName")
	private String propName;
	
	@SerializedName("BiValueDictionary")
	private Map<String, Map<String, Float>> biValueMap;
	
	@SerializedName("MissingFieldAssignmentHandler")
	private int mfah = 0;
	
	@SerializedName("MissingValueAssignmentHandler")
	private int mvah = 0;
	
	public 
	BivariatDistributionSettings(BivariateDistribution bd) 
			throws EditorException
	{
		Map<Integer, Map<Integer, Float>> map = bd.distribution;
		ValuesListProperty<?> property = bd.property;
		assert_validMap(map, property);
		for(int i : map.keySet()){
			try{
				assert_validMap(map.get(i), property);
			}
			catch(IllegalArgumentException e){
				throw new IllegalArgumentException("Invalid map for property '"
						+property.getRangeLabel(i)+"': "+e.getMessage());
			}
		}
		biValueMap = new HashMap<>();
		for(int i : map.keySet()){
			Map<String, Float> tempMap = new HashMap<>();
			for(int j : map.get(i).keySet()){
				tempMap.put(property.getRangeLabel(j), map.get(i).get(j));
			}
			biValueMap.put(property.getRangeLabel(i), tempMap);
		}
	}
	
	private void 
	assert_validMap(Map<Integer, ?> map, ValuesListProperty<?> property) 
			throws EditorException
	{
		List<Integer> pids = property.getUnSortedRangeIDs();
		if(map.size() != pids.size()){
			throw new IllegalArgumentException("The given map was of the wrong size.  "
					+"expected "+pids.size()+", found "+map.size());
		}
		for(int i : pids){
			if(!map.containsKey(i)){
				throw new IllegalArgumentException("Missing label in distribution: "
						+property.getRangeLabel(i));
			}
		}
	}
	
	public BivariateDistribution 
	toInternalMap(ValuesListProperty<?> p) 
			throws EditorException
	{
		if(!p.getName().equals(propName)){
			throw new EditorException("Tried to bind property '"+p.getName()+
					"'" + "to distribution '"+distID+
					"', which is bound to '"+propName+"'");
		}
		
		BivariateDistribution bd = new BivariateDistribution(p);
		Map<String, Integer> labelMap = new HashMap<>();
		
		for(String s : biValueMap.keySet()){
			for(int rid : p.getUnSortedRangeIDs()){
				if(p.getRangeLabel(rid).equals(s)){
					labelMap.put(s, rid);
					continue;
				}
				throw new EditorException("Unknown value in '"+distID+"': "+s);
			}
		}
		for(String r1 : biValueMap.keySet()){
			for( String r2 : biValueMap.get(r1).keySet() ){
				bd.setValue(
						labelMap.get(r1),
						labelMap.get(r2),
						biValueMap.get(r1).get(r2));
			}
		}
		//Just in case the way IDs are generated changed between loadings
		bd.setID(distID);
		return bd;
	}
	public String
	propertyName(){
		return propName;
	}
	@Override
	public String 
	getObjectID() {
		return distID;
	}
}
