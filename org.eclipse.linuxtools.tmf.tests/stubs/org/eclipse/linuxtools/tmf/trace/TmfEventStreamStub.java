/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.trace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import org.eclipse.linuxtools.tmf.stream.TmfEventStream;
import org.eclipse.linuxtools.tmf.stream.ITmfEventParser;

/**
 * <b><u>TmfEventStreamStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventStreamStub extends TmfEventStream {

    // ========================================================================
    // Attributes
    // ========================================================================

    // The actual stream
    private final RandomAccessFile fStream;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param filename
     * @param parser
     * @throws FileNotFoundException
     */
    public TmfEventStreamStub(String filename, ITmfEventParser parser) throws FileNotFoundException {
        this(filename, parser, DEFAULT_CACHE_SIZE);
    }

    /**
     * @param filename
     * @param parser
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfEventStreamStub(String filename, ITmfEventParser parser, int cacheSize) throws FileNotFoundException {
        super(filename, parser, cacheSize);
        fStream = new RandomAccessFile(filename, "r");
        indexStream(true);
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    public RandomAccessFile getStream() {
        return fStream;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfStreamLocator#seekLocation(java.lang.Object)
     */
    public StreamContext seekLocation(Object location) {
    	StreamContext context = null;
        try {
			fStream.seek((location != null) ? (Long) location : 0);
			context = new StreamContext(getCurrentLocation());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfStreamLocator#getCurrentLocation()
     */
    public Object getCurrentLocation() {
        try {
            return new Long(fStream.getFilePointer());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // ========================================================================
    // Helper functions
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfEventStream#getAttributes()
     */
    public Map<String, Object> getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

}
