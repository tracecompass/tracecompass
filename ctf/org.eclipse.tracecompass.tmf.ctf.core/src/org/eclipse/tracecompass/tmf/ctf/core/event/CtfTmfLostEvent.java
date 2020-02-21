/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.tmf.core.event.ITmfLostEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

import com.google.common.primitives.Longs;

/**
 * An implementation of {@link ITmfLostEvent} for use in the CTF adaptor.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public class CtfTmfLostEvent extends CtfTmfEvent implements ITmfLostEvent {

    private final TmfTimeRange fTimeRange;
    private final long fNbLost;

    /**
     * Constructor. Only {@link CtfTmfEventFactory} should call this.
     *
     * @param trace
     *            The origin trace
     * @param rank
     *            The rank of the event in the trace
     * @param content
     *            The event's payload (fields). In case this event has some.
     * @param fileName
     *            The name of the trace file from which this event comes
     * @param cpu
     *            The CPU on which this event happened
     * @param declaration
     *            The CTF Event Declaration object that created this event
     * @param timeRange
     *            The time range of lost events indicated by this one
     * @param nbLost
     *            The number of lost events in the range
     */
    CtfTmfLostEvent(CtfTmfTrace trace,
            long rank,
            String fileName,
            int cpu,
            IEventDeclaration declaration,
            TmfTimeRange timeRange,
            long nbLost,
            IEventDefinition def) {
        /*
         * Only the factory should call this method, the cast to
         * (TmfNanoTimestamp) should be safe.
         */
        super(trace, rank, timeRange.getStartTime(), fileName, cpu, declaration, def);
        fTimeRange = timeRange;
        fNbLost = nbLost;
    }

    @Override
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    @Override
    public long getNbLostEvents() {
        return fNbLost;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + getTimeRange().hashCode();
        result = prime * result + Longs.hashCode(getNbLostEvents());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        /* super.equals() checks that the classes are the same */
        CtfTmfLostEvent other = checkNotNull((CtfTmfLostEvent) obj);
        if (!getTimeRange().equals(other.getTimeRange())) {
            return false;
        }
        return (getNbLostEvents() == other.getNbLostEvents());
    }

}
