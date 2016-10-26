package org.snrg_nyc.persistence;

import com.google.gson.JsonParseException;

/**
 * A very specific exception for when an object in a 
 * {@link PersistentDataEntry} is not of a known class while deserializing.
 * <p>
 * This was made because deserialization cannot continue for the object,
 * but it is not desirable to stop all deserialization upon finding an unknown
 * object, so this exception is intended to be caught and dealt with, 
 * unlike other instances of {@link JsonParseException}.
 * 
 * @author Devin Hastings
 */
public class JsonNoClassException extends JsonParseException {
	private static final long serialVersionUID = 1L;

	public JsonNoClassException(String msg) {
		super(msg);
	}

}
