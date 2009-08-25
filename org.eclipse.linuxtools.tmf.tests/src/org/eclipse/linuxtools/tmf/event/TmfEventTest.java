/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * <b><u>TmfEventTest</u></b>
 * <p>
 * JUNit test suite for the TmfEvent class.
 */
public class TmfEventTest {

    // ========================================================================
    // Constructor
    // ========================================================================
    
    @Test
    public void testTmfEvent() {
        TmfTimestamp      timestamp = new TmfTimestamp(12345, (byte) 2, 5);
        TmfEventSource    source    = new TmfEventSource("Source");
        TmfEventFormat    format    = new TmfEventFormat(new String[] { "field1", "field2" });
        TmfEventType      type      = new TmfEventType("Type", format);
        TmfEventContent   content   = new TmfEventContent("Some content", format);
        TmfEventReference reference = new TmfEventReference("Reference");

        // Create the event
        TmfEvent event = new TmfEvent(timestamp, source, type, content, reference);

        // Check the event timestamp
        TmfTimestamp ts = event.getTimestamp();
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale",     2, ts.getScale());
        assertEquals("getPrecision", 5, ts.getPrecision());

        // Check the original event timestamp
        ts = event.getOriginalTimestamp();
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale",     2, ts.getScale());
        assertEquals("getPrecision", 5, ts.getPrecision());

        // Check the event source
        TmfEventSource src = event.getSource();
        assertEquals("getValue", "Source", src.getSourceId());

        // Check the event type
        TmfEventType tp = event.getType();
        assertEquals("getValue", "Type", tp.getTypeId());
        assertEquals("getFormat", "field1", tp.getFormat().getLabels()[0]);
        assertEquals("getFormat", "field2", tp.getFormat().getLabels()[1]);

        // Check the event content
        TmfEventContent cnt = event.getContent();
        assertEquals("getField", 1, cnt.getFields().length);
        assertEquals("getField", "Some content", cnt.getField(0).toString());

        // Check the event reference
        TmfEventReference ref = event.getReference();
        assertEquals("getValue", "Reference", ref.getValue());
    }

    @Test
    public void testTmfEvent2() {
        TmfTimestamp      original  = new TmfTimestamp(12345, (byte) 2, 5);
        TmfTimestamp      effective = new TmfTimestamp(12350, (byte) 2, 5);
        TmfEventSource    source    = new TmfEventSource("Source");
        TmfEventFormat    format    = new TmfEventFormat(new String[] { "field1", "field2" });
        TmfEventType      type      = new TmfEventType("Type", format);
        TmfEventContent   content   = new TmfEventContent("Some content", format);
        TmfEventReference reference = new TmfEventReference("Reference");

        // Create the event
        TmfEvent event = new TmfEvent(original, effective, source, type, content, reference);

        // Check the event timestamp
        TmfTimestamp ts = event.getTimestamp();
        assertEquals("getValue", 12350, ts.getValue());
        assertEquals("getscale",     2, ts.getScale());
        assertEquals("getPrecision", 5, ts.getPrecision());

        // Check the original event timestamp
        ts = event.getOriginalTimestamp();
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale",     2, ts.getScale());
        assertEquals("getPrecision", 5, ts.getPrecision());

        // Check the event source
        TmfEventSource src = event.getSource();
        assertEquals("getValue", "Source", src.getSourceId());

        // Check the event type
        TmfEventType tp = event.getType();
        assertEquals("getValue", "Type", tp.getTypeId());
        assertEquals("getFormat", "field1", tp.getFormat().getLabels()[0]);
        assertEquals("getFormat", "field2", tp.getFormat().getLabels()[1]);

        // Check the event content
        TmfEventContent cnt = event.getContent();
        assertEquals("getField", 1, cnt.getFields().length);
        assertEquals("getField", "Some content", cnt.getField(0).toString());

        // Check the event reference
        TmfEventReference ref = event.getReference();
        assertEquals("getValue", "Reference", ref.getValue());
    }

}
