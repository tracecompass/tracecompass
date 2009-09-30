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

package org.eclipse.linuxtools.lttng.stubs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;

/**
 * <b><u>LTTngTraceStub</u></b>
 * <p>
 * Dummy test trace. Use in conjunction with LTTngEventParserStub.
 */
public class LTTngTraceStub extends TmfTrace {

    // ========================================================================
    // Attributes
    // ========================================================================

    // The actual stream
    private final RandomAccessFile fTrace;

    // The associated event parser
    private final ITmfEventParser fParser;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param filename
     * @param parser
     * @throws FileNotFoundException
     */
    public LTTngTraceStub(String filename) throws FileNotFoundException {
        this(filename, DEFAULT_CACHE_SIZE);
    }

    /**
     * @param filename
     * @param parser
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public LTTngTraceStub(String filename, int cacheSize) throws FileNotFoundException {
        super(filename, cacheSize, false);
        fTrace = new RandomAccessFile(filename, "r");
    	fParser = new LTTngEventParserStub();
    	indexStream();
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    public RandomAccessFile getStream() {
        return fTrace;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfStreamLocator#seekLocation(java.lang.Object)
     */
	public TmfTraceContext seekLocation(Object location) {
        TmfTraceContext context = null;
       	try {
       		synchronized(fTrace) {
        		fTrace.seek((location != null) ? (Long) location : 0);
        		context = new TmfTraceContext(getCurrentLocation(), null, 0);
        		TmfTraceContext context2 = new TmfTraceContext(getCurrentLocation(), null, 0);
        		TmfEvent event = parseEvent(context2);
        		context.setTimestamp(event.getTimestamp());
       		}
        } catch (IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
        return context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfStreamLocator#getCurrentLocation()
     */
    @Override
	public Object getCurrentLocation() {
       	try {
       		return new Long(fTrace.getFilePointer());
       	} catch (IOException e) {
       		// TODO Auto-generated catch block
       		e.printStackTrace();
       	}
        return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#parseEvent()
	 */
	@Override
	public TmfEvent parseEvent(TmfTraceContext context) {
       	try {
   			// paserNextEvent updates the context
   			TmfEvent event = fParser.parseNextEvent(this, context);
//   			if (event != null) {
//   				context.setTimestamp(event.getTimestamp());
//   			}
       		return event;
       	}
       	catch (IOException e) {
       		e.printStackTrace();
       	}
       	return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[LTTngTraceStub]";
	}

//    // ========================================================================
//    // Helper functions
//    // ========================================================================
//
//    /* (non-Javadoc)
//     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfEventStream#getAttributes()
//     */
//    public Map<String, Object> getAttributes() {
//        // TODO Auto-generated method stub
//        return null;
//    }

}
