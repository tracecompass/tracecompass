/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.callstack;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.osgi.util.NLS;

/**
 * The state provider for traces that support the Call Stack view.
 *
 * The attribute tree should have the following structure:
 *<pre>
 * (root)
 *   \-- Threads
 *        |-- (Thread 1)
 *        |    \-- CallStack (stack-attribute)
 *        |         |-- 1
 *        |         |-- 2
 *        |        ...
 *        |         \-- n
 *        |-- (Thread 2)
 *        |    \-- CallStack (stack-attribute)
 *        |         |-- 1
 *        |         |-- 2
 *        |        ...
 *        |         \-- n
 *       ...
 *        \-- (Thread n)
 *             \-- CallStack (stack-attribute)
 *                  |-- 1
 *                  |-- 2
 *                 ...
 *                  \-- n
 *</pre>
 * where:
 * <br>
 * (Thread n) is an attribute whose name is the name of the thread
 * <br>
 * CallStack is a stack-attribute whose pushed values are either a string,
 * int or long representing the function name or address in the call stack.
 * The type of value used must be constant for a particular CallStack.
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public abstract class CallStackStateProvider extends AbstractTmfStateProvider {

    /** CallStack state system ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.callstack"; //$NON-NLS-1$
    /** Thread attribute */
    public static final String THREADS = "Threads"; //$NON-NLS-1$
    /** CallStack stack-attribute */
    public static final String CALL_STACK = "CallStack"; //$NON-NLS-1$
    /** Undefined function exit name */
    public static final String UNDEFINED = "UNDEFINED"; //$NON-NLS-1$

    /** Dummy function name for when no function is expected */
    private static final String NO_FUNCTION = "no function"; //$NON-NLS-1$

    /**
     * Default constructor
     *
     * @param trace
     *            The trace for which we build this state system
     */
    public CallStackStateProvider(ITmfTrace trace) {
        super(trace, ITmfEvent.class, ID);
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        if (!considerEvent(event)) {
            return;
        }
        try {
            /* Check if the event is a function entry */
            String functionEntryName = functionEntry(event);
            if (functionEntryName != null) {
                long timestamp = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                String thread = getThreadName(event);
                int quark = ss.getQuarkAbsoluteAndAdd(THREADS, thread, CALL_STACK);
                ITmfStateValue value = TmfStateValue.newValueString(functionEntryName);
                ss.pushAttribute(timestamp, value, quark);
                return;
            }

            /* Check if the event is a function exit */
            String functionExitName = functionExit(event);
            if (functionExitName != null) {
                long timestamp = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                String thread = getThreadName(event);
                int quark = ss.getQuarkAbsoluteAndAdd(THREADS, thread, CALL_STACK);
                ITmfStateValue poppedValue = ss.popAttribute(timestamp, quark);
                String poppedName = (poppedValue == null ? NO_FUNCTION : poppedValue.unboxStr());

                /*
                 * Verify that the value we are popping matches the one in the
                 * event field, unless the latter is undefined.
                 */
                if (!functionExitName.equals(UNDEFINED) &&
                        !functionExitName.equals(poppedName)) {
                    Activator.logWarning(NLS.bind(
                            Messages.CallStackStateProvider_UnmatchedPoppedValue,
                            functionExitName,
                            poppedName));
                }
            }

        } catch (TimeRangeException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if this event should be considered at all for function entry/exit
     * analysis. This check is only run once per event, before
     * {@link #functionEntry} and {@link #functionExit} (to avoid repeating
     * checks in those methods).
     *
     * @param event
     *            The event to check
     * @return If false, the event will be ignored by the state provider. If
     *         true processing will continue.
     */
    protected abstract boolean considerEvent(ITmfEvent event);

    /**
     * Check an event if it indicates a function entry.
     *
     * @param event
     *            An event to check for function entry
     * @return The function name of the function entry, or null if not a
     *         function entry.
     */
    protected abstract String functionEntry(ITmfEvent event);

    /**
     * Check an event if it indicates a function exit.
     *
     * @param event
     *            An event to check for function exit
     * @return The function name, or UNDEFINED, for a function exit, or null if
     *         not a function exit.
     */
    protected abstract String functionExit(ITmfEvent event);

    /**
     * Return the thread name of a function entry or exit event.
     *
     * @param event
     *            The event
     * @return The thread name (as will be shown in the view)
     */
    protected abstract String getThreadName(ITmfEvent event);
}
