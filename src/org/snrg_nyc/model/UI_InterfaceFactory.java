package org.snrg_nyc.model;

/**
 * A simple abstract factory for building a {@link UI_Interface}.
 * Eventually, support for build options may be added.
 * @author devin
 *
 */
public class UI_InterfaceFactory{
	public UI_InterfaceFactory(){}
	
	public UI_Interface build(){
		return new BusinessLogic();
	}
}
