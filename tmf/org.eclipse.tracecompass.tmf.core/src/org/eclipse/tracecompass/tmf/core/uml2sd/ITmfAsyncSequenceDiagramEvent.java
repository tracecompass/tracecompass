/**********************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.uml2sd;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * <p>
 * Interface for asynchronous sequence diagram events.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ITmfAsyncSequenceDiagramEvent extends ITmfSyncSequenceDiagramEvent {
    /**
     * Returns end timestamp of message (i.e. receive time)
     *
     * @return end timestamp of message (i.e. receive time)
     */
    ITmfTimestamp getEndTime();
}
