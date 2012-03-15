/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson, MontaVista Software
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *   Yufen Kuo       (ykuo@mvista.com) - add support to allow user specify trace library path
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.core.trace;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.lttng.core.TraceHelper;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEventContent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEventType;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngLocation;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.internal.lttng.core.exceptions.LttngException;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.utility.LiveTraceManager;
import org.eclipse.linuxtools.internal.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

class LTTngTraceException extends LttngException {
    static final long serialVersionUID = -1636648737081868146L;

    public LTTngTraceException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>LTTngTrace</u></b>
 * <p>
 * 
 * LTTng trace implementation. It accesses the C trace handling library (seeking, reading and parsing) through the JNI
 * component.
 */
public class LTTngTrace extends TmfTrace<LttngEvent> {

    public final static boolean PRINT_DEBUG = false;
    public final static boolean UNIQUE_EVENT = true;

    private final static boolean SHOW_LTT_DEBUG_DEFAULT = false;
    private final static boolean IS_PARSING_NEEDED_DEFAULT = !UNIQUE_EVENT;
    private final static int CHECKPOINT_PAGE_SIZE = 50000;
    private final static long LTTNG_STREAMING_INTERVAL = 2000; // in ms

    // Reference to our JNI trace
    private JniTrace currentJniTrace;

    LttngTimestamp eventTimestamp;
    String eventSource;
    LttngEventContent eventContent;
    String eventReference;

    // The actual event
    LttngEvent currentLttngEvent;

    // The current location
    LttngLocation previousLocation;

    LttngEventType eventType;

    // Hashmap of the possible types of events (Tracefile/CPU/Marker in the JNI)
    HashMap<Integer, LttngEventType> traceTypes;
    
    // This vector will be used to quickly find a marker name from a position
    Vector<Integer> traceTypeNames;
    
    private String traceLibPath;

    private long fStreamingInterval = 0;

    public LTTngTrace() {
    }

    @Override
    public boolean validate(IProject project, String path) {
        if (super.validate(project, path)) {
            String traceLibPath = TraceHelper.getTraceLibDirFromProject(project);
            try {
                LTTngTraceVersion version = new LTTngTraceVersion(path, traceLibPath);
                return version.isValidLttngTrace();
            } catch (LttngException e) {
            }
        }
        return false;
    }

    @Override
    public void initTrace(String name, String path, Class<LttngEvent> eventType) throws FileNotFoundException {
        initLTTngTrace(name, path, eventType, CHECKPOINT_PAGE_SIZE, false);
    }

    @Override
    public void initTrace(String name, String path, Class<LttngEvent> eventType, int cacheSize) throws FileNotFoundException {
        initLTTngTrace(name, path, eventType, cacheSize, false);
    }

    @Override
    public void initTrace(String name, String path, Class<LttngEvent> eventType, boolean indexTrace) throws FileNotFoundException {
        initLTTngTrace(name, path, eventType, CHECKPOINT_PAGE_SIZE, indexTrace);
    }

    @Override
    public void initTrace(String name, String path, Class<LttngEvent> eventType, int cacheSize, boolean indexTrace) throws FileNotFoundException {
        initLTTngTrace(name, path, eventType, cacheSize, indexTrace);
    }

    private synchronized void initLTTngTrace(String name, String path, Class<LttngEvent> eventType, int cacheSize, boolean indexTrace) throws FileNotFoundException {
        super.initTrace(name, path, eventType, indexTrace);
        try {
            currentJniTrace = JniTraceFactory.getJniTrace(path, traceLibPath, SHOW_LTT_DEBUG_DEFAULT);
        } catch (Exception e) {
            throw new FileNotFoundException(e.getMessage());
        }

        // Export all the event types from the JNI side
        traceTypes = new HashMap<Integer, LttngEventType>();
        traceTypeNames = new Vector<Integer>();
        initialiseEventTypes(currentJniTrace);

        // Build the re-used event structure
        eventTimestamp = new LttngTimestamp();
        eventSource = ""; //$NON-NLS-1$
        this.eventType = new LttngEventType();
        eventContent = new LttngEventContent(currentLttngEvent);
        eventReference = getName();

        // Create the skeleton event
        currentLttngEvent = new LttngEvent(this, eventTimestamp, eventSource, this.eventType, eventContent,
                eventReference, null);

        // Create a new current location
        previousLocation = new LttngLocation();

        // Set the currentEvent to the eventContent
        eventContent.setEvent(currentLttngEvent);

        // // Bypass indexing if asked
        // if ( bypassIndexing == false ) {
        // indexTrace(true);
        // }
        // else {
        // Even if we don't have any index, set ONE checkpoint
        // fCheckpoints.add(new TmfCheckpoint(new LttngTimestamp(0L) , new
        // LttngLocation() ) );

        initializeStreamingMonitor();
    }

