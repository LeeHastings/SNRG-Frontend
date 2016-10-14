package org.snrg_nyc.model.internal;

import java.util.Map;
import java.util.Set;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.persistence.Transferable;
import org.snrg_nyc.util.ConstKeyMap;
import org.snrg_nyc.util.Either;

/**
 * An object of settings, basically a map pointing to strings or another 
 * map of strings.
 * @author devin
 *
 */
public class SimConfig extends Transferable {
	private static final long serialVersionUID = 1L;
	
	private Map<String, Either<String, Map<String, String>>> data;
	
	public ConstKeyMap<String, Either<String, ConstKeyMap<String, String>>>
		safeData;
	
	private final String fsm_id = "FSM_ID";
	
	public SimConfig(String fsm_id){
		data.put(this.fsm_id, Either.left(fsm_id));
	}
	public 
	SimConfig(String fsm_id, 
			Map<String, Either<String, Map<String, String>>> map) 
			throws EditorException 
	{
		this(fsm_id);
		data = map;
	}
	
	public String 
	getFsm_ID(){
		return data.get(fsm_id).left;
	}
	public void 
	setFsm_ID(String fsm_id) throws EditorException{
		if(fsm_id == null){
			throw new EditorException("The FSM_ID cannot be null");
		}
		else {
			data.put(this.fsm_id, Either.left(fsm_id));
		}
	}
	
	public String
	getString(String key) throws EditorException{
		if(!data.containsKey(key)){
			throw new EditorException("Unknown key in SimConfig '"
					+getFsm_ID()+"' - "+key);
		}
		if(!data.get(key).hasLeft()){
			throw new EditorException("Item '"+key+"' in SimConfig '"
					+getFsm_ID()+"' is not a string!");
		}
		return data.get(key).left;
	}
	
	public void
	setString(String key, String value) throws EditorException{
		if(!data.containsKey(key)){
			throw new EditorException("Unknown key in SimConfig '"
					+getFsm_ID()+"' - "+key);
		}
		if(!data.get(key).hasLeft()){
			throw new EditorException("Item '"+key+"' in SimConfig '"
					+getFsm_ID()+"' is not a string!");
		}
		data.put(key, Either.left(value));
	}
	
	public boolean
	containsKey(String key){
		return data.containsKey(key);
	}
	
	public boolean
	isMap(String key) throws EditorException {
		if(!data.containsKey(key)){
			throw new EditorException("Unknown key in SimConfig '"
					+getFsm_ID()+"' - "+key);
		}
		return data.get(key).hasRight();
	}
	
	public ConstKeyMap<String, String>
	getMap(String key) throws EditorException{
		if(!data.containsKey(key)){
			throw new EditorException("Unknown key in SimConfig '"
					+getFsm_ID()+"' - "+key);
		}
		if(!data.get(key).hasRight()){
			throw new EditorException("Item '"+key+"' in SimConfig '"
					+getFsm_ID()+"' is not a map!");
		}
		return new ConstKeyMap<>(data.get(key).right);
	}
	
	public Set<String>
	keySet(){
		return data.keySet();
	}
	
	@Override
	public String 
	getObjectID() {
		return "ignored / see FSM_ID";
	}
}
