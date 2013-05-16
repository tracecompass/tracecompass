/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.interval;

import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

/**
 * This is the basic interface for accessing state intervals. See
 * StateInterval.java for a basic implementation.
 *
 * A StateInterval is meant to be immutable. All implementing (non-abstract)
 * classes should ideally be marked as 'final'.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
public interface ITmfStateInterval {

    /**
     * Retrieve the start time of the interval
     *
     * @return the start time of the interval
     */
    long getStartTime();

    /**
     * Retrieve the end time of the interval
     *
     * @return the end time of the interval
     */
    long getEndTime();

    /**
     * In case the "real" end time of the interval is not exactly the same as
     * the end time you want to show in views, you can implement this method to
     * assign a different value that the viewer can use.
     *
     * If not, you can simply have it return the same as getEndTime().
     *
     * @return The end time that views should use
     */
    long getViewerEndTime();

    /**
     * Retrieve the quark of the attribute this state interval refers to
     *
     * @return the quark of the attribute this state interval refers to
     */
    int getAttribute();

    /**
     * Retrieve the state value represented by this interval
     *
     * @return the state value represented by this interval
     */
    ITmfStateValue getStateValue();

    /**
     * Test if this interval intersects another timestamp, inclusively.
     *
     * @param timestamp
     *            The target timestamp
     * @return True if the interval and timestamp intersect, false if they don't
     */
    boolean intersects(long timestamp);
}
