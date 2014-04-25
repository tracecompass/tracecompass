/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

/**
 * This object (singleton) traces all TmfSignals in the application.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfSignalTracer {

    static TmfSignalTracer fInstance;

    /**
     * @return The single instance of the signal tracer object
     */
    static public TmfSignalTracer getInstance() {
        if (fInstance == null) {
            fInstance = new TmfSignalTracer();
        }
        return fInstance;
    }

    private TmfSignalTracer() {
    }

    /**
     * Handler for all TMF signal types
     *
     * @param signal
     *            Incoming signal
     */
    @TmfSignalHandler
    public void traceSignal(TmfSignal signal) {
        System.out.println(signal.getSource().toString() + ": " + signal.toString()); //$NON-NLS-1$
    }
}
