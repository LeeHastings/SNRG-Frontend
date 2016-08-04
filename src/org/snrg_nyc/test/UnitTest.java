package org.snrg_nyc.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snrg_nyc.model.EditorTester;
import org.snrg_nyc.model.NodeEditor;

//TODO update this test for pathogens, edges, and layers

public class UnitTest {
	
	public static void main(String[] args){
		test_validInput();
	}
	
	static void 
	test_validInput(){
		
		EditorTester bl = new NodeEditor();
		System.out.println("Node Property types:");
		
		bl.utest_setPrintMode(true);
		
		List<String> types = bl.getPropertyTypes();
		for(String type : types){
			System.out.println("\t" + type);
		}
		try {
			
			bl.scratch_new("gender", "EnumeratorProperty", 
					"Gender of individual (static)");
			bl.scratch_setDependencyLevel(0);
			int rid1g = bl.scratch_addRange("male");
			int rid2g = bl.scratch_addRange("female");
			Map<Integer, Float> probmap = new HashMap<>();
			
			probmap.put(rid1g, 6f);
			probmap.put(rid2g, 4f);
			bl.scratch_setDefaultDistribution(probmap);
			
			int genderID = bl.scratch_commit();
			
			
			bl.scratch_new("age", "IntegerRangeProperty", 
					"Age (in 'years') at start of simulation");
			bl.scratch_setDependencyLevel(0);
			
			int rid1a = bl.scratch_addRange("2x");
			bl.scratch_setRangeMin(rid1a, 16);
			bl.scratch_setRangeMax(rid1a, 25);
			
			int rid2a = bl.scratch_addRange("3x");
			bl.scratch_setRangeMin(rid2a, 26);
			bl.scratch_setRangeMax(rid2a, 35);
			
			int rid3 = bl.scratch_addRange("4x");
			bl.scratch_setRangeMin(rid3, 36);
			bl.scratch_setRangeMax(rid3, 45);
			
			int rid4 = bl.scratch_addRange("5x");
			bl.scratch_setRangeMin(rid4, 46);
			bl.scratch_setRangeMax(rid4, 55);
			
			int rid5 = bl.scratch_addRange("6x");
			bl.scratch_setRangeMin(rid5, 56);
			bl.scratch_setRangeMax(rid5, 65);
			
			probmap.clear();
			probmap.put(rid1a, 15f);
			probmap.put(rid2a,12f);
			probmap.put(rid3, 4f);
			probmap.put(rid4, 3f);
			probmap.put(rid5, 1f);
			bl.scratch_setDefaultDistribution(probmap);
			
			bl.scratch_commit();
			
			bl.scratch_new("longevity", "IntegerRangeProperty",
					"Number of days individual is in risk population");
			bl.scratch_setDependencyLevel(1);
			
			int rid1 = bl.scratch_addRange("male_transient");
			bl.scratch_setRangeMin(rid1, 1);
			bl.scratch_setRangeMax(rid1, 14);
			
			int rid2 = bl.scratch_addRange("male_steady");
			bl.scratch_setRangeMin(rid2, 365);
			bl.scratch_setRangeMax(rid2, 3650);
			
			rid3 = bl.scratch_addRange("female_transient");
			bl.scratch_setRangeMin(rid3, 1);
			bl.scratch_setRangeMax(rid3, 7);
			
			rid4 = bl.scratch_addRange("female_steady");
			bl.scratch_setRangeMin(rid4, 730);
			bl.scratch_setRangeMax(rid4, 7300);
			
			bl.scratch_addDependency(genderID);
			
			probmap.clear();
			Map<Integer, Integer> conditions = new HashMap<>();
			
			conditions.put(genderID, rid1g);
			probmap.put(rid1, 10f);
			probmap.put(rid2, 90f);
			probmap.put(rid3, 0f);
			probmap.put(rid4, 0f);
			bl.scratch_addConditionalDistribution(conditions, probmap);
			
			conditions.clear();
			probmap.clear();
			conditions.put(genderID, rid2g);
			probmap.put(rid4, 5f);
			probmap.put(rid3, 95f);
			probmap.put(rid1, 0f);
			probmap.put(rid2, 0f);
			bl.scratch_addConditionalDistribution(conditions, probmap);
			
			probmap.clear();
			probmap.put(rid1, 25f);
			probmap.put(rid2, 25f);
			probmap.put(rid3, 25f);
			probmap.put(rid4, 25f);
			bl.scratch_setDefaultDistribution(probmap);
			
			bl.scratch_commit();
			
			int drugLID = bl.layer_new("drug_co_use");
			bl.scratch_newInLayer(drugLID, "averagedegree", "IntegerRangeProperty", 
					"The ideal degree in this network layer");
			
			bl.scratch_setDependencyLevel(0);
			
			rid1 = bl.scratch_addRange("low");
			bl.scratch_setRangeMin(rid1, 1);
			bl.scratch_setRangeMax(rid1, 3);
			
			rid2 = bl.scratch_addRange("medium");
			bl.scratch_setRangeMin(rid2, 4);
			bl.scratch_setRangeMax(rid2, 7);
			
			rid3 = bl.scratch_addRange("high");
			bl.scratch_setRangeMin(rid3, 8);
			bl.scratch_setRangeMax(rid3, 16);
			
			probmap.clear();
			probmap.put(rid1, 40f);
			probmap.put(rid2, 5f);
			probmap.put(rid3, 2f);
			
			bl.scratch_setDefaultDistribution(probmap);
			bl.scratch_commit();
			
			bl.save("hcv_project");
			bl.load("hcv_project");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	static void 
	test_invalidInput(){
		EditorTester bl = new NodeEditor();
	}
}
