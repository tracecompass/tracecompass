/*******************************************************************************
 * Copyright (c) 2014, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.synchronization;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.tracecompass.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Test suite for time offset of traces.
 */
@SuppressWarnings("javadoc")
public class TimeOffsetTest {

    /** Time-out tests after 1 minute. */
    @Rule
    public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final TmfTestTrace TEST_TRACE = TmfTestTrace.A_TEST_10K;
    private static final String PROJECT = "Test Project";
    private static final String RESOURCE = "Test Resource";
    private static final long ONE_MS = 1000000L;
    private IFile fResource;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT);
        if (!project.exists()) {
            project.create(null);
        }
        project.open(null);
        fResource = project.getFile(RESOURCE);
        if (!fResource.exists()) {
            final InputStream source = new ByteArrayInputStream(new byte[0]);
            fResource.create(source, true, null);
        }
        fResource.setPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, fResource.getParent().getLocation().toOSString());
    }

    @After
    public void tearDown() throws CoreException {
        if (fResource != null && fResource.exists()) {
            fResource.getProject().delete(true, null);
        }
    }

    private ITmfTrace createAndIndexTrace() throws TmfTraceException {
        TmfTraceStub trace = new TmfTraceStub(fResource, TEST_TRACE.getFullPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
        trace.indexTrace(true);
        return trace;
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Test
    public void testNoOffset() throws TmfTraceException {
        ITmfTrace trace = createAndIndexTrace();
        final TmfContext context = (TmfContext) trace.seekEvent(0);

        ITmfEvent event = trace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        event = trace.getNext(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());

        trace.dispose();
    }

    @Test
    public void testPositiveOffset() throws TmfTraceException {
        ITmfTimestampTransform tt = TimestampTransformFactory.createWithOffset(ONE_MS);
        TimestampTransformFactory.setTimestampTransform(fResource, tt);

        ITmfTrace trace = createAndIndexTrace();
        final TmfContext context = (TmfContext) trace.seekEvent(0);

        ITmfEvent event = trace.getNext(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        event = trace.getNext(context);
        assertEquals("Event timestamp", 3, event.getTimestamp().getValue());

        trace.dispose();
    }

    @Test
    public void testNegativeOffset() throws TmfTraceException {
        ITmfTimestampTransform tt = TimestampTransformFactory.createWithOffset(-ONE_MS);
        TimestampTransformFactory.setTimestampTransform(fResource, tt);

        ITmfTrace trace = createAndIndexTrace();
        final TmfContext context = (TmfContext) trace.seekEvent(0);

        ITmfEvent event = trace.getNext(context);
        assertEquals("Event timestamp", 0, event.getTimestamp().getValue());
        event = trace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        trace.dispose();
    }

    @Test
    public void testClearOffset() throws TmfTraceException {
        ITmfTimestampTransform tt = TimestampTransformFactory.createWithOffset(ONE_MS);
        TimestampTransformFactory.setTimestampTransform(fResource, tt);
        TimestampTransformFactory.setTimestampTransform(fResource, null);

        ITmfTrace trace = createAndIndexTrace();
        final TmfContext context = (TmfContext) trace.seekEvent(0);

        ITmfEvent event = trace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        event = trace.getNext(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());

        trace.dispose();
    }
}
