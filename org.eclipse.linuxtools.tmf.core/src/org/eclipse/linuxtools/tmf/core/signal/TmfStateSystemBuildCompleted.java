/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Signal sent when the state system has completed its build.
 * 
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfStateSystemBuildCompleted extends TmfSignal {

    private ITmfTrace<?> fTrace;
    
    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param trace
     *            The state system of which trace just finished building
     */
    public TmfStateSystemBuildCompleted(Object source, ITmfTrace<?> trace) {
        super(source);
        fTrace = trace;
    }

    /**
     * @return The trace referred to by this signal
     */
    public ITmfTrace<?> getTrace() {
        return fTrace;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[TmfStateSystemBuildCompleted (" + fTrace.toString() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
