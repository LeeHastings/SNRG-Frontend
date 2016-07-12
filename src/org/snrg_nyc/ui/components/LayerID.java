package org.snrg_nyc.ui.components;

public class LayerID {
	private int id;
	private boolean used;
	
	public LayerID(int id){
		this.id = id;
		used = true;
	}
	public LayerID(){
		used = false;
	}
	public Integer get(){
		if(used){
			return id;
		}
		else {
			return null;
		}
	}
	public boolean used(){
		return used;
	}
}
