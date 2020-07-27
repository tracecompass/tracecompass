/*******************************************************************************
 * Copyright (c) 2014, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Patrick Tasse - Move field declarations to trace
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.trace.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.core.trace.text.TextTraceEventContent;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.text.SyslogEvent;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.text.SyslogTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.text.SyslogTrace.Field;
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
        defaultPreferences.put(ITmfTimePreferencesConstants.LOCALE, Locale.US.toLanguageTag());
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
        defaultPreferences.put(ITmfTimePreferencesConstants.LOCALE, Locale.getDefault().toLanguageTag());
        TmfTimestampFormat.updateDefaultFormats();
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    @Test
    public void testEmptyConstructor() {
        SyslogTrace trace = new SyslogTrace();
        assertEquals("getEventType",      null, trace.getEventType());
        assertEquals("getPath",      null, trace.getPath());
        assertEquals("getName",      "", trace.getName());
        assertEquals("getCacheSize", 100, trace.getCacheSize());

        ITmfTimestamp initRange = TmfTimestamp.fromSeconds(60);
        assertEquals("getInitialRangeOffset", initRange, trace.getInitialRangeOffset());

        trace.dispose();
    }

    @Test
    public void testValidation() throws URISyntaxException, IOException {
        SyslogTrace trace = new SyslogTrace();
        String validTracePath = fTestFile.getAbsolutePath();
        IStatus status = trace.validate(null, validTracePath);
        assertTrue(status.isOK());
        assertTrue(status instanceof TraceValidationStatus);
        assertEquals(185, ((TraceValidationStatus) status).getConfidence());

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

        trace.dispose();
    }

    @Test
    public void testInitTrace() throws Exception {
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(PATH), null);
        String path = FileLocator.toFileURL(location).toURI().getPath();
        SyslogTrace trace = new SyslogTrace();
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(PATH));
        trace.initTrace(resource, path, SyslogEvent.class);
        assertEquals("getEventType",      SyslogEvent.class, trace.getEventType());
        assertEquals("getPath",      fTestFile.toURI().getPath(), trace.getPath());
        assertEquals("getName",      NAME, trace.getName());
        assertEquals("getCacheSize", 100, trace.getCacheSize());
        trace.dispose();
    }

    // ------------------------------------------------------------------------
    // Indexing
    // ------------------------------------------------------------------------

    @Test
    public void testTraceIndexing() {
        assertEquals("getNbEvents", 6, fTrace.getNbEvents());

        ITmfTimestamp initRange = TmfTimestamp.fromSeconds(60);
        assertEquals("getInitialRangeOffset", initRange, fTrace.getInitialRangeOffset());
    }

    // ------------------------------------------------------------------------
    // Parsing
    // ------------------------------------------------------------------------

    @Test
    public void testTraceParsing() {
        ITmfContext context = fTrace.seekEvent(0);
        SyslogEvent event = fTrace.getNext(context);
        assertNotNull(event);
        TextTraceEventContent content = event.getContent();
        assertEquals("getTimestamp", "Jan 1 01:01:01", event.getTimestamp().toString());
        assertEquals("getField:HOST", "HostA", content.getFieldValue(Field.HOST));
        assertEquals("getField:LOGGER", "LoggerA", content.getFieldValue(Field.LOGGER));
        assertEquals("getField:FILE", "SourceFileA", content.getFieldValue(Field.FILE));
        assertEquals("getField:LINE", "4", content.getFieldValue(Field.LINE));
        assertEquals("getField:MESSAGE", "Message A", content.getFieldValue(Field.MESSAGE).toString());
        event = fTrace.getNext(context);
        assertNotNull(event);
        content = event.getContent();
        assertEquals("getTimestamp", "Jan 1 02:02:02", event.getTimestamp().toString());
        assertEquals("getField:HOST", "HostB", content.getFieldValue(Field.HOST));
        assertEquals("getField:LOGGER", "LoggerB", content.getFieldValue(Field.LOGGER));
        assertEquals("getField:FILE", "SourceFileB", content.getFieldValue(Field.FILE));
        assertEquals("getField:LINE", "5", content.getFieldValue(Field.LINE));
        assertEquals("getField:MESSAGE", "Message B", content.getFieldValue(Field.MESSAGE).toString());
        event = fTrace.getNext(context);
        assertNotNull(event);
        content = event.getContent();
        assertEquals("getTimestamp", "Jan 1 03:03:03", event.getTimestamp().toString());
        assertEquals("getField:HOST", "HostC", content.getFieldValue(Field.HOST));
        assertEquals("getField:LOGGER", "LoggerC", content.getFieldValue(Field.LOGGER));
        assertEquals("getField:FILE", "SourceFileC", content.getFieldValue(Field.FILE));
        assertEquals("getField:LINE", "6", content.getFieldValue(Field.LINE));
        assertEquals("getField:MESSAGE", "Message C", content.getFieldValue(Field.MESSAGE).toString());
        event = fTrace.getNext(context);
        assertNotNull(event);
        content = event.getContent();
        assertEquals("getTimestamp", "Jan 1 04:04:04", event.getTimestamp().toString());
        assertEquals("getField:HOST", "HostD", content.getFieldValue(Field.HOST));
        assertEquals("getField:LOGGER", "LoggerD", content.getFieldValue(Field.LOGGER));
        assertEquals("getField:FILE", "SourceFileD", content.getFieldValue(Field.FILE));
        assertEquals("getField:LINE", "7", content.getFieldValue(Field.LINE));
        assertEquals("getField:MESSAGE", "Message D", content.getFieldValue(Field.MESSAGE).toString());
        event = fTrace.getNext(context);
        assertNotNull(event);
        content = event.getContent();
        assertEquals("getTimestamp", "Jan 1 05:05:05", event.getTimestamp().toString());
        assertEquals("getField:HOST", "HostE", content.getFieldValue(Field.HOST));
        assertEquals("getField:LOGGER", "LoggerE", content.getFieldValue(Field.LOGGER));
        assertEquals("getField:FILE", "SourceFileE", content.getFieldValue(Field.FILE));
        assertEquals("getField:LINE", "8", content.getFieldValue(Field.LINE));
        assertEquals("getField:MESSAGE", "", content.getFieldValue(Field.MESSAGE).toString());
        event = fTrace.getNext(context);
        assertNotNull(event);
        content = event.getContent();
        assertEquals("getTimestamp", "Jan 1 06:06:06", event.getTimestamp().toString());
        assertEquals("getField:HOST", "HostF", content.getFieldValue(Field.HOST));
        assertEquals("getField:LOGGER", "LoggerF", content.getFieldValue(Field.LOGGER));
        assertEquals("getField:FILE", "SourceFileF", content.getFieldValue(Field.FILE));
        assertEquals("getField:LINE", "9", content.getFieldValue(Field.LINE));
        assertEquals("getField:MESSAGE", "Message F", content.getFieldValue(Field.MESSAGE).toString());
        event = fTrace.getNext(context);
        assertEquals("event", null, event);
        context.dispose();
    }

    @Test
    public void testLocationRatio() {
        ITmfContext context = fTrace.seekEvent(3);
        double ratio = fTrace.getLocationRatio(context.getLocation());
        SyslogEvent event = fTrace.getNext(context);
        assertNotNull(event);
        TextTraceEventContent content = event.getContent();
        Object logger = content.getFieldValue(Field.LOGGER);
        context.dispose();
        context = fTrace.seekEvent(ratio);
        event = fTrace.getNext(context);
        assertNotNull(event);
        content = event.getContent();
        assertEquals("getField:LOGGER", logger.toString(), content.getFieldValue(Field.LOGGER).toString());
        context.dispose();
        context = fTrace.seekEvent(0.0);
        event = fTrace.getNext(context);
        assertNotNull(event);
        content = event.getContent();
        assertEquals("getField:LOGGER", "LoggerA", content.getFieldValue(Field.LOGGER));
        context.dispose();
        context = fTrace.seekEvent(1.0);
        event = fTrace.getNext(context);
        assertEquals("event", null, event);
        context.dispose();
    }

    /**
     * Run readingBounds for trace: testfiles/syslog
     */
    @Test
    public void testBounds(){
        testReadingBounds("Jan 1 01:01:01" , "Jan 1 06:06:06");
    }

    /**
     * Test that the methods for reading the trace without indexing it return
     * the right values.
     */
    private static void testReadingBounds(String expectedStart, String expectedEnd) {
        ITmfTimestamp start = fTrace.readStart();
        assertNotNull("Failed to read Syslog trace start", start);
        assertEquals("readStart", expectedStart, start.toString());
        ITmfTimestamp end = fTrace.readEnd();
        assertNotNull("Failed to read Syslog trace end", end);
        assertEquals("readEnd", expectedEnd, end.toString());
    }
}