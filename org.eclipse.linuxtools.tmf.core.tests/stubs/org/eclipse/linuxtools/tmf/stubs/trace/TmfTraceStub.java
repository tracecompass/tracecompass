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

package org.eclipse.linuxtools.tmf.stubs.trace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.parser.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * <b><u>TmfTraceStub</u></b>
 * <p>
 * Dummy test trace. Use in conjunction with TmfEventParserStub.
 */
@SuppressWarnings("nls")
public class TmfTraceStub extends TmfTrace<TmfEvent> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The actual stream
    private RandomAccessFile fTrace;

    // The associated event parser
    private ITmfEventParser fParser;

    // The synchronization lock
    private ReentrantLock fLock = new ReentrantLock();
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param filename
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename) throws FileNotFoundException {
        this(filename, DEFAULT_INDEX_PAGE_SIZE, false);
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
        this(filename, DEFAULT_INDEX_PAGE_SIZE, waitForCompletion);
    }
    
    /**
     * @param filename
     * @param cacheSize
     * @param waitForCompletion
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename, int cacheSize, boolean waitForCompletion) throws FileNotFoundException {
        super(null, TmfEvent.class, filename, cacheSize, false);
        fTrace = new RandomAccessFile(filename, "r");
        fParser = new TmfEventParserStub();
    }

    
    /**
     * @param filename
     * @param cacheSize
     * @param waitForCompletion
     * @param parser
     * @throws FileNotFoundException
     */
    public TmfTraceStub(String filename, int cacheSize, boolean waitForCompletion, ITmfEventParser parser) throws FileNotFoundException {
        super(filename, TmfEvent.class, filename, cacheSize, false);
        fTrace = new RandomAccessFile(filename, "r");
        fParser = parser;
    }
    
    /**
     */
    @Override
	public TmfTraceStub clone() {
    	TmfTraceStub clone = null;
   		try {
			clone = (TmfTraceStub) super.clone();
	       	clone.fTrace  = new RandomAccessFile(getPath(), "r");
	       	clone.fParser = new TmfEventParserStub();
		} catch (CloneNotSupportedException e) {
		} catch (FileNotFoundException e) {
		}
    	return clone;
    }
 
    @Override
	public ITmfTrace<TmfEvent> copy() {
		ITmfTrace<TmfEvent> returnedValue = null;
		returnedValue = clone();
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
	    fLock.lock();
        try {
            if (fTrace != null) {
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
        finally{
            fLock.unlock();
        }
		return null;
    }


	@Override
    public TmfContext seekLocation(double ratio) {
	    fLock.lock();
        try {
            if (fTrace != null) {
                ITmfLocation<?> location = new TmfLocation<Long>(new Long((long) (ratio * fTrace.length())));
                TmfContext context = seekLocation(location);
                context.setRank(ITmfContext.UNKNOWN_RANK);
                return context;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fLock.unlock();
        }
        
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public double getLocationRatio(ITmfLocation location) {
        fLock.lock();
        try {
            if (fTrace != null) {
                if (location.getLocation() instanceof Long) {
                    return (double) ((Long) location.getLocation()) / fTrace.length();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fLock.unlock();
        }
        return 0;
    }

    @Override
	public TmfLocation<Long> getCurrentLocation() {
        fLock.lock();
        try {
            if (fTrace != null) {
                return new TmfLocation<Long>(fTrace.getFilePointer());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fLock.unlock();
        }
        return null;
    }

	@Override
	public TmfEvent parseEvent(TmfContext context) {
	    fLock.lock();
       	try {
   			// parseNextEvent will update the context
       	    if (fTrace != null) {
       	        TmfEvent event = fParser.parseNextEvent(this, context.clone());
       	        return event;
       	    }
       	}
       	catch (IOException e) {
       		e.printStackTrace();
       	} finally {
       	    fLock.unlock();
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
	
	@Override
	public void dispose() {
	    fLock.lock();
	    try {
	        if (fTrace != null) {
	            fTrace.close();
	            fTrace = null;
	        }
	    } catch (IOException e) {
	        // Ignore
	    } finally {
	        fLock.unlock();  
	    }
	    super.dispose();
	}

}