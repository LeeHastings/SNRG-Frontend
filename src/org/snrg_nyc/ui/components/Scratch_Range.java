package org.snrg_nyc.ui.components;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.PropertiesEditor;

public class Scratch_Range {
	private PropertiesEditor ui;
	final int rid;
	
	private Integer min;
	private Integer max;
	private String label;
	private int hash;
	
	public Scratch_Range(PropertiesEditor ui, int rid) throws EditorException{
		this.rid = rid;
		this.ui = ui;
		if(!ui.scratch_isRangedProperty()){
			throw new IllegalStateException("Cannot edit ranges of a non-ranged item.");
		}
		if(!ui.scratch_getRangeIDs().contains(rid)){
			throw new IllegalArgumentException("Invalid Range ID: "+rid);
		}
		if(ui.scratch_rangeIsSet(rid)){
			if(ui.scratch_getType().equals("IntegerRangeProperty")){
				min = ui.scratch_getRangeMin(rid);
				max = ui.scratch_getRangeMax(rid);
			}
			label = ui.scratch_getRangeLabel(rid);
		}
		hash = + ui.hashCode() << 4;
	}
	
	@Override
	public int hashCode(){
		return rid + hash;
	}
	
	public void delete() throws EditorException{
		ui.scratch_removeRange(rid);
	}
	
	public Integer getMin() throws EditorException{
		return min;
	}
	public Integer getMax() throws EditorException{
		return max;
	}
	public String getLabel() throws EditorException{
		return label;
	}
	
	public void setMin(int min) throws EditorException{
		ui.scratch_setRangeMin(rid, min);
		this.min = ui.scratch_getRangeMin(rid);
	}
	public void setMax(int max) throws EditorException{
		ui.scratch_setRangeMax(rid, max);
		this.max = ui.scratch_getRangeMax(rid);
	}
	public void setLabel(String label) throws EditorException{
		//Do not bother with identical labels
		if(label == null){
			throw new IllegalArgumentException("Cannot set the label to null!");
		}
		if(this.label != null && this.label.equals(label)){
			return;
		}
		ui.scratch_setRangeLabel(rid, label);
		this.label = ui.scratch_getRangeLabel(rid);
	}

	public boolean isReady() throws EditorException {
		return ui.scratch_rangeIsSet(rid);
	}
}
