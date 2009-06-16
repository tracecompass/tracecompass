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

package org.eclipse.linuxtools.tmf.eventlog;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * <b><u>TmfEventStreamStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventStreamStub extends TmfEventStream {

    // ========================================================================
    // Attributes
    // ========================================================================

    // ========================================================================
    // Constructors
    // ========================================================================

   /**
     * @param filename
     * @throws FileNotFoundException 
     */
    public TmfEventStreamStub(String filename, ITmfEventParser parser) throws FileNotFoundException {
        super(filename, parser);
    }

    /**
     * @param filename
     * @throws FileNotFoundException 
     */
    public TmfEventStreamStub(String filename, ITmfEventParser parser, int cacheSize) throws FileNotFoundException {
        super(filename, parser, cacheSize);
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    // ========================================================================
    // Operators
    // ========================================================================

    @Override
    public void seekLocation(Object location) throws IOException {
        seek((Long) location);
    }

    @Override
    public Object getCurrentLocation() {
        try {
            return new Long(getFilePointer());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // ========================================================================
    // Helper functions
    // ========================================================================

}
