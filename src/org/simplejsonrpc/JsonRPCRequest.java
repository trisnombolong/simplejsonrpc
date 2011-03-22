package org.simplejsonrpc;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({"jsonrpc", "method", "params", "id"})
@JsonIgnoreProperties({"notification"})
public class JsonRPCRequest {

	private String jsonrpc = "2.0";
	private String method;
	private List<Object> params;
	private Object id;
	private boolean notification;
	
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

	public void setNotification(boolean notification) {
		this.notification = notification;
	}
	
	public boolean isNotification() {
		return notification;
	}

}
