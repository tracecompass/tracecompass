/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.tmf.core.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp.TimestampType;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 */
public class CtfTmfTrace extends TmfEventProvider<CtfTmfEvent> implements ITmfTrace<CtfTmfEvent> {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // the Ctf Trace
    private CTFTrace fTrace;

    // The number of events collected
    protected long fNbEvents = 0;

    // The time span of the event stream
    private ITmfTimestamp fStartTime = TmfTimestamp.BIG_CRUNCH;
    private ITmfTimestamp fEndTime = TmfTimestamp.BIG_BANG;

    // The trace resource
    private IResource fResource;

    /* Reference to the state system assigned to this trace */
    protected IStateSystemQuerier ss = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public CtfTmfTrace() {
        super();
    }

    /**
     * Method initTrace.
     * @param resource IResource
     * @param path String
     * @param eventType Class<CtfTmfEvent>
     * @throws TmfTraceException
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#initTrace(IResource, String, Class<CtfTmfEvent>)
     */
    @Override
    public void initTrace(final IResource resource, final String path, final Class<CtfTmfEvent> eventType)
            throws TmfTraceException {
        EventDeclaration ed;
        ITmfEventField eventField;
        @SuppressWarnings("unused")
        CtfTmfEventType type;

        this.fResource = resource;
        try {
            this.fTrace = new CTFTrace(path);
            for( int i =0 ; i< this.fTrace.getNbEventTypes(); i++) {
                ed = this.fTrace.getEventType(i);
                eventField = parseDeclaration(ed);
                /*
                 * Populate the event manager with event types that are there in
                 * the beginning.
                 */
                type = new CtfTmfEventType(ed.getName(), eventField);
            }

            /* Set the start and (current) end times for this trace */
            final CtfIterator iterator = new CtfIterator(this, 0, 0);
            if(iterator.getLocation().equals(CtfIterator.NULL_LOCATION)) {
                /* Handle the case where the trace is empty */
                this.setStartTime(TmfTimestamp.BIG_BANG);
            } else {
                this.setStartTime(iterator.getCurrentEvent().getTimestamp());
                /*
                 * is the trace empty
                 */
                if( iterator.hasMoreEvents()){
                    iterator.goToLastEvent();
                }
                this.setEndTime(iterator.getCurrentEvent().getTimestamp());
            }

        } catch (final CTFReaderException e) {
            /*
             * If it failed at the init(), we can assume it's because the file
             * was not found or was not recognized as a CTF trace. Throw into
             * the new type of exception expected by the rest of TMF.
             */
            throw new TmfTraceException(e.getMessage(), e);
        }

        TmfSignalManager.register(this);
        //FIXME This should be called via the ExperimentUpdated signal
        buildStateSystem();

        /* Refresh the project, so it can pick up new files that got created. */
        if ( resource != null) {
            try {
                resource.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                throw new TmfTraceException(e.getMessage(), e);
            }
        }
    }

    private static ITmfEventField parseDeclaration(EventDeclaration ed) {
        EventDefinition eventDef = ed.createDefinition(null);
        return new CtfTmfContent(ITmfEventField.ROOT_FIELD_ID,
                CtfTmfEvent.parseFields(eventDef));
    }

