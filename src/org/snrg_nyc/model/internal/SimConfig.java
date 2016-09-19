package org.snrg_nyc.model.internal;

import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.persistence.Transferable;

public class SimConfig extends Transferable {
	private static final long serialVersionUID = 1L;
	private Map<String, ConfigData<?>> data = new HashMap<>();
	
	public static class ConfigData<T>{
		T item;
		public ConfigData(T item){
			this.item = item;
		}
		public static ConfigData<?> fromString(String name, String item){
			if(item.matches("\\d+\\.\\d+")){
				return new ConfigData<Float>(Float.parseFloat(item));
			}
			else {
				return new ConfigData<String>(item);
			}
		}
		@Override
		public String toString(){
			return item.toString();
		}
	}
	
	public ConfigData<?> get(String id){
		return data.get(id);
	}
	
	@Override
	public String getObjectID() {
		return "ignored / see FSM_ID";
	}

}
