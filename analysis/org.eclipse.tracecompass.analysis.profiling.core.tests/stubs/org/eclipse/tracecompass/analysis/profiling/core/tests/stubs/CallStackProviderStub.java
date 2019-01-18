/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.tests.stubs;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackStateProvider;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A call stack state provider stub
 *
 * @author Geneviève Bastien
 */
public class CallStackProviderStub extends CallStackStateProvider {

    /** Name of the entry event */
    public static final String ENTRY = "entry";
    /** Name of the exit event */
    public static final String EXIT = "exit";
    /** Name of the function name field */
    public static final String FIELD_NAME = "op";
    private static final ITmfStateValue NO_FUNC_EXIT = TmfStateValue.newValueString("unknown");

    /**
     * Constructor
     *
     * @param trace
     *            The trace to run this provider on
     */
    public CallStackProviderStub(@NonNull ITmfTrace trace) {
        super(trace);
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public @NonNull CallStackStateProvider getNewInstance() {
        return new CallStackProviderStub(getTrace());
    }

    @Override
    protected boolean considerEvent(@NonNull ITmfEvent event) {
        return true;
    }

    @Override
    protected @Nullable ITmfStateValue functionEntry(@NonNull ITmfEvent event) {
        String name = event.getName();
        if (ENTRY.equals(name)) {
            ITmfEventField field = event.getContent().getField(FIELD_NAME);
            if (field != null) {
                return TmfStateValue.newValueString((String) field.getValue());
            }
        }
        return null;
    }

    @Override
    protected @Nullable ITmfStateValue functionExit(@NonNull ITmfEvent event) {
        String name = event.getName();
        if (EXIT.equals(name)) {
            ITmfEventField field = event.getContent().getField(FIELD_NAME);
            if (field != null) {
                return TmfStateValue.newValueString((String) field.getValue());
            }
            // Field is null, but this is an exit, return something
            return NO_FUNC_EXIT;
        }
        return null;
    }

    @Override
    protected int getProcessId(@NonNull ITmfEvent event) {
        return getProcessIdFromEvent(event);
    }

    /**
     * Get the TID from the event
     *
     * @param event
     *            The event
     * @return The tid
     */
    public static int getProcessIdFromEvent(@NonNull ITmfEvent event) {
        ITmfEventField field = event.getContent().getField("pid");
        if (field != null) {
            return Integer.parseInt((String) field.getValue());
        }
        return CallStackStateProvider.UNKNOWN_PID;
    }

    @Override
    protected long getThreadId(@NonNull ITmfEvent event) {
        return getThreadIdFromEvent(event);
    }

    /**
     * Get the TID from the event
     *
     * @param event
     *            The event
     * @return The tid
     */
    public static long getThreadIdFromEvent(@NonNull ITmfEvent event) {
        ITmfEventField field = event.getContent().getField("tid");
        if (field != null) {
            return Integer.parseInt((String) field.getValue());
        }
        return CallStackStateProvider.UNKNOWN_PID;
    }

}
