/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Updated for removal of context clone
 *   Geneviève Bastien - Added the createTimestamp function
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.ctf.core.event.CTFClock;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceProperties;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * The CTf trace handler
 *
 * @version 1.0
 * @author Matthew khouzam
 */
public class CtfTmfTrace extends TmfTrace
        implements ITmfEventParser, ITmfTraceProperties, ITmfPersistentlyIndexable {

    // -------------------------------------------
    // Constants
    // -------------------------------------------
    /**
     * Default cache size for CTF traces
     */
    protected static final int DEFAULT_CACHE_SIZE = 50000;

    /*
     * The Ctf clock unique identifier field
     */
    private static final String CLOCK_HOST_PROPERTY = "uuid"; //$NON-NLS-1$
    private static final int CONFIDENCE = 10;

    // -------------------------------------------
    // Fields
    // -------------------------------------------

    /* Reference to the CTF Trace */
    private CTFTrace fTrace;

    // -------------------------------------------
    // TmfTrace Overrides
    // -------------------------------------------
    /**
     * Method initTrace.
     *
     * @param resource
     *            The resource associated with this trace
     * @param path
     *            The path to the trace file
     * @param eventType
     *            The type of events that will be read from this trace
     * @throws TmfTraceException
     *             If something went wrong while reading the trace
     */
    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> eventType)
            throws TmfTraceException {
        /*
         * Set the cache size. This has to be done before the call to super()
         * because the super needs to know the cache size.
         */
        setCacheSize();

        super.initTrace(resource, path, eventType);

        try {
            this.fTrace = new CTFTrace(path);
            CtfIteratorManager.addTrace(this);
            CtfTmfContext ctx;
            /* Set the start and (current) end times for this trace */
            ctx = (CtfTmfContext) seekEvent(0L);
            CtfTmfEvent event = getNext(ctx);
            if ((ctx.getLocation().equals(CtfIterator.NULL_LOCATION)) || (ctx.getCurrentEvent() == null)) {
                /* Handle the case where the trace is empty */
                this.setStartTime(TmfTimestamp.BIG_BANG);
            } else {
                final ITmfTimestamp curTime = event.getTimestamp();
                this.setStartTime(curTime);
                this.setEndTime(curTime);
            }

        } catch (final CTFReaderException e) {
            /*
             * If it failed at the init(), we can assume it's because the file
             * was not found or was not recognized as a CTF trace. Throw into
             * the new type of exception expected by the rest of TMF.
             */
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void dispose() {
        CtfIteratorManager.removeTrace(this);
        if (fTrace != null) {
            fTrace.dispose();
            fTrace = null;
        }
        super.dispose();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation sets the confidence to 10 if the trace is a
     * valid CTF trace.
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        IStatus validTrace = new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
        try {
            final CTFTrace temp = new CTFTrace(path);
            if (!temp.majorIsSet()) {
                validTrace = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CtfTmfTrace_MajorNotSet);
            } else {
                CTFTraceReader ctfTraceReader = new CTFTraceReader(temp);
                if (!ctfTraceReader.hasMoreEvents()) {
                    // TODO: This will need an additional check when we support live traces
                    // because having no event is valid for a live trace
                    validTrace = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CtfTmfTrace_NoEvent);
                }
                ctfTraceReader.dispose();
            }
            temp.dispose();
        } catch (final CTFReaderException e) {
            validTrace = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CtfTmfTrace_ReadingError +": " + e.toString()); //$NON-NLS-1$
        } catch (final BufferOverflowException e){
            validTrace = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CtfTmfTrace_ReadingError +": " + Messages.CtfTmfTrace_BufferOverflowErrorMessage); //$NON-NLS-1$
        }

        return validTrace;
    }

    /**
     * Method getCurrentLocation. This is not applicable in CTF
     *
     * @return null, since the trace has no knowledge of the current location
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getCurrentLocation()
     * @since 3.0
     */
    @Override
    public ITmfLocation getCurrentLocation() {
        return null;
    }

    /**
     * @since 3.0
     */
    @Override
    public double getLocationRatio(ITmfLocation location) {
        final CtfLocation curLocation = (CtfLocation) location;
        final CtfTmfContext context = new CtfTmfContext(this);
        context.setLocation(curLocation);
        context.seek(curLocation.getLocationInfo());
        final CtfLocationInfo currentTime = ((CtfLocationInfo) context.getLocation().getLocationInfo());
        final long startTime = getIterator(this, context).getStartTime();
        final long endTime = getIterator(this, context).getEndTime();
        return ((double) currentTime.getTimestamp() - startTime)
                / (endTime - startTime);
    }

    /**
     * Method seekEvent.
     *
     * @param location
     *            ITmfLocation<?>
     * @return ITmfContext
     * @since 3.0
     */
    @Override
    public synchronized ITmfContext seekEvent(final ITmfLocation location) {
        CtfLocation currentLocation = (CtfLocation) location;
        CtfTmfContext context = new CtfTmfContext(this);
        if (fTrace == null) {
            context.setLocation(null);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        }
        /*
         * The rank is set to 0 if the iterator seeks the beginning. If not, it
         * will be set to UNKNOWN_RANK, since CTF traces don't support seeking
         * by rank for now.
         */
        if (currentLocation == null) {
            currentLocation = new CtfLocation(new CtfLocationInfo(0L, 0L));
            context.setRank(0);
        }
        if (currentLocation.getLocationInfo() == CtfLocation.INVALID_LOCATION) {
            currentLocation = new CtfLocation(getEndTime().getValue() + 1, 0L);
        }
        context.setLocation(currentLocation);
        if (location == null) {
            CtfTmfEvent event = getIterator(this, context).getCurrentEvent();
            if (event != null) {
                currentLocation = new CtfLocation(event.getTimestamp().getValue(), 0);
            }
        }
        if (context.getRank() != 0) {
            context.setRank(ITmfContext.UNKNOWN_RANK);
        }
        return context;
    }

    @Override
    public synchronized ITmfContext seekEvent(double ratio) {
        CtfTmfContext context = new CtfTmfContext(this);
        if (fTrace == null) {
            context.setLocation(null);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        }
        final long end = this.getEndTime().getValue();
        final long start = this.getStartTime().getValue();
        final long diff = end - start;
        final long ratioTs = Math.round(diff * ratio) + start;
        context.seek(ratioTs);
        context.setRank(ITmfContext.UNKNOWN_RANK);
        return context;
    }

    /**
     * Method readNextEvent.
     *
     * @param context
     *            ITmfContext
     * @return CtfTmfEvent
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getNext(ITmfContext)
     */
    @Override
    public synchronized CtfTmfEvent getNext(final ITmfContext context) {
        if (fTrace == null) {
            return null;
        }
        CtfTmfEvent event = null;
        if (context instanceof CtfTmfContext) {
            if (context.getLocation() == null || CtfLocation.INVALID_LOCATION.equals(context.getLocation().getLocationInfo())) {
                return null;
            }
            CtfTmfContext ctfContext = (CtfTmfContext) context;
            event = ctfContext.getCurrentEvent();

            if (event != null) {
                updateAttributes(context, event.getTimestamp());
                ctfContext.advance();
                ctfContext.increaseRank();
            }
        }

        return event;
    }

    /**
     * gets the CTFtrace that this is wrapping
     *
     * @return the CTF trace
     */
    public CTFTrace getCTFTrace() {
        return fTrace;
    }

    /**
     * Ctf traces have a clock with a unique uuid that will be used to identify
     * the host. Traces with the same clock uuid will be known to have been made
     * on the same machine.
     *
     * Note: uuid is an optional field, it may not be there for a clock.
     */
    @Override
    public String getHostId() {
        CTFClock clock = getCTFTrace().getClock();
        if (clock != null) {
            String clockHost = (String) clock.getProperty(CLOCK_HOST_PROPERTY);
            if (clockHost != null) {
                return clockHost;
            }
        }
        return super.getHostId();
    }

    // -------------------------------------------
    // ITmfTraceProperties
    // -------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public Map<String, String> getTraceProperties() {
        return fTrace.getEnvironment();
    }

    // -------------------------------------------
    // Clocks
    // -------------------------------------------

    /**
     * gets the clock offset
     *
     * @return the clock offset in ns
     */
    public long getOffset() {
        if (fTrace != null) {
            return fTrace.getOffset();
        }
        return 0;
    }

    /**
     * Returns whether or not an event is in the metadata of the trace,
     * therefore if it can possibly be in the trace. It does not verify whether
     * or not the event is actually in the trace
     *
     * @param eventName
     *            The name of the event to check
     * @return Whether the event is in the metadata or not
     * @since 2.1
     */
    public boolean hasEvent(final String eventName) {
        Map<Long, IEventDeclaration> events = fTrace.getEvents(0L);
        if (events != null) {
            for (IEventDeclaration decl : events.values()) {
                if (decl.getName().equals(eventName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return whether all requested events are in the metadata
     *
     * @param names
     *            The array of events to check for
     * @return Whether all events are in the metadata
     * @since 2.1
     */
    public boolean hasAllEvents(String[] names) {
        for (String name : names) {
            if (!hasEvent(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the metadata contains at least one of the requested
     * events
     *
     * @param names
     *            The array of event names of check for
     * @return Whether one of the event is present in trace metadata
     * @since 2.1
     */
    public boolean hasAtLeastOneOfEvents(String[] names) {
        for (String name : names) {
            if (hasEvent(name)) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------
    // Parser
    // -------------------------------------------

    @Override
    public CtfTmfEvent parseEvent(ITmfContext context) {
        CtfTmfEvent event = null;
        if (context instanceof CtfTmfContext) {
            final ITmfContext tmpContext = seekEvent(context.getLocation());
            event = getNext(tmpContext);
        }
        return event;
    }

    /**
     * Sets the cache size for a CtfTmfTrace.
     */
    protected void setCacheSize() {
        setCacheSize(DEFAULT_CACHE_SIZE);
    }

    // -------------------------------------------
    // Helpers
    // -------------------------------------------

    private static CtfIterator getIterator(CtfTmfTrace trace, CtfTmfContext context) {
        return CtfIteratorManager.getIterator(trace, context);
    }

    /**
     * Get an iterator to the trace
     *
     * @return an iterator to the trace
     * @since 2.0
     */
    public CtfIterator createIterator() {
        try {
            return new CtfIterator(this);
        } catch (CTFReaderException e) {
            Activator.logError(e.getMessage(), e);
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Timestamp transformation functions
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public CtfTmfTimestamp createTimestamp(long ts) {
        return new CtfTmfTimestamp(getTimestampTransform().transform(ts));
    }

    private static int fCheckpointSize = -1;

    /**
     * @since 3.0
     */
    @Override
    public synchronized int getCheckpointSize() {
        if (fCheckpointSize == -1) {
            TmfCheckpoint c = new TmfCheckpoint(new CtfTmfTimestamp(0), new CtfLocation(0, 0), 0);
            ByteBuffer b = ByteBuffer.allocate(ITmfCheckpoint.MAX_SERIALIZE_SIZE);
            b.clear();
            c.serialize(b);
            fCheckpointSize = b.position();
        }

        return fCheckpointSize;
    }

    @Override
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfBTreeTraceIndexer(this, interval);
    }

    /**
     * @since 3.0
     */
    @Override
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new CtfLocation(bufferIn);
    }
}
