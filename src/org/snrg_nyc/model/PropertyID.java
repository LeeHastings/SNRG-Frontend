package org.snrg_nyc.model;

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
		if(!lid.isPresent()){
			throw new IllegalStateException("There is no layerID on this propertyID");
		}
		return lid.get();
	}
	public boolean usesLayer(){
		return lid.isPresent();
	}
}
