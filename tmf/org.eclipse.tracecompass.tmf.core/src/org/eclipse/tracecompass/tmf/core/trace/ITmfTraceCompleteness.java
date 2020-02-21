/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial implementation
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

/**
 * An interface that provides information about the completeness of a trace. A
 * trace is considered complete when it is known that no more data will be added
 * to it.
 */
public interface ITmfTraceCompleteness {

    /**
     * Returns whether or not a trace is complete.
     *
     * @return true if a trace is complete, false otherwise
     */
    boolean isComplete();

    /**
     * Set the completeness of a trace.
     *
     * @param isComplete
     *            whether the trace should be considered complete or not
     */
    void setComplete(boolean isComplete);
}
