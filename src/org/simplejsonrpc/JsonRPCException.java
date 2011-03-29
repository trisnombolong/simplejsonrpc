package org.simplejsonrpc;


public class JsonRPCException extends Exception {

	private static final long serialVersionUID = 3452922280823648776L;

	private final JsonRPCErrorObject errorObject;
	
	public JsonRPCException(int code, String message, Object data) {
		super(message);
		errorObject = new JsonRPCErrorObject(code, message, data);
	}
	
	public JsonRPCException(JsonRPCErrorObject errorObject) {
		this.errorObject = errorObject;
	}

	public JsonRPCErrorObject getErrorObject() {
		return errorObject;
	}
	
}
