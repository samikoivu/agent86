package com.samikoivu.agent86.lib;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.samikoivu.agent86.callbacks.test.TestCallbacks;
import com.samikoivu.agent86.callbacks.ClassLoaderUtil;
import com.samikoivu.agent86.callbacks.test.TestSensor;
import com.samikoivu.agent86.callbacks.test.TestSensor.TestCallback;
import com.samikoivu.agent86.lib.bytecode.ByteCodeTools;
import com.samikoivu.agent86.lib.bytecode.CodeBlock;
import com.samikoivu.agent86.lib.bytecode.Injector;
import com.samikoivu.agent86.lib.bytecode.Inspector;
import com.samikoivu.agent86.lib.bytecode.Injector.Option;
import com.samikoivu.agent86.lib.test.InjectionTest;
import com.samikoivu.agent86.lib.test.MethodListingTest;

class InspectorInjectorTest {
	
	private byte[] loadClassData(Class<?> target) throws IOException {
		// load class data manually
		InputStream is = target.getResourceAsStream(target.getSimpleName() + ".class");
		assertNotNull("Class resource must not be null", is);
		ByteArrayOutputStream os = new ByteArrayOutputStream(); 
	    byte[] buffer = new byte[0xFFFF];
	    for (int len = is.read(buffer); len != -1; len = is.read(buffer)) { 
	        os.write(buffer, 0, len);
	    }

	    return os.toByteArray();
	}

