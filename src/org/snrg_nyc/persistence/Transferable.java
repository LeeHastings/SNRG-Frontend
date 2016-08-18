package org.snrg_nyc.persistence;

import java.io.Serializable;

/**
 * An interface that describes all objects that can be serialized and loaded
 * by an {@link org.snrg_nyc.persistence.ExperimentSerializer}
 * @author Devin Hastings
 */
public interface Transferable extends Serializable {
	/**
	 * An ID that describes the object, as specified in the file format
	 * @return A descriptive ID for the object.
	 */
	public String getObjectID();
}
