/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.signal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b><u>TmfSignalHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 * <p>
 * TODO: Error/exception handling
 */
public class TmfSignalManager {

	/**
	 * The set of event listeners and their corresponding handler methods.
	 */
	static private Map<Object, Method[]> fListeners = new HashMap<Object, Method[]>();

	// TODO: read from the preferences
	private static boolean fTraceIsActive = false;
	private static TmfSignalTrace fSignalTracer;

//	private static TmfSignalManager fInstance;

	static {
		if (fTraceIsActive) {
			fSignalTracer = new TmfSignalTrace();
			addListener(fSignalTracer);
		}
	}

	public static synchronized void addListener(Object listener) {
		Method[] methods = getSignalHandlerMethods(listener);
		if (methods.length > 0)
			fListeners.put(listener, methods);
	}

	public static synchronized void removeListener(Object listener) {
		fListeners.remove(listener);
	}

//	public static TmfSignalManager getInstance() {
//		if (fInstance == null) {
//			fInstance = new TmfSignalManager();
//		}
//		return fInstance;
//	}

	/**
	 * Invokes the handling methods that expect this signal.
	 * 
	 * The list of handlers is built on-the-fly to allow for the dynamic
	 * creation/deletion of signal handlers. Since the number of signal
	 * handlers shouldn't be too high, this is not a big performance issue
	 * to pay for the flexibility.
	 * 
	 * @param signal
	 */
//	private class Dispatch implements Runnable {
//
//		private final Method method;
//		private final Object entry;
//		private final Object signal;
//
//		public Dispatch(Method m, Object e, Object s) {
//			method = m;
//			entry = e;
//			signal = s;
//		}
//
//		public void run() {
//			try {
//				method.invoke(entry, new Object[] { signal });
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	private void dispatch(Method method, Object key, Object signal) {
//		Dispatch disp = new Dispatch(method, key, signal);
//		new Thread(disp).start();
//	}

	static public synchronized void dispatchSignal(Object signal) {

		// Build the list of listener methods that are registered for this signal
		Class<?> signalClass = signal.getClass();
		Map<Object, List<Method>> listeners = new HashMap<Object, List<Method>>();
		listeners.clear();
		for (Map.Entry<Object, Method[]> entry : fListeners.entrySet()) {
			List<Method> matchingMethods = new ArrayList<Method>();
			for (Method method : entry.getValue()) {
				if (method.getParameterTypes()[0].isAssignableFrom(signalClass)) {
					matchingMethods.add(method);
				}
			}
			if (!matchingMethods.isEmpty()) {
				listeners.put(entry.getKey(), matchingMethods);
			}
		}

		// Call the signal handlers
		for (Map.Entry<Object, List<Method>> entry : listeners.entrySet()) {
			for (Method method : entry.getValue()) {
//				getInstance().dispatch(method, entry.getKey(), signal);
				try {
					method.invoke(entry.getKey(), new Object[] { signal });
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
				}
			}
		}
	}

	/**
	 * Returns the list of signal handlers in the listener. Signal handler name
	 * is irrelevant; only the annotation (@TmfSignalHandler) is important.
	 * 
	 * @param listener
	 * @return
	 */
	static private Method[] getSignalHandlerMethods(Object listener) {
		List<Method> handlers = new ArrayList<Method>();
		Method[] methods = listener.getClass().getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(TmfSignalHandler.class)) {
				handlers.add(method);
			}
		}
		return handlers.toArray(new Method[handlers.size()]);
	}

}