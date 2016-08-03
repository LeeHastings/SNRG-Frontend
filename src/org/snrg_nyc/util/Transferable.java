package org.snrg_nyc.util;

import java.io.Serializable;

/**
 * An interface for storing and retrieving data from experiments
 * @author Devin Hastings
 */
public interface Transferable extends Serializable {
	public String getObjectID();
}
