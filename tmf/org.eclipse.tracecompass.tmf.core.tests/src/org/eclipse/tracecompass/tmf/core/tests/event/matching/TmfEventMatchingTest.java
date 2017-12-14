/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.ITmfMatchEventDefinition;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventDependency.DependencyEvent;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching.Direction;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.event.TmfEventTypeStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Table;

/**
 * Test the {@link TmfEventMatching} class
 *
 * @author Geneviève Bastien
 */
public class TmfEventMatchingTest {

    private static final @NonNull IProgressMonitor PROGRESS_MONITOR = new NullProgressMonitor();
    private TmfTraceStub fT1;
    private TmfTraceStub fT2;
    private @NonNull Collection<@NonNull ITmfTrace> fTraces = Collections.EMPTY_LIST;

    private static class IntMatchingKey implements IEventMatchingKey {

        private final @NonNull Integer fId;

        public IntMatchingKey(@NonNull Integer id) {
            fId = id;
        }

        @Override
        public int hashCode() {
            return fId.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o instanceof IntMatchingKey) {
                IntMatchingKey key = (IntMatchingKey) o;
                return key.fId.equals(fId);
            }
            return false;
        }
    }

    private static class StringMatchingKey implements IEventMatchingKey {

        private final @NonNull String fStr;

        public StringMatchingKey(@NonNull String id) {
            fStr = id;
        }

        @Override
        public int hashCode() {
            return fStr.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o instanceof StringMatchingKey) {
                StringMatchingKey key = (StringMatchingKey) o;
                return key.fStr.equals(fStr);
            }
            return false;
        }
    }

    private static class StubEventMatching implements ITmfMatchEventDefinition {

        @Override
        public IEventMatchingKey getEventKey(ITmfEvent event) {
            if (!(event instanceof MatchEventStub)) {
                return null;
            }
            int id = ((MatchEventStub) event).fId;
            // Use a String key if the id is negative, otherwise use an int key
            if (id < 0) {
                return new StringMatchingKey(String.valueOf(id));
            }
            return new IntMatchingKey(id);
        }

        @Override
        public boolean canMatchTrace(ITmfTrace trace) {
            return true;
        }

        @Override
        public Direction getDirection(ITmfEvent event) {
            if (!(event instanceof MatchEventStub)) {
                return null;
            }
            return ((MatchEventStub) event).fDirection;
        }

    }

    private static class MatchEventStub extends TmfEvent {

        private final int fId;
        private final Direction fDirection;

        public MatchEventStub(final ITmfTrace trace, final ITmfTimestamp timestamp, int id, Direction direction) {
            super(trace,
                    ITmfContext.UNKNOWN_RANK,
                    timestamp,
                    new TmfEventTypeStub(),
                    new TmfEventField("stub", "stub", null));
            fId = id;
            fDirection = direction;
        }

    }

    private static class TmfEventMatchingStub extends TmfEventMatching {

        public TmfEventMatchingStub(Collection<@NonNull ITmfTrace> traces) {
            super(traces);
        }

        @Override
        public Table<ITmfTrace, IEventMatchingKey, DependencyEvent> getUnmatchedIn() {
            return super.getUnmatchedIn();
        }

        @Override
        public Table<ITmfTrace, IEventMatchingKey, DependencyEvent> getUnmatchedOut() {
            return super.getUnmatchedOut();
        }

    }

    /**
     * Initializing the traces
     */
    @Before
    public void init() {
        TmfTraceStub t1 = new TmfTraceStub();
        t1.init("t1");
        TmfTraceStub t2 = new TmfTraceStub();
        t2.init("t2");

        Collection<@NonNull ITmfTrace> traces = new LinkedList<>();
        traces.add(t1);
        traces.add(t2);
        fT1 = t1;
        fT2 = t2;
        fTraces = traces;
        TmfEventMatching.registerMatchObject(new StubEventMatching());
    }

    /**
     * Clean up
     */
    @After
    public void cleanup() {
        TmfTraceStub trace = fT1;
        if (trace != null) {
            trace.dispose();
        }
        trace = fT2;
        if (trace != null) {
            trace.dispose();
        }
    }

    /**
     * Test event matching where event matches only from the same hosts, no cleanup
     * is expected
     */
    @Test
    public void testHostToSelf() {
        // Test-specific data initialization
        int count = 1;
        int matchedKey1 = count++;
        int unmatchedKey = count++;
        int matchedKey3 = count++;

        Collection<@NonNull ITmfTrace> traces = fTraces;
        assertNotNull(traces);
        TmfEventMatchingStub matching = new TmfEventMatchingStub(traces);
        matching.initMatching();

        TmfTraceStub trace = fT1;
        assertNotNull(trace);

        // Add an unmatched cause
        matching.matchEvent(new MatchEventStub(trace, TmfTimestamp.fromNanos(1L), matchedKey1, Direction.CAUSE), trace, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedOut().row(trace).size());

        // Add the matching effect, the events should be removed
        matching.matchEvent(new MatchEventStub(trace, TmfTimestamp.fromNanos(3L), matchedKey1, Direction.EFFECT), trace, PROGRESS_MONITOR);
        assertEquals(0, matching.getUnmatchedOut().row(trace).size());

        // Add an unmatched effect
        matching.matchEvent(new MatchEventStub(trace, TmfTimestamp.fromNanos(5L), unmatchedKey, Direction.EFFECT), trace, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedIn().row(trace).size());
        assertEquals(0, matching.getUnmatchedOut().row(trace).size());

        // Add an unmatched cause
        matching.matchEvent(new MatchEventStub(trace, TmfTimestamp.fromNanos(7L), matchedKey3, Direction.CAUSE), trace, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedIn().row(trace).size());
        assertEquals(1, matching.getUnmatchedOut().row(trace).size());

        // Add a matched effect, the cause should be removed, but the unmatched one
        // should still be there
        matching.matchEvent(new MatchEventStub(trace, TmfTimestamp.fromNanos(7L), matchedKey3, Direction.EFFECT), trace, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedIn().row(trace).size());
        assertEquals(0, matching.getUnmatchedOut().row(trace).size());
    }

    /**
     * Test event matching from different hosts, there should be cleanup of event
     * data when matches occur
     */
    @Test
    public void testMultiHost() {
        // Test-specific data initialization
        int count = 1;
        int matchedKey1 = count++;
        int unmatchedKey1 = count++;
        int unmatchedKey2 = count++;
        int unmatchedKey3 = count++;
        int unmatchedKey4 = count++;
        int matchedKey2 = count++;

        Collection<@NonNull ITmfTrace> traces = fTraces;
        assertNotNull(traces);
        TmfEventMatchingStub matching = new TmfEventMatchingStub(traces);
        matching.initMatching();

        TmfTraceStub t1 = fT1;
        assertNotNull(t1);
        TmfTraceStub t2 = fT2;
        assertNotNull(t2);

        /*
         * Have t1 send and receive all its packets before any of t2, no match should be
         * made at this point
         */

        // Add an unmatched cause
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(1L), unmatchedKey1, Direction.CAUSE), t1, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedOut().row(t1).size());

        // Add an unmatched effect
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(3L), unmatchedKey2, Direction.EFFECT), t1, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedOut().row(t1).size());
        assertEquals(1, matching.getUnmatchedIn().row(t1).size());

        // Add another unmatched effect
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(5L), unmatchedKey3, Direction.EFFECT), t1, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedOut().row(t1).size());
        assertEquals(2, matching.getUnmatchedIn().row(t1).size());

        // Add a cause that will be matched later
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(7L), matchedKey1, Direction.CAUSE), t1, PROGRESS_MONITOR);
        assertEquals(2, matching.getUnmatchedOut().row(t1).size());
        assertEquals(2, matching.getUnmatchedIn().row(t1).size());

        // Add an effect that will be matched later
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(11L), matchedKey2, Direction.EFFECT), t1, PROGRESS_MONITOR);
        assertEquals(2, matching.getUnmatchedOut().row(t1).size());
        assertEquals(3, matching.getUnmatchedIn().row(t1).size());

        // Add an unmatched cause
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(13L), unmatchedKey4, Direction.CAUSE), t1, PROGRESS_MONITOR);
        assertEquals(3, matching.getUnmatchedOut().row(t1).size());
        assertEquals(3, matching.getUnmatchedIn().row(t1).size());

        /* Let t2 receive and send the packets */

        // Match the sent packet, it should cleanup the unmatched outgoing from t1,
        // except the last one
        matching.matchEvent(new MatchEventStub(t2, TmfTimestamp.fromNanos(20L), matchedKey1, Direction.EFFECT), t2, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedOut().row(t1).size());
        assertEquals(3, matching.getUnmatchedIn().row(t1).size());

        // Match the received packet, it should cleanup the unmatched incoming from t1
        matching.matchEvent(new MatchEventStub(t2, TmfTimestamp.fromNanos(21L), matchedKey2, Direction.CAUSE), t2, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedOut().row(t1).size());
        assertEquals(0, matching.getUnmatchedIn().row(t1).size());

    }

    /**
     * Test event matching from different hosts and with different key types, there
     * should be cleanup of event data when matches occur, but only for the appropriate key
     */
    @Test
    public void testMultiHostMultiKeyType() {
        // Test-specific data initialization
        int count = 1;
        int matchedKey1 = count++;
        int unmatchedKey1 = count++;
        int unmatchedKey2 = count++;
        int unmatchedKey3 = count++;
        int unmatchedKey4 = count++;
        int unmatchedStringKey1 = -count++;
        int matchedStringKey1 = -count++;
        int matchedKey2 = count++;

        Collection<@NonNull ITmfTrace> traces = fTraces;
        assertNotNull(traces);
        TmfEventMatchingStub matching = new TmfEventMatchingStub(traces);
        matching.initMatching();

        TmfTraceStub t1 = fT1;
        assertNotNull(t1);
        TmfTraceStub t2 = fT2;
        assertNotNull(t2);

        /*
         * Have t1 send and receive all its packets before any of t2, no match should be
         * made at this point
         */

        // Add an unmatched cause
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(1L), unmatchedKey1, Direction.CAUSE), t1, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedOut().row(t1).size());

        // Add an unmatched effect
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(3L), unmatchedKey2, Direction.EFFECT), t1, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedOut().row(t1).size());
        assertEquals(1, matching.getUnmatchedIn().row(t1).size());

        // Add another unmatched effect
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(5L), unmatchedKey3, Direction.EFFECT), t1, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedOut().row(t1).size());
        assertEquals(2, matching.getUnmatchedIn().row(t1).size());

        // Add a string key that will not be matched
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(6L), unmatchedStringKey1, Direction.CAUSE), t1, PROGRESS_MONITOR);
        assertEquals(2, matching.getUnmatchedOut().row(t1).size());
        assertEquals(2, matching.getUnmatchedIn().row(t1).size());

        // Add a cause that will be matched later
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(7L), matchedKey1, Direction.CAUSE), t1, PROGRESS_MONITOR);
        assertEquals(3, matching.getUnmatchedOut().row(t1).size());
        assertEquals(2, matching.getUnmatchedIn().row(t1).size());

        // Add an effect that will be matched later
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(11L), matchedKey2, Direction.EFFECT), t1, PROGRESS_MONITOR);
        assertEquals(3, matching.getUnmatchedOut().row(t1).size());
        assertEquals(3, matching.getUnmatchedIn().row(t1).size());

        // Add an unmatched cause
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(13L), unmatchedKey4, Direction.CAUSE), t1, PROGRESS_MONITOR);
        assertEquals(4, matching.getUnmatchedOut().row(t1).size());
        assertEquals(3, matching.getUnmatchedIn().row(t1).size());

        // Add a string key that will not be matched
        matching.matchEvent(new MatchEventStub(t1, TmfTimestamp.fromNanos(15L), matchedStringKey1, Direction.CAUSE), t1, PROGRESS_MONITOR);
        assertEquals(5, matching.getUnmatchedOut().row(t1).size());
        assertEquals(3, matching.getUnmatchedIn().row(t1).size());

        /* Let t2 receive and send the packets */

        // Match the sent packet, it should cleanup the unmatched outgoing from t1,
        // except the last one and the strings
        matching.matchEvent(new MatchEventStub(t2, TmfTimestamp.fromNanos(20L), matchedKey1, Direction.EFFECT), t2, PROGRESS_MONITOR);
        assertEquals(3, matching.getUnmatchedOut().row(t1).size());
        assertEquals(3, matching.getUnmatchedIn().row(t1).size());

        // Match the received packet, it should cleanup the unmatched incoming from t1
        matching.matchEvent(new MatchEventStub(t2, TmfTimestamp.fromNanos(21L), matchedKey2, Direction.CAUSE), t2, PROGRESS_MONITOR);
        assertEquals(3, matching.getUnmatchedOut().row(t1).size());
        assertEquals(0, matching.getUnmatchedIn().row(t1).size());

        // Match the received string key, it should cleanup the unmatched string key from t1
        matching.matchEvent(new MatchEventStub(t2, TmfTimestamp.fromNanos(22L), matchedStringKey1, Direction.EFFECT), t2, PROGRESS_MONITOR);
        assertEquals(1, matching.getUnmatchedOut().row(t1).size());
        assertEquals(0, matching.getUnmatchedIn().row(t1).size());

    }

}
