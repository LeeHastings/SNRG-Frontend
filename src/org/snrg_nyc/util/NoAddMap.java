package org.snrg_nyc.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class for editing maps that forbids adding keys
 * @author Devin Hastings
 *
 * @param <T> The key of the map
 * @param <S> The value of the map
 */
public class NoAddMap<T,S> extends ConstKeyMap<T,S>{

	public NoAddMap(Map<T,S> map) {
		super(map);
	}
	public void remove(T key){
		map.remove(key);
	}
	public void remove(T key, S value){
		map.remove(key, value);
	}
	

	@Override
	/**
	 * Return the set of keys from the map.  This set is backed by the map and 
	 * changes in one will be reflected in the other. The set does not support
	 * <code>add</code> or <code>addAll</code> methods.
	 * @return The keyset of the map
	 */
	public Set<T>
	keySet(){
		return map.keySet();
	}
	
	@Override
	public Set<Map.Entry<T,S>> 
	entrySet() {
		return map.entrySet();
	}
}
