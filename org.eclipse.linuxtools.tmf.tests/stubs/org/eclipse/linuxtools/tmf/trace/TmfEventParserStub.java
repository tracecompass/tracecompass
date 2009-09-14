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

package org.eclipse.linuxtools.tmf.trace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

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

	private final int NB_FORMATS = 10;
    private final TmfEventFormat[] fFormats;

    // ========================================================================
    // Constructors
    // ========================================================================

    public TmfEventParserStub() {
    	fFormats = new TmfEventFormat[NB_FORMATS];
    	for (int i = 0; i < NB_FORMATS; i++) {
    		Vector<String> format = new Vector<String>();
    		for (int j = 1; j <= i; j++) {
    			format.add(new String("Fmt-" + i + "-Fld-" + j));
    		}
    		String[] fields = new String[i];
    		fFormats[i] = new TmfEventFormat(format.toArray(fields));
    	}
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfEventParser#parseNextEvent()
     */
    static final String typePrefix = "Type-";
    public TmfEvent getNextEvent(ITmfTrace eventStream) throws IOException {

        if (! (eventStream instanceof TmfTraceStub)) {
            return null;
        }

        RandomAccessFile stream = ((TmfTraceStub) eventStream).getStream();

        try {
            long ts        = stream.readLong();
            String source  = stream.readUTF();
            String type    = stream.readUTF();
            int reference  = stream.readInt();
            int typeIndex  = Integer.parseInt(type.substring(typePrefix.length()));
            String[] fields = new String[typeIndex];
            for (int i = 0; i < typeIndex; i++) {
                fields[i] = stream.readUTF();
            }
            String content = "[";
            for (int i = 0; i < typeIndex - 1; i++) {
                content += fields[i] + ", ";
            }
            content += "]";
            
            TmfEvent event = new TmfEvent(
                    new TmfTimestamp(ts, (byte) -3, 0),     // millisecs
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
