package org.snrg_nyc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A map in which you can view and edit values, but you cannot add or 
 * remove key-value pairs.
 * @author Devin Hastings
 *
 * @param <T> The key type
 * @param <S> The value type
 */
public class ConstKeyMap<T, S>{
	protected final Map<T, S> map;
	
	public ConstKeyMap(Map<T, S> map){
		this.map = map;
	}
	
	public S 
	get(T key){
		return map.get(key);
	}
	
	public void
	set(T key, S value){
		if(map.containsKey(key)){
			map.put(key, value);
		}
		else {
			throw new IllegalArgumentException("Key not found: "+key);
		}
	}
	/**
	 * Copy the map keyset (this is not backed by the map)
	 * @return A copy of the map's keyset
	 */
	public Set<T>
	keySet(){
		return new HashSet<T>(map.keySet());
	}
	
	public Collection<S>
	values(){
		return map.values();
	}
	
	public boolean 
	containsKey(T key) {
		return map.containsKey(key);
	}

	public boolean 
	containsValue(S value) {
		return map.containsValue(value);
	}
	
	/**
	 * Return a copy of the map's entry set (this is not backed by the map)
	 * @return A copy of the map's entry set
	 */
	public Set<Map.Entry<T,S>> 
	entrySet() {
		return new HashSet<>(map.entrySet());
	}

	public boolean 
	isEmpty() {
		return map.isEmpty();
	}

	public int 
	size() {
		return map.size();
	}
}
