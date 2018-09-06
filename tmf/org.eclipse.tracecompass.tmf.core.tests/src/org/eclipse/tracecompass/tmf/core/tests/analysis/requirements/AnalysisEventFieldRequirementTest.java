/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.analysis.requirements;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement.PriorityLevel;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisEventFieldRequirement;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceWithPreDefinedEvents;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test the {@link TmfAnalysisEventFieldRequirement} class
 *
 * @author Bernd Hufmann
 */
public class AnalysisEventFieldRequirementTest {

    private static final @NonNull String EVENT1 = "abc";
    private static final @NonNull String EVENT2 = "def";
    private static final @NonNull String EVENT3 = "ghi";

    private static final @NonNull String EVENT1_FIELD1 = "mno";
    private static final @NonNull String EVENT1_FIELD2 = "pqr";

    private static final @NonNull String EVENT2_FIELD1 = "stu";
    private static final @NonNull String EVENT2_FIELD2 = "vwx";
    private static final @NonNull String EVENT_FIELD = "bla";

    /* A trace class with pre-defined events */
    private static class TraceWithEvents extends TmfTraceStub implements ITmfTraceWithPreDefinedEvents {

        @Override
        public @NonNull Set<? extends @NonNull ITmfEventType> getContainedEventTypes() {
            return ImmutableSet.of(
                    new ITmfEventType() {

                        @Override
                        public @NonNull String getName() {
                            return EVENT1;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2);
                        }
                    },
                    new ITmfEventType() {
                        @Override
                        public @NonNull String getName() {
                            return EVENT2;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2);
                        }
                    },
                    new ITmfEventType() {
                        @Override
                        public @NonNull String getName() {
                            return EVENT2;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(EVENT2_FIELD1, EVENT2_FIELD2);
                        }
                    });
        }

    }

    private final @NonNull TmfTrace trace = new TraceWithEvents();

    /**
     * Clean up
     */
    @After
    public void cleanup() {
        trace.dispose();
    }

    /**
     * Test with optional requirements
     */
    @Test
    public void testOptionalRequirements() {
        /* Test optional requirement */
        TmfAbstractAnalysisRequirement req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1));
        assertTrue(req.test(trace));

        req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2));
        assertTrue(req.test(trace));

        req = new TmfAnalysisEventFieldRequirement("", ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2));
        assertTrue(req.test(trace));

        req = new TmfAnalysisEventFieldRequirement(EVENT1, checkNotNull(Collections.emptyList()));
        assertTrue(req.test(trace));

        req = new TmfAnalysisEventFieldRequirement("", checkNotNull(Collections.emptyList()));
        assertTrue(req.test(trace));
    }

    /**
     * Test with mandatory requirements
     */
    @Test
    public void testMandatoryRequirements() {
        /* Test mandatory requirement */
        TmfAbstractAnalysisRequirement req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1), PriorityLevel.MANDATORY);
        assertTrue(req.test(trace));

        req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2), PriorityLevel.MANDATORY);
        assertTrue(req.test(trace));

        /* EVENT3 is not part of the trace. Test case that one of the events is part of the trace */
        req = new TmfAnalysisEventFieldRequirement(EVENT3, ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2), PriorityLevel.MANDATORY);
        assertFalse(req.test(trace));

        /* EVENT_FIELD is not an event field of the trace */
        req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2, EVENT_FIELD), PriorityLevel.MANDATORY);
        assertFalse(req.test(trace));

        /* Test case that all events need to have the given fields */
        req = new TmfAnalysisEventFieldRequirement("", ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2), PriorityLevel.MANDATORY);
        assertTrue(req.test(trace));

        /* Test case that all events need to have the given fields */
        req = new TmfAnalysisEventFieldRequirement("", ImmutableSet.of(EVENT2_FIELD1), PriorityLevel.MANDATORY);
        assertFalse(req.test(trace));

        /* Test case that empty list of event fields behaves like Event Requirements */
        req = new TmfAnalysisEventFieldRequirement(EVENT1, checkNotNull(Collections.emptyList()));
        assertTrue(req.test(trace));
    }

    /**
     * Test with {@link PriorityLevel#AT_LEAST_ONE} requirements
     */
    @Test
    public void testAtLeastOneRequirements() {
        /* Test at least one requirement */
        TmfAbstractAnalysisRequirement req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1), PriorityLevel.AT_LEAST_ONE);
        assertTrue(req.test(trace));

        req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2), PriorityLevel.AT_LEAST_ONE);
        assertTrue(req.test(trace));

        /* EVENT3 is not part of the trace. Test case that the event is part of the trace */
        req = new TmfAnalysisEventFieldRequirement(EVENT3, ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2), PriorityLevel.AT_LEAST_ONE);
        assertFalse(req.test(trace));

        /* EVENT_FIELD is not an event field of the trace */
        req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2, EVENT_FIELD), PriorityLevel.AT_LEAST_ONE);
        assertTrue(req.test(trace));

        /* Test case that all events need to have at least one of the given fields */
        req = new TmfAnalysisEventFieldRequirement("", ImmutableSet.of(EVENT1_FIELD1, EVENT2_FIELD2), PriorityLevel.AT_LEAST_ONE);
        assertTrue(req.test(trace));

        /* Test case that all events need to have the given fields */
        req = new TmfAnalysisEventFieldRequirement("", ImmutableSet.of(EVENT2_FIELD1), PriorityLevel.AT_LEAST_ONE);
        assertFalse(req.test(trace));

        /* Test case that empty list of event fields behaves like Event Requirements */
        req = new TmfAnalysisEventFieldRequirement(EVENT1, checkNotNull(Collections.emptyList()), PriorityLevel.AT_LEAST_ONE);
        assertTrue(req.test(trace));
    }

    /**
     * Test with {@link PriorityLevel#ALL_OR_NOTHING} requirements
     */
    @Test
    public void testAllOrNothingRequirements() {
        /* Test at least one requirement */
        TmfAbstractAnalysisRequirement req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1), PriorityLevel.ALL_OR_NOTHING);
        assertTrue(req.test(trace));

        req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2), PriorityLevel.ALL_OR_NOTHING);
        assertTrue(req.test(trace));

        /* EVENT3 is not part of the trace. Test case that the event is part of the trace */
        req = new TmfAnalysisEventFieldRequirement(EVENT3, ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2), PriorityLevel.ALL_OR_NOTHING);
        assertTrue(req.test(trace));

        /* EVENT_FIELD is not an event field of the trace */
        req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1, EVENT1_FIELD2, EVENT_FIELD), PriorityLevel.ALL_OR_NOTHING);
        assertFalse(req.test(trace));

        /* Test case that all events need to have either all or none of the given fields */
        req = new TmfAnalysisEventFieldRequirement("", ImmutableSet.of(EVENT1_FIELD1, EVENT2_FIELD2), PriorityLevel.ALL_OR_NOTHING);
        assertFalse(req.test(trace));

        /* Test case that all events need to have the given fields */
        req = new TmfAnalysisEventFieldRequirement("", ImmutableSet.of(EVENT1_FIELD1), PriorityLevel.ALL_OR_NOTHING);
        assertTrue(req.test(trace));

        /* Test case that empty list of event fields behaves like Event Requirements */
        req = new TmfAnalysisEventFieldRequirement(EVENT1, checkNotNull(Collections.emptyList()), PriorityLevel.ALL_OR_NOTHING);
        assertTrue(req.test(trace));
    }

    /**
     * Test event requirements on a trace with no pre-defined events. They
     * should all pass
     */
    @Test
    public void testNoPreDefinedEvents() {
        /* A simple trace with no pre-defined events */
        TmfTrace traceNoEvents = new TmfTraceStub();

        TmfAbstractAnalysisRequirement req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1), PriorityLevel.MANDATORY);
        assertTrue(req.test(traceNoEvents));

        req = new TmfAnalysisEventFieldRequirement(EVENT1, ImmutableSet.of(EVENT1_FIELD1), PriorityLevel.OPTIONAL);
        assertTrue(req.test(traceNoEvents));

        traceNoEvents.dispose();
    }

}
