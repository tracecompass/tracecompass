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

package org.eclipse.linuxtools.tmf.stubs.trace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.parser.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * <b><u>TmfEventParserStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("nls")
public class TmfEventParserStub implements ITmfEventParser<TmfEvent> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	private static final int NB_TYPES = 10;
    private final TmfEventType[] fTypes;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TmfEventParserStub() {
    	fTypes = new TmfEventType[NB_TYPES];
    	for (int i = 0; i < NB_TYPES; i++) {
    		Vector<String> fields = new Vector<String>();
    		for (int j = 1; j <= i; j++) {
    		    String field = "Fmt-" + i + "-Fld-" + j;
    		    fields.add(field);
    		}
    		String[] fieldArray = new String[i];
    		ITmfEventField rootField = TmfEventField.makeRoot(fields.toArray(fieldArray));
    		fTypes[i] = new TmfEventType("UnitTest", "Type-" + i, rootField);
    	}
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    static final String typePrefix = "Type-";
    @Override
	@SuppressWarnings("unchecked")
	public ITmfEvent parseNextEvent(ITmfTrace<TmfEvent> eventStream, ITmfContext context) throws IOException {

        if (! (eventStream instanceof TmfTraceStub)) {
            return null;
        }

       	// Highly inefficient...
       	RandomAccessFile stream = ((TmfTraceStub) eventStream).getStream();
//       	String name = eventStream.getName();
//       	name = name.substring(name.lastIndexOf('/') + 1);

       	// no need to use synchronized since it's already cover by the calling method
       	
       	long location = 0;
       	if (context != null)
       	    location = ((TmfLocation<Long>) (context.getLocation())).getLocation();
       	stream.seek(location);

       	try {
       	    long ts        = stream.readLong();
       	    String source  = stream.readUTF();
       	    String type    = stream.readUTF();
       	    Integer reference  = stream.readInt();
       	    int typeIndex  = Integer.parseInt(type.substring(typePrefix.length()));
       	    String[] fields = new String[typeIndex];
       	    for (int i = 0; i < typeIndex; i++) {
       	        fields[i] = stream.readUTF();
       	    }

       	    StringBuffer content = new StringBuffer("[");
       	    if (typeIndex > 0) {
       	        content.append(fields[0]);
       	    }
       	    for (int i = 1; i < typeIndex; i++) {
       	        content.append(", ").append(fields[i]);
       	    }
       	    content.append("]");

            TmfEventField root = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, content.toString());
       	    ITmfEvent event = new TmfEvent(eventStream,
       	            new TmfTimestamp(ts, -3, 0),     // millisecs
       	            source, fTypes[typeIndex], root, reference.toString());
       	    return event;
       	} catch (EOFException e) {
       	}
        return null;
    }

}