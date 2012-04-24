/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.stubs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * <b><u>LTTngTraceStub</u></b>
 * <p>
 * Dummy test trace. Use in conjunction with LTTngEventParserStub.
 */
@SuppressWarnings("nls")
public class LTTngTraceStub extends TmfTrace<LttngEvent> implements ITmfEventParser<LttngEvent> {

    // ========================================================================
    // Attributes
    // ========================================================================

    // The actual stream
    private final RandomAccessFile fTrace;

    // The associated event parser
    private final ITmfEventParser<LttngEvent> fParser;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param filename
     * @param parser
     * @throws FileNotFoundException
     */
    public LTTngTraceStub(final IResource resource) throws FileNotFoundException {
        this(resource, DEFAULT_TRACE_CACHE_SIZE);
    }

    /**
     * @param filename
     * @param parser
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public LTTngTraceStub(final IResource resource, final int cacheSize) throws FileNotFoundException {
        //      super(resource, LttngEvent.class, resource.getName(), cacheSize, true);
        super(resource, LttngEvent.class, resource.getName(), cacheSize);
        fTrace = new RandomAccessFile(resource.getName(), "r");
        fParser = new LTTngEventParserStub();
    }

    public void indexTrace() {
        fIndexer.buildIndex(true);
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    public RandomAccessFile getStream() {
        return fTrace;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfStreamLocator#seekLocation(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public TmfContext seekEvent(final ITmfLocation<?> location) {
        TmfContext context = null;
        try {
            synchronized(fTrace) {
                fTrace.seek((location != null) ? ((TmfLocation<Long>) location).getLocation() : 0);
                context = new TmfContext(getCurrentLocation(), 0);
                //        		TmfTraceContext context2 = new TmfTraceContext(getCurrentLocation(), 0);
                //        		TmfEvent event = parseEvent(context2);
                //        		context.setTimestamp(event.getTimestamp());
            }
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return context;
    }

    @Override
    public TmfContext seekEvent(final double ratio) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getLocationRatio(final ITmfLocation<?> location) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfStreamLocator#getCurrentLocation()
     */
    @Override
    public ITmfLocation<?> getCurrentLocation() {
        try {
            return new TmfLocation<Long>(fTrace.getFilePointer());
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#parseEvent()
     */
    @Override
    public ITmfEvent parseEvent(final ITmfContext context) {
//        try {
            // paserNextEvent updates the context
            final LttngEvent event = (LttngEvent) fParser.parseEvent(context);
            //   			if (event != null) {
            //   				context.setTimestamp(event.getTimestamp());
            //   			}
            return event;
//        }
//        catch (final IOException e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[LTTngTraceStub]";
    }

    //    // ========================================================================
    //    // Helper functions
    //    // ========================================================================
    //
    //    /* (non-Javadoc)
    //     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfEventStream#getAttributes()
    //     */
    //    public Map<String, Object> getAttributes() {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }

}