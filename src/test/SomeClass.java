package test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;


public class SomeClass {

	public ServletContext context;
	public HttpSession session;
	
	public SomeClass(ServletContext context, HttpSession session) {
		this.context = context;
		this.session = session;
		System.out.println("Initialized with session");
	}
	
	public SomeClass(ServletContext context) {
		this.context = context;
		System.out.println("Initialized without session");
	}

	public double someMethod(int someInt, double someDouble) {
		return someInt + someDouble;
	}
	
	public String testMethod() {
		if(this.session != null)
			return "Conätext is " + context + " and Session is "+session;
		else
			return "Cont¥ext is " + context + dontService();
	}
	
	private String dontService() {
		return "loift";
	}
	
}
