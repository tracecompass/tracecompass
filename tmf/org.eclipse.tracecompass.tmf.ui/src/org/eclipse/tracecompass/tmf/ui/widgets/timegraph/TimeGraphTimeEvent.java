/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.EventObject;

/**
 * Time selection event
 *
 * @author Patrick Tasse
 */
public class TimeGraphTimeEvent extends EventObject {

    /**
     * Default serial version UID for this class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The selection begin time.
     */
    private final long fBeginTime;

    /**
     * The selection end time.
     */
    private final long fEndTime;

    /**
     * Standard constructor
     *
     * @param source
     *            The source of this event
     * @param beginTime
     *            The selection begin time
     * @param endTime
     *            The selection end time
     */
    public TimeGraphTimeEvent(Object source, long beginTime, long endTime) {
        super(source);
        fBeginTime = beginTime;
        fEndTime = endTime;
    }

    /**
     * @return the selection begin time
     */
    public long getBeginTime() {
        return fBeginTime;
    }

    /**
     * @return the selection end time
     */
    public long getEndTime() {
        return fEndTime;
    }

}
