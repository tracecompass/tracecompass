/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.StubEventMatching;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.IMatchProcessingUnit;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.junit.Test;

/**
 * Test event matching for kernel traces
 *
 * @author Geneviève Bastien
 */
public class EventMatchingTest {

    private static @NonNull ITmfTrace getKernelXmlTrace(String traceFile) throws TmfTraceException {
        TmfXmlKernelTraceStub trace = new TmfXmlKernelTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(traceFile);
        IStatus status = trace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        trace.initTrace(null, filePath.toOSString(), ITmfEvent.class);
        return trace;
    }

    private static class TestMatchPu implements IMatchProcessingUnit {

        private List<TmfEventDependency> fMatches = new ArrayList<>();

        @Override
        public void init(@NonNull Collection<@NonNull ITmfTrace> fTraces) {

        }

        @Override
        public void addMatch(@NonNull TmfEventDependency match) {
            fMatches.add(match);
        }

        @Override
        public void matchingEnded() {

        }

        @Override
        public int countMatches() {
            return fMatches.size();
        }

        public List<TmfEventDependency> getMatches() {
            return fMatches;
        }

    }

    /**
     * Testing the packet matching
     *
     * @throws TmfTraceException
     *             Exception thrown when initializing trace
     */
    @Test
    public void testMatching() throws TmfTraceException {
        String trace1File = "client.xml";
        String trace2File = "server.xml";
        testEventMatching(trace1File, trace2File);
    }

    private static void validateMatch(TmfEventDependency match, String source, String destination, long sourceTs, long destTs) {
        String title = source + '(' + sourceTs + ") -> " + destination + '(' + destTs + ')';
        assertNotNull(match);
        assertEquals("Source: " + title, source, match.getSource().getTrace().getHostId());
        assertEquals("Source ts: " + title, sourceTs, match.getSource().getTimestamp().getValue());
        assertEquals("Destination: " + title, destination, match.getDestination().getTrace().getHostId());
        assertEquals("Destination ts: " + title, destTs, match.getDestination().getTimestamp().getValue());
    }

    /**
     * Testing the packet matching when multiple matches apply to a same event.
     *
     * @throws TmfTraceException
     *             Exception thrown when initializing trace
     */
    @Test
    public void testMatchingMultiMatchers() throws TmfTraceException {
        String trace1File = "client.xml";
        // This trace has another field name for matching, so StubEventMatching
        // should not be able to match these events
        String trace2File = "server2.xml";

        // Register a second event matching class for the other field
        TmfEventMatching.registerMatchObject(new StubEventMatching() {

            @Override
            public @Nullable IEventMatchingKey getEventKey(@Nullable ITmfEvent event) {
                if (event == null) {
                    return null;
                }
                Integer fieldValue = event.getContent().getFieldValue(Integer.class, "otherMsgField");
                if (fieldValue == null) {
                    return null;
                }
                return new StubEventKey(fieldValue);
            }

        });

        testEventMatching(trace1File, trace2File);
    }

    private void testEventMatching(String trace1File, String trace2File) throws TmfTraceException {
        ITmfTrace trace1 = null;
        ITmfTrace trace2 = null;
        TmfExperiment experiment = null;

        try {
            trace1 = getKernelXmlTrace("testfiles/network/" + trace1File);
            trace2 = getKernelXmlTrace("testfiles/network/" + trace2File);

            ITmfTrace[] traces = { trace1, trace2 };

            experiment = new TmfExperiment(ITmfEvent.class, "experiment", traces, 1000, null);
            experiment.traceOpened(new TmfTraceOpenedSignal(this, experiment, null));

            TestMatchPu testMatchPu = new TestMatchPu();
            TmfEventMatching twoTraceMatch = new TmfEventMatching(Collections.singleton(experiment), testMatchPu);
            assertTrue(twoTraceMatch.matchEvents());

            List<TmfEventDependency> matches = testMatchPu.getMatches();
            assertEquals(4, matches.size());

            // Test the 3 matches
            validateMatch(matches.get(0), trace1File, trace1File, 15, 20);
            validateMatch(matches.get(1), trace1File, trace2File, 30, 50);
            validateMatch(matches.get(2), trace2File, trace1File, 70, 100);
            validateMatch(matches.get(3), trace1File, trace1File, 105, 115);

        } finally {
            if (experiment != null) {
                experiment.dispose();
            }
            if (trace1 != null) {
                trace1.dispose();
            }
            if (trace2 != null) {
                trace2.dispose();
            }
        }
    }

}
