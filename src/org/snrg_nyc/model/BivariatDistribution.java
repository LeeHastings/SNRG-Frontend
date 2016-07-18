package org.snrg_nyc.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

class BivariatDistribution {
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
	
	public BivariatDistribution(Map<Integer, Map<Integer, Float>> map, EnumeratorProperty property){
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
	private void assert_validMap(Map<Integer, ?> map, EnumeratorProperty ep){
		List<Integer> pids = ep.getUnSortedRangeIDs();
		if(map.size() != pids.size()){
			throw new IllegalArgumentException("The given map was of the wrong size.  "
					+"expected "+pids.size()+", found "+map.size());
		}
		for(int i : pids){
			if(!map.containsKey(i)){
				throw new IllegalArgumentException("Missing label in distribution: "
						+ep.getRangeLabel(i));
			}
		}
	}
}