	@Test
	void testMethodListing() throws IOException {
		Class<?> target = MethodListingTest.class;
		byte[] data = loadClassData(target);
		ByteCodeTools bct = new ByteCodeTools(target.getName(), data);
		Inspector inspector = bct.getInspector();
		List<CodeBlock> methods = inspector.getMethods();
		assertEquals(5, methods.size(), "Four methods and constructor");
		// ensure methods in alphabetical order
		methods.sort(new Comparator<CodeBlock>() {
			@Override
			public int compare(CodeBlock o1, CodeBlock o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		CodeBlock con = methods.get(0);
		assertEquals("<init>", con.getName());
		assertTrue(con.isPublic());
		assertTrue(con.isConstructor());

		CodeBlock a1 = methods.get(1);
		assertEquals("a", a1.getName());
		assertTrue(a1.isPublic());
		
		CodeBlock a2 = methods.get(2);
		assertEquals("a", a2.getName());
		assertTrue(a2.isPublic());
		
		CodeBlock b = methods.get(3);
		assertEquals("b", b.getName());
		assertTrue(b.isPublic());

		CodeBlock c = methods.get(4);
		assertEquals("c", c.getName());
		assertFalse(c.isPublic(), "method c is not public");
		assertFalse(c.matches("c"), "method c should not match without args");
		assertFalse(c.matches("c", String.class), "method c should not match with incomplete arg list");
		assertFalse(c.matches("d", String.class, String.class, Class.class), "method c should not match with wrong name");
		assertTrue(c.matches("c", String.class, String.class, Class.class), "method c with correct args should match");
	}
	
	@Test
	void testIsFor() throws IOException {
		Class<?> target = InjectionTest.class;
		byte[] data = loadClassData(target);
		ByteCodeTools bct = new ByteCodeTools(target.getName(), data);
		Inspector inspector = bct.getInspector();
		assertTrue(inspector.isFor(target));
		assertFalse(inspector.isFor(String.class));
	}
	

	@Test
	void testIsForNoProvidedClassName() throws IOException {
		Class<?> target = InjectionTest.class;
		byte[] data = loadClassData(target);
		ByteCodeTools bct = new ByteCodeTools(null, data);
		Inspector inspector = bct.getInspector();
		assertTrue(inspector.isFor(target));
		assertFalse(inspector.isFor(String.class));
	}
	

	public boolean testInjectionAtStartCallbackPerformed = false;
	@Test
	void testInjectionAtStart() throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		Class<?> target = InjectionTest.class;
		byte[] data = loadClassData(target);
		ByteCodeTools bct = new ByteCodeTools(target.getName(), data);
		Injector injector = bct.getInjector();
		Inspector inspector = bct.getInspector();
		List<CodeBlock> methods = inspector.getMethods();
		boolean added = false;
		for (CodeBlock method : methods) {
			if (method.getName().equals("inc")) {
				TestCallbacks callback = TestCallbacks.TEST_SENSOR;
				Option callbackOptions = Option.AT_START;
				injector.addCallback(method, callback, callbackOptions );
				added = true;
			}
		}
		assertTrue(added, "callback added");
		
		ClassLoaderUtil util = new ClassLoaderUtil();
		Class<?> cls = util.defineClassUtil(InjectionTest.class.getName(), bct.getData());
		final Object instance = cls.newInstance();
		TestSensor.clear();
		assertFalse(TestSensor.wasCalled(), "Sensor prior to call has not been called");
		Method incMtd = cls.getMethod("inc");
		final Method getValueMtd = cls.getMethod("getValue");
		TestSensor.registerCallback(new TestCallback() {
			@Override
			public void testCallback() {
				try {
					int i = (Integer) getValueMtd.invoke(instance);
					assertEquals(0, i, "Value needs to be zero at callback injected at the start");
					testInjectionAtStartCallbackPerformed = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void testCallback(Long l1, Long l2, Integer i1) {
			}
			
		});

		incMtd.invoke(instance); // call method via reflection because it's a separate classloader
		assertTrue(TestSensor.wasCalled(), "Sensor after call has been called");
		assertTrue(testInjectionAtStartCallbackPerformed, "Callback has been called");
	}

	public boolean testInjectionAtReturnCallbackPerformed = false;
	@Test
	void testInjectionAtReturn() throws Exception {
		Class<?> target = InjectionTest.class;
		byte[] data = loadClassData(target);
		ByteCodeTools bct = new ByteCodeTools(target.getName(), data);
		Injector injector = bct.getInjector();
		Inspector inspector = bct.getInspector();
		List<CodeBlock> methods = inspector.getMethods();
		boolean added = false;
		for (CodeBlock method : methods) {
			if (method.getName().equals("inc")) {
				TestCallbacks callback = TestCallbacks.TEST_SENSOR;
				Option callbackOptions = Option.BEFORE_RETURN;
				injector.addCallback(method, callback, callbackOptions );
				added = true;
			}
		}
		assertTrue(added, "callback added");
		
		ClassLoaderUtil util = new ClassLoaderUtil();
		Class<?> cls = util.defineClassUtil(InjectionTest.class.getName(), bct.getData());
		final Object instance = cls.newInstance();
		TestSensor.clear();
		assertFalse(TestSensor.wasCalled(), "Sensor prior to call has not been called");
		Method incMtd = cls.getMethod("inc");
		final Method getValueMtd = cls.getMethod("getValue");
		TestSensor.registerCallback(new TestCallback() {

			@Override
			public void testCallback() {
				try {
					int i = (Integer) getValueMtd.invoke(instance);
					assertEquals(1, i, "Value needs to be incremented at the callback injected at the end");
					testInjectionAtReturnCallbackPerformed = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void testCallback(Long l1, Long l2, Integer i1) {
			}
			
		});
		incMtd.invoke(instance); // call method via reflection because it's a separate classloader
		TestSensor.registerCallback(null);
		assertTrue(TestSensor.wasCalled(), "Sensor after call has been called");
		assertTrue(testInjectionAtReturnCallbackPerformed, "Callback has been called");
	}

	public boolean testInjectionPassingArgsCallbackPerformed = false;
	@Test
	void testInjectionPassingArgs() throws Exception {
		Class<?> target = InjectionTest.class;
		byte[] data = loadClassData(target);
		ByteCodeTools bct = new ByteCodeTools(target.getName(), data);
		Injector injector = bct.getInjector();
		Inspector inspector = bct.getInspector();
		List<CodeBlock> methods = inspector.getMethods();
		boolean added = false;
		for (CodeBlock method : methods) {
			if (method.getName().equals("addAll")) {
				TestCallbacks callback = TestCallbacks.TEST_SENSOR_WITH_ARGS;
				injector.addCallback(method, callback, Option.AT_START, Option.PASS_ARGS);
				added = true;
			}
		}
		assertTrue(added, "callback added");
		
		ClassLoaderUtil util = new ClassLoaderUtil();
		Class<?> cls = util.defineClassUtil(InjectionTest.class.getName(), bct.getData());
		final Object instance = cls.newInstance();
		TestSensor.clear();
		assertFalse(TestSensor.wasCalled(), "Sensor prior to call has not been called");
		Method addAllMtd = cls.getMethod("addAll", Long.class, Long.class, Integer.class);
		final Method getValueMtd = cls.getMethod("getValue");
		TestSensor.registerCallback(new TestCallback() {

			@Override
			public void testCallback() {
				System.out.println("Wrong callback");
			}

			@Override
			public void testCallback(Long l1, Long l2, Integer i1) {
				try {
					System.out.println("Callback with: " + l1 +" " + l2 + " " + i1);
					int i = (Integer) getValueMtd.invoke(instance);
					assertEquals(0, i, "Value needs to be zero at callback injected at the start");
					testInjectionPassingArgsCallbackPerformed = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
		addAllMtd.invoke(instance, Long.valueOf(3), Long.valueOf(7), Integer.valueOf(11)); // call method via reflection because it's a separate classloader
		TestSensor.registerCallback(null);
		assertTrue(TestSensor.wasCalled(), "Sensor after call has been called");
		assertTrue(testInjectionPassingArgsCallbackPerformed, "Callback has been called");
	}

}
