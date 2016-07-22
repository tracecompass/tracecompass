/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.callstack;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.ust.core.callstack.LttngUstCallStackAnalysisRequirement;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
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

    private static final @NonNull Collection<String> validFields = ImmutableSet.of(CONTEXT_VTID, CONTEXT_PROCNAME);
    private static final @NonNull Collection<String> badFields = ImmutableSet.of(CONTEXT_OTHER, CONTEXT_PROCNAME);

    enum EventType {
        EVT_EXIT_FAST           (FUNC_EXIT_FAST, validFields),
        EVT_EXIT                (FUNC_EXIT, validFields),
        EVT_ENTRY_FAST          (FUNC_ENTRY_FAST, validFields),
        EVT_ENTRY               (FUNC_ENTRY, validFields),
        EVT_OTHER               (OTHER_EVENT, validFields),
        EVT_ENTRY_BAD_FIELDS    (FUNC_ENTRY, badFields),
        EVT_ENTRY_FAST_BAD_FIELDS     (FUNC_ENTRY_FAST, badFields),
        EVT_ENTRY_FAST_EMPTY_FIELDS   (FUNC_ENTRY_FAST, ImmutableSet.of());

        private final @NonNull CtfTmfEventType fType;

        EventType(@NonNull String name, @NonNull Collection<String> fields) {
            fType = new CtfTmfEventType(name, null) {
                @Override
                public String getName() {
                    return name;
                }
                @Override
                public @NonNull Collection<String> getFieldNames() {
                    return fields;
                }
            };
        }

        @NonNull CtfTmfEventType getEventType() {
            return fType;
        }
    }

    enum TestData {
        TRACE_WITH_VALID_EVENTS(EventType.EVT_ENTRY, EventType.EVT_EXIT, true),
        TRACE_WITH_VALID_EVENTS_FAST(EventType.EVT_ENTRY_FAST, EventType.EVT_EXIT_FAST, true),
        TRACE_WITH_VALID_EVENTS_MISSING_FIELDS(EventType.EVT_ENTRY_BAD_FIELDS,
                EventType.EVT_EXIT, false),
        TRACE_WITH_VALID_EVENTS_MISSING_FIELDS_FAST(EventType.EVT_ENTRY_FAST_BAD_FIELDS,
                    EventType.EVT_EXIT_FAST, false),
        TRACE_WITH_VALID_EVENTS_WRONG_FIELDS(EventType.EVT_ENTRY_FAST_EMPTY_FIELDS,
                EventType.EVT_EXIT_FAST, false),
        TRACE_WITH_MISSING_EVENTS(EventType.EVT_OTHER,
                    EventType.EVT_EXIT_FAST, false),
        TRACE_MISMATCH_EVENTS(EventType.EVT_ENTRY_FAST, EventType.EVT_EXIT, false);

        private final @NonNull LttngUstTrace fTrace;
        private final boolean fIsValid;

        TestData(EventType first, EventType second, boolean isValid) {
            fTrace = new LttngUstTrace() {
                @Override
                public Set<CtfTmfEventType> getContainedEventTypes() {
                    return ImmutableSet.of(first.getEventType(), second.getEventType());
                }
            };
            fIsValid = isValid;
        }

        @NonNull LttngUstTrace getTrace() {
            return fTrace;
        }

        boolean isValid() {
            return fIsValid;
        }

    }

    /**
     * Test Call Stack Analysis requirements
     */
    @Test
    public void testCallStackRequirements() {
        LttngUstCallStackAnalysisRequirement req = new LttngUstCallStackAnalysisRequirement(ILttngUstEventLayout.DEFAULT_LAYOUT);
        for (TestData item: TestData.values()) {
            assertEquals(item.name(), item.isValid(), req.test(item.getTrace()));
        }
    }
}
