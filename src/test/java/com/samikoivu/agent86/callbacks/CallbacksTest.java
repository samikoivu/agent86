package com.samikoivu.agent86.callbacks;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import com.samikoivu.agent86.callbacks.Callbacks;

class CallbacksTest {

	@Test
	void testCallbackExistsAndHasRightArgsAndModifiers() throws Exception {
		Callbacks[] cbs = Callbacks.values();
		for (Callbacks cb : cbs) {
			String mname = cb.getMethodName();
			@SuppressWarnings("rawtypes") // we don't want to mix Generics and arrays
			Class[] parameterTypes = cb.getMethodArgs();
			Method method = cb.getCallbackClass().getDeclaredMethod(mname, parameterTypes);
			int mod = method.getModifiers();
			assertTrue("Callback method is public", Modifier.isPublic(mod));
			assertTrue("Callback method is static", Modifier.isStatic(mod));
		}
	}

}
