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
 * @author alexmont
 * 
 */
public interface ITmfStateInterval {

    /**
     * Retrieve the start time of the interval
     * 
     * @return
     */
    public long getStartTime();

    /**
     * Retrieve the end time of the interval
     * 
     * @return
     */
    public long getEndTime();

    /**
     * Retrieve the quark of the attribute this state interval refers to
     * 
     * @return
     */
    public int getAttribute();

    /**
     * Retrieve the state value represented by this interval
     * 
     * @return
     */
    public ITmfStateValue getStateValue();

    /**
     * Test if this interval intersects another timestamp, inclusively.
     * 
     * @param timestamp
     *            The target timestamp
     * @return True if the interval and timestamp intersect, false if they don't
     */
    public boolean intersects(long timestamp);
}
