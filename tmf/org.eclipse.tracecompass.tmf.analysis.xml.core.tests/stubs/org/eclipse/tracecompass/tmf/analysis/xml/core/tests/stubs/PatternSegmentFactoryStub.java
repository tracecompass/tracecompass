/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stubs;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.segment.TmfXmlPatternSegment;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;

import com.google.common.collect.ImmutableMap;

/**
 * Factory generating various pattern segments and data used for tests
 *
 * @author Jean-Christian Kouame
 *
 */
public class PatternSegmentFactoryStub {

    private static final @NonNull String NAME_FIELD_1 = "field1";
    private static final @NonNull String NAME_FIELD_2 = "field2";
    private static final @NonNull String NAME_FIELD_3 = "field3";
    /**
     * The content for the segment TEST_2
     */
    private static final @NonNull Map<@NonNull String, @NonNull Object> TEST_2_CONTENT = ImmutableMap
            .of(NAME_FIELD_1, 5l,
            NAME_FIELD_2, "test",
            NAME_FIELD_3, 1);

    /**
     * Start event for pattern segment TEST_2
     */
    public static final @NonNull ITmfEvent TEST_2_START_EVENT = new TmfEvent(null, ITmfContext.UNKNOWN_RANK, TmfTimestamp.fromNanos(1), null, null);
    /**
     * end event for pattern segment TEST_2
     */
    public static final @NonNull ITmfEvent TEST_2_END_EVENT = new TmfEvent(null, ITmfContext.UNKNOWN_RANK, TmfTimestamp.fromNanos(10), null, null);

    /**
     * The pattern segment TEST_2
     */
    public static final @NonNull TmfXmlPatternSegment TEST_2 = new TmfXmlPatternSegment(TEST_2_START_EVENT.getTimestamp().getValue(), TEST_2_END_EVENT.getTimestamp().getValue(), "test2", TEST_2_CONTENT);

    /**
     * The pattern segment TEST_3
     */
    public static final @NonNull TmfXmlPatternSegment TEST_3 = new TmfXmlPatternSegment(TEST_2_START_EVENT.getTimestamp().getValue(), TEST_2_START_EVENT.getTimestamp().getValue(), "open", Collections.emptyMap());
}
