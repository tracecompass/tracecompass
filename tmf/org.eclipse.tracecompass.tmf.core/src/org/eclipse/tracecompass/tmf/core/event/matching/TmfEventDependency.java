/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.matching;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Represents a dependency (match) between two events, where source event leads
 * to destination event
 *
 * @author Geneviève Bastien
 */
public class TmfEventDependency {

    private final DependencyEvent fSource;
    private final DependencyEvent fDestination;

    /**
     * A stripped down class representing an event from a trace at a certain
     * timestamp. This contains only the required information for event matching
     * between traces, without the extra fields that take up a lot of memory.
     *
     * @since 3.3
     */
    public static class DependencyEvent {
        private final @NonNull ITmfTrace fTrace;
        private final ITmfTimestamp fTimestamp;

        /**
         * Constructor
         *
         * @param event
         *            The complete trace event to use for this event
         */
        public DependencyEvent(ITmfEvent event) {
            fTrace = event.getTrace();
            fTimestamp = event.getTimestamp();
        }

        /**
         * Get the trace this event representation is from
         *
         * @return The trace of this event
         */
        public ITmfTrace getTrace() {
            return fTrace;
        }

        /**
         * Get the timestamp of this event representation, in nanoseconds
         *
         * @return The timestamp of the event, in nanoseconds
         */
        public ITmfTimestamp getTimestamp() {
            return fTimestamp;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fTrace, fTimestamp);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DependencyEvent)) {
                return false;
            }
            DependencyEvent other = (DependencyEvent) obj;
            return Objects.equals(fTrace, other.fTrace) && Objects.equals(fTimestamp, other.fTimestamp);
        }

    }

    /**
     * Constructor
     *
     * @param source
     *            The source event of this dependency
     * @param destination
     *            The destination event of this dependency
     * @deprecated use {@link #TmfEventDependency(DependencyEvent, DependencyEvent)}
     */
    @Deprecated
    public TmfEventDependency(final ITmfEvent source, final ITmfEvent destination) {
        fSource = new DependencyEvent(source);
        fDestination = new DependencyEvent(destination);
    }

    /**
     * Constructor
     *
     * @param source
     *            The source event of this dependency
     * @param destination
     *            The destination event of this dependency
     * @since 3.3
     */
    public TmfEventDependency(final DependencyEvent source, final DependencyEvent destination) {
        fSource = source;
        fDestination = destination;
    }

    /**
     * Get the source of this dependency
     *
     * @return The source event
     * @since 3.3
     */
    public DependencyEvent getSource() {
        return fSource;
    }

    /**
     * Get the destination of this dependency
     *
     * @return The source event
     * @since 3.3
     */
    public DependencyEvent getDestination() {
        return fDestination;
    }

    /**
     * Getter for fSourceEvent
     *
     * @return The source event
     * @deprecated Use {@link #getSource()} instead
     */
    @Deprecated
    public ITmfEvent getSourceEvent() {
        return null;
    }

    /**
     * Getter for fDestEvent
     *
     * @return the Destination event
     * @deprecated Use {@link #getDestination()} instead
     */
    @Deprecated
    public ITmfEvent getDestinationEvent() {
        return null;
    }

}
