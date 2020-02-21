/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

/**
 * This object (singleton) traces all TmfSignals in the application.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfSignalTracer {

    private static TmfSignalTracer fInstance;

    /**
     * @return The single instance of the signal tracer object
     */
    public static TmfSignalTracer getInstance() {
        if (fInstance == null) {
            fInstance = new TmfSignalTracer();
        }
        return fInstance;
    }

    private TmfSignalTracer() {
        // Do nothing
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
