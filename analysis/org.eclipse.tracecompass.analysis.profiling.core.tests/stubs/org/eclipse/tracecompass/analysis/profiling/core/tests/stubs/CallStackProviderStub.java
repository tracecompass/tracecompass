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

    private static final String ENTRY = "entry";
    private static final String EXIT = "exit";

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
            ITmfEventField field = event.getContent().getField("op");
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
            ITmfEventField field = event.getContent().getField("op");
            if (field != null) {
                return TmfStateValue.newValueString((String) field.getValue());
            }
        }
        return null;
    }

    @Override
    protected int getProcessId(@NonNull ITmfEvent event) {
        ITmfEventField field = event.getContent().getField("pid");
        if (field != null) {
            return Integer.parseInt((String) field.getValue());
        }
        return CallStackStateProvider.UNKNOWN_PID;
    }

    @Override
    protected long getThreadId(@NonNull ITmfEvent event) {
        ITmfEventField field = event.getContent().getField("tid");
        if (field != null) {
            return Integer.parseInt((String) field.getValue());
        }
        return CallStackStateProvider.UNKNOWN_PID;
    }

}
