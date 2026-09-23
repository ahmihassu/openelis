package org.bahmni.feed.openelis.odoo;

/**
 * Thrown when Odoo XML-RPC login or execute fails.
 */
public class OdooException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OdooException(String message) {
		super(message);
	}

	public OdooException(String message, Throwable cause) {
		super(message, cause);
	}
}
