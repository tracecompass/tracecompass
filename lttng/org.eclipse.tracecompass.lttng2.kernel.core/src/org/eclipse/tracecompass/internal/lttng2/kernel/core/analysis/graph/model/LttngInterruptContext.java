/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model;

import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.building.LttngKernelExecGraphProvider.Context;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;

/**
 * A class representing an interrupt context in the kernel. It associates the
 * reason of the context with an event.
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class LttngInterruptContext {

    /**
     * The default context when none is defined
     */
    public static final LttngInterruptContext DEFAULT_CONTEXT = new LttngInterruptContext(new TmfEvent(null, ITmfContext.UNKNOWN_RANK, TmfTimestamp.BIG_BANG, null, null), Context.NONE);

    private final ITmfEvent fEvent;
    private final Context fContext;

    /**
     * Constructor
     *
     * @param event
     *            The event representing the start of this interrupt context
     * @param ctx
     *            The context type
     */
    public LttngInterruptContext(ITmfEvent event, Context ctx) {
        fEvent = event;
        fContext = ctx;
    }

    /**
     * The event associated with this interrupt context
     *
     * @return The event marking the entry in this interrupt context
     */
    public ITmfEvent getEvent() {
        return fEvent;
    }

    /**
     * Get the type of interrupt context this context represents
     *
     * @return The type of interrupt context
     */
    public Context getContext() {
        return fContext;
    }

}
