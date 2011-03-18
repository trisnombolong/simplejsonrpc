package org.simplejsonrpc;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({"code", "message", "data"})
public class JsonRPCErrorObject {

	private int code;
	private String message;
	private Object data;

	public JsonRPCErrorObject() {
		
	}
	
	public JsonRPCErrorObject(int code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}
	
	public void setCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Object getData() {
		return data;
	}
}
