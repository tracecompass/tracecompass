/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for removal of context clone
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLongLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * <b><u>TmfTraceStub</u></b>
 * <p>
 * Dummy test trace. Use in conjunction with TmfEventParserStub.
 */
@SuppressWarnings("javadoc")
public class TmfTraceStub extends TmfTrace implements ITmfEventParser {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The actual stream
    private RandomAccessFile fTrace;

//    // The associated event parser
//    private ITmfEventParser<TmfEvent> fParser;

    // The synchronization lock
    private final ReentrantLock fLock = new ReentrantLock();

    private ITmfTimestamp fInitialRangeOffset = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TmfTraceStub() {
        super();
        setParser(new TmfEventParserStub(this));
    }

    /**
     * @param path
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path) throws TmfTraceException {
        this(path, ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false);
    }

    /**
     * @param path
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final int cacheSize) throws TmfTraceException {
        this(path, cacheSize, false);
    }

    /**
     * @param path
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final int cacheSize, final long interval) throws TmfTraceException {
        super(null, ITmfEvent.class, path, cacheSize, interval, null, null);
        try {
            fTrace = new RandomAccessFile(path, "r"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            throw new TmfTraceException(e.getMessage());
        }
        setParser(new TmfEventParserStub(this));
    }

    /**
     * @param path
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final int cacheSize, final ITmfTraceIndexer indexer) throws TmfTraceException {
        this(path, cacheSize, false, null, indexer);
    }

    /**
     * @param path
     * @param waitForCompletion
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final boolean waitForCompletion) throws TmfTraceException {
        this(path, ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, waitForCompletion);
    }

    /**
     * @param path
     * @param cacheSize
     * @param waitForCompletion
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final int cacheSize, final boolean waitForCompletion) throws TmfTraceException {
        super(null, ITmfEvent.class, path, cacheSize, 0, null, null);
        try {
            fTrace = new RandomAccessFile(path, "r"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            throw new TmfTraceException(e.getMessage());
        }
        setParser(new TmfEventParserStub(this));
        if (waitForCompletion) {
            indexTrace(true);
        }
    }

    /**
     * @param path
     * @param cacheSize
     * @param waitForCompletion
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final IResource resource,  final String path, final int cacheSize, final boolean waitForCompletion) throws TmfTraceException {
        super(resource, ITmfEvent.class, path, cacheSize, 0, null, null);
        try {
            fTrace = new RandomAccessFile(path, "r"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            throw new TmfTraceException(e.getMessage());
        }
        setParser(new TmfEventParserStub(this));
    }

    /**
     * @param path
     * @param cacheSize
     * @param waitForCompletion
     * @param parser
     * @throws FileNotFoundException
     */
    public TmfTraceStub(final String path, final int cacheSize, final boolean waitForCompletion,
            final ITmfEventParser parser, final ITmfTraceIndexer indexer) throws TmfTraceException {
        super(null, ITmfEvent.class, path, cacheSize, 0, indexer, null);
        try {
            fTrace = new RandomAccessFile(path, "r"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            throw new TmfTraceException(e.getMessage());
        }
        setParser((parser != null) ? parser : new TmfEventParserStub(this));
    }

    /**
     * Copy constructor
     */
    public TmfTraceStub(final TmfTraceStub trace) throws TmfTraceException {
        super(trace);
        try {
            fTrace = new RandomAccessFile(getPath(), "r"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            throw new TmfTraceException(e.getMessage());
        }
        setParser(new TmfEventParserStub(this));
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> type) throws TmfTraceException {
        try {
            fTrace = new RandomAccessFile(path, "r"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            throw new TmfTraceException(e.getMessage());
        }
        setParser(new TmfEventParserStub(this));
        super.initTrace(resource, path, type);
    }

    @Override
    public void initialize(final IResource resource, final String path, final Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initialize(resource, path, type);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public RandomAccessFile getStream() {
        return fTrace;
    }

    public void setInitialRangeOffset(ITmfTimestamp initOffset) {
        fInitialRangeOffset = initOffset;
    }

    @Override
    public ITmfTimestamp getInitialRangeOffset() {
        if (fInitialRangeOffset != null) {
            return fInitialRangeOffset;
        }
        return super.getInitialRangeOffset();
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    @Override
    public TmfContext seekEvent(final ITmfLocation location) {
        try {
            fLock.lock();
            try {
                if (fTrace != null) {
                    // Position the trace at the requested location and
                    // returns the corresponding context
                    long loc  = 0;
                    long rank = 0;
                    if (location != null) {
                        loc = (Long) location.getLocationInfo();
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
            finally{
                fLock.unlock();
            }
        } catch (final NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public TmfContext seekEvent(final double ratio) {
        fLock.lock();
        try {
            if (fTrace != null) {
                final ITmfLocation location = new TmfLongLocation(Long.valueOf(Math.round(ratio * fTrace.length())));
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
    public double getLocationRatio(ITmfLocation location) {
        fLock.lock();
        try {
            if (fTrace != null) {
                if (location.getLocationInfo() instanceof Long) {
                    return (double) ((Long) location.getLocationInfo()) / fTrace.length();
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            fLock.unlock();
        }
        return 0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        fLock.lock();
        try {
            if (fTrace != null) {
                return new TmfLongLocation(fTrace.getFilePointer());
            }
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
            if (fTrace != null && getParser() != null && context != null) {
                final ITmfEvent event = getParser().parseEvent(context);
                return event;
            }
        } finally {
            fLock.unlock();
        }
        return null;
    }

    @Override
    public synchronized void setNbEvents(final long nbEvents) {
        super.setNbEvents(nbEvents);
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
    public synchronized void dispose() {
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

    @Override
    public IStatus validate(IProject project, String path) {
        if (fileExists(path)) {
            return Status.OK_STATUS;
        }
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "File does not exist: " + path);
    }

}