    private void initializeStreamingMonitor() {
        JniTrace jniTrace = getCurrentJniTrace();
        if (jniTrace == null || (!jniTrace.isLiveTraceSupported() || !LiveTraceManager.isLiveTrace(jniTrace.getTracepath()))) {
            // Set the time range of the trace
            TmfContext context = seekLocation(null);
            LttngEvent event = getNextEvent(context);
            LttngTimestamp startTime = new LttngTimestamp(event.getTimestamp());
            LttngTimestamp endTime = new LttngTimestamp(currentJniTrace.getEndTime().getTime());
            setTimeRange(new TmfTimeRange(startTime, endTime));
            TmfTraceUpdatedSignal signal = new TmfTraceUpdatedSignal(this, this, getTimeRange());
            broadcast(signal);
            return;
        }

        // Set the time range of the trace
        TmfContext context = seekLocation(null);
        LttngEvent event = getNextEvent(context);
        setEndTime(TmfTimestamp.BIG_BANG);
        final long startTime = event != null ? event.getTimestamp().getValue() : TmfTimestamp.BIG_BANG.getValue();
        fStreamingInterval = LTTNG_STREAMING_INTERVAL;

        final Thread thread = new Thread("Streaming Monitor for trace " + getName()) { //$NON-NLS-1$
            LttngTimestamp safeTimestamp = null;
            TmfTimeRange timeRange = null;

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                while (!fExecutor.isShutdown()) {
                    TmfExperiment<?> experiment = TmfExperiment.getCurrentExperiment();
                    if (experiment != null) {
                        @SuppressWarnings("rawtypes")
                        final TmfEventRequest request = new TmfEventRequest<TmfEvent>(TmfEvent.class, TmfTimeRange.ETERNITY, 0, ExecutionType.FOREGROUND) {
                            @Override
                            public void handleCompleted() {
                                updateJniTrace();
                            }
                        };
                        synchronized (experiment) {
                            experiment.sendRequest(request);
                        }
                        try {
                            request.waitForCompletion();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        updateJniTrace();
                    }
                    try {
                        Thread.sleep(LTTNG_STREAMING_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            private void updateJniTrace() {
                JniTrace jniTrace = getCurrentJniTrace();
                currentJniTrace.updateTrace();
                long endTime = jniTrace.getEndTime().getTime();
                LttngTimestamp startTimestamp = new LttngTimestamp(startTime);
                LttngTimestamp endTimestamp = new LttngTimestamp(endTime);
                if (safeTimestamp != null && safeTimestamp.compareTo(getTimeRange().getEndTime(), false) > 0) {
                    timeRange = new TmfTimeRange(startTimestamp, safeTimestamp);
                } else {
                    timeRange = null;
                }
                safeTimestamp = endTimestamp;
                if (timeRange != null) {
                    setTimeRange(timeRange);
                }
            }
        };
        thread.start();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.TmfTrace#getStreamingInterval()
     */
    @Override
    public long getStreamingInterval() {
        return fStreamingInterval;
    }

    /**
     * Default Constructor.
     * <p>
     *
     * @param name
     *            Name of the trace
     * @param path
     *            Path to a <b>directory</b> that contain an LTTng trace.
     * 
     * @exception Exception
     *                (most likely LTTngTraceException or FileNotFoundException)
     */
    public LTTngTrace(String name, String path) throws Exception {
        // Call with "wait for completion" true and "skip indexing" false
        this(name, path, null, true, false);
    }

    /**
     * Constructor, with control over the indexing.
     * <p>
     * 
     * @param name
     *            Name of the trace
     * @param path
     *            Path to a <b>directory</b> that contain an LTTng trace.
     * @param waitForCompletion
     *            Should we wait for indexign to complete before moving on.
     * 
     * @exception Exception
     *                (most likely LTTngTraceException or FileNotFoundException)
     */
    public LTTngTrace(String name, String path, boolean waitForCompletion) throws Exception {
        // Call with "skip indexing" false
        this(name, path, null, waitForCompletion, true);
    }

    /**
     * Default constructor, with control over the indexing and possibility to bypass indexation
     * <p>
     * 
     * @param name
     *            Name of the trace
     * @param path
     *            Path to a <b>directory</b> that contain an LTTng trace.
     * @param traceLibPath
     *            Path to a <b>directory</b> that contains LTTng trace libraries.
     * @param waitForCompletion
     *            Should we wait for indexign to complete before moving on.
     * @param bypassIndexing
     *            Should we bypass indexing completly? This is should only be useful for unit testing.
     * 
     * @exception Exception
     *                (most likely LTTngTraceException or FileNotFoundException)
     * 
     */
    public LTTngTrace(String name, String path, String traceLibPath, boolean waitForCompletion, boolean bypassIndexing)
            throws Exception {
        super(name, LttngEvent.class, path, CHECKPOINT_PAGE_SIZE, false);
        initTrace(name, path, LttngEvent.class, !bypassIndexing);
        this.traceLibPath = traceLibPath;
    }

    /*
     * Copy constructor is forbidden for LttngEvenmStream
     */
    public LTTngTrace(LTTngTrace other) throws Exception {
        this(other.getName(), other.getPath(), other.getTraceLibPath(), false, true);
        this.fCheckpoints = other.fCheckpoints;
        setTimeRange(new TmfTimeRange(new LttngTimestamp(other.getStartTime()), new LttngTimestamp(other.getEndTime())));
    }

    @Override
    public LTTngTrace copy() {
        LTTngTrace returnedTrace = null;

        try {
            returnedTrace = new LTTngTrace(this);
        } catch (Exception e) {
            System.out.println("ERROR : Could not create LTTngTrace copy (createTraceCopy)."); //$NON-NLS-1$
            e.printStackTrace();
        }

        return returnedTrace;
    }

    @Override
    public synchronized LTTngTrace clone() {
        LTTngTrace clone = null;
        try {
            clone = (LTTngTrace) super.clone();
            try {
                clone.currentJniTrace = JniTraceFactory.getJniTrace(getPath(), getTraceLibPath(),
                        SHOW_LTT_DEBUG_DEFAULT);
            } catch (JniException e) {
                // e.printStackTrace();
            }

            // Export all the event types from the JNI side
            clone.traceTypes = new HashMap<Integer, LttngEventType>();
            clone.traceTypeNames = new Vector<Integer>();
            clone.initialiseEventTypes(clone.currentJniTrace);

            // Verify that all those "default constructor" are safe to use
            clone.eventTimestamp = new LttngTimestamp();
            clone.eventSource = ""; //$NON-NLS-1$
            clone.eventType = new LttngEventType();
            clone.eventContent = new LttngEventContent(clone.currentLttngEvent);
            clone.eventReference = getName();

            // Create the skeleton event
            clone.currentLttngEvent = new LttngEvent(this, clone.eventTimestamp, clone.eventSource, clone.eventType,
                    clone.eventContent, clone.eventReference, null);

            // Create a new current location
            clone.previousLocation = new LttngLocation();

            // Set the currentEvent to the eventContent
            clone.eventContent.setEvent(clone.currentLttngEvent);

            // Set the start time of the trace
            setTimeRange(new TmfTimeRange(new LttngTimestamp(clone.currentJniTrace.getStartTime().getTime()),
                    new LttngTimestamp(clone.currentJniTrace.getEndTime().getTime())));
        } catch (CloneNotSupportedException e) {
        }

        return clone;
    }

    public String getTraceLibPath() {
        return traceLibPath;
    }

    /*
     * Fill out the HashMap with "Type" (Tracefile/Marker)
     * 
     * This should be called at construction once the trace is open
     */
    private void initialiseEventTypes(JniTrace trace) {
        // Work variables
        LttngEventType tmpType = null;
        String[] markerFieldsLabels = null;

        String newTracefileKey = null;
        Integer newMarkerKey = null;

        JniTracefile newTracefile = null;
        JniMarker newMarker = null;

        // First, obtain an iterator on TRACEFILES of owned by the TRACE
        Iterator<String> tracefileItr = trace.getTracefilesMap().keySet().iterator();

        while (tracefileItr.hasNext()) {
            newTracefileKey = tracefileItr.next();
            newTracefile = trace.getTracefilesMap().get(newTracefileKey);

            // From the TRACEFILE read, obtain its MARKER
            Iterator<Integer> markerItr = newTracefile.getTracefileMarkersMap().keySet().iterator();
            while (markerItr.hasNext()) {
                newMarkerKey = markerItr.next();
                newMarker = newTracefile.getTracefileMarkersMap().get(newMarkerKey);

                // From the MARKER we can obtain the MARKERFIELDS keys (i.e.
                // labels)
                markerFieldsLabels = newMarker.getMarkerFieldsHashMap().keySet()
                        .toArray(new String[newMarker.getMarkerFieldsHashMap().size()]);

                tmpType = new LttngEventType(newTracefile.getTracefileName(), newTracefile.getCpuNumber(),
                        newMarker.getName(), newMarkerKey.intValue(), markerFieldsLabels);

                // Add the type to the map/vector
                addEventTypeToMap(tmpType);
            }
        }
    }

    /*
     * Add a new type to the HashMap
     * 
     * As the hashmap use a key format that is a bit dangerous to use, we should always add using this function.
     */
    private void addEventTypeToMap(LttngEventType newEventType) {
        int newTypeKey = EventTypeKey.getEventTypeHash(newEventType);

        this.traceTypes.put(newTypeKey, newEventType);
        this.traceTypeNames.add(newTypeKey);
    }

    /**
     * Return the latest saved location. Note : Modifying the returned location may result in buggy positionning!
     * 
     * @return The LttngLocation as it was after the last operation.
     * 
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngLocation
     */
    @Override
    public synchronized ITmfLocation<?> getCurrentLocation() {
        return previousLocation;
    }

    /**
     * Position the trace to the event at the given location.
     * <p>
     * NOTE : Seeking by location is very fast compare to seeking by position but is still slower than "ReadNext", avoid
     * using it for small interval.
     * 
     * @param location
     *            Location of the event in the trace. If no event available at this exact location, we will position
     *            ourself to the next one.
     * 
     * @return The TmfContext that point to this event
     * 
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngLocation
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfContext
     */
    @Override
    public synchronized TmfContext seekLocation(ITmfLocation<?> location) {

        // // [lmcfrch]
        // lastTime = 0;

        if (PRINT_DEBUG) {
            System.out.println("seekLocation(location) location -> " + location); //$NON-NLS-1$
        }

        // If the location in context is null, create a new one
        LttngLocation curLocation = null;
        if (location == null) {
            curLocation = new LttngLocation();
            TmfContext context = seekEvent(curLocation.getOperationTime());
            context.setRank(ITmfContext.INITIAL_RANK);
            return context;
        } else {
            curLocation = (LttngLocation) location;
        }

        // *** NOTE :
        // Update to location should (and will) be done in SeekEvent.

        // The only seek valid in LTTng is with the time, we call
        // seekEvent(timestamp)
        TmfContext context = seekEvent(curLocation.getOperationTime());

        // If the location is marked with the read next flag
        // then it is pointing to the next event following the operation time
        if (curLocation.isLastOperationReadNext()) {
            getNextEvent(context);
        }

        return context;
    }

    /**
     * Position the trace to the event at the given time.
     * <p>
     * NOTE : Seeking by time is very fast compare to seeking by position but is still slower than "ReadNext", avoid
     * using it for small interval.
     * 
     * @param timestamp
     *            Time of the event in the trace. If no event available at this exact time, we will position ourself to
     *            the next one.
     * 
     * @return The TmfContext that point to this event
     * 
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngLocation
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfContext
     */
    @Override
    public synchronized TmfContext seekEvent(ITmfTimestamp timestamp) {

        if (PRINT_DEBUG) {
            System.out.println("seekEvent(timestamp) timestamp -> " + timestamp); //$NON-NLS-1$
        }

        // Call JNI to seek
        currentJniTrace.seekToTime(new JniTime(timestamp.getValue()));

        // Save the time at which we seeked
        previousLocation.setOperationTime(timestamp.getValue());
        // Set the operation marker as seek, to be able to detect we did "seek"
        // this event
        previousLocation.setLastOperationSeek();

        LttngLocation curLocation = new LttngLocation(previousLocation);

        return new TmfContext(curLocation);
    }

    /**
     * Position the trace to the event at the given position (rank).
     * <p>
     * NOTE : Seeking by position is very slow in LTTng, consider seeking by timestamp.
     * 
     * @param position
     *            Position (or rank) of the event in the trace, starting at 0.
     * 
     * @return The TmfContext that point to this event
     * 
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngLocation
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfContext
     */
    @Override
    public synchronized TmfContext seekEvent(long position) {

        if (PRINT_DEBUG) {
            System.out.println("seekEvent(position) position -> " + position); //$NON-NLS-1$
        }

        ITmfTimestamp timestamp = null;
        long index = position / getCacheSize();

        // Get the timestamp of the closest check point to the given position
        if (fCheckpoints.size() > 0) {
            if (index >= fCheckpoints.size()) {
                index = fCheckpoints.size() - 1;
            }
            timestamp = fCheckpoints.elementAt((int) index).getTimestamp();
        }
        // If none, take the start time of the trace
        else {
            timestamp = getStartTime();
        }

        // Seek to the found time
        TmfContext tmpContext = seekEvent(timestamp);
        tmpContext.setRank((index + 1) * fIndexPageSize);
        previousLocation = (LttngLocation) tmpContext.getLocation();

        // Ajust the index of the event we found at this check point position
        Long currentPosition = index * getCacheSize();

        Long lastTimeValueRead = 0L;

        // Get the event at current position. This won't move to the next one
        JniEvent tmpJniEvent = currentJniTrace.findNextEvent();
        // Now that we are positionned at the checkpoint,
        // we need to "readNext" (Position - CheckpointPosition) times or until
        // trace "run out"
        while ((tmpJniEvent != null) && (currentPosition < position)) {
            tmpJniEvent = currentJniTrace.readNextEvent();
            currentPosition++;
        }

        // If we found our event, save its timestamp
        if (tmpJniEvent != null) {
            lastTimeValueRead = tmpJniEvent.getEventTime().getTime();
        }

        // Set the operation marker as seek, to be able to detect we did "seek"
        // this event
        previousLocation.setLastOperationSeek();
        // Save read event time
        previousLocation.setOperationTime(lastTimeValueRead);

        // *** VERIFY ***
        // Is that too paranoid?
        //
        // We don't trust what upper level could do with our internal location
        // so we create a new one to return instead
        LttngLocation curLocation = new LttngLocation(previousLocation);

        return new TmfContext(curLocation);
    }

    @Override
    public TmfContext seekLocation(double ratio) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getLocationRatio(ITmfLocation<?> location) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Return the event in the trace according to the given context. Read it if necessary.
     * <p>
     * Similar (same?) as ParseEvent except that calling GetNext twice read the next one the second time.
     * 
     * @param context
     *            Current TmfContext where to get the event
     * 
     * @return The LttngEvent we read of null if no event are available
     * 
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngLocation
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfContext
     */

    public int nbEventsRead = 0;

    @Override
    public synchronized LttngEvent getNextEvent(ITmfContext context) {

        if (PRINT_DEBUG) {
            System.out.println("getNextEvent(context) context.getLocation() -> " //$NON-NLS-1$
                    + context.getLocation());
        }

        LttngEvent returnedEvent = null;
        LttngLocation curLocation = null;

        curLocation = (LttngLocation) context.getLocation();
        // If the location in context is null, create a new one
        if (curLocation == null) {
            curLocation = getCurrentLocation(context);
        }

        // *** Positioning trick :
        // GetNextEvent only read the trace if :
        // 1- The last operation was NOT a ParseEvent --> A read is required
        // OR
        // 2- The time of the previous location is different from the current
        // one --> A seek + a read is required
        if ((!(curLocation.isLastOperationParse()))
                || (previousLocation.getOperationTimeValue() != curLocation.getOperationTimeValue())) {
            if (previousLocation.getOperationTimeValue() != curLocation.getOperationTimeValue()) {
                if (PRINT_DEBUG) {
                    System.out.println("\t\tSeeking in getNextEvent. [ LastTime : " //$NON-NLS-1$
                            + previousLocation.getOperationTimeValue() + " CurrentTime" //$NON-NLS-1$
                            + curLocation.getOperationTimeValue() + " ]"); //$NON-NLS-1$
                }
                seekEvent(curLocation.getOperationTime());
            }
            // Read the next event from the trace. The last one will NO LONGER
            // BE VALID.
            returnedEvent = readNextEvent(curLocation);

        } else {
            // No event was read, just return the one currently loaded (the last
            // one we read)
            returnedEvent = currentLttngEvent;

            // Set the operation marker as read to both locations, to be able to
            // detect we need to read the next event
            previousLocation.setLastOperationReadNext();
            curLocation.setLastOperationReadNext();
        }

        // If we read an event, set it's time to the locations (both previous
        // and current)
        if (returnedEvent != null) {
            setPreviousAndCurrentTimes(context, returnedEvent, curLocation);
        }

        return returnedEvent;
    }

    // this method was extracted for profiling purposes
    private synchronized void setPreviousAndCurrentTimes(ITmfContext context, LttngEvent returnedEvent, LttngLocation curLocation) {

        ITmfTimestamp eventTimestamp = returnedEvent.getTimestamp();
        // long eventTime = eventTimestamp.getValue();
        previousLocation.setOperationTime(eventTimestamp.getValue());
        curLocation.setOperationTime(eventTimestamp.getValue());
        updateIndex(context, context.getRank(), eventTimestamp);
        context.updateRank(1);
    }

    protected void updateIndex(TmfContext context, long rank, ITmfTimestamp timestamp) {

        if (getStartTime().compareTo(timestamp, false) > 0)
            setStartTime(timestamp);
        if (getEndTime().compareTo(timestamp, false) < 0)
            setEndTime(timestamp);
        if (rank != ITmfContext.UNKNOWN_RANK) {
            if (fNbEvents <= rank)
                fNbEvents = rank + 1;
            // Build the index as we go along
            if ((rank % fIndexPageSize) == 0) {
                // Determine the table position
                long position = rank / fIndexPageSize;
                // Add new entry at proper location (if empty)
                if (fCheckpoints.size() == position) {
                    addCheckPoint(context, timestamp);
                }
            }
        }
    }

    private void addCheckPoint(TmfContext context, ITmfTimestamp timestamp) {
        ITmfLocation<?> location = context.getLocation().clone();
        fCheckpoints.add(new TmfCheckpoint(timestamp.clone(), location));
    }

    // this method was extracted for profiling purposes
    private synchronized LttngEvent readNextEvent(LttngLocation curLocation) {
        LttngEvent returnedEvent;
        // Read the next event from the trace. The last one will NO LONGER BE
        // VALID.
        returnedEvent = readEvent(curLocation);
        nbEventsRead++;

        // Set the operation marker as read to both locations, to be able to
        // detect we need to read the next event
        previousLocation.setLastOperationReadNext();
        curLocation.setLastOperationReadNext();
        return returnedEvent;
    }

    // this method was extracted for profiling purposes
    private LttngLocation getCurrentLocation(ITmfContext context) {
        LttngLocation curLocation;
        curLocation = new LttngLocation();
        context.setLocation(curLocation);
        return curLocation;
    }

    /**
     * Return the event in the trace according to the given context. Read it if necessary.
     * <p>
     * Similar (same?) as GetNextEvent except that calling ParseEvent twice will return the same event
     * 
     * @param context
     *            Current TmfContext where to get the event
     * 
     * @return The LttngEvent we read of null if no event are available
     * 
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngLocation
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfContext
     */
    @Override
    public synchronized LttngEvent parseEvent(ITmfContext context) {

        if (PRINT_DEBUG) {
            System.out.println("parseEvent(context) context.getLocation() -> " //$NON-NLS-1$
                    + context.getLocation());
        }

        LttngEvent returnedEvent = null;
        LttngLocation curLocation = null;

        // If the location in context is null, create a new one
        if (context.getLocation() == null) {
            curLocation = new LttngLocation();
            context.setLocation(curLocation);
        }
        // Otherwise, we use the one in context; it should be a valid
        // LttngLocation
        else {
            curLocation = (LttngLocation) context.getLocation();
        }

        // *** HACK ***
        // TMF assumes it is possible to read (GetNextEvent) to the next Event
        // once ParseEvent() is called
        // In LTTNG, there is not difference between "Parsing" and "Reading" an
        // event.
        // So, before "Parsing" an event, we have to make sure we didn't "Read"
        // it alreafy.
        // Also, "Reading" invalidate the previous Event in LTTNG and seek back
        // is very costly,
        // so calling twice "Parse" will return the same event, giving a way to
        // get the "Currently loaded" event

        // *** Positionning trick :
        // ParseEvent only read the trace if :
        // 1- The last operation was NOT a ParseEvent --> A read is required
        // OR
        // 2- The time of the previous location is different from the current
        // one --> A seek + a read is required
        if (!curLocation.isLastOperationParse()
                || (previousLocation.getOperationTimeValue() != curLocation.getOperationTimeValue())) {
            // Previous time != Current time : We need to reposition to the
            // current time
            if (previousLocation.getOperationTimeValue() != curLocation.getOperationTimeValue()) {
                if (PRINT_DEBUG) {
                    System.out.println("\t\tSeeking in getNextEvent. [ LastTime : " //$NON-NLS-1$
                            + previousLocation.getOperationTimeValue() + " CurrentTime" //$NON-NLS-1$
                            + curLocation.getOperationTimeValue() + " ]"); //$NON-NLS-1$
                }
                seekEvent(curLocation.getOperationTime());
            }

            // Read the next event from the trace. The last one will NO LONGER
            // BE VALID.
            returnedEvent = readEvent(curLocation);
        } else {
            // No event was read, just return the one currently loaded (the last
            // one we read)
            returnedEvent = currentLttngEvent;
        }

        // If we read an event, set it's time to the locations (both previous
        // and current)
        if (returnedEvent != null) {
            previousLocation.setOperationTime((LttngTimestamp) returnedEvent.getTimestamp());
            curLocation.setOperationTime((LttngTimestamp) returnedEvent.getTimestamp());
        }

        // Set the operation marker as parse to both location, to be able to
        // detect we already "read" this event
        previousLocation.setLastOperationParse();
        curLocation.setLastOperationParse();

        return returnedEvent;
    }

    /*
     * Read the next event from the JNI and convert it as Lttng Event<p>
     * 
     * @param location Current LttngLocation that to be updated with the event timestamp
     * 
     * @return The LttngEvent we read of null if no event are available
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngLocation
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    private synchronized LttngEvent readEvent(LttngLocation location) {
        LttngEvent returnedEvent = null;
        JniEvent tmpEvent = null;

        // Read the next event from JNI. THIS WILL INVALIDATE THE CURRENT LTTNG
        // EVENT.
        tmpEvent = currentJniTrace.readNextEvent();

        if (tmpEvent != null) {
            // *** NOTE
            // Convert will update the currentLttngEvent
            returnedEvent = convertJniEventToTmf(tmpEvent);

            location.setOperationTime((LttngTimestamp) returnedEvent.getTimestamp());
        }
        // *** NOTE
        // If the read failed (likely the last event in the trace), set the
        // LastReadTime to the JNI time
        // That way, even if we try to read again, we will step over the bogus
        // seek and read
        else {
            location.setOperationTime(getEndTime().getValue() + 1);
        }

        return returnedEvent;
    }

    /**
     * Method to convert a JniEvent into a LttngEvent.
     * <p>
     * 
     * Note : This method will call LttngEvent convertEventJniToTmf(JniEvent, boolean) with a default value for
     * isParsingNeeded
     * 
     * @param newEvent
     *            The JniEvent to convert into LttngEvent
     * 
     * @return The converted LttngEvent
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent
     */
    public synchronized LttngEvent convertJniEventToTmf(JniEvent newEvent) {
        currentLttngEvent = convertJniEventToTmf(newEvent, IS_PARSING_NEEDED_DEFAULT);

        return currentLttngEvent;
    }

    /**
     * Method to convert a JniEvent into a LttngEvent
     * 
     * @param jniEvent
     *            The JniEvent to convert into LttngEvent
     * @param isParsingNeeded
     *            A boolean value telling if the event should be parsed or not.
     * 
     * @return The converted LttngEvent
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent
     */
    public synchronized LttngEvent convertJniEventToTmf(JniEvent jniEvent, boolean isParsingNeeded) {

        if (UNIQUE_EVENT) {

            // ***
            // UNHACKED : We can no longer do that because TCF need to maintain
            // several events at once.
            // This is very slow to do so in LTTng, this has to be temporary.
            // *** HACK ***
            // To save time here, we only set value instead of allocating new
            // object
            // This give an HUGE performance improvement
            // all allocation done in the LttngTrace constructor
            // ***
            eventTimestamp.setValue(jniEvent.getEventTime().getTime());
            eventSource = jniEvent.requestEventSource();

            eventType = traceTypes.get(EventTypeKey.getEventTypeHash(jniEvent));

            String fullTracePath = getName();
            String reference = fullTracePath.substring(fullTracePath.lastIndexOf('/') + 1);
            currentLttngEvent.setReference(reference);

            eventContent.emptyContent();

            currentLttngEvent.setType(eventType);
            // Save the jni reference
            currentLttngEvent.updateJniEventReference(jniEvent);

            // Parse now if was asked
            // Warning : THIS IS SLOW
            if (isParsingNeeded) {
                eventContent.getFields();
            }

            return currentLttngEvent;
        } else {
            return convertJniEventToTmfMultipleEventEvilFix(jniEvent, isParsingNeeded);
        }

    }

    /**
     * This method is a temporary fix to support multiple events at once in TMF This is expected to be slow and should
     * be fixed in another way. See comment in convertJniEventToTmf();
     * 
     * @param jniEvent
     *            The current JNI Event
     * @return Current Lttng Event fully parsed
     */
    private synchronized LttngEvent convertJniEventToTmfMultipleEventEvilFix(JniEvent jniEvent, boolean isParsingNeeded) {
        // *** HACK ***
        // Below : the "fix" with all the new and the full-parse
        // Allocating new memory is slow.
        // Parsing every events is very slow.
        eventTimestamp = new LttngTimestamp(jniEvent.getEventTime().getTime());
        eventSource = jniEvent.requestEventSource();
        eventReference = getName();
        eventType = new LttngEventType(traceTypes.get(EventTypeKey.getEventTypeHash(jniEvent)));
        eventContent = new LttngEventContent(currentLttngEvent);
        currentLttngEvent = new LttngEvent(this, eventTimestamp, eventSource, eventType, eventContent, eventReference,
                null);

        // The jni reference is no longer reliable but we will keep it anyhow
        currentLttngEvent.updateJniEventReference(jniEvent);
        // Ensure that the content is correctly set
        eventContent.setEvent(currentLttngEvent);

        // Parse the event if it was needed
        // *** WARNING ***
        // ONLY for testing, NOT parsing events with non-unique events WILL
        // result in segfault in the JVM
        if (isParsingNeeded) {
            eventContent.getFields();
        }

        return currentLttngEvent;
    }

    /**
     * Reference to the current LttngTrace we are reading from.
     * <p>
     * 
     * Note : This bypass the framework and should not be use, except for testing!
     * 
     * @return Reference to the current LttngTrace
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    public JniTrace getCurrentJniTrace() {
        return currentJniTrace;
    }

    /**
     * Return a reference to the current LttngEvent we have in memory.
     * 
     * @return The current (last read) LttngEvent
     * 
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent
     */
    public synchronized LttngEvent getCurrentEvent() {
        return currentLttngEvent;
    }

    /**
     * Get the major version number for the current trace
     * 
     * @return Version major or -1 if unknown
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     * 
     */
    public short getVersionMajor() {
        if (currentJniTrace != null) {
            return currentJniTrace.getLttMajorVersion();
        } else {
            return -1;
        }
    }

    /**
     * Get the minor version number for the current trace
     * 
     * @return Version minor or -1 if unknown
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     * 
     */
    public short getVersionMinor() {
        if (currentJniTrace != null) {
            return currentJniTrace.getLttMinorVersion();
        } else {
            return -1;
        }
    }

    /**
     * Get the number of CPU for this trace
     * 
     * @return Number of CPU or -1 if unknown
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     * 
     */
    public int getCpuNumber() {
        if (currentJniTrace != null) {
            return currentJniTrace.getCpuNumber();
        } else {
            return -1;
        }
    }

    /**
     * Print the content of the checkpoint vector.
     * <p>
     * 
     * This is intended for debug purpose only.
     */
    public void printCheckpointsVector() {
        System.out.println("StartTime : " //$NON-NLS-1$
                + getTimeRange().getStartTime().getValue());
        System.out.println("EndTime   : " //$NON-NLS-1$
                + getTimeRange().getEndTime().getValue());

        for (int pos = 0; pos < fCheckpoints.size(); pos++) {
            System.out.print(pos + ": " + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
            System.out.print(fCheckpoints.get(pos).getTimestamp() + "\t"); //$NON-NLS-1$
            System.out.println(fCheckpoints.get(pos).getLocation());
        }
    }

    @Override
    public synchronized void dispose() {
        if (currentJniTrace != null)
            currentJniTrace.closeTrace();
        super.dispose();
    }

    /**
     * Return a String identifying this trace.
     * 
     * @return String that identify this trace
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        String returnedData = "";

        returnedData += "Path :" + getPath() + " ";
        returnedData += "Trace:" + currentJniTrace + " ";
        returnedData += "Event:" + currentLttngEvent;

        return returnedData;
    }

}

/*
 * EventTypeKey inner class
 * 
 * This class is used to make the process of generating the HashMap key more transparent and so less error prone to use
 */
final class EventTypeKey {
    // *** WARNING ***
    // These two getEventTypeKey() functions should ALWAYS construct the key the
    // same ways!
    // Otherwise, every type search will fail!

    // added final to encourage inlining.

    // generating a hash code by hand to avoid a string creation
    final static public int getEventTypeHash(LttngEventType newEventType) {
        return generateHash(newEventType.getTracefileName(), newEventType.getCpuId(), newEventType.getMarkerName());
    }

    final private static int generateHash(String traceFileName, long cpuNumber, String markerName) {
        // 0x1337 is a prime number. The number of CPUs is always under 8192 on
        // the current kernel, so this will work with the current linux kernel.
        int cpuHash = (int) (cpuNumber * (0x1337));
        return traceFileName.hashCode() ^ (cpuHash) ^ markerName.hashCode();
    }

    // generating a hash code by hand to avoid a string creation
    final static public int getEventTypeHash(JniEvent newEvent) {
        return generateHash(newEvent.getParentTracefile().getTracefileName(), newEvent.getParentTracefile()
                .getCpuNumber(), newEvent.requestEventMarker().getName());
    }

}
