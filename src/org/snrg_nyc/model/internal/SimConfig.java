package org.snrg_nyc.model.internal;

import java.util.HashMap;
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
	
	public static class Setting {
		private Either<String, Map<String, Setting>> data;
		private ConstKeyMap<String, Setting> safeMap = null;
		
		public Setting(Either<String, Map<String, Setting>> data){
			this.data = data;
			if(data.hasRight()){
				safeMap = new ConstKeyMap<String, Setting>(data.right);
			}
		}
		public boolean
		isMap(){
			return data.hasRight();
		}
		public ConstKeyMap<String, Setting>
		getMap() throws EditorException{
			if(!isMap()){
				throw new EditorException(
						"Tried to get a map from a string config object!");
			}
			else {
				return safeMap;
			}
		}
		public String
		getString() throws EditorException{
			if(isMap()){
				throw new EditorException(
						"Tried to get a string from a map setting!");
			}
			else {
				return data.left;
			}
		}
		public void
		setString(String value) throws EditorException{
			if(isMap()){
				throw new EditorException(
						"Tried to get a string from a map setting!");
			}
			else {
				data = Either.left(value);
			}
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, Setting> data;
	private ConstKeyMap<String, Setting> safeData;
	
	private final String fsm_id = "FSM_ID";

	public SimConfig(String fsm_id){
		data = new HashMap<>();
		data.put(this.fsm_id, new Setting(Either.left(fsm_id)));
	}
	public 
	SimConfig(String fsm_id, 
			Map<String, Setting> map) 
			throws EditorException 
	{
		this(fsm_id);
		data = map;
		safeData = new ConstKeyMap<>(data);
	}
	
	public String 
	getFsm_ID() throws EditorException{
		return data.get(fsm_id).getString();
	}
	public void 
	setFsm_ID(String fsm_id) throws EditorException{
		if(fsm_id == null){
			throw new EditorException("The FSM_ID cannot be null");
		}
		else {
			data.get(this.fsm_id).setString(fsm_id);
		}
	}
	
	public String
	getString(String key) throws EditorException{
		return data.get(key).getString();
	}
	
	public void
	setString(String key, String value) throws EditorException{
		data.get(key).setString(value);
	}
	
	public boolean
	containsKey(String key){
		return data.containsKey(key);
	}
	
	public boolean
	isMap(String key) throws EditorException {
		return data.get(key).isMap();
	}
	
	public ConstKeyMap<String, Setting>
	getMap(String key) throws EditorException{
		return data.get(key).getMap();
	}
	
	public ConstKeyMap<String, Setting>
	getMap(){
		return safeData;
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
	
	protected Map<String, Setting> data(){
		return data;
	}
}
