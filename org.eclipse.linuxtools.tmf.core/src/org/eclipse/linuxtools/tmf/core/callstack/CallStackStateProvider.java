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

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

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

    /**
     * Version number of this state provider. Please bump this if you modify
     * the contents of the generated state history in some way.
     */
    private static final int VERSION = 0;

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
    public int getVersion() {
        return VERSION;
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        String functionEntryName = functionEntry(event);
        try {
            if (functionEntryName != null) {
                long timestamp = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                String thread = threadName(event);
                int quark = ss.getQuarkAbsoluteAndAdd(THREADS, thread, CALL_STACK);
                ITmfStateValue value = TmfStateValue.newValueString(functionEntryName);
                ss.pushAttribute(timestamp, value, quark);
            } else if (functionExit(event) != null) {
                long timestamp = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                String thread = threadName(event);
                int quark = ss.getQuarkAbsoluteAndAdd(THREADS, thread, CALL_STACK);
                ss.popAttribute(timestamp, quark);
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
     * Check an event for function entry
     * @param event an event to check for function entry
     * @return the function name for a function entry, or null otherwise.
     */
    public abstract String functionEntry(ITmfEvent event);

    /**
     * Check an event for function exit
     * @param event an event to check for function exit
     * @return the function name or UNDEFINED for a function exit, or null otherwise.
     */
    public abstract String functionExit(ITmfEvent event);

    /**
     * Return the thread name for a function entry or exit event
     * @param event an event
     * @return the thread name
     */
    public abstract String threadName(ITmfEvent event);
}
