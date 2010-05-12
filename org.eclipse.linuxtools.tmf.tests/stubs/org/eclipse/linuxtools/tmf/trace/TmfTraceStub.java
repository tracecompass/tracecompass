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
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
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
    private RandomAccessFile fTrace;

    // The associated event parser
    private ITmfEventParser fParser;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param filename
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename) throws FileNotFoundException {
        super("TmfTraceStub", TmfEvent.class, filename);
        fTrace  = new RandomAccessFile(filename, "r");
        fParser = new TmfEventParserStub();
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
     * @param waitForCompletion
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename, boolean waitForCompletion) throws FileNotFoundException {
        this(filename, DEFAULT_CACHE_SIZE, waitForCompletion);
    }
    
    /**
     * @param filename
     * @param cacheSize
     * @param waitForCompletion
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename, int cacheSize, boolean waitForCompletion) throws FileNotFoundException {
        super(filename, TmfEvent.class, filename, cacheSize);
        fTrace = new RandomAccessFile(filename, "r");
        fParser = new TmfEventParserStub();
    }

//    /**
//     * @param other
//     */
//    public TmfTraceStub(TmfTraceStub other) {
//        this(filename, DEFAULT_CACHE_SIZE, waitForCompletion);
//    }
    
    /**
     */
    @Override
	public TmfTraceStub clone() {
    	TmfTraceStub clone = null;
   		try {
			clone = (TmfTraceStub) super.clone();
	       	clone.fTrace  = new RandomAccessFile(getName(), "r");
	       	clone.fParser = new TmfEventParserStub();
		} catch (CloneNotSupportedException e) {
		} catch (FileNotFoundException e) {
		}
    	return clone;
    }
 
    public ITmfTrace createTraceCopy() {
		ITmfTrace returnedValue = null;
		try {
			returnedValue = new TmfTraceStub(this.getName());
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return returnedValue;
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

	@Override
	@SuppressWarnings("unchecked")
	public TmfContext seekLocation(ITmfLocation<?> location) {
        try {
        	synchronized(fTrace) {
        		// Position the trace at the requested location and
        		// returns the corresponding context
        		long loc  = 0;
        		long rank = 0;
        		if (location != null) {
        			loc = ((TmfLocation<Long>) location).getLocation();
        			rank = ITmfContext.UNKNOWN_RANK;
        		}
        		if (loc != fTrace.getFilePointer()) {
        			fTrace.seek(loc);
        		}
        		TmfContext context = new TmfContext(getCurrentLocation(), rank);
        		return context;
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }

    @Override
	public TmfLocation<Long> getCurrentLocation() {
        try {
            return new TmfLocation<Long>(fTrace.getFilePointer());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

	@Override
	public TmfEvent parseEvent(TmfContext context) {
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

	@Override
	public void setTimeRange(TmfTimeRange range) {
    	super.setTimeRange(range);
    }

	@Override
	public void setStartTime(TmfTimestamp startTime) {
    	super.setStartTime(startTime);
    }

	@Override
	public void setEndTime(TmfTimestamp endTime) {
    	super.setEndTime(endTime);
    }

}