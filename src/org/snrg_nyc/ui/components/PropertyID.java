package org.snrg_nyc.ui.components;

public class PropertyID {
	private int pid;
	private LayerID lid;
	
	public PropertyID(int pid){
		this.pid = pid;
		lid = new LayerID();
	}
	public PropertyID(int lid, int pid){
		this.pid = pid;
		this.lid = new LayerID(lid);
	}
	public int pid(){
		return pid;
	}
	public Integer lid(){
		return lid.get();
	}
	public boolean usesLayer(){
		return lid.used();
	}
}
