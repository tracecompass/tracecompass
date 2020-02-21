/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.uml2sd.core;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * A interface for handling time ranges.
 *
 * @author sveyrier
 */
public interface ITimeRange {

    /**
     * Returns the time when the message began.
     * @return the time when the message began
     */
    ITmfTimestamp getStartTime();

    /**
     * Returns the time when the message ended.
     *
     * @return the time when the message ended
     */
    ITmfTimestamp getEndTime();

    /**
     * Returns flag to indicate whether time information is available or not.
     *
     * @return flag to indicate whether time information is available or not
     */
    boolean hasTimeInfo();
}
