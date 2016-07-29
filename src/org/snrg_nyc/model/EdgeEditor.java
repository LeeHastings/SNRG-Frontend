package org.snrg_nyc.model;

import org.snrg_nyc.model.internal.EditorException;
import org.snrg_nyc.model.internal.NodeLayer;

public class EdgeEditor extends PropertiesEditor_Impl {
	private static String noLayersMsg = "There are no layers in this editor";
	private EdgeSettings settings;
	
	public EdgeEditor(NodeEditor parent, NodeLayer layer){
		super();
		settings.setLayerName(layer.getName());
		settings.setPropertyDefinitionList(properties);
	}
	@Override
	public boolean test_nodePropNameIsUnique(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Class<?>[] getPropertyClasses() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * In an instance of {@link EdgeSettings}, layers are not allowed.
	 * This will throw an exception. Most other layer-based methods will simply
	 * be ignored.
	 */
	@Override
	public int layer_new(String n) throws EditorException{
		throw new EditorException(noLayersMsg);
	}

	/**
	 * In an instance of {@link EdgeSettings}, layers are not allowed.
	 * This will throw an exception.
	 */
	@Override
	public void scratch_newInLayer(int lid, String n, String t, String d) throws EditorException{
		throw new EditorException(noLayersMsg);
	}

}
