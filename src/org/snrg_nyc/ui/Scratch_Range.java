package org.snrg_nyc.ui;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.PropertiesEditor;

class Scratch_Range {
	private PropertiesEditor ui;
	final int rid;
	
	private String min;
	private String max;
	private String label;
	private int hash;
	
	public Scratch_Range(PropertiesEditor ui, int rid) throws EditorException{
		this.rid = rid;
		this.ui = ui;
		min = "";
		max = "";
		label = "";
		if(!ui.scratch_isRangedProperty()){
			throw new IllegalStateException("Cannot edit ranges of a non-ranged item.");
		}
		if(!ui.scratch_getRangeIDs().contains(rid)){
			throw new IllegalArgumentException("Invalid Range ID: "+rid);
		}
		if(ui.scratch_rangeIsSet(rid)){
			if(ui.scratch_getType().equals("IntegerRangeProperty")){
				min = Integer.toString(ui.scratch_getRangeMin(rid));
				max = Integer.toString(ui.scratch_getRangeMax(rid));
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
	
	public String getMin() throws EditorException{
		return min;
	}
	public String getMax() throws EditorException{
		return max;
	}
	public String getLabel() throws EditorException{
		return (label != null && label.length() > 0) ? label : "<empty>";
	}
	
	public void setMin(int min) throws EditorException{
		ui.scratch_setRangeMin(rid, min);
		this.min = Integer.toString(ui.scratch_getRangeMin(rid));
	}
	public void setMax(int max) throws EditorException{
		ui.scratch_setRangeMax(rid, max);
		this.max = Integer.toString(ui.scratch_getRangeMax(rid));
	}
	public void setLabel(String label) throws EditorException{
		//Do not bother with empty or identical labels
		if(this.label.equals(label) || label.equals("<empty>")){
			return;
		}
		ui.scratch_setRangeLabel(rid, label);
		this.label = ui.scratch_getRangeLabel(rid);
	}

	public boolean isReady() throws EditorException {
		return ui.scratch_rangeIsSet(rid);
	}
}
