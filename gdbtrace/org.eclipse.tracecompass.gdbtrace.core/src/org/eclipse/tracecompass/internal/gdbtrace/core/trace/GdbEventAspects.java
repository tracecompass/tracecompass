/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Bernd Hufmann - Add Content aspect
 *******************************************************************************/

package org.eclipse.tracecompass.internal.gdbtrace.core.trace;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.gdbtrace.core.event.GdbTraceEvent;
import org.eclipse.tracecompass.internal.gdbtrace.core.event.GdbTraceEventContent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfContentFieldAspect;

import com.google.common.collect.ImmutableList;

/**
 * Event table column definition for GDB traces.
 *
 * @author Alexandre Montplaisir
 */
public final class GdbEventAspects {

    private GdbEventAspects() {}

    private static final @NonNull Iterable<ITmfEventAspect> GDB_ASPECTS =
            ImmutableList.of(
                    new TmfContentFieldAspect(GdbTraceEventContent.TRACE_FRAME, GdbTraceEventContent.TRACE_FRAME),
                    new TmfContentFieldAspect(GdbTraceEventContent.TRACEPOINT, GdbTraceEventContent.TRACEPOINT),
                    new GdbFileAspect(),
                    ITmfEventAspect.BaseAspects.CONTENTS
                    );

    private static class GdbFileAspect implements ITmfEventAspect {

        @Override
        public String getName() {
            return "File"; //$NON-NLS-1$
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof GdbTraceEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((GdbTraceEvent) event).getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }
    }

    /**
     * Get the event aspects specific to GDB traces.
     *
     * @return The set of aspects
     */
    public static @NonNull Iterable<ITmfEventAspect> getAspects() {
        return GDB_ASPECTS;
    }
}
