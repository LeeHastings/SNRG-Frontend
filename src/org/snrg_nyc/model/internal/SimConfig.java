package org.snrg_nyc.model.internal;

import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.persistence.Transferable;
import org.snrg_nyc.util.Either;

/**
 * An object of settings, basically a map pointing to strings or another 
 * map of strings.
 * @author devin
 *
 */
public class SimConfig extends Transferable {
	private static final long serialVersionUID = 1L;
	private Map<String, Either<String, Map<String, String>>> data = 
			new HashMap<>();
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
		return data.get(fsm_id).left();
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
	
	public Map<String, Either<String, Map<String, String>>> 
	data(){
		return data;
	}
	
	@Override
	public String 
	getObjectID() {
		return "ignored / see FSM_ID";
	}
}
