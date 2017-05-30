/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add support for thread id
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.callstack;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.LttngUst20EventLayout;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.callstack.CallStackStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;

import com.google.common.collect.ImmutableSet;

/**
 * Callstack provider for LTTng-UST traces.
 *
 * If the traces contains 'func_entry' and 'func_exit' event (see the
 * lttng-ust-cyg-profile manpage), AND contains vtid and procname contexts, we
 * can use this information to populate the TMF Callstack View.
 *
 * Granted, most UST traces will not contain this information. In this case,
 * this will simply build an empty state system, and the view will remain
 * unavailable.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public class LttngUstCallStackProvider extends CallStackStateProvider {

    /**
     * Version number of this state provider. Please bump this if you modify
     * the contents of the generated state history in some way.
     */
    private static final int VERSION = 3;

    /** Event names indicating function entry */
    private final Set<String> funcEntryEvents;

    /** Event names indicating function exit */
    private final Set<String> funcExitEvents;

    private final ILttngUstEventLayout fLayout;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param trace
     *            The UST trace
     */
    public LttngUstCallStackProvider(ITmfTrace trace) {
        super(trace);

        if (trace instanceof LttngUstTrace) {
            fLayout = ((LttngUstTrace) trace).getEventLayout();
        } else {
            /* For impostor trace types, assume they use the LTTng 2.0 layout */
            fLayout = LttngUst20EventLayout.getInstance();
        }

        funcEntryEvents = ImmutableSet.of(
                fLayout.eventCygProfileFuncEntry(),
                fLayout.eventCygProfileFastFuncEntry());

        funcExitEvents = ImmutableSet.of(
                fLayout.eventCygProfileFuncExit(),
                fLayout.eventCygProfileFastFuncExit());
    }

    // ------------------------------------------------------------------------
    // Methods from AbstractTmfStateProvider
    // ------------------------------------------------------------------------

    @Override
    public LttngUstCallStackProvider getNewInstance() {
        return new LttngUstCallStackProvider(getTrace());
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    // ------------------------------------------------------------------------
    // Methods from CallStackStateProvider
    // ------------------------------------------------------------------------

    /**
     * Check that this event contains the required information we need to be
     * used in the call stack view. We need at least the "procname" and "vtid"
     * contexts.
     *
     * The "vpid" is useful too, but optional.
     */
    @Override
    protected boolean considerEvent(ITmfEvent event) {
        if (!(event instanceof CtfTmfEvent)) {
            return false;
        }
        Object tid = TmfTraceUtils.resolveEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
        if (!(tid instanceof Integer)) {
            return false;
        }
        return true;
    }

    @Override
    public @Nullable ITmfStateValue functionEntry(ITmfEvent event) {
        String eventName = event.getName();
        if (!funcEntryEvents.contains(eventName)) {
            return null;
        }
        Long address = (Long) event.getContent().getField(fLayout.fieldAddr()).getValue();
        return TmfStateValue.newValueLong(address);
    }

    @Override
    public @Nullable ITmfStateValue functionExit(ITmfEvent event) {
        String eventName = event.getName();
        if (!funcExitEvents.contains(eventName)) {
            return null;
        }
        /*
         * The 'addr' field may or may not be present in func_exit events,
         * depending on if cyg-profile.so or cyg-profile-fast.so was used.
         */
        ITmfEventField field = event.getContent().getField(fLayout.fieldAddr());
        if (field == null) {
            return TmfStateValue.nullValue();
        }
        Long address = (Long) field.getValue();
        return TmfStateValue.newValueLong(address);
    }

    @Override
    protected int getProcessId(@NonNull ITmfEvent event) {
        /* The "vpid" context may not be present! We need to check */
        ITmfEventField content = event.getContent();
        ITmfEventField vpidContextField = content.getField(fLayout.contextVpid());
        if (vpidContextField == null) {
            return UNKNOWN_PID;
        }
        return ((Long) vpidContextField.getValue()).intValue();
    }

    @Override
    protected long getThreadId(ITmfEvent event) {
        /* We checked earlier that the "vtid" context is present */
        Integer tid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
        if (tid == null) {
            return UNKNOWN_PID;
        }
        return tid.longValue();
    }

    @Override
    public @Nullable String getThreadName(ITmfEvent event) {
        /* We checked earlier that the "procname" context is present */
        ITmfEventField content = event.getContent();
        ITmfEventField field = content.getField(fLayout.contextProcname());
        String procName = field == null ? StringUtils.EMPTY : (String.valueOf(field.getValue()) + '-');
        long vtid = getThreadId(event);
        return (procName + Long.toString(vtid));
    }
}
