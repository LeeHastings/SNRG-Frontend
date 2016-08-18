package org.snrg_nyc.model;

import java.util.Map;

import org.snrg_nyc.model.internal.NodeLayer;
import org.snrg_nyc.persistence.Transferable;

class EdgeEditor extends PropertiesEditor_Impl {
	private static String noLayersMsg = "There are no layers in this editor";
	private EdgeSettings settings;
	private NodeEditor parent;
	
	public 
	EdgeEditor(NodeEditor parent, NodeLayer layer){
		super();
		this.parent = parent;
		settings = new EdgeSettings(layer.getName());
		settings.setPropertyDefinitionList(properties);
	}
	public
	EdgeEditor(NodeEditor parent, EdgeSettings settings, 
			Map<String, Transferable> objects) throws EditorException
	{
		super();
		this.parent = parent;
		this.settings = settings;
		properties = settings.getPropertyDefinitionList();
		loadDistributions(objects);
	}
	
	@Override
	public boolean 
	test_nodePropNameIsUnique(String name) {
		return parent.test_nodePropNameIsUnique(name);
	}

	@Override
	protected Class<?>[] 
	getPropertyClasses() {
		//Use the same classes as the pathogen editor
		return PathogenEditor.pathogenPropertyTypes;
	}
	
	@Override 
	protected Map<String, Transferable> getSavedObjects() throws EditorException{
		Map<String, Transferable> base = super.getSavedObjects();
		base.put("edgesettings_"+settings.getLayerName(), settings);
		return base;
	}
	
	/**
	 * In an instance of {@link EdgeSettings}, layers are not allowed.
	 * @throws EditorException Thrown because there are no layers in an
	 * {@link EdgeSettings} object.
	 */
	@Override
	public int 
	layer_new(String n) throws EditorException{
		throw new EditorException(noLayersMsg);
	}
	@Override
	public boolean allowsLayers() {
		return false;
	}

}
