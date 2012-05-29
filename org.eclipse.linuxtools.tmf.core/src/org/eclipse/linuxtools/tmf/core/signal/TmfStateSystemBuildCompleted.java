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
     * @param source
     */
    public TmfStateSystemBuildCompleted(Object source, ITmfTrace<?> trace) {
        super(source);
        fTrace = trace;
    }

    /**
     * @param source
     */
    public ITmfTrace<?> getTrace() {
        return fTrace;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfStateSystemBuildCompleted (" + fTrace.toString() + ")]";
    }

}
