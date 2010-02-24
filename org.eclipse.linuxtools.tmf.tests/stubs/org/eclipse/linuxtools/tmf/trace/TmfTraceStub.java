/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.parser.ITmfEventParser;

/**
 * <b><u>TmfTraceStub</u></b>
 * <p>
 * Dummy test trace. Use in conjunction with TmfEventParserStub.
 */
public class TmfTraceStub extends TmfTrace<TmfEvent> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The actual stream
    private final RandomAccessFile fTrace;

    // The associated event parser
    private final ITmfEventParser fParser;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param filename
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename) throws FileNotFoundException {
        this(filename, DEFAULT_CACHE_SIZE, false);
    }

    /**
     * @param filename
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename, int cacheSize) throws FileNotFoundException {
        this(filename, cacheSize, false);
    }

    /**
     * @param filename
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename, boolean waitForCompletion) throws FileNotFoundException {
        this(filename, DEFAULT_CACHE_SIZE, waitForCompletion);
    }

    /**
     * @param filename
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename, int cacheSize, boolean waitForCompletion) throws FileNotFoundException {
        super(TmfEvent.class, filename, cacheSize);
        fTrace = new RandomAccessFile(filename, "r");
        fParser = new TmfEventParserStub();
        indexTrace(waitForCompletion);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public RandomAccessFile getStream() {
        return fTrace;
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfStreamLocator#seekLocation(java.lang.Object)
     */
	public TmfTraceContext seekLocation(Object location) {
        try {
        	synchronized(fTrace) {
        		// Position the trace, read the event (to obtain its timestamp)
        		// and then re-position the trace (not great...)
        		fTrace.seek((location != null) ? (Long) location : 0);
        		TmfTraceContext context = new TmfTraceContext(getCurrentLocation());
        		return context;
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
	 * @see org.eclipse.linuxtools.tmf.trace.TmfTrace#parseEvent()
	 */
	@Override
	public TmfEvent parseEvent(TmfTraceContext context) {
       	try {
   			// paserNextEvent updates the context
   			TmfEvent event = fParser.parseNextEvent(this, context);
       		return event;
       	}
       	catch (IOException e) {
       		e.printStackTrace();
       	}
       	return null;
	}

}