package org.snrg_nyc.model.editor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.snrg_nyc.model.AttachmentProperty;
import org.snrg_nyc.model.BooleanProperty;
import org.snrg_nyc.model.EnumeratorProperty;
import org.snrg_nyc.model.FractionProperty;
import org.snrg_nyc.model.IntegerRangeProperty;
import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.model.PropertiesEditor_Impl;


/**
 * An editor class for creating node properties.
 * The only public methods are those listed in the {@link PropertiesEditor}.
 * @author Devin Hastings
 *
 */
public class NodeEditor extends PropertiesEditor_Impl {
	
	/*         *\
	 * Members *
	\*         */

	/** Node Property classes that can be created in the editor */
	private static final Class<?>[] nodePropertyTypes = {
			EnumeratorProperty.class, 
			IntegerRangeProperty.class, 
			BooleanProperty.class,
			FractionProperty.class,
			AttachmentProperty.class
		};
	
	private NodeSettings nodeSettings = new NodeSettings();
	
	/*         *\
	 * Methods *
	\*         */
	
	public NodeEditor(){
		super();
		nodeProperties = new ArrayList<>();
		nodeLayers = new ArrayList<>();
		
		nodeSettings.setLayerAttributesList(nodeLayers);
		nodeSettings.setPropertyDefinitionList(nodeProperties);
	}
	
	@Override
	public Map<String, Serializable> getSavedObjects() throws EditorException{
		Map<String, Serializable> map = super.getSavedObjects();
		map.put("nodesettings", nodeSettings);
		return map;
	}
	@Override
	public void load(String experimentName) throws EditorException {
		Map<String, Serializable> objects = deserializeExperiment(experimentName);
		System.out.println("Found map:\n"+objects.toString());
		if(!objects.containsKey("nodesettings")){
			throw new EditorException("Tried to open an experiment without node settings!");
		}
		nodeSettings = (NodeSettings) objects.get("nodesettings");
		objects.remove("nodesettings");
		
		nodeLayers = nodeSettings.getLayerAttributesList();
		nodeProperties = nodeSettings.getPropertyDefinitionList();
		
		loadDistributions(objects);
	}

	@Override
	public Class<?>[] getPropertyClasses() {
		return nodePropertyTypes;
	}

}
