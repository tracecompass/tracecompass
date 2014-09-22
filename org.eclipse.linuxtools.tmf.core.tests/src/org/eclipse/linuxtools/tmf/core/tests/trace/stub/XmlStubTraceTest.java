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

package org.eclipse.linuxtools.tmf.core.tests.trace.stub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.junit.Test;

/**
 * Test suite for the {@link TmfXmlTraceStub} class
 *
 * @author Geneviève Bastien
 */
public class XmlStubTraceTest {

    private static final Path VALID_FILE = new Path("testfiles/stub_xml_traces/valid/test.xml");
    private static final Path VALID_PATH = new Path("testfiles/stub_xml_traces/valid");
    private static final Path INVALID_PATH = new Path("testfiles/stub_xml_traces/invalid");

    private static final String EVENT_A = "A";
    private static final String EVENT_B = "B";
    private static final String FIELD_A = "b";
    private static final String FIELD_B = "f";

    private static IPath getAbsolutePath(Path relativePath) {
        Plugin plugin = TmfCoreTestPlugin.getDefault();
        if (plugin == null) {
            /*
             * Shouldn't happen but at least throw something to get the test to
             * fail early
             */
            throw new IllegalStateException();
        }
        URL location = FileLocator.find(plugin.getBundle(), relativePath, null);
        try {
            IPath path = new Path(FileLocator.toFileURL(location).getPath());
            return path;
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    /**
     * Test the
     * {@link TmfXmlTraceStub#validate(org.eclipse.core.resources.IProject, String)}
     * method
     */
    @Test
    public void testValidate() {
        TmfXmlTraceStub trace = new TmfXmlTraceStub();
        File[] invalidFiles = getAbsolutePath(INVALID_PATH).toFile().listFiles();
        assertTrue(invalidFiles.length > 0);
        for (File f : invalidFiles) {
            assertTrue(!trace.validate(null, f.getAbsolutePath()).isOK());
        }

        File[] validFiles = getAbsolutePath(VALID_PATH).toFile().listFiles();
        assertTrue(validFiles.length > 0);
        for (File f : validFiles) {
            assertTrue(trace.validate(null, f.getAbsolutePath()).isOK());
        }
    }

    /**
     * Test the reading and querying the XML trace and make sure fields are
     * present
     */
    @Test
    public void testDevelopmentTrace() {
        TmfXmlTraceStub trace = new TmfXmlTraceStub();
        IStatus status = trace.validate(null, getAbsolutePath(VALID_FILE).toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }

        try {
            trace.initTrace(null, getAbsolutePath(VALID_FILE).toOSString(), TmfEvent.class);
        } catch (TmfTraceException e1) {
            fail(e1.getMessage());
        }

        CustomEventRequest req = new CustomEventRequest(trace);
        trace.sendRequest(req);
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

    private static IStatus testEvent(ITmfEvent event) {
        switch (event.getType().getName()) {
        case EVENT_A: {
            ITmfEventField content = event.getContent();
            if (!event.getSource().equals("1")) {
                return new Status(IStatus.ERROR, TmfCoreTestPlugin.PLUGIN_ID, "Events of type A should have source 1 but was " + event.getSource());
            }
            if (content.getField(FIELD_A) == null) {
                return new Status(IStatus.ERROR, TmfCoreTestPlugin.PLUGIN_ID, String.format("Field %s does not exist in event %s", FIELD_A, EVENT_A));
            }
            break;
        }
        case EVENT_B: {
            ITmfEventField content = event.getContent();
            if (!event.getSource().equals("2")) {
                return new Status(IStatus.ERROR, TmfCoreTestPlugin.PLUGIN_ID, "Events of type B should have source 2 but was " + event.getSource());
            }
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
