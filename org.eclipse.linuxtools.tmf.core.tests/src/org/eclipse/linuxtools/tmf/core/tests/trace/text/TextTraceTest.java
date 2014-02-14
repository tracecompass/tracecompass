/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceEventContent;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.text.SyslogEvent;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.text.SyslogEventType.Index;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.text.SyslogTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings({ "nls", "javadoc"})
public class TextTraceTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final String NAME = "syslog";
    private static final String PATH = "testfiles/" + NAME;

    private static final String OTHER_PATH = "testfiles/" + "A-Test-10K";

    private static SyslogTrace fTrace = null;
    private static File fTestFile = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @BeforeClass
    public static void setUp() throws Exception {
        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.DATIME, "MMM d HH:mm:ss");
        defaultPreferences.put(ITmfTimePreferencesConstants.SUBSEC, ITmfTimePreferencesConstants.SUBSEC_NO_FMT);
        TmfTimestampFormat.updateDefaultFormats();

        if (fTrace == null) {
            try {
                URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(PATH), null);
                URI uri = FileLocator.toFileURL(location).toURI();
                fTestFile = new File(uri);

                fTrace = new SyslogTrace();
                IResource resource = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(PATH));
                fTrace.initTrace(resource, uri.getPath(), SyslogEvent.class);
                // Dummy request to force the trace indexing
                TmfEventRequest request = new TmfEventRequest(
                        SyslogEvent.class,
                        TmfTimeRange.ETERNITY,
                        0,
                        ITmfEventRequest.ALL_DATA,
                        ExecutionType.FOREGROUND) {
                };
                fTrace.sendRequest(request);
                request.waitForCompletion();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @AfterClass
    public static void tearDown() {
        fTrace.dispose();
        fTrace = null;
        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.DATIME, ITmfTimePreferencesConstants.TIME_HOUR_FMT);
        defaultPreferences.put(ITmfTimePreferencesConstants.SUBSEC, ITmfTimePreferencesConstants.SUBSEC_NANO_FMT);
        TmfTimestampFormat.updateDefaultFormats();
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    @Test
    public void testEmptyConstructor() {
        SyslogTrace trace = new SyslogTrace();
        assertEquals("getType",      null, trace.getType());
        assertEquals("getPath",      null, trace.getPath());
        assertEquals("getName",      "", trace.getName());
        assertEquals("getCacheSize", 100, trace.getCacheSize());

        TmfTimestamp initRange = new TmfTimestamp(60, ITmfTimestamp.SECOND_SCALE);
        assertEquals("getInitialRangeOffset", initRange, trace.getInitialRangeOffset());
    }

    @Test
    public void testValidation() throws URISyntaxException, IOException {
        SyslogTrace trace = new SyslogTrace();
        String validTracePath = fTestFile.getAbsolutePath();
        IStatus status = trace.validate(null, validTracePath);
        assertTrue(status.isOK());
        assertTrue(status instanceof TraceValidationStatus);
        assertEquals(100, ((TraceValidationStatus) status).getConfidence());

        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(OTHER_PATH), null);
        URI uri = FileLocator.toFileURL(location).toURI();
        File otherFile  = new File(uri);

        String validNoConfidenceTrace = otherFile.getAbsolutePath();
        status = trace.validate(null, validNoConfidenceTrace);
        assertTrue(status instanceof TraceValidationStatus);
        assertEquals(0, ((TraceValidationStatus) status).getConfidence());
        assertTrue(status.isOK());

        String invalidTrace = fTestFile.getParentFile().getAbsolutePath();
        status = trace.validate(null, invalidTrace);
        assertFalse(status.isOK());
    }

    @Test
    public void testInitTrace() throws Exception {
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(PATH), null);
        String path = FileLocator.toFileURL(location).toURI().getPath();
        SyslogTrace trace = new SyslogTrace();
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(PATH));
        trace.initTrace(resource, path, SyslogEvent.class);
        assertEquals("getType",      SyslogEvent.class, trace.getType());
        assertEquals("getPath",      fTestFile.toURI().getPath(), trace.getPath());
        assertEquals("getName",      NAME, trace.getName());
        assertEquals("getCacheSize", 100, trace.getCacheSize());
    }

    // ------------------------------------------------------------------------
    // Indexing
    // ------------------------------------------------------------------------

    @Test
    public void testTraceIndexing() {
        assertEquals("getNbEvents", 6, fTrace.getNbEvents());

        TmfTimestamp initRange = new TmfTimestamp(60, ITmfTimestamp.SECOND_SCALE);
        assertEquals("getInitialRangeOffset", initRange, fTrace.getInitialRangeOffset());
    }

    // ------------------------------------------------------------------------
    // Parsing
    // ------------------------------------------------------------------------

    @Test
    public void testTraceParsing() {
        ITmfContext context = fTrace.seekEvent(0);
        SyslogEvent event = fTrace.getNext(context);
        TextTraceEventContent content = (TextTraceEventContent) event.getContent();
        assertEquals("getTimestamp", "Jan 1 01:01:01", event.getTimestamp().toString());
        assertEquals("getField:TIMESTAMP", "Jan 1 01:01:01", content.getFieldValue(Index.TIMESTAMP));
        assertEquals("getField:HOST", "HostA", content.getFieldValue(Index.HOST));
        assertEquals("getField:LOGGER", "LoggerA", content.getFieldValue(Index.LOGGER));
        assertEquals("getField:MESSAGE", "Message A", content.getFieldValue(Index.MESSAGE).toString());
        event = fTrace.getNext(context);
        content = (TextTraceEventContent) event.getContent();
        assertEquals("getTimestamp", "Jan 1 02:02:02", event.getTimestamp().toString());
        assertEquals("getField:TIMESTAMP", "Jan 1 02:02:02", content.getFieldValue(Index.TIMESTAMP));
        assertEquals("getField:HOST", "HostB", content.getFieldValue(Index.HOST));
        assertEquals("getField:LOGGER", "LoggerB", content.getFieldValue(Index.LOGGER));
        assertEquals("getField:MESSAGE", "Message B", content.getFieldValue(Index.MESSAGE).toString());
        event = fTrace.getNext(context);
        content = (TextTraceEventContent) event.getContent();
        assertEquals("getTimestamp", "Jan 1 03:03:03", event.getTimestamp().toString());
        assertEquals("getField:TIMESTAMP", "Jan 1 03:03:03", content.getFieldValue(Index.TIMESTAMP));
        assertEquals("getField:HOST", "HostC", content.getFieldValue(Index.HOST));
        assertEquals("getField:LOGGER", "LoggerC", content.getFieldValue(Index.LOGGER));
        assertEquals("getField:MESSAGE", "Message C", content.getFieldValue(Index.MESSAGE).toString());
        event = fTrace.getNext(context);
        content = (TextTraceEventContent) event.getContent();
        assertEquals("getTimestamp", "Jan 1 04:04:04", event.getTimestamp().toString());
        assertEquals("getField:TIMESTAMP", "Jan 1 04:04:04", content.getFieldValue(Index.TIMESTAMP));
        assertEquals("getField:HOST", "HostD", content.getFieldValue(Index.HOST));
        assertEquals("getField:LOGGER", "LoggerD", content.getFieldValue(Index.LOGGER));
        assertEquals("getField:MESSAGE", "Message D", content.getFieldValue(Index.MESSAGE).toString());
        event = fTrace.getNext(context);
        content = (TextTraceEventContent) event.getContent();
        assertEquals("getTimestamp", "Jan 1 05:05:05", event.getTimestamp().toString());
        assertEquals("getField:TIMESTAMP", "Jan 1 05:05:05", content.getFieldValue(Index.TIMESTAMP));
        assertEquals("getField:HOST", "HostE", content.getFieldValue(Index.HOST));
        assertEquals("getField:LOGGER", "LoggerE", content.getFieldValue(Index.LOGGER));
        assertEquals("getField:MESSAGE", "", content.getFieldValue(Index.MESSAGE).toString());
        event = fTrace.getNext(context);
        content = (TextTraceEventContent) event.getContent();
        assertEquals("getTimestamp", "Jan 1 06:06:06", event.getTimestamp().toString());
        assertEquals("getField:TIMESTAMP", "Jan 1 06:06:06", content.getFieldValue(Index.TIMESTAMP));
        assertEquals("getField:HOST", "HostF", content.getFieldValue(Index.HOST));
        assertEquals("getField:LOGGER", "LoggerF", content.getFieldValue(Index.LOGGER));
        assertEquals("getField:MESSAGE", "Message F", content.getFieldValue(Index.MESSAGE).toString());
        event = fTrace.getNext(context);
        assertEquals("event", null, event);
        context.dispose();
    }

    @Test
    public void testLocationRatio() {
        ITmfContext context = fTrace.seekEvent(3);
        double ratio = fTrace.getLocationRatio(context.getLocation());
        SyslogEvent event = fTrace.getNext(context);
        TextTraceEventContent content = (TextTraceEventContent) event.getContent();
        Object logger = content.getFieldValue(Index.LOGGER);
        context.dispose();
        context = fTrace.seekEvent(ratio);
        event = fTrace.getNext(context);
        content = (TextTraceEventContent) event.getContent();
        assertEquals("getField:LOGGER", logger.toString(), content.getFieldValue(Index.LOGGER).toString());
        context.dispose();
        context = fTrace.seekEvent(0.0);
        event = fTrace.getNext(context);
        content = (TextTraceEventContent) event.getContent();
        assertEquals("getField:LOGGER", "LoggerA", content.getFieldValue(Index.LOGGER));
        context.dispose();
        context = fTrace.seekEvent(1.0);
        event = fTrace.getNext(context);
        assertEquals("event", null, event);
        context.dispose();
    }
}
