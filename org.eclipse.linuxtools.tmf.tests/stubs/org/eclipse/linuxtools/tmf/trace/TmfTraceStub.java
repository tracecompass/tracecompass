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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import org.eclipse.linuxtools.tmf.event.TmfEvent;

/**
 * <b><u>TmfTraceStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceStub extends TmfTrace {

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
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename) throws FileNotFoundException {
        this(filename, DEFAULT_PAGE_SIZE);
    }

    /**
     * @param filename
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename, int cacheSize) throws FileNotFoundException {
        super(filename, cacheSize);
        fTrace = new RandomAccessFile(filename, "r");
        fParser = new TmfEventParserStub();
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
			fTrace.seek((location != null) ? (Long) location : 0);
			context = new TmfTraceContext(getCurrentLocation(), 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfStreamLocator#getCurrentLocation()
     */
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
	public TmfEvent parseNextEvent() {
		try {
			TmfEvent event = fParser.getNextEvent(this);
			return event;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

    // ========================================================================
    // Helper functions
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfEventStream#getAttributes()
     */
    public Map<String, Object> getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

}
