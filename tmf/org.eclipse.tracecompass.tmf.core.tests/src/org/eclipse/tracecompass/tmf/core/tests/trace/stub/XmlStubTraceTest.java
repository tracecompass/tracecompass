/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.trace.stub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStubNs;
import org.junit.After;
import org.junit.Test;

/**
 * Test suite for the {@link TmfXmlTraceStubNs} class
 *
 * @author Geneviève Bastien
 */
public class XmlStubTraceTest {

    private static final String VALID_FILE = "testfiles/stub_xml_traces/valid/test.xml";
    private static final String VALID_PATH = "testfiles/stub_xml_traces/valid";
    private static final String INVALID_PATH = "testfiles/stub_xml_traces/invalid";

    private static final String EVENT_A = "A";
    private static final String EVENT_B = "B";
    private static final String FIELD_A = "b";
    private static final String FIELD_B = "f";

    private ITmfTrace fTestTrace = null;

    /**
     * Tear down the test
     */
    @After
    public void tearDown() {
        ITmfTrace trace = fTestTrace;
        if (trace == null) {
            return;
        }
        fTestTrace = null;
        String directory = TmfTraceManager.getSupplementaryFileDir(trace);
        try {
            trace.dispose();
        } finally {
            File dir = new File(directory);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    file.delete();
                }
                dir.delete();
            }
        }
    }

    /**
     * Test the
     * {@link TmfXmlTraceStubNs#validate(org.eclipse.core.resources.IProject, String)}
     * method
     */
    @Test
    public void testValidate() {
        fTestTrace = new TmfXmlTraceStubNs();
        File[] invalidFiles = TmfCoreTestPlugin.getAbsoluteFilePath(INVALID_PATH).toFile().listFiles();
        assertTrue(invalidFiles.length > 0);
        for (File f : invalidFiles) {
            assertTrue(!fTestTrace.validate(null, f.getAbsolutePath()).isOK());
        }

        File[] validFiles = TmfCoreTestPlugin.getAbsoluteFilePath(VALID_PATH).toFile().listFiles();
        assertTrue(validFiles.length > 0);
        for (File f : validFiles) {
            assertTrue(fTestTrace.validate(null, f.getAbsolutePath()).isOK());
        }
    }

    /**
     * Test the reading and querying the XML trace and make sure fields are
     * present
     */
    @Test
    public void testDevelopmentTrace() {
        fTestTrace = TmfXmlTraceStubNs.setupTrace(TmfCoreTestPlugin.getAbsoluteFilePath(VALID_FILE));

        CustomEventRequest req = new CustomEventRequest(fTestTrace);
        fTestTrace.sendRequest(req);
        try {
            req.waitForCompletion();
            if (req.isCancelled()) {
                fail(req.getStatus().getMessage());
            }
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
        assertEquals(4, req.getCount());
    }

    /**
     * Test the presence and resolve of the aspects for this trace
     */
    @Test
    public void testAspects() {
        fTestTrace = TmfXmlTraceStubNs.setupTrace(TmfCoreTestPlugin.getAbsoluteFilePath(VALID_FILE));

        ITmfEventAspect<?> cpuAspect = null;
        ITmfEventAspect<?> testAspect = null;
        int aspectCount = 0;
        for (ITmfEventAspect<?> aspect : fTestTrace.getEventAspects()) {
            aspectCount++;
            if (aspect instanceof TmfCpuAspect) {
                cpuAspect = aspect;
            } else if (aspect.getName().equals("test")) {
                testAspect = aspect;
            }
        }
        /* Check the presence of the cpu and test aspects */
        assertEquals("Number of aspects", 5, aspectCount);
        assertNotNull(cpuAspect);
        assertNotNull(testAspect);

        ITmfContext ctx;
        ctx = fTestTrace.seekEvent(0L);
        assertNotNull(ctx);
        ITmfEvent event = fTestTrace.getNext(ctx);
        assertNotNull(event);
        assertEquals("Cpu aspect of event 1", 1, cpuAspect.resolve(event));
        assertEquals("Test aspect of event 1", "abc", testAspect.resolve(event));
        event = fTestTrace.getNext(ctx);
        assertNotNull(event);
        assertEquals("Cpu aspect of event 2", 1, cpuAspect.resolve(event));
        assertEquals("Test aspect of event 2", "abc", testAspect.resolve(event));
        event = fTestTrace.getNext(ctx);
        assertNotNull(event);
        assertEquals("Cpu aspect of event 3", 2, cpuAspect.resolve(event));
        assertEquals("Test aspect of event 3", "def", testAspect.resolve(event));
        event = fTestTrace.getNext(ctx);
        assertNotNull(event);
        assertEquals("Cpu aspect of event 4", 1, cpuAspect.resolve(event));
        assertEquals("Test aspect of event 4", "def", testAspect.resolve(event));

        ctx.dispose();
    }

    private static IStatus testEvent(ITmfEvent event) {
        switch (event.getName()) {
        case EVENT_A: {
            ITmfEventField content = event.getContent();
            if (content.getField(FIELD_A) == null) {
                return new Status(IStatus.ERROR, TmfCoreTestPlugin.PLUGIN_ID, String.format("Field %s does not exist in event %s", FIELD_A, EVENT_A));
            }
            break;
        }
        case EVENT_B: {
            ITmfEventField content = event.getContent();
            if (content.getField(FIELD_B) == null) {
                return new Status(IStatus.ERROR, TmfCoreTestPlugin.PLUGIN_ID, String.format("Field %s does not exist in event %s", FIELD_B, EVENT_B));
            }
            break;
        }
        default:
            return new Status(IStatus.ERROR, TmfCoreTestPlugin.PLUGIN_ID, "Unexpected event " + event.getType().getName());
        }
        return Status.OK_STATUS;
    }

    private class CustomEventRequest extends TmfEventRequest {
        private final ITmfTrace fTrace;
        private IStatus fResult = Status.OK_STATUS;
        private int fCount = 0;

        public CustomEventRequest(ITmfTrace trace) {
            super(trace.getEventType(),
                    TmfTimeRange.ETERNITY,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);
            this.fTrace = trace;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event.getTrace() == fTrace) {
                fCount++;
                IStatus result = testEvent(event);
                if (!result.isOK()) {
                    fResult = result;
                    this.cancel();
                }
            }
        }

        public IStatus getStatus() {
            return fResult;
        }

        public int getCount() {
            return fCount;
        }

    }
}
