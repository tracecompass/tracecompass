/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson, École Polytechnique de Montréal
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

package org.eclipse.tracecompass.tmf.ctf.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.CTFCallsite;
import org.eclipse.tracecompass.ctf.core.event.CTFClock;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.internal.tmf.ctf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator.CtfIterator;
import org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator.CtfIteratorManager;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceProperties;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceWithPreDefinedEvents;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.CtfConstants;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocationInfo;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfTmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventType;
import org.eclipse.tracecompass.tmf.ctf.core.event.aspect.CtfChannelAspect;
import org.eclipse.tracecompass.tmf.ctf.core.event.aspect.CtfCpuAspect;
import org.eclipse.tracecompass.tmf.ctf.core.event.lookup.CtfTmfCallsite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * The CTf trace handler
 *
 * @version 1.0
 * @author Matthew khouzam
 */
public class CtfTmfTrace extends TmfTrace
        implements ITmfTraceProperties, ITmfPersistentlyIndexable,
        ITmfTraceWithPreDefinedEvents, AutoCloseable {

    // -------------------------------------------
    // Constants
    // -------------------------------------------

    /**
     * Default cache size for CTF traces
     */
    protected static final int DEFAULT_CACHE_SIZE = 50000;

    /**
     * Event aspects available for all CTF traces
     * @since 1.0
     */
    protected static final @NonNull Collection<ITmfEventAspect> CTF_ASPECTS =
            checkNotNull(ImmutableList.of(
                    ITmfEventAspect.BaseAspects.TIMESTAMP,
                    new CtfChannelAspect(),
                    new CtfCpuAspect(),
                    ITmfEventAspect.BaseAspects.EVENT_TYPE,
                    ITmfEventAspect.BaseAspects.CONTENTS
                    ));

    /**
     * The Ctf clock unique identifier field
     */
    private static final String CLOCK_HOST_PROPERTY = "uuid"; //$NON-NLS-1$
    private static final int CONFIDENCE = 10;

    // -------------------------------------------
    // Fields
    // -------------------------------------------

    private final Map<String, CtfTmfEventType> fContainedEventTypes =
            Collections.synchronizedMap(new HashMap<String, CtfTmfEventType>());

    private final CtfIteratorManager fIteratorManager =
            new CtfIteratorManager(this);

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
            /*
             * Register every event type. When you call getType, it will
             * register a trace to that type in the TmfEventTypeManager
             */
            try (CtfIterator iter = fIteratorManager.getIterator(ctx)) {
                for (IEventDeclaration ied : iter.getEventDeclarations()) {
                    CtfTmfEventType ctfTmfEventType = fContainedEventTypes.get(ied.getName());
                    if (ctfTmfEventType == null) {
                        List<ITmfEventField> content = new ArrayList<>();
                        /* Should only return null the first time */
                        for (String fieldName : ied.getFields().getFieldsList()) {
                            content.add(new TmfEventField(fieldName, null, null));
                        }
                        ITmfEventField contentTree = new TmfEventField(
                                ITmfEventField.ROOT_FIELD_ID,
                                null,
                                content.toArray(new ITmfEventField[content.size()])
                                );

                        ctfTmfEventType = new CtfTmfEventType(ied.getName(), contentTree);
                        fContainedEventTypes.put(ctfTmfEventType.getName(), ctfTmfEventType);
                    }
                }
            }
        } catch (final CTFException e) {
            /*
             * If it failed at the init(), we can assume it's because the file
             * was not found or was not recognized as a CTF trace. Throw into
             * the new type of exception expected by the rest of TMF.
             */
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        dispose();
    }

    @Override
    public synchronized void dispose() {
        fIteratorManager.dispose();
        if (fTrace != null) {
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
        try {
            final CTFTrace trace = new CTFTrace(path);
            if (!trace.majorIsSet()) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CtfTmfTrace_MajorNotSet);
            }
            try (CTFTraceReader ctfTraceReader = new CTFTraceReader(trace)) {
                if (!ctfTraceReader.hasMoreEvents()) {
                    // TODO: This will need an additional check when we
                    // support live traces
                    // because having no event is valid for a live trace
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CtfTmfTrace_NoEvent);
                }
            }
            return new CtfTraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID, trace.getEnvironment());
        } catch (final CTFException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CtfTmfTrace_ReadingError + ": " + e.toString()); //$NON-NLS-1$
        } catch (final BufferOverflowException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CtfTmfTrace_ReadingError + ": " + Messages.CtfTmfTrace_BufferOverflowErrorMessage); //$NON-NLS-1$
        }
    }

    @Override
    public Iterable<ITmfEventAspect> getEventAspects() {
        return CTF_ASPECTS;
    }

    /**
     * Method getCurrentLocation. This is not applicable in CTF
     *
     * @return null, since the trace has no knowledge of the current location
     * @see org.eclipse.tracecompass.tmf.core.trace.ITmfTrace#getCurrentLocation()
     */
    @Override
    public ITmfLocation getCurrentLocation() {
        return null;
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        final CtfLocation curLocation = (CtfLocation) location;
        final CtfTmfContext context = new CtfTmfContext(this);
        context.setLocation(curLocation);
        context.seek(curLocation.getLocationInfo());
        final CtfLocationInfo currentTime = ((CtfLocationInfo) context.getLocation().getLocationInfo());
        final long startTime = fIteratorManager.getIterator(context).getStartTime();
        final long endTime = fIteratorManager.getIterator(context).getEndTime();
        return ((double) currentTime.getTimestamp() - startTime)
                / (endTime - startTime);
    }

    /**
     * Method seekEvent.
     *
     * @param location
     *            ITmfLocation<?>
     * @return ITmfContext
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
            currentLocation = new CtfLocation(fTrace.getCurrentEndTime() + 1, 0L);
        }
        context.setLocation(currentLocation);
        if (location == null) {
            long timestamp = fIteratorManager.getIterator(context).getCurrentTimestamp();
            currentLocation = new CtfLocation(timestamp, 0);
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
        final long end = fTrace.getCurrentEndTime();
        final long start = fTrace.getCurrentStartTime();
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
     * @see org.eclipse.tracecompass.tmf.core.trace.ITmfTrace#getNext(ITmfContext)
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
     * Ctf traces have a clock with a unique uuid that will be used to identify
     * the host. Traces with the same clock uuid will be known to have been made
     * on the same machine.
     *
     * Note: uuid is an optional field, it may not be there for a clock.
     */
    @Override
    public String getHostId() {
        CTFClock clock = fTrace.getClock();
        if (clock != null) {
            String clockHost = (String) clock.getProperty(CLOCK_HOST_PROPERTY);
            if (clockHost != null) {
                return clockHost;
            }
        }
        return super.getHostId();
    }

    /**
     * Get the first callsite that matches the event name
     *
     * @param eventName The event name to look for
     * @return The best callsite candidate
     */
    public @Nullable CtfTmfCallsite getCallsite(String eventName) {
        CTFCallsite callsite = fTrace.getCallsite(eventName);
        if (callsite != null) {
            return new CtfTmfCallsite(callsite);
        }
        return null;
    }

    /**
     * Get the closest matching callsite for given event name and instruction
     * pointer
     *
     * @param eventName
     *            The event name
     * @param ip
     *            The instruction pointer
     * @return The closest matching callsite
     */
    public @Nullable CtfTmfCallsite getCallsite(String eventName, long ip) {
        CTFCallsite calliste = fTrace.getCallsite(eventName, ip);
        if (calliste != null) {
            return new CtfTmfCallsite(calliste);
        }
        return null;
    }

    /**
     * Get the CTF environment variables defined in this CTF trace, in <name,
     * value> form. This comes from the trace's CTF metadata.
     *
     * @return The CTF environment
     */
    public Map<String, String> getEnvironment() {
        return fTrace.getEnvironment();
    }

    // -------------------------------------------
    // ITmfTraceProperties
    // -------------------------------------------

    @Override
    public Map<String, String> getTraceProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.putAll(fTrace.getEnvironment());
        properties.put(Messages.CtfTmfTrace_HostID, getHostId());
        return properties;
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
     * Convert a CTF timestamp in CPU cycles to its equivalent in nanoseconds
     * for this trace.
     *
     * @param cycles
     *            The timestamp in cycles
     * @return The timestamp in nanoseconds
     */
    public long timestampCyclesToNanos(long cycles) {
        return fTrace.timestampCyclesToNanos(cycles);
    }

    /**
     * Convert a CTF timestamp in nanoseconds to its equivalent in CPU cycles
     * for this trace.
     *
     * @param nanos
     *            The timestamp in nanoseconds
     * @return The timestamp in cycles
     */
    public long timestampNanoToCycles(long nanos) {
        return fTrace.timestampNanoToCycles(nanos);
    }

    /**
     * Gets the list of declared events
     */
    @Override
    public Set<CtfTmfEventType> getContainedEventTypes() {
        return ImmutableSet.copyOf(fContainedEventTypes.values());
    }

    /**
     * Register an event type to this trace.
     *
     * Public visibility so that {@link CtfTmfEvent#getType} can call it.
     *
     * FIXME This could probably be made cleaner?
     *
     * @param eventType
     *            The event type to register
     */
    public void registerEventType(CtfTmfEventType eventType) {
        fContainedEventTypes.put(eventType.getName(), eventType);
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
    // CtfIterator factory methods
    // -------------------------------------------

    /**
     * Get an iterator to the trace
     *
     * @return an iterator to the trace
     */
    public ITmfContext createIterator() {
        try {
            return new CtfIterator(fTrace, this);
        } catch (CTFException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Get an iterator to the trace, , which will initially point to the given
     * location/rank.
     *
     * @param ctfLocationData
     *            The initial timestamp the iterator will be pointing to
     * @param rank
     *            The initial rank
     * @return The new iterator
     */
    public ITmfContext createIterator(CtfLocationInfo ctfLocationData, long rank) {
        try {
            return new CtfIterator(fTrace, this, ctfLocationData, rank);
        } catch (CTFException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Create the 'CtfIterator' object from a CtfTmfContext.
     *
     * @param context
     *            The iterator will initially be pointing to this context
     * @return A new CtfIterator object
     * @since 1.0
     */
    public ITmfContext createIteratorFromContext(CtfTmfContext context) {
        return fIteratorManager.getIterator(context);
    }

    /**
     * Dispose an iterator that was create with
     * {@link #createIteratorFromContext}
     *
     * @param context
     *            The last context that was pointed to by the iterator (this is
     *            the 'key' to find the correct iterator to dispose).
     * @since 1.0
     */
    public void disposeContext(CtfTmfContext context) {
        fIteratorManager.removeIterator(context);
    }

    // ------------------------------------------------------------------------
    // Timestamp transformation functions
    // ------------------------------------------------------------------------

    /**
     * @since 1.0
     */
    @Override
    public @NonNull TmfNanoTimestamp createTimestamp(long ts) {
        return new TmfNanoTimestamp(getTimestampTransform().transform(ts));
    }

    private static int fCheckpointSize = -1;

    @Override
    public synchronized int getCheckpointSize() {
        if (fCheckpointSize == -1) {
            TmfCheckpoint c = new TmfCheckpoint(new TmfNanoTimestamp(0), new CtfLocation(0, 0), 0);
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

    @Override
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new CtfLocation(bufferIn);
    }

    @Override
    public boolean isComplete() {
        if (getResource() == null) {
            return true;
        }

        String host = null;
        String port = null;
        String sessionName = null;
        try {
            host = getResource().getPersistentProperty(CtfConstants.LIVE_HOST);
            port = getResource().getPersistentProperty(CtfConstants.LIVE_PORT);
            sessionName = getResource().getPersistentProperty(CtfConstants.LIVE_SESSION_NAME);
        } catch (CoreException e) {
            Activator.getDefault().logError(e.getMessage(), e);
            // Something happened to the resource, assume we won't get any more
            // data from it
            return true;
        }
        return host == null || port == null || sessionName == null;
    }

    @Override
    public void setComplete(final boolean isComplete) {
        super.setComplete(isComplete);
        try {
            if (isComplete) {
                getResource().setPersistentProperty(CtfConstants.LIVE_HOST, null);
                getResource().setPersistentProperty(CtfConstants.LIVE_PORT, null);
                getResource().setPersistentProperty(CtfConstants.LIVE_SESSION_NAME, null);
            }
        } catch (CoreException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
    }
}
