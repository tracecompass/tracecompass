/**********************************************************************
 * Copyright (c) 2015 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.trace;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventFactory;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * Factory for {@link LttngUstEvent}.
 *
 * @author Matthew Khouzam
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class LttngUstEventFactory extends CtfTmfEventFactory {

    private static final @NonNull LttngUstEventFactory INSTANCE = new LttngUstEventFactory();

    /**
     * Private constructor.
     */
    private LttngUstEventFactory() {
        super();
    }

    public static @NonNull LttngUstEventFactory instance() {
        return INSTANCE;
    }

    @Override
    public CtfTmfEvent createEvent(CtfTmfTrace trace, IEventDefinition eventDef, @Nullable String fileName) {
        /* Prepare what to pass to CtfTmfEvent's constructor */
        final IEventDeclaration eventDecl = eventDef.getDeclaration();
        final long ts = eventDef.getTimestamp();
        final ITmfTimestamp timestamp = trace.createTimestamp(trace.timestampCyclesToNanos(ts));

        int sourceCPU = eventDef.getCPU();

        String reference = (fileName == null ? NO_STREAM : fileName);

        /* Handle the special case of lost events */
        if (eventDecl.getName().equals(CTFStrings.LOST_EVENT_NAME)) {
            return createLostEvent(trace, eventDef, eventDecl, ts, timestamp, sourceCPU, reference);
        }

        /* Handle standard event types */
        return new LttngUstEvent(trace,
                ITmfContext.UNKNOWN_RANK,
                timestamp,
                reference, // filename
                sourceCPU,
                eventDecl,
                eventDef);
    }
}