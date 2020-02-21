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
 * Interface for synchronous sequence diagram events.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ITmfSyncSequenceDiagramEvent {

    /**
     * Returns Name of message.
     *
     * @return Name of message
     */
    String getName();

    /**
     * Returns name of sender of message.
     *
     * @return name of sender of message
     */
    String getSender();

    /**
     * Returns Name of receiver of message.
     *
     * @return Name of receiver of message
     */
    String getReceiver();

    /**
     * Returns Start time of message (i.e. send time).
     *
     * @return Start timestamp of message (i.e. send time)
     */
    ITmfTimestamp getStartTime();
}
