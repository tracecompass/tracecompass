/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.ust.core.trace.callstack;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.linuxtools.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.linuxtools.tmf.core.callstack.CallStackStateProvider;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;

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
public class LttngUstCallStackProvider extends CallStackStateProvider {

    // ------------------------------------------------------------------------
    // Event strings
    // ------------------------------------------------------------------------

    /** Name of the fake field for the vtid contexts */
    private static final String CONTEXT_VTID = "context._vtid"; //$NON-NLS-1$

    /** Name of the fake field for the procname context */
    private static final String CONTEXT_PROCNAME = "context._procname"; //$NON-NLS-1$

    /** Field name for the target function address */
    private static final String FIELD_ADDR = "addr"; //$NON-NLS-1$

    /** Event names indicating function entry */
    private static final Set<String> FUNC_ENTRY_EVENTS = new HashSet<String>();

    /** Event names indicating function exit */
    private static final Set<String> FUNC_EXIT_EVENTS = new HashSet<String>();

    static {
        /* This seems overkill, but it will be checked every event. Gotta go FAST! */
        FUNC_ENTRY_EVENTS.add("lttng_ust_cyg_profile:func_entry"); //$NON-NLS-1$
        FUNC_ENTRY_EVENTS.add("lttng_ust_cyg_profile_fast:func_entry"); //$NON-NLS-1$

        FUNC_EXIT_EVENTS.add("lttng_ust_cyg_profile:func_exit"); //$NON-NLS-1$
        FUNC_EXIT_EVENTS.add("lttng_ust_cyg_profile_fast:func_exit"); //$NON-NLS-1$
    }

    /**
     * Version number of this state provider. Please bump this if you modify
     * the contents of the generated state history in some way.
     */
    private static final int VERSION = 1;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param trace
     *            The UST trace
     */
    public LttngUstCallStackProvider(LttngUstTrace trace) {
        super(trace);
    }

    // ------------------------------------------------------------------------
    // Methods from AbstractTmfStateProvider
    // ------------------------------------------------------------------------

    @Override
    public LttngUstTrace getTrace() {
        /* Type is enforced by the constructor */
        return (LttngUstTrace) super.getTrace();
    }

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
     */
    @Override
    protected boolean considerEvent(ITmfEvent event) {
        if (!(event instanceof CtfTmfEvent)) {
            return false;
        }
        ITmfEventField content = ((CtfTmfEvent) event).getContent();
        if (content.getField(CONTEXT_VTID) == null ||
                content.getField(CONTEXT_PROCNAME) == null) {
            return false;
        }
        return true;
    }

    @Override
    public String functionEntry(ITmfEvent event) {
        String eventName = ((CtfTmfEvent) event).getEventName();
        if (!FUNC_ENTRY_EVENTS.contains(eventName)) {
            return null;
        }
        Long address = (Long) event.getContent().getField(FIELD_ADDR).getValue();
        return Long.toHexString(address);
    }

    @Override
    public String functionExit(ITmfEvent event) {
        String eventName = ((CtfTmfEvent) event).getEventName();
        if (!FUNC_EXIT_EVENTS.contains(eventName)) {
            return null;
        }
        /*
         * The 'addr' field may or may not be present in func_exit events,
         * depending on if cyg-profile.so or cyg-profile-fast.so was used.
         */
        ITmfEventField field = event.getContent().getField(FIELD_ADDR);
        if (field == null) {
            return CallStackStateProvider.UNDEFINED;
        }
        Long address = (Long) field.getValue();
        return Long.toHexString(address);
    }

    @Override
    public String getThreadName(ITmfEvent event) {
        /* Class type and content was already checked if we get called here */
        ITmfEventField content = ((CtfTmfEvent) event).getContent();
        String procName = (String) content.getField(CONTEXT_PROCNAME).getValue();
        Long vtid = (Long) content.getField(CONTEXT_VTID).getValue();

        if (procName == null || vtid == null) {
            throw new IllegalStateException();
        }

        return new String(procName + '-' + vtid.toString());
    }
}
