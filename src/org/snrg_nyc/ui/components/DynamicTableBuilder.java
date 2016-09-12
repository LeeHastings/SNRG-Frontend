package org.snrg_nyc.ui.components;

import java.util.Map;

import javafx.util.Callback;
import javafx.util.StringConverter;

public class DynamicTableBuilder<T,S,R> {
	private Callback<T, String> colFactory = T::toString;
	
	public DynamicTable<T,S,R> 
	build(Map<T, Map<S,R>> map, StringConverter<R> converter){
		return new DynamicTable<T,S,R>(map,converter,colFactory);
	}
	public DynamicTableBuilder<T,S,R> setColumnFactory(Callback<T, String> col){
		colFactory = col;
		return this;
	}
}