    /**
     * Method dispose.
     * @see org.eclipse.linuxtools.tmf.core.component.ITmfComponent#dispose()
     */
    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
    }

    /**
     * Method broadcast.
     * @param signal TmfSignal
     * @see org.eclipse.linuxtools.tmf.core.component.ITmfComponent#broadcast(TmfSignal)
     */
    @Override
    public void broadcast(final TmfSignal signal) {
        TmfSignalManager.dispatchSignal(signal);
    }

    /**
     * Method validate.
     * @param project IProject
     * @param path String
     * @return boolean
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#validate(IProject, String)
     */
    @Override
    public boolean validate(final IProject project, final String path) {
        try {
            final CTFTrace temp = new CTFTrace(path);
            return temp.majortIsSet(); // random test
        } catch (final CTFReaderException e) {
            /* Nope, not a CTF trace we can read */
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Method getEventType.
     * @return the trace path
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getEventType()
     */
    @Override
    public Class<CtfTmfEvent> getEventType() {
        return fType;
    }

    /**
     * Method getNbEnvVars.
     * @return int
     */
    public int getNbEnvVars() {
        return this.fTrace.getEnvironment().size();
    }


    /**
     * Method getEnvNames.
     * @return String[]
     */
    public String[] getEnvNames() {
        final String[] s = new String[getNbEnvVars()];
        return this.fTrace.getEnvironment().keySet().toArray(s);
    }

    /**
     * Method getEnvValue.
     * @param key String
     * @return String
     */
    public String getEnvValue(final String key)    {
        return this.fTrace.getEnvironment().get(key);
    }


    /**

     * @return the trace path * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getPath()
     */
    @Override
    public String getPath() {
        return this.fTrace.getPath();
    }

    /**
     * Method getName.
     * @return String
     * @see org.eclipse.linuxtools.tmf.core.component.ITmfComponent#getName()
     */
    @Override
    public String getName() {
        String traceName = (fResource != null) ? fResource.getName() : null;
        // If no resource was provided, extract the display name the trace path
        if (traceName == null) {
            final String path = this.fTrace.getPath();
            final int sep = path.lastIndexOf(IPath.SEPARATOR);
            traceName = (sep >= 0) ? path.substring(sep + 1) : path;
        }
        return traceName;
    }

    /**
     * Method getCacheSize.
     * @return int
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getCacheSize()
     */
    @Override
    public int getCacheSize() {
        return 50000; // not true, but it works
    }

    /**
     * Method getNbEvents.
     * @return long
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getNbEvents()
     */
    @Override
    public long getNbEvents() {
        return this.fNbEvents;
    }

    /**
     * Method getTimeRange.
     * @return TmfTimeRange
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getTimeRange()
     */
    @Override
    public TmfTimeRange getTimeRange() {
        return new TmfTimeRange(this.fStartTime, this.fEndTime);
    }

    /**
     * Method getStartTime.
     * @return ITmfTimestamp
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getStartTime()
     */
    @Override
    public ITmfTimestamp getStartTime() {
        return this.fStartTime;
    }

    /**
     * Method getEndTime.
     * @return ITmfTimestamp
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getEndTime()
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return this.fEndTime;
    }

    /**
     * Method getCurrentLocation. This is not applicable in CTF
     * @return null, since the trace has no knowledge of the current location
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getCurrentLocation()
     */
    @Override
    public ITmfLocation<?> getCurrentLocation() {
        return null;
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    /**
     * Method setTimeRange.
     * @param range TmfTimeRange
     */
    protected void setTimeRange(final TmfTimeRange range) {
        this.fStartTime = range.getStartTime();
        this.fEndTime = range.getEndTime();
    }

    /**
     * Method setStartTime.
     * @param startTime ITmfTimestamp
     */
    protected void setStartTime(final ITmfTimestamp startTime) {
        this.fStartTime = startTime;
    }

    /**
     * Method setEndTime.
     * @param endTime ITmfTimestamp
     */
    protected void setEndTime(final ITmfTimestamp endTime) {
        this.fEndTime = endTime;
    }

    // ------------------------------------------------------------------------
    // TmfProvider
    // ------------------------------------------------------------------------

    /**
     * Method armRequest.
     * @param request ITmfDataRequest<CtfTmfEvent>
     * @return ITmfContext
     */
    @Override
    public ITmfContext armRequest(final ITmfDataRequest<CtfTmfEvent> request) {
        if ((request instanceof ITmfEventRequest<?>)
                && !TmfTimestamp.BIG_BANG
                .equals(((ITmfEventRequest<CtfTmfEvent>) request)
                        .getRange().getStartTime())
                        && (request.getIndex() == 0)) {
            final ITmfContext context = seekEvent(((ITmfEventRequest<CtfTmfEvent>) request)
                    .getRange().getStartTime());
            ((ITmfEventRequest<CtfTmfEvent>) request)
            .setStartIndex((int) context.getRank());
            return context;
        }
        return seekEvent(request.getIndex());
    }

//    /**
//     * The trace reader keeps its own iterator: the "context" parameter here
//     * will be ignored.
//     *
//     * If you wish to specify a new context, instantiate a new CtfIterator and
//     * seek() it to where you want, and use that to read events.
//     *
//     * FIXME merge with getNextEvent below once they both use the same parameter
//     * type.
//     * @param context ITmfContext
//     * @return CtfTmfEvent
//     */
//    @Override
//    public CtfTmfEvent getNext(final ITmfContext context) {
//        return readNextEvent(context);
//    }

    // ------------------------------------------------------------------------
    // ITmfTrace
    // ------------------------------------------------------------------------

    /**
     * Method seekEvent.
     * @param location ITmfLocation<?>
     * @return ITmfContext
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(ITmfLocation<?>)
     */
    @Override
    public ITmfContext seekEvent(final ITmfLocation<?> location) {
        CtfLocation currentLocation = (CtfLocation) location;
        if (currentLocation == null) {
            currentLocation = new CtfLocation(0L);
        }
        CtfIterator context = new CtfIterator(this);
        
        if (currentLocation.getLocation() == CtfLocation.INVALID_LOCATION) {
            ((CtfTmfTimestamp) getEndTime()).setType(TimestampType.NANOS);
            currentLocation.setLocation(getEndTime().getValue() + 1);
        }
        context.setLocation(currentLocation);
        context.setRank(ITmfContext.UNKNOWN_RANK);
        return context;
    }

    /**
     * Method getLocationRatio.
     * @param location ITmfLocation<?>
     * @return double
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getLocationRatio(ITmfLocation<?>)
     */
    @Override
    public double getLocationRatio(final ITmfLocation<?> location) {
        final CtfLocation curLocation = (CtfLocation) location;
        CtfIterator iterator = new CtfIterator(this);
        iterator.seek(curLocation.getLocation());
        return ((double) iterator.getCurrentEvent().getTimestampValue() - iterator
                .getStartTime())
                / (iterator.getEndTime() - iterator.getStartTime());
    }

    /**
     * Method getStreamingInterval.
     * @return long
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getStreamingInterval()
     */
    @Override
    public long getStreamingInterval() {
        return 0;
    }

    /**
     * Method seekEvent.
     * @param timestamp ITmfTimestamp
     * @return ITmfContext
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(ITmfTimestamp)
     */
    @Override
    public ITmfContext seekEvent(final ITmfTimestamp timestamp) {
        CtfIterator context = new CtfIterator(this);
        context.seek(timestamp.getValue());
        context.setRank(ITmfContext.UNKNOWN_RANK);
        return context;
    }

    /**
     * Seek by rank
     * @param rank long
     * @return ITmfContext
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(long)
     */
    @Override
    public ITmfContext seekEvent(final long rank) {
        CtfIterator context = new CtfIterator(this);
        context.seekRank(rank);
        context.setRank(rank);
        return context;
    }

    /**
     * Seek rank ratio
     * @param ratio double
     * @return ITmfContext
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(double)
     */
    @Override
    public ITmfContext seekEvent(final double ratio) {
        CtfIterator context = new CtfIterator(this);
        context.seek((long) (this.fNbEvents * ratio));
        context.setRank(ITmfContext.UNKNOWN_RANK);
        return context;
    }

    /**
     * Method readNextEvent.
     * @param context ITmfContext
     * @return CtfTmfEvent
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getNext(ITmfContext)
     */
    @Override
    public CtfTmfEvent getNext(final ITmfContext context) {
        CtfTmfEvent event = null;
        if (context instanceof CtfIterator) {
            CtfIterator ctfIterator = (CtfIterator) context;
            event = ctfIterator.getCurrentEvent();
            ctfIterator.advance();
        }
        return event;
    }

    /**
     * Method getResource.
     * @return IResource
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getResource()
     */
    @Override
    public IResource getResource() {
        return this.fResource;
    }

    /**
     * Method getStateSystem.
     * @return IStateSystemQuerier
     */
    public IStateSystemQuerier getStateSystem() {
        return this.ss;
    }

    /**
     * Method getCTFTrace.
     * @return CTFTrace
     */
    CTFTrace getCTFTrace() {
        return fTrace;
    }


    /**
     * Suppressing the warning, because the 'throws' will usually happen in
     * sub-classes.
     * @throws TmfTraceException
     */
    @SuppressWarnings("static-method")
    protected void buildStateSystem() throws TmfTraceException {
        /*
         * Nothing is done in the basic implementation, please specify
         * how/if to build a state system in derived classes.
         */
        return;
    }

}
