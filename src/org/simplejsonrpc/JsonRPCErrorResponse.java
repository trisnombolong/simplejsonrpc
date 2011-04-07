package org.simplejsonrpc;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({"jsonrpc", "error", "id"})
public class JsonRPCErrorResponse extends JsonRPCResponse{

	private JsonRPCErrorObject error;
	
	public JsonRPCErrorResponse() {
		error = new JsonRPCErrorObject();
	}

	public JsonRPCErrorResponse(int code, String message, Object data, Object id) {
		super(id);
		this.error = new JsonRPCErrorObject();
		this.error.setCode(code);
		this.error.setMessage(message);
		this.error.setData(data);
	}

	public JsonRPCErrorResponse(JsonRPCErrorObject error, Object id) {
		this.error = error;
		this.setId(id);
	}

	public void setError(JsonRPCErrorObject error) {
		this.error = error;
	}

	public JsonRPCErrorObject getError() {
		return error;
	}
	
}
