package org.snrg_nyc.model.internal;

import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.persistence.Transferable;

public class SimConfig extends Transferable {
	private static final long serialVersionUID = 1L;
	private Map<String, ConfigData<?>> data = new HashMap<>();
	private final String fsm_id = "FSM_ID";
	
	public static class ConfigData<T>{
		private T data;
		public ConfigData(T data){
			this.data = data;
		}
		public T getData(){
			return data;
		}
	}
	
	public SimConfig(String fsm_id){
		data.put(this.fsm_id, new ConfigData<String>(fsm_id));
	}
	public SimConfig(String fsm_id, Map<String, ConfigData<?>> map){
		this(fsm_id);
		data = map;
	}
	
	public String 
	getFsm_ID(){
		return (String) data.get(fsm_id).getData();
	}
	public void 
	setFsm_ID(String fsim_id) throws EditorException{
		if(fsim_id == null){
			throw new EditorException("The FSM_ID cannot be null");
		}
		else {
			((ConfigData<String>) data.get(this.fsm_id)).data = fsim_id;
		}
	}
	
	public Map<String, ConfigData<?>> 
	data(){
		return data;
	}
	
	@Override
	public String 
	getObjectID() {
		return "ignored / see FSM_ID";
	}
}
