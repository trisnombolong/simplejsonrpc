package org.simplejsonrpc;

import java.util.List;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({"jsonrpc", "method", "params", "id"})
public class JsonRPCRequest {

	public String jsonrpc = "2.0";
	public String method;
	public List<Object> params;
	public Object id;

	public String getJsonrpc() {
		return jsonrpc;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getId() {
		return id;
	}

}
