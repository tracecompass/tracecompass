/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.execution.graph;

import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraphProvider.Context;
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
 * @since 2.4
 */
public class OsInterruptContext {

    /**
     * The default context when none is defined
     */
    public static final OsInterruptContext DEFAULT_CONTEXT = new OsInterruptContext(new TmfEvent(null, ITmfContext.UNKNOWN_RANK, TmfTimestamp.BIG_BANG, null, null), Context.NONE);

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
    public OsInterruptContext(ITmfEvent event, Context ctx) {
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
