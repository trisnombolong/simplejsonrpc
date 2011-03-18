package org.simplejsonrpc;


public abstract class JsonRPCResponse {

	private final String jsonrpc = "2.0";
	private Object id;

	public JsonRPCResponse() {

	}

	public JsonRPCResponse(Object id) {
		this.id = id;
	}

	public String getJsonrpc() {
		return jsonrpc;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getId() {
		return id;
	}

}
