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
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * The generic trace parser in TMF.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfContext
 */
public interface ITmfEventParser {

    /**
     * Parses the trace event referenced by the context.
     * The context should *not* be altered.
     *
     * @param context the trace context
     * @return a parsed event (null if none)
     */
    ITmfEvent parseEvent(ITmfContext context);

}
