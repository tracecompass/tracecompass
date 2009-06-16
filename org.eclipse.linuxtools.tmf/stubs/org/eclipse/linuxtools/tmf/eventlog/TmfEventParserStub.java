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

package org.eclipse.linuxtools.tmf.eventlog;

import java.io.EOFException;
import java.io.IOException;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.event.TmfEventFormat;
import org.eclipse.linuxtools.tmf.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfEventType;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>TmfEventParserStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventParserStub implements ITmfEventParser {

    // ========================================================================
    // Attributes
    // ========================================================================

    private final TmfEventFormat[] fFormats;

    // ========================================================================
    // Constructors
    // ========================================================================

    public TmfEventParserStub() {
        fFormats = new TmfEventFormat[] {
                new TmfEventFormat(new String[] { "Fmt1-Fld-1" }),
                new TmfEventFormat(new String[] { "Fmt2-Fld-1", "Fmt2-Fld-2" }),
                new TmfEventFormat(new String[] { "Fmt3-Fld-1", "Fmt3-Fld-2", "Fmt3-Fld-3" }),
                new TmfEventFormat(new String[] { "Fmt4-Fld-1", "Fmt4-Fld-2", "Fmt4-Fld-3", "Fmt4-Fld-4" }),
                new TmfEventFormat(new String[] { "Fmt5-Fld-1", "Fmt5-Fld-2", "Fmt5-Fld-3", "Fmt5-Fld-4", "Fmt5-Fld-5" }),
        };
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    // ========================================================================
    // Operators
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfEventParser#parseNextEvent()
     */
    static final String typePrefix = "Type-";
    @Override
    public TmfEvent getNextEvent(TmfEventStream stream) throws IOException {
        try {
            long ts        = stream.readLong();
            String source  = stream.readUTF();
            String type    = stream.readUTF();
            int reference  = stream.readInt();
            int typeIndex  = Integer.parseInt(type.substring(typePrefix.length()));
            String[] content = new String[typeIndex];
            for (int i = 0; i < typeIndex; i ++) {
                content[i] = stream.readUTF();
            }
            TmfEvent event = new TmfEvent(
                    new TmfTimestamp(ts, (byte) 0, 0),
                    new TmfEventSource(source),
                    new TmfEventType(type, fFormats[typeIndex]),
                    new TmfEventContent(content, fFormats[typeIndex]),
                    new TmfEventReference(reference));
            return event;
        } catch (EOFException e) {
        }
        return null;
    }

}
