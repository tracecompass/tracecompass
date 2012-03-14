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

package org.eclipse.linuxtools.lttng.stubs;

import java.io.IOException;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.parser.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * <b><u>TmfEventParserStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class LTTngEventParserStub implements ITmfEventParser<LttngEvent> {

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.parser.ITmfEventParser#parseNextEvent(org.eclipse.linuxtools.tmf.core.trace.ITmfTrace, org.eclipse.linuxtools.tmf.core.trace.TmfContext)
     */
    @Override
    public ITmfEvent parseNextEvent(ITmfTrace<LttngEvent> stream, ITmfContext context)
                    throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

//    // ========================================================================
//    // Attributes
//    // ========================================================================
//
//	private final int NB_FORMATS = 10;
//    private final TmfEventFormat[] fFormats;
//
//    // ========================================================================
//    // Constructors
//    // ========================================================================
//
//    public LTTngEventParserStub() {
//    	fFormats = new TmfEventFormat[NB_FORMATS];
//    	for (int i = 0; i < NB_FORMATS; i++) {
//    		Vector<String> format = new Vector<String>();
//    		for (int j = 1; j <= i; j++) {
//    			format.add(new String("Fmt-" + i + "-Fld-" + j));
//    		}
//    		String[] fields = new String[i];
//    		fFormats[i] = new TmfEventFormat(format.toArray(fields));
//    	}
//    }
//
//    // ========================================================================
//    // Accessors
//    // ========================================================================
//
//    // ========================================================================
//    // Operators
//    // ========================================================================
//
//    /* (non-Javadoc)
//     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfEventParser#parseNextEvent()
//     */
//    static final String typePrefix = "Type-";
//    public TmfEvent parseNextEvent(ITmfTrace eventStream, TmfTraceContext context) throws IOException {
//
//        if (! (eventStream instanceof LTTngTraceStub)) {
//            return null;
//        }
//
//       	// Highly inefficient...
//       	RandomAccessFile stream = ((LTTngTraceStub) eventStream).getStream();
//       	String name = eventStream.getName();
//       	name = name.substring(name.lastIndexOf('/') + 1);
//
//		synchronized(stream) {
//        	long location = 0;
//        	if (context != null)
//        		location = (Long) (context.getLocation());
//        	stream.seek(location);
//
//    		try {
//    			// Read the individual fields
//        		long ts       = stream.readLong();
//        		String source = stream.readUTF();
//        		String type   = stream.readUTF();
//    			@SuppressWarnings("unused")
//    			int reference = stream.readInt();
//
//        		// Read the event parts
//        		int typeIndex  = Integer.parseInt(type.substring(typePrefix.length()));
//        		String[] fields = new String[typeIndex];
//        		for (int i = 0; i < typeIndex; i++) {
//        			fields[i] = stream.readUTF();
//        		}
//
//        		// Format the content from the individual fields
//        		String content = "[";
//        		if (typeIndex > 0) {
//        			content += fields[0];
//        		}
//        		for (int i = 1; i < typeIndex; i++) {
//        			content += ", " + fields[i];
//        		}
//        		content += "]";
//
//        		// Update the context
//        		context.setLocation(stream.getFilePointer());
//        		context.incrIndex();
//       			try {
//       				long ts2 = stream.readLong();
//           			context.setTimestamp(new LTTngTimestampStub(ts2));
//        		} catch (EOFException e) {
//        			context.setTimestamp(null);
//            	}
//
//       	    	// Create the event
//       			TmfEvent event = new TmfEvent(
//       					new LTTngTimestampStub(ts),
//       					new TmfEventSource(source),
//       					new TmfEventType(type, fFormats[typeIndex]),
//       					new TmfEventContent(content, fFormats[typeIndex]),
//       					new TmfEventReference(name));
//
//       			return event;
//
//    		} catch (EOFException e) {
//    			context.setTimestamp(null);
//        	}
//        }
//        return null;
//    }

}