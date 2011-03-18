package org.simplejsonrpc;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({"jsonrpc", "result", "id"})
public class JsonRPCResultResponse extends JsonRPCResponse {

	private Object result;

	public JsonRPCResultResponse() {

	}

	public JsonRPCResultResponse(Object result, Object id) {
		super(id);
		this.result = result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object getResult() {
		return result;
	}
	
}
