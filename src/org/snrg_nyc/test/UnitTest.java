package org.snrg_nyc.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.EditorTester;
import org.snrg_nyc.model.NodeEditor;
import org.snrg_nyc.model.PropertiesEditor;

//TODO update this test for pathogens, edges, and layers

public class UnitTest {
	
	public static void main(String[] args){
		String mode = "none";
		if(args.length > 0){
			mode = args[0];
		}
		switch(mode.toLowerCase()){
		case "none":
		case "normal":
			test_validInput();
			break;
		case "random":
			int count = 100;
			if(args.length > 1){
				count = Integer.parseInt(args[1]);
			}
			test_invalidInput(count);
			break;
		default:
			System.err.println("Unknown command: "+mode);
			break;
		}
		return;
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
			bl.scratch_newInLayer(drugLID, "averagedegree", 
					"IntegerRangeProperty", 
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
			try{
				bl.utest_loadWithMessages("hcv_project");
			}
			catch(EditorException e){
				e.printStackTrace();
				bl = new NodeEditor();
				bl.utest_loadWithMessages("hcv_project");
			}
			
		} catch (EditorException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Run a number of commands on a {@link PropertiesEditor}, 
	 * selected at random from a subset of commands
	 * @param runs Number of commands to run
	 */
	static void 
	test_invalidInput( int runs){
		EditorTester bl = new NodeEditor();
		//bl.utest_setPrintMode(true);

		List<String> types = bl.getPropertyTypes();
		System.out.printf("Types: %s\n\n",types.toString());

		List<Action<EditorException>> functions = new ArrayList<>();
		
		Random rand = new Random();
		functions.add((bl2)->{
			String type;
			if(Math.random() < 0.1){
				type = "BadType";
			}
			else {
				type = types.get(rand.nextInt(types.size()) );
			}
			String name = "property" + randString();
			
			if(Math.random() < 0.75){
				System.out.printf("Creating property: %s - %s\n", name, type);
				bl2.scratch_new(name, type, "Test Property");
			}
			else {
				List<Integer> layers = bl2.layer_getLayerIDs();
				//Get a sometimes invalid layer ID
				int lid = 0;
				if(layers.size() > 0){
					lid = rand.nextInt(layers.size()); 
				}
				System.out.printf("Creating layer(%d/%d) property: %s - %s\n",
						lid, layers.size()-1, name, type);
				
				bl2.scratch_newInLayer(lid, name, type, "Layer Property");
			}
		});
		
		functions.addAll(scratchMethods());
		
		while(Math.random() > 0.2){
			String name = "layer"+randString();
			System.out.println("Adding layer: "+ name);
			try {
				bl.layer_new(name);
			} catch (EditorException e) {
				e.printStackTrace();
			}
		}
		
		int randMax = functions.size()-1;
		for(int i = 0; i < runs; i++){
			try {
				int func = (int) Math.round(Math.random()*randMax) ;
				List<Integer> layers = bl.layer_getLayerIDs();
				List<Integer> pathogens = bl.pathogen_getPathogenIDs();
				
				if(Math.random() > 0.5){
					System.out.printf("Executing function %d/%d - ", 
							func, randMax);
					functions.get(func).run(bl);
				}
				else if(Math.random() > 0.25 && layers.size() > 0) {
					int lid = layers.get((int) (Math.random()*layers.size()) );

					System.out.printf("Executing function %d/%d on edge "
							+ "%d/%d - ", 
							func, randMax, lid, layers.size());
					functions.get(func).run(bl.layer_getEdgeEditor(lid));
				}
				else if (pathogens.size() > 0){
					int pid = pathogens.get((int) (Math.random()*pathogens.size()) );

					System.out.printf("Executing function %d/%d on pathogen "
							+ "%d/%d - ", 
							func, randMax, pid, pathogens.size());
					functions.get(func).run(bl.pathogen_getEditor(pid));
				}
			} catch (EditorException e) {
				System.out.flush();
				//e.printStackTrace();
				System.err.println("Editor Error: "+e.getMessage());
				System.err.flush();
			}
		}
		
		try {
			bl.save("test_garbage");
		} catch (EditorException e) {
			e.printStackTrace();
		}
	}
	static String randString(){
		String num = Double.toString(Math.random()).substring(2);
		if(num.length() > 5){
			return num.substring(0, 5);
		}
		else {
			return num;
		}
	}
	
	/**
	 * An interface similar to {@link Runnable}, but the function throws an
	 * exception.
	 * @author Devin Hastings
	 *
	 * @param <T> The type of object thrown by the function;
	 */
	static interface Action<T extends Throwable> {
		public void run(PropertiesEditor bl) throws T;
	}
	static List<Action<EditorException>> scratchMethods(){

		Random rand = new Random();
		List<Action<EditorException>> functions = new ArrayList<>();
		
		functions.add((bl2)->{
			int dep = rand.nextInt(5);
			System.out.println("Setting dependency level: "+dep);
			bl2.scratch_setDependencyLevel(dep);
		});
		
		functions.add((bl2)->{
			System.out.println("Adding dependencies");
			for(int i : bl2.scratch_getPotentialDependencies()){
				if(Math.random() > 0.2){
					bl2.scratch_addDependency(i);
				}
			}
			if(Math.random() < 0.2){
				bl2.scratch_addDependency((int) (Math.random()*4));
			}
		});
		
		functions.add((bl2)->{
			try{
				System.out.printf("Commiting property %s %s\n", 
						bl2.scratch_getType(), bl2.scratch_getName());
			}
			catch(EditorException e){
				System.out.println("Commiting invalid scratch property");
			}
			bl2.scratch_commit();
		});
		
		functions.add((bl2)->{
			String label ="range"+ randString();
			System.out.println("Adding range: "+label);
			bl2.scratch_addRange(label);
		});
		
		functions.add((bl2)->{
			System.out.println("Setting int range values");
			for(int rid : bl2.scratch_getRangeIDs()){
				if(Math.random() > 0.1){
					bl2.scratch_setRangeMin(rid, rand.nextInt(500)-80);
					bl2.scratch_setRangeMax(rid, rand.nextInt(1000)+200);
				}
			}
			if(Math.random() < 0.1){
				int rid = rand.nextInt(20);
				bl2.scratch_setRangeMin(rid, rand.nextInt(200));
				bl2.scratch_setRangeMax(rid, rand.nextInt(300));
			}
		});
		
		functions.add((bl2)->{
			float init = (float) Math.random()*10;
			System.out.println("Setting Fraction value: "+init);
			bl2.scratch_setFractionInitValue(init);
		});
		
		functions.add((bl2)->{
			boolean init = Math.random() >= 0.5;
			System.out.println("Setting Boolean value: "+init);
			bl2.scratch_setBooleanInitValue(init);
		});
		
		functions.add((bl2)->{
			System.out.println("Using uniform distribution");
			bl2.scratch_useUniformDistribution();
		});
		
		functions.add((bl2)->{
			System.out.println("Setting default distribution");
			Map<Integer, Float> map = new HashMap<>();
			try{
				for(int i : bl2.scratch_getRangeIDs()){
					if(Math.random() >= 0.1){
						map.put(i, (float) (Math.random()*8)-1);
					}
				}
				if(Math.random() > 0.9){
					map.put(rand.nextInt(40), (float)(Math.random()*10));
				}
			} 
			catch(EditorException e){
				System.out.println("\t(Not a valid distribution)");
			}
			bl2.scratch_setDefaultDistribution(map);
		});
		
		functions.add((bl2)->{
			System.out.println("Adding a conditional distribution");
			Map<Integer, Integer> condition = new HashMap<>();
			Map<Integer, Float> distMap = new HashMap<>();
			for(int pid : bl2.scratch_getDependencies()){
				if(Math.random() > 0.1){
					List<Integer> rids = bl2.nodeProp_getRangeItemIDs(pid);
					int rid = rids.get(rand.nextInt(rids.size()) );
					condition.put(pid, rid);
				}
			}
			if(Math.random() < 0.1){
				condition.put(rand.nextInt(5), rand.nextInt(5));
			}
			for(int rid : bl2.scratch_getRangeIDs()){
				if(Math.random() > 0.1){
					distMap.put(rid, (float) (Math.random()*10)-1);
				}
			}
			if(Math.random() < 0.1){
				distMap.put(rand.nextInt(8), (rand.nextFloat()*8)-1);
			}
			bl2.scratch_addConditionalDistribution(condition, distMap);
		});
		
		functions.add((bl2)->{
			String pathogen = "pathogen" + randString();
			System.out.println("Setting pathogen type: "+pathogen);
			bl2.scratch_setPathogenType(pathogen);
		});
		
		return functions;
	}
}
