/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
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
 * This class manages the set of signal listeners and the signals they are
 * interested in. When a signal is broadcasted, the appropriate listeners
 * signal handlers are invoked.
 * <p>
 */
public class TmfSignalManager {

	// The set of event listeners and their corresponding handler methods.
	// Note: listeners could be restricted to ITmfComponents but there is no
	// harm in letting anyone use this since it is not tied to anything but
	// the signal data type.
	static private Map<Object, Method[]> fListeners = new HashMap<Object, Method[]>();

	// If requested, add universal signal tracer
	// TODO: Temporary solution: should be enabled/disabled dynamically 
	private static boolean fTraceIsActive = false;
	private static TmfSignalTracer fSignalTracer;
	static {
		if (fTraceIsActive) {
			fSignalTracer = TmfSignalTracer.getInstance();
			register(fSignalTracer);
		}
	}

	public static synchronized void register(Object listener) {
		Method[] methods = getSignalHandlerMethods(listener);
		if (methods.length > 0)
			fListeners.put(listener, methods);
	}

	public static synchronized void deregister(Object listener) {
		fListeners.remove(listener);
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

	/**
	 * Invokes the handling methods that listens to signals of a given type.
	 * 
	 * The list of handlers is built on-the-fly to allow for the dynamic
	 * creation/deletion of signal handlers. Since the number of signal
	 * handlers shouldn't be too high, this is not a big performance issue
	 * to pay for the flexibility.
	 * 
	 * For synchronization purposes, the signal is bracketed by two synch signals.
	 * 
	 * @param signal the signal to dispatch
	 */
	static int fSynchId = 0;
	static public synchronized void dispatchSignal(TmfSignal signal) {
		fSynchId++;
		sendSignal(new TmfStartSynchSignal(fSynchId));
		signal.setReference(fSynchId);
		sendSignal(signal);
		sendSignal(new TmfEndSynchSignal(fSynchId));
//		Tracer.traceSignal(signal);
	}

	static private void sendSignal(TmfSignal signal) {

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

}