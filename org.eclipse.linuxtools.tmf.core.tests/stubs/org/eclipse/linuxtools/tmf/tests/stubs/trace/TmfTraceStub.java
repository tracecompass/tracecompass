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

package org.eclipse.linuxtools.tmf.tests.stubs.trace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * <b><u>TmfTraceStub</u></b>
 * <p>
 * Dummy test trace. Use in conjunction with TmfEventParserStub.
 */
@SuppressWarnings("nls")
public class TmfTraceStub extends TmfTrace<TmfEvent> implements ITmfEventParser<TmfEvent> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The actual stream
    private RandomAccessFile fTrace;

//    // The associated event parser
//    private ITmfEventParser<TmfEvent> fParser;

    // The synchronization lock
    private final ReentrantLock fLock = new ReentrantLock();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param path
     * @throws FileNotFoundException
     */
    public TmfTraceStub() {
        super();
        fParser = new TmfEventParserStub(this);
    }

    /**
     * @param path
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path) throws FileNotFoundException {
        this(path, DEFAULT_TRACE_CACHE_SIZE, false);
    }

    /**
     * @param path
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final int cacheSize) throws FileNotFoundException {
        this(path, cacheSize, false);
    }

    /**
     * @param path
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final int cacheSize, final long interval) throws FileNotFoundException {
        super(null, TmfEvent.class, path, cacheSize, interval);
        fTrace = new RandomAccessFile(path, "r");
        fParser = new TmfEventParserStub(this);
    }

    /**
     * @param path
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final int cacheSize, final ITmfTraceIndexer<?> indexer) throws FileNotFoundException {
        this(path, cacheSize, false, null, indexer);
    }

    /**
     * @param path
     * @param waitForCompletion
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final boolean waitForCompletion) throws FileNotFoundException {
        this(path, DEFAULT_TRACE_CACHE_SIZE, waitForCompletion);
    }

    /**
     * @param path
     * @param cacheSize
     * @param waitForCompletion
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final int cacheSize, final boolean waitForCompletion) throws FileNotFoundException {
        super(null, TmfEvent.class, path, cacheSize);
        fTrace = new RandomAccessFile(path, "r");
        fParser = new TmfEventParserStub(this);
    }

    /**
     * @param path
     * @param cacheSize
     * @param waitForCompletion
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final IResource resource,  final String path, final int cacheSize, final boolean waitForCompletion) throws FileNotFoundException {
        super(resource, TmfEvent.class, path, cacheSize);
        fTrace = new RandomAccessFile(path, "r");
        fParser = new TmfEventParserStub(this);
    }

    /**
     * @param path
     * @param cacheSize
     * @param waitForCompletion
     * @param parser
     * @throws FileNotFoundException
     */
    @SuppressWarnings("unchecked")
    public TmfTraceStub(final String path, final int cacheSize, final boolean waitForCompletion,
            final ITmfEventParser<TmfEvent> parser, final ITmfTraceIndexer<?> indexer) throws FileNotFoundException {
        super(null, TmfEvent.class, path, cacheSize, 0, indexer);
        fTrace = new RandomAccessFile(path, "r");
        fParser = (ITmfEventParser<ITmfEvent>) ((parser != null) ? parser : new TmfEventParserStub(this));
    }

    /**
     * Copy constructor
     */
    public TmfTraceStub(final TmfTraceStub trace) throws FileNotFoundException {
        super(trace);
        fTrace = new RandomAccessFile(getPath(), "r");
        fParser = new TmfEventParserStub(this);
    }

    public void indexTrace() {
        fIndexer.buildIndex(true);
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<TmfEvent> type) throws FileNotFoundException {
        fTrace = new RandomAccessFile(path, "r");
        fParser = new TmfEventParserStub(this);
        super.initTrace(resource, path, type);
    }

    @Override
    public void initialize(final IResource resource, final String path, final Class<TmfEvent> type) throws FileNotFoundException {
        super.initialize(resource, path, type);
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
    public TmfContext seekEvent(final ITmfLocation<?> location) {
        try {
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
                    final TmfContext context = new TmfContext(getCurrentLocation(), rank);
                    return context;
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final NullPointerException e) {
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            e.printStackTrace();
        }
        finally{
            fLock.unlock();
        }
        return null;
    }


    @Override
    public TmfContext seekEvent(final double ratio) {
        fLock.lock();
        try {
            if (fTrace != null) {
                final ITmfLocation<?> location = new TmfLocation<Long>(Long.valueOf((long) (ratio * fTrace.length())));
                final TmfContext context = seekEvent(location);
                context.setRank(ITmfContext.UNKNOWN_RANK);
                return context;
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            fLock.unlock();
        }

        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public double getLocationRatio(final ITmfLocation location) {
        fLock.lock();
        try {
            if (fTrace != null)
                if (location.getLocation() instanceof Long)
                    return (double) ((Long) location.getLocation()) / fTrace.length();
        } catch (final IOException e) {
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
            if (fTrace != null)
                return new TmfLocation<Long>(fTrace.getFilePointer());
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            fLock.unlock();
        }
        return null;
    }

    @Override
    public ITmfEvent parseEvent(final ITmfContext context) {
        fLock.lock();
        try {
            // parseNextEvent will update the context
            if (fTrace != null) {
                final ITmfEvent event = fParser.parseEvent(context.clone());
                return event;
            }
//        }
//        catch (final IOException e) {
//            e.printStackTrace();
        } finally {
            fLock.unlock();
        }
        return null;
    }

    @Override
    public void setTimeRange(final TmfTimeRange range) {
        super.setTimeRange(range);
    }

    @Override
    public void setStartTime(final ITmfTimestamp startTime) {
        super.setStartTime(startTime);
    }

    @Override
    public void setEndTime(final ITmfTimestamp endTime) {
        super.setEndTime(endTime);
    }

    @Override
    public void setStreamingInterval(final long interval) {
        super.setStreamingInterval(interval);
    }

    @Override
    public void dispose() {
        fLock.lock();
        try {
            if (fTrace != null) {
                fTrace.close();
                fTrace = null;
            }
        } catch (final IOException e) {
            // Ignore
        } finally {
            fLock.unlock();
        }
        super.dispose();
    }

}