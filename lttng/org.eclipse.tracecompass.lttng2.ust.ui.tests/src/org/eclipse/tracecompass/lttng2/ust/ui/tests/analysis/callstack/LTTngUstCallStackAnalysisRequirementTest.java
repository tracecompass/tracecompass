/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.ui.tests.analysis.callstack;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.ust.ui.analysis.callstack.LttngUstCallStackAnalysisRequirement;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventType;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test the {@link LttngUstCallStackAnalysisRequirement} class
 *
 * @author Bernd Hufmann
 */
public class LTTngUstCallStackAnalysisRequirementTest {

    private static final @NonNull String FUNC_EXIT_FAST = "lttng_ust_cyg_profile_fast:func_exit";
    private static final @NonNull String FUNC_EXIT = "lttng_ust_cyg_profile:func_exit";
    private static final @NonNull String FUNC_ENTRY_FAST = "lttng_ust_cyg_profile_fast:func_entry";
    private static final @NonNull String FUNC_ENTRY = "lttng_ust_cyg_profile:func_entry";
    private static final @NonNull String OTHER_EVENT = "OTHER";

    private static final @NonNull String CONTEXT_VTID = "context._vtid";
    private static final @NonNull String CONTEXT_PROCNAME = "context._procname";
    private static final @NonNull String CONTEXT_OTHER = "context._other";

    /* A trace class with pre-defined events with valid events and fields */
    private static class TraceWithValidEvents extends LttngUstTrace {
        @Override
        public @NonNull Set<@NonNull CtfTmfEventType> getContainedEventTypes() {
            return ImmutableSet.of(
                    new CtfTmfEventType(FUNC_ENTRY, null) {

                        @Override
                        public @NonNull String getName() {
                            return FUNC_ENTRY;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(CONTEXT_VTID, CONTEXT_PROCNAME);
                        }
                    },
                    new CtfTmfEventType(FUNC_EXIT, null) {
                        @Override
                        public @NonNull String getName() {
                            return FUNC_EXIT;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(CONTEXT_VTID, CONTEXT_PROCNAME);
                        }
                    });
        }
    }

    /* A trace class with pre-defined events with valid events and fields */
    private static class TraceWithValidEventsFast extends LttngUstTrace {
        @Override
        public @NonNull Set<@NonNull CtfTmfEventType> getContainedEventTypes() {
            return ImmutableSet.of(
                    new CtfTmfEventType(FUNC_ENTRY_FAST, null) {

                        @Override
                        public @NonNull String getName() {
                            return FUNC_ENTRY_FAST;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(CONTEXT_VTID, CONTEXT_PROCNAME);
                        }
                    },
                    new CtfTmfEventType(FUNC_EXIT_FAST, null) {
                        @Override
                        public @NonNull String getName() {
                            return FUNC_EXIT_FAST;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(CONTEXT_VTID, CONTEXT_PROCNAME);
                        }
                    });
        }
    }

    /*
     * A trace class with pre-defined events with valid events but missing
     * fields
     */
    private static class TraceWithValidEventsMissingFields extends LttngUstTrace {
        @Override
        public @NonNull Set<@NonNull CtfTmfEventType> getContainedEventTypes() {
            return ImmutableSet.of(
                    new CtfTmfEventType(FUNC_ENTRY, null) {
                        @Override
                        public @NonNull String getName() {
                            return FUNC_ENTRY;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return Collections.EMPTY_LIST;
                        }
                    },
                    new CtfTmfEventType(FUNC_EXIT, null) {
                        @Override
                        public @NonNull String getName() {
                            return FUNC_EXIT;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(CONTEXT_VTID, CONTEXT_PROCNAME);
                        }
                    });
        }
    }

    /*
     * A trace class with pre-defined events with valid events but missing
     * fields
     */
    private static class TraceWithValidEventsMissingFieldsFast extends LttngUstTrace {
        @Override
        public @NonNull Set<@NonNull CtfTmfEventType> getContainedEventTypes() {
            return ImmutableSet.of(
                    new CtfTmfEventType(FUNC_ENTRY_FAST, null) {
                        @Override
                        public @NonNull String getName() {
                            return FUNC_ENTRY_FAST;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(CONTEXT_VTID, CONTEXT_PROCNAME);
                        }
                    },
                    new CtfTmfEventType(FUNC_EXIT_FAST, null) {
                        @Override
                        public @NonNull String getName() {
                            return FUNC_EXIT_FAST;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return Collections.EMPTY_LIST;
                        }
                    });
        }
    }

    /*
     * A trace class with pre-defined events with valid events but missing
     * fields and other fields
     */
    private static class TraceWithValidEventsWrongFields extends LttngUstTrace {
        @Override
        public @NonNull Set<@NonNull CtfTmfEventType> getContainedEventTypes() {
            return ImmutableSet.of(
                    new CtfTmfEventType(FUNC_ENTRY, null) {
                        @Override
                        public @NonNull String getName() {
                            return FUNC_ENTRY;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(CONTEXT_OTHER, CONTEXT_PROCNAME);
                        }
                    },
                    new CtfTmfEventType(FUNC_EXIT, null) {
                        @Override
                        public @NonNull String getName() {
                            return FUNC_EXIT;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(CONTEXT_OTHER, CONTEXT_PROCNAME);
                        }
                    });
        }
    }

    /* A trace class with pre-defined events with missing events */
    private static class TraceWithMissingEvents extends LttngUstTrace {
        @Override
        public @NonNull Set<@NonNull CtfTmfEventType> getContainedEventTypes() {
            return ImmutableSet.of(
                    new CtfTmfEventType(OTHER_EVENT, null) {
                        @Override
                        public @NonNull String getName() {
                            return OTHER_EVENT;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(CONTEXT_VTID, CONTEXT_PROCNAME);
                        }
                    },
                    new CtfTmfEventType(FUNC_EXIT_FAST, null) {
                        @Override
                        public @NonNull String getName() {
                            return FUNC_EXIT_FAST;
                        }

                        @Override
                        public ITmfEventField getRootField() {
                            return null;
                        }

                        @Override
                        public Collection<String> getFieldNames() {
                            return ImmutableSet.of(CONTEXT_VTID, CONTEXT_PROCNAME);
                        }
                    });
        }
    }

    private final @NonNull TmfTrace traceValid = new TraceWithValidEvents();
    private final @NonNull TmfTrace traceValidFast = new TraceWithValidEventsFast();
    private final @NonNull TmfTrace traceValidMissingFields = new TraceWithValidEventsMissingFields();
    private final @NonNull TmfTrace traceValidMissingFiledsFast = new TraceWithValidEventsMissingFieldsFast();
    private final @NonNull TmfTrace traceValidEventsWrongFields = new TraceWithValidEventsWrongFields();
    private final @NonNull TmfTrace traceMissingEvents = new TraceWithMissingEvents();

    /**
     * Test with optional requirements
     */
    @Test
    public void testCallStackRequirements() {
        /* Test optional requirement */
        LttngUstCallStackAnalysisRequirement req = new LttngUstCallStackAnalysisRequirement(ILttngUstEventLayout.DEFAULT_LAYOUT);
        assertTrue(req.test(traceValid));
        assertTrue(req.test(traceValidFast));
        assertFalse(req.test(traceValidMissingFields));
        assertFalse(req.test(traceValidMissingFiledsFast));
        assertFalse(req.test(traceValidEventsWrongFields));
        assertFalse(req.test(traceMissingEvents));
    }
}
