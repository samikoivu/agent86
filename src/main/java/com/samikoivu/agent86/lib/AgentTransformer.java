package com.samikoivu.agent86.lib;

import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.samikoivu.agent86.callbacks.Callbacks;
import com.samikoivu.agent86.callbacks.ClassCollector;
import com.samikoivu.agent86.lib.bytecode.ByteCodeTools;
import com.samikoivu.agent86.lib.bytecode.CodeBlock;
import com.samikoivu.agent86.lib.bytecode.Injector;
import com.samikoivu.agent86.lib.bytecode.Inspector;
import com.samikoivu.agent86.lib.bytecode.Injector.Option;

/**
 * Responsible for transforming class definitions. Code is injected into java.lang.String definition, as well as any Servlet methods,
 * that calls back into logic which records the requested data.
 */
public class AgentTransformer implements ClassFileTransformer {
	
	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		SuperSimpleLogger logger = SuperSimpleLogger.getInstance();
		PrintStream log = logger.getStream();
		//log.println("className: " + className + " redef: " + classBeingRedefined);
		try {
			ByteCodeTools bct = new ByteCodeTools(className, classfileBuffer);
			Inspector inspector = bct.getInspector();
			Injector injector = bct.getInjector();
			if (className == null) className = inspector.getName();
			
			ClassCollector.collect(className); // collect loaded classes
	
			// String collection, add a callback to all String constructors
			if (inspector.isFor(String.class)) {
				List<CodeBlock> cons = inspector.getMethods();
				for (CodeBlock con : cons) {
					if (con.isConstructor() && con.isPublic()) {
						injector.addCallback(con, Callbacks.COLLECT_STRING, Option.BEFORE_RETURN, Option.PASS_THIS);
					}
				}
				
				log.println("Injected java.lang.String");
			}
			
			// Initial implementation targeted all Servlet subclasses, but targetting just the HttpServlet seems to be just fine
			if (!inspector.isInterface() // don't bother with interfaces
				&& "javax/servlet/http/HttpServlet".equals(className)) {
				// Servlet method injection
				List<CodeBlock> methods = inspector.getMethods();
				CodeBlock service = null;
				CodeBlock doGet = null;
				CodeBlock doPost = null;
				for (CodeBlock mtd : methods) {
					if (mtd.matches("service", HttpServletRequest.class, HttpServletResponse.class) && !mtd.isAbstract()) {
						service = mtd;
					}
					if (mtd.matches("doGet", HttpServletRequest.class, HttpServletResponse.class) && !mtd.isAbstract()) {
						doGet = mtd;
					}
					if (mtd.matches("doPost", HttpServletRequest.class, HttpServletResponse.class) && !mtd.isAbstract()) {
						doPost = mtd;
					}
				}
		
				// if there is a service method, inject it, otherwise inject doGet or doPost or both (depending on what exists)
				if (service != null) {
					injector.addCallback(service, Callbacks.BEGIN_REQUEST, Option.AT_START, Option.PASS_ARGS);
					injector.addCallback(service, Callbacks.END_REQUEST, Option.BEFORE_RETURN);
					log.println("Injected " + className + ".service(...)");
				} else {
					// no service
					if (doGet != null) {
						injector.addCallback(doGet, Callbacks.BEGIN_REQUEST, Option.AT_START, Option.PASS_ARGS);
						injector.addCallback(doGet, Callbacks.END_REQUEST, Option.BEFORE_RETURN);
						log.println("Injected " + className + ".doGet(...)");
					}
					
					if (doPost != null) {
						injector.addCallback(doPost, Callbacks.BEGIN_REQUEST, Option.AT_START, Option.PASS_ARGS);
						injector.addCallback(doPost, Callbacks.END_REQUEST, Option.BEFORE_RETURN);
						log.println("Injected " + className + ".doPost(...)");
					}
				}
			}
			return bct.getData();
		} catch (Throwable t) {
			t.printStackTrace(log);
			throw t;
		}
	}

}
