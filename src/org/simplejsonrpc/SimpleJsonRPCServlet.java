package org.simplejsonrpc;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Servlet implementation class JAxisServlet
 */
public class SimpleJsonRPCServlet extends HttpServlet {

	public enum ServiceContext {
		SERVLET_CONTEXT, SESSION_CONTEXT
	};

	private static final long serialVersionUID = 1L;

	public static final String SERVICE_CLASS_INIT_PARAMETER = "service-class";
	public static final String SERVICE_CONTEXT_INIT_PARAMETER = "service-context";
	public static final String SERVICE_OBJECT_SESSION_PARAMETER = "service-object";

	private Class<?> serviceClass;
	private ServiceContext serviceContext;
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Fetches the class name from the init parameter
	 * {@link SimpleJsonRPCServlet.SERVICE_CLASS_INIT_PARAMETER}
	 * 
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		String serviceClassName = config.getInitParameter(SERVICE_CLASS_INIT_PARAMETER);
		try {
			serviceClass = Class.forName(serviceClassName);
		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		}
		String serviceScope = config.getInitParameter(SERVICE_CONTEXT_INIT_PARAMETER);
		if (serviceScope == null) {
			serviceContext = ServiceContext.SESSION_CONTEXT;
		} else {
			if (serviceScope.equals("session")) {
				serviceContext = ServiceContext.SESSION_CONTEXT;
			} else if (serviceScope.equals("servlet")) {
				serviceContext = ServiceContext.SERVLET_CONTEXT;
			} else {
				throw new ServletException("Incorrect value for " + SERVICE_CONTEXT_INIT_PARAMETER);
			}
		}
		super.init(config);
	}

	/**
	 * Creates an instance of the service class. Tries to find a constructor
	 * with ServletContext and HttpSession as its parameters and uses the
	 * default constructor if not present.
	 * 
	 * @param session
	 *            The HttpSession in which the object will reside.
	 * @return An instance of the service class.
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 */
	private Object createServiceObject(HttpSession session) throws IllegalAccessException, InvocationTargetException, InstantiationException,
			NoSuchMethodException {
		Object serviceObject = null;
		try {
			serviceObject = ConstructorUtils.invokeConstructor(serviceClass, getServletContext(), session);
		} catch (NoSuchMethodException e) {
			serviceObject = ConstructorUtils.invokeConstructor(serviceClass);
		}
		return serviceObject;
	}

	/**
	 * Creates an instance of the service class. Tries to find a constructor
	 * with ServletContext as its only parameter and uses the default
	 * constructor if not present.
	 * 
	 * @return An instance of the service class
	 * @throws IllegalAccessException
	 *             Thrown if creation fails.
	 * @throws InvocationTargetException
	 *             Thrown if creation fails.
	 * @throws InstantiationException
	 *             Thrown if creation fails.
	 * @throws NoSuchMethodException
	 *             Thrown if creation fails.
	 */
	private Object createServiceObject() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		Object serviceObject = null;
		try {
			serviceObject = ConstructorUtils.invokeConstructor(serviceClass, getServletContext());
		} catch (NoSuchMethodException e) {
			serviceObject = ConstructorUtils.invokeConstructor(serviceClass);
		}
		return serviceObject;
	}

	/**
	 * Retrieves the Service Object from Service/SessionContext. Creates and
	 * stores it if not present.
	 * 
	 * @param session
	 *            The session context.
	 * @return The service instance
	 * @throws IllegalAccessException
	 *             Thrown if creation fails.
	 * @throws InvocationTargetException
	 *             Thrown if creation fails.
	 * @throws InstantiationException
	 *             Thrown if creation fails.
	 * @throws NoSuchMethodException
	 *             Thrown if creation fails.
	 */
	private Object getServiceObject(HttpSession session) throws IllegalAccessException, InvocationTargetException, InstantiationException,
			NoSuchMethodException {
		Object serviceObject = null;
		switch (serviceContext) {
		case SESSION_CONTEXT:
			serviceObject = session.getAttribute(SERVICE_OBJECT_SESSION_PARAMETER + serviceClass.getName());
			if (serviceObject == null) {
				serviceObject = createServiceObject(session);
				session.setAttribute(SERVICE_OBJECT_SESSION_PARAMETER + serviceClass.getName(), serviceObject);
			}
			break;
		case SERVLET_CONTEXT:
			serviceObject = getServletContext().getAttribute(SERVICE_OBJECT_SESSION_PARAMETER + serviceClass.getName());
			if (serviceObject == null) {
				serviceObject = createServiceObject();
				getServletContext().setAttribute(SERVICE_OBJECT_SESSION_PARAMETER + serviceClass.getName(), serviceObject);
			}
			break;
		}
		return serviceObject;
	}

	private JsonRPCRequest mapRequestNode(JsonNode node) throws JsonParseException, JsonMappingException, IOException {
		JsonRPCRequest request = mapper.readValue(node, JsonRPCRequest.class);
		request.setNotification(!node.has("id"));
		return request;
	}

	/**
	 * <p>
	 * Fetches the JSON-RPC Request from POST Data and executes the appropriate
	 * method.
	 * </p>
	 * <p>
	 * It proceeds as follows: Fetching and parsing the POST Data. Answers with
	 * a error response if parsing fails, as specified in JSON-RPC 2.0. Second
	 * step is mapping the data to the JsonRPCRequest class. Answers with the
	 * "Invalid request" error, if mapping fails. Third step is fetching the
	 * service instance from servlet/session context. Creates an instance if not
	 * present. If creating an instance fails it answers with the
	 * "Internal error." response. Fourth step is executing the appropiate
	 * method for the request. If the method is not found, it answers with the
	 * "Mothod unknown." response as specified in JSON-RPC 2.0. If an exception
	 * is thrown it answers with a -32099 error code with the exception message.
	 * If the thrown exception is a decendant from JsonRPCException, it uses the
	 * Embedded JsonRPCErrorObject as an error. Last step is serializing all the
	 * created responses and sending them away (optionaly GZIPed if
	 * Accepted-Encoding is set appropiatly in the HTTP request).
	 * </p>
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json; encoding=UTF-8");
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readTree(request.getInputStream());
		} catch (JsonParseException e) {
			mapper.writeValue(response.getOutputStream(), new JsonRPCErrorResponse(-32700, "Parse error.", null, null));
			return;
		}
		List<JsonRPCRequest> requests = new LinkedList<JsonRPCRequest>();
		try {
			if (rootNode.isArray()) {
				for (Iterator<JsonNode> nodes = rootNode.getElements(); nodes.hasNext();) {
					requests.add(this.mapRequestNode(nodes.next()));
				}
			} else {
				requests.add(this.mapRequestNode(rootNode));
			}
		} catch (JsonMappingException e) {
			mapper.writeValue(response.getOutputStream(), new JsonRPCErrorResponse(-32600, "Invalid Request.", null, null));
		}

		List<Object> responses = new LinkedList<Object>();
		Object serviceObject = null;
		try {
			serviceObject = getServiceObject(request.getSession());
		} catch (Exception e) {
			e.printStackTrace();
			for (JsonRPCRequest jsonRequest : requests) {
				JsonRPCErrorResponse error = new JsonRPCErrorResponse();
				error.setId(jsonRequest.getId());
				error.getError().setMessage("Internal error.");
				error.getError().setCode(-32603);
				responses.add(error);
			}
			if (!rootNode.isArray()) {
				mapper.writeValue(response.getOutputStream(), responses.get(0));
			} else {
				mapper.writeValue(response.getOutputStream(), responses);
			}
			return;
		}

		for (JsonRPCRequest jsonRequest : requests) {
			Object result = null;
			JsonRPCResponse jsonResponse = null;
			try {
				Object [] params;
				if(jsonRequest.getParams() == null) {
					params = new Object[0];
				} else {
					params = jsonRequest.getParams().toArray();
				}
				result = MethodUtils.invokeMethod(serviceObject, jsonRequest.getMethod(), params);
				jsonResponse = new JsonRPCResultResponse(result, jsonRequest.getId());
			} catch (NoSuchMethodException e) {
				JsonRPCErrorResponse error = new JsonRPCErrorResponse();
				error.setId(jsonRequest.getId());
				error.getError().setMessage("Method not found.");
				error.getError().setCode(-32601);
				jsonResponse = error;
			} catch (InvocationTargetException e) {
				JsonRPCErrorResponse error;
				if (e.getTargetException() instanceof JsonRPCException) {
					JsonRPCException jsonException = (JsonRPCException) e.getTargetException();
					error = new JsonRPCErrorResponse(jsonException.getErrorObject(), jsonRequest.getId());
				} else {
					error = new JsonRPCErrorResponse();
					error.setId(jsonRequest.getId());
					error.getError().setMessage(e.getTargetException().getMessage());
					error.getError().setCode(-32099);
				}
				jsonResponse = error;
			} catch (Exception e) {
				JsonRPCErrorResponse error = new JsonRPCErrorResponse();
				error.setId(jsonRequest.getId());
				error.getError().setMessage(e.getMessage());
				error.getError().setCode(-32099);
				jsonResponse = error;
			}
			if (!jsonRequest.isNotification())
				responses.add(jsonResponse);
		}
		if (!responses.isEmpty()) {
			OutputStream out = null;
			if (StringUtils.contains(request.getHeader("Accept-Encoding"), "gzip")) {
				out = new GZIPOutputStream(response.getOutputStream());
				response.setHeader("Content-Encoding", "gzip");
			} else {
				out = response.getOutputStream();
			}
			if (!rootNode.isArray()) {
				mapper.writeValue(out, responses.get(0));
			} else {
				mapper.writeValue(out, responses);
			}
			out.close();
		}
	}
}
