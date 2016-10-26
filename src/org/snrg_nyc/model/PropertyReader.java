package org.snrg_nyc.model;

import java.util.List;
import java.util.Map;

/**
 * A simple interface for reading properties.
 * It should be a wrapper for a {@link PropertiesEditor}
 * @author Devin Hastings
 *
 */
public interface PropertyReader {
	public String name() throws EditorException;
	public String description() throws EditorException;
	public String type() throws EditorException;
	public int dependencyLevel() throws EditorException;
	public List<Integer> dependencies() throws EditorException;
	
	public boolean initBool()throws EditorException;
	public float initFraction()throws EditorException;
	
	public List<Integer> rangeIDs() throws EditorException;
	public String rangeLabel(int rid) throws EditorException;
	public int rangeMin(int rid) throws EditorException;
	public int rangeMax(int rid) throws EditorException;
	
	public int pathogenID() throws EditorException;
	public String pathogenType() throws EditorException;
	
	public boolean isRanged() throws EditorException;
	public boolean uniformDistribution() throws EditorException;
	
	public List<Integer> distributionIDs() throws EditorException;
	public Map<Integer,Integer> distributionConditions(int cid)throws EditorException;
	public Map<Integer, Float> distributionMap( int cid) throws EditorException;
	public Map<Integer, Float> defaultDistribution() throws EditorException;
	
}
