package org.snrg_nyc.model;

import org.snrg_nyc.model.properties.NodeProperty;

public interface Editor_Internal {
	public NodeProperty internal_getNodeProp(int pid) throws EditorException;
}
