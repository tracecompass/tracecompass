/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Update register methods
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;

/**
 * This class manages the set of signal listeners and the signals they are
 * interested in. When a signal is broadcasted, the appropriate listeners
 * signal handlers are invoked.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfSignalManager {

    // The set of event listeners and their corresponding handler methods.
    // Note: listeners could be restricted to ITmfComponents but there is no
    // harm in letting anyone use this since it is not tied to anything but
    // the signal data type.
    private static Map<Object, Method[]> fListeners = new HashMap<Object, Method[]>();
    private static Map<Object, Method[]> fVIPListeners = new HashMap<Object, Method[]>();

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

    /**
     * Register an object to the signal manager. This object can then implement
     * handler methods, marked with @TmfSignalHandler and with the expected
     * signal type as parameter.
     *
     * @param listener
     *            The object that will be notified of new signals
     */
    public static synchronized void register(Object listener) {
        deregister(listener); // make sure that listener is only registered once
        Method[] methods = getSignalHandlerMethods(listener);
        if (methods.length > 0) {
            fListeners.put(listener, methods);
        }
    }

    /**
     * Register an object to the signal manager as a "VIP" listener. All VIP
     * listeners will all receive the signal before the manager moves on to the
     * lowly, non-VIP listeners.
     *
     * @param listener
     *            The object that will be notified of new signals
     */
    public static synchronized void registerVIP(Object listener) {
        deregister(listener); // make sure that listener is only registered once
        Method[] methods = getSignalHandlerMethods(listener);
        if (methods.length > 0) {
            fVIPListeners.put(listener, methods);
        }
    }

    /**
     * De-register a listener object from the signal manager. This means that
     * its @TmfSignalHandler methods will no longer be called.
     *
     * @param listener
     *            The object to de-register
     */
    public static synchronized void deregister(Object listener) {
        fVIPListeners.remove(listener);
        fListeners.remove(listener);
    }

    /**
     * Returns the list of signal handlers in the listener. Signal handler name
     * is irrelevant; only the annotation (@TmfSignalHandler) is important.
     *
     * @param listener
     * @return
     */
    private static Method[] getSignalHandlerMethods(Object listener) {
        List<Method> handlers = new ArrayList<Method>();
        Method[] methods = listener.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(TmfSignalHandler.class)) {
                handlers.add(method);
            }
        }
        return handlers.toArray(new Method[handlers.size()]);
    }

    static int fSignalId = 0;

    /**
     * Invokes the handling methods that listens to signals of a given type.
     *
     * The list of handlers is built on-the-fly to allow for the dynamic
     * creation/deletion of signal handlers. Since the number of signal handlers
     * shouldn't be too high, this is not a big performance issue to pay for the
     * flexibility.
     *
     * For synchronization purposes, the signal is bracketed by two synch
     * signals.
     *
     * @param signal
     *            the signal to dispatch
     */
    public static synchronized void dispatchSignal(TmfSignal signal) {
        int signalId = fSignalId++;
        sendSignal(new TmfStartSynchSignal(signalId));
        signal.setReference(signalId);
        sendSignal(signal);
        sendSignal(new TmfEndSynchSignal(signalId));
    }

    private static void sendSignal(TmfSignal signal) {
        sendSignal(fVIPListeners, signal);
        sendSignal(fListeners, signal);
    }

    private static void sendSignal(Map<Object, Method[]> listeners, TmfSignal signal) {

        if (TmfCoreTracer.isSignalTraced()) {
            TmfCoreTracer.traceSignal(signal, "(start)"); //$NON-NLS-1$
        }

        // Build the list of listener methods that are registered for this signal
        Class<?> signalClass = signal.getClass();
        Map<Object, List<Method>> targets = new HashMap<Object, List<Method>>();
        targets.clear();
        for (Map.Entry<Object, Method[]> entry : listeners.entrySet()) {
            List<Method> matchingMethods = new ArrayList<Method>();
            for (Method method : entry.getValue()) {
                if (method.getParameterTypes()[0].isAssignableFrom(signalClass)) {
                    matchingMethods.add(method);
                }
            }
            if (!matchingMethods.isEmpty()) {
                targets.put(entry.getKey(), matchingMethods);
            }
        }

        // Call the signal handlers
        for (Map.Entry<Object, List<Method>> entry : targets.entrySet()) {
            for (Method method : entry.getValue()) {
                try {
                    method.invoke(entry.getKey(), new Object[] { signal });
                    if (TmfCoreTracer.isSignalTraced()) {
                        Object key = entry.getKey();
                        String hash = String.format("%1$08X", entry.getKey().hashCode()); //$NON-NLS-1$
                        String target = "[" + hash + "] " + key.getClass().getSimpleName() + ":" + method.getName();   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                        TmfCoreTracer.traceSignal(signal, target);
                    }
                } catch (IllegalArgumentException e) {
                    Activator.logError("Exception handling signal " + signal + " in method " + method, e); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (IllegalAccessException e) {
                    Activator.logError("Exception handling signal " + signal + " in method " + method, e); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (InvocationTargetException e) {
                    Activator.logError("Exception handling signal " + signal + " in method " + method, e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }

        if (TmfCoreTracer.isSignalTraced()) {
            TmfCoreTracer.traceSignal(signal, "(end)"); //$NON-NLS-1$
        }
    }

}
