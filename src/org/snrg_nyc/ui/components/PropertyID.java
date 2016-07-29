package org.snrg_nyc.ui.components;

import java.util.Optional;

public class PropertyID {
	private int pid;
	private Optional<Integer> lid;
	
	public PropertyID(int pid){
		this.pid = pid;
		lid = Optional.empty();
	}
	public PropertyID(int lid, int pid){
		this.pid = pid;
		this.lid = Optional.of(lid);
	}
	public int pid(){
		return pid;
	}
	public Integer lid(){
		return lid.get();
	}
	public boolean usesLayer(){
		return lid.isPresent();
	}
}
