/**********************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
