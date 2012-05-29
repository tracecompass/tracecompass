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
import org.eclipse.linuxtools.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp.TimestampType;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

public class CtfTmfTrace extends TmfTrace<CtfTmfEvent> implements ITmfEventParser{

    //-------------------------------------------
    //        Fields
    //-------------------------------------------

    /* Reference to the state system assigned to this trace */
    protected IStateSystemQuerier ss = null;

    /* Reference to the CTF Trace */
    private CTFTrace fTrace;

    //-------------------------------------------
    //        TmfTrace Overrides
    //-------------------------------------------
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
        super.initTrace(resource, path, eventType);
        EventDeclaration ed;
        ITmfEventField eventField;
        @SuppressWarnings("unused")
        CtfTmfEventType type;

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

    /**
     * Method validate.
     * @param project IProject
     * @param path String
     * @return boolean
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#validate(IProject, String)
     */
    @Override
    public boolean validate(@SuppressWarnings("unused") final IProject project, final String path) {
        try {
            final CTFTrace temp = new CTFTrace(path);
            return temp.majortIsSet(); // random test
        } catch (final CTFReaderException e) {
            /* Nope, not a CTF trace we can read */
            return false;
        }
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



    @Override
    public double getLocationRatio(ITmfLocation<?> location) {
        final CtfLocation curLocation = (CtfLocation) location;
        CtfIterator iterator = new CtfIterator(this);
        iterator.seek(curLocation.getLocation());
        return ((double) iterator.getCurrentEvent().getTimestampValue() - iterator
                .getStartTime())
                / (iterator.getEndTime() - iterator.getStartTime());
    }

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


    @Override
    public ITmfContext seekEvent(double ratio) {
        CtfIterator context = new CtfIterator(this);
        context.seek((long) (this.getNbEvents() * ratio));
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
    public synchronized CtfTmfEvent getNext(final ITmfContext context) {
        CtfTmfEvent event = null;
        if (context instanceof CtfIterator) {
            CtfIterator ctfIterator = (CtfIterator) context;
            event = ctfIterator.getCurrentEvent();
            ctfIterator.advance();
        }
        return event;
    }

    /**
     * Suppressing the warning, because the 'throws' will usually happen in
     * sub-classes.
     * @throws TmfTraceException
     */
    @SuppressWarnings({ "static-method", "unused" })
    protected void buildStateSystem() throws TmfTraceException {
        /*
         * Nothing is done in the basic implementation, please specify
         * how/if to build a state system in derived classes.
         */
        return;
    }

    /**
     * Method getStateSystem.
     *
     * @return IStateSystemQuerier
     */
    public IStateSystemQuerier getStateSystem() {
        return this.ss;
    }

    /**
     *
     * @param ed
     * @return
     */
    private static ITmfEventField parseDeclaration(EventDeclaration ed) {
        EventDefinition eventDef = ed.createDefinition(null);
        return new CtfTmfContent(ITmfEventField.ROOT_FIELD_ID,
                CtfTmfEvent.parseFields(eventDef));
    }

    /**
     * gets the CTFtrace that this is wrapping
     * @return the CTF trace
     */
    public CTFTrace getCTFTrace() {
        return fTrace;
    }


    //-------------------------------------------
    //        Environment Parameters
    //-------------------------------------------
    /**
     * Method getNbEnvVars.
     *
     * @return int
     */
    public int getNbEnvVars() {
        return this.fTrace.getEnvironment().size();
    }

    /**
     * Method getEnvNames.
     *
     * @return String[]
     */
    public String[] getEnvNames() {
        final String[] s = new String[getNbEnvVars()];
        return this.fTrace.getEnvironment().keySet().toArray(s);
    }

    /**
     * Method getEnvValue.
     *
     * @param key
     *            String
     * @return String
     */
    public String getEnvValue(final String key) {
        return this.fTrace.getEnvironment().get(key);
    }

    //-------------------------------------------
    //        Parser
    //-------------------------------------------

    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        CtfTmfEvent event = null;
        if( context instanceof CtfIterator ){
            CtfIterator itt = (CtfIterator) context;
            event = itt.getCurrentEvent();
        }
        return event;
    }

}
