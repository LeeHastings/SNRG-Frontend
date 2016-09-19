package org.snrg_nyc.model.internal;

import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.persistence.Transferable;

public class SimConfig extends Transferable {
	private static final long serialVersionUID = 1L;
	private Map<String, String> data = new HashMap<>();
	
	public SimConfig(){}
	public SimConfig(Map<String, String> map){
		data = map;
	}
	
	public Map<String, String> 
	data(){
		return data;
	}
	
	@Override
	public String 
	getObjectID() {
		return "ignored / see FSM_ID";
	}
}
