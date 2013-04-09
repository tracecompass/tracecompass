/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Updated for removal of context clone
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * The CTf trace handler
 *
 * @version 1.0
 * @author Matthew khouzam
 */
public class CtfTmfTrace extends TmfTrace implements ITmfEventParser {

    // -------------------------------------------
    // Constants
    // -------------------------------------------
    /**
     * Default cache size for CTF traces
     */
    protected static final int DEFAULT_CACHE_SIZE = 50000;

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
     *             If something when wrong while reading the trace
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

        @SuppressWarnings("unused")
        CtfTmfEventType type;

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
     * Method validate.
     *
     * @param project
     *            IProject
     * @param path
     *            String
     * @return IStatus IStatus.error or Status.OK_STATUS
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#validate(IProject, String)
     * @since 2.0
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        IStatus validTrace = Status.OK_STATUS;
        try {
            final CTFTrace temp = new CTFTrace(path);
            boolean valid = temp.majortIsSet(); // random test
            temp.dispose();
            if (!valid) {
                validTrace = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CtfTmfTrace_MajorNotSet);
            }
        } catch (final CTFReaderException e) {
            validTrace = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CtfTmfTrace_ReadingError +": " + e.toString()); //$NON-NLS-1$
        }
        return validTrace;
    }

    /**
     * Method getCurrentLocation. This is not applicable in CTF
     *
     * @return null, since the trace has no knowledge of the current location
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getCurrentLocation()
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

    // -------------------------------------------
    // Environment Parameters
    // -------------------------------------------
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
        return new CtfIterator(this);
    }
}
