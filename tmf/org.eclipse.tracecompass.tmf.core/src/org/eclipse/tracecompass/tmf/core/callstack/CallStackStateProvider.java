/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.callstack;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * The state provider for traces that support the Call Stack view.
 *
 * The attribute tree should have the following structure:
 *
 * <pre>
 * (root)
 *   +-- Processes
 *       +-- (PID 1000)
 *       |    +-- (TID 1000)
 *       |    |    +-- CallStack (stack-attribute)
 *       |    |         +-- 1
 *       |    |         +-- 2
 *       |    |        ...
 *       |    |         +-- n
 *       |    +-- (TID 1001)
 *       |         +-- CallStack (stack-attribute)
 *       |              +-- 1
 *       |              +-- 2
 *       |             ...
 *       |              +-- n
 *       |
 *       +-- (PID 2000)
 *            +-- (TID 2000)
 *                 +-- CallStack (stack-attribute)
 *                      +-- 1
 *                      +-- 2
 *                     ...
 *                      +-- n
 * </pre>
 *
 * where:
 * <ul>
 * <li>(PID n) is an attribute name representing a unique process identifier.
 * </li>
 * <li>(TID n) is an attribute whose name is the display name of the thread.
 * Optionally, its value is a long representing the thread id, used for sorting.
 * </li>
 * <li>"CallStack" is a stack-attribute whose pushed values are either a string,
 * int or long representing the function name or address in the call stack. The
 * type of value used must be constant for a particular CallStack.</li>
 * </ul>
 *
 * @author Patrick Tasse
 */
@NonNullByDefault
public abstract class CallStackStateProvider extends AbstractTmfStateProvider {

    /** Thread attribute
     * @since 2.0 */
    public static final String PROCESSES = "Processes"; //$NON-NLS-1$

    /** CallStack stack-attribute */
    public static final String CALL_STACK = "CallStack"; //$NON-NLS-1$

    /** Undefined process ID
     * @since 2.0 */
    protected static final int UNDEFINED_PID = -1;

    /** Undefined function exit name */
    protected static final String UNDEFINED = "UNDEFINED"; //$NON-NLS-1$

    /** CallStack state system ID */
    private static final String ID = "org.eclipse.linuxtools.tmf.callstack"; //$NON-NLS-1$

    /** Dummy function name for when no function is expected */
    private static final String NO_FUNCTION = "no function"; //$NON-NLS-1$

    /**
     * Default constructor
     *
     * @param trace
     *            The trace for which we build this state system
     */
    public CallStackStateProvider(ITmfTrace trace) {
        super(trace, ID);
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        if (!considerEvent(event)) {
            return;
        }

        ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());

        try {
            /* Check if the event is a function entry */
            String functionEntryName = functionEntry(event);
            if (functionEntryName != null) {
                long timestamp = event.getTimestamp().toNanos();
                int pid = getProcessId(event);
                String threadName = getThreadName(event);
                int threadQuark = ss.getQuarkAbsoluteAndAdd(PROCESSES, Integer.toString(pid), threadName);

                long threadId = getThreadId(event);
                ss.updateOngoingState(TmfStateValue.newValueLong(threadId), threadQuark);

                int callStackQuark = ss.getQuarkRelativeAndAdd(threadQuark, CALL_STACK);
                ITmfStateValue value = TmfStateValue.newValueString(functionEntryName);
                ss.pushAttribute(timestamp, value, callStackQuark);
                return;
            }

            /* Check if the event is a function exit */
            String functionExitName = functionExit(event);
            if (functionExitName != null) {
                long timestamp = event.getTimestamp().toNanos();
                int pid = getProcessId(event);
                String thread = getThreadName(event);
                int quark = ss.getQuarkAbsoluteAndAdd(PROCESSES, Integer.toString(pid), thread, CALL_STACK);
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

        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restrict the return type for {@link ITmfStateProvider#getNewInstance}.
     *
     * @since 2.0
     */
    @Override
    public abstract CallStackStateProvider getNewInstance();

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
    protected abstract @Nullable String functionEntry(ITmfEvent event);

    /**
     * Check an event if it indicates a function exit.
     *
     * @param event
     *            An event to check for function exit
     * @return The function name, or UNDEFINED, for a function exit, or null if
     *         not a function exit.
     */
    protected abstract @Nullable String functionExit(ITmfEvent event);

    /**
     * Return the process ID of a function entry event.
     *
     * Use {@link #UNDEFINED_PID} if it is not known.
     *
     * @param event
     *            The event
     * @return The process ID
     * @since 2.0
     */
    protected abstract int getProcessId(ITmfEvent event);

    /**
     * Return the thread id of a function entry event.
     *
     * @param event
     *            The event
     * @return The thread id
     * @since 2.0
     */
    protected abstract long getThreadId(ITmfEvent event);

    /**
     * Return the thread name of a function entry or exit event.
     *
     * @param event
     *            The event
     * @return The thread name (as will be shown in the view)
     */
    protected abstract String getThreadName(ITmfEvent event);
}
