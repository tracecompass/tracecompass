/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/


package org.eclipse.linuxtools.tmf.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * <b><u>TmfTraceEventTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceEventTest {

    // ========================================================================
    // Constructor
    // ========================================================================

    @Test
    public void testTmfTraceEvent() throws Exception {
        TmfTimestamp      timestamp = new TmfTimestamp(12345, (byte) 2, 5);
        TmfEventSource    source    = new TmfEventSource("Source");
        TmfEventFormat    format    = new TmfEventFormat(new String[] { "field1", "field2" });
        TmfEventType      type      = new TmfEventType("Type", format);
        TmfEventContent   content   = new TmfEventContent("Some content", format);
        TmfEventReference reference = new TmfEventReference("Reference");

        // Create the trace event
        TmfTraceEvent event = 
            new TmfTraceEvent(timestamp, source, type, content, reference, "path", "filename", 10);

        // Check the event timestamp
        TmfTimestamp evTS = event.getTimestamp();
        assertEquals("getValue", 12345, evTS.getValue());
        assertEquals("getscale",     2, evTS.getScale());
        assertEquals("getPrecision", 5, evTS.getPrecision());

        // Check the event source
        TmfEventSource evSrc = event.getSource();
        assertEquals("getValue", "Source", evSrc.getSourceId());

        // Check the event type
        TmfEventType evType = event.getType();
        assertEquals("getValue", "Type", evType.getTypeId());
        assertEquals("getFormat", "field1", evType.getFormat().getLabels()[0]);
        assertEquals("getFormat", "field2", evType.getFormat().getLabels()[1]);

        // Check the event content
        TmfEventContent evContent = event.getContent();
        assertEquals("getField", 1, evContent.getFields().length);
        assertEquals("getField", "Some content", evContent.getField(0).toString());

        // Check the event reference
        TmfEventReference evRef = event.getReference();
        assertEquals("getValue", "Reference", evRef.getValue());

        // Check the event file reference
        assertEquals("getPath", "path", event.getSourcePath());
        assertEquals("getFile", "filename", event.getFileName());
        assertEquals("getLineNumber", 10, event.getLineNumber());
    }

}
