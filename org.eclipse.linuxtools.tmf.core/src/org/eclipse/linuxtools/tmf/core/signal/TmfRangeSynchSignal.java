/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
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

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;

/**
 * A new active time range has been selected
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfRangeSynchSignal extends TmfSignal {

    private final TmfTimeRange fCurrentRange;
    private final ITmfTimestamp fCurrentTime;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param range
     *            The time range to which we synchronized
     * @param ts
     *            The current selected timestamp, independent from the time
     *            range
     * @since 2.0
     */
    public TmfRangeSynchSignal(Object source, TmfTimeRange range, ITmfTimestamp ts) {
        super(source);
        fCurrentRange = range;
        fCurrentTime = ts;
    }

    /**
     * @return This signal's time range
     * @since 2.0
     */
    public TmfTimeRange getCurrentRange() {
        return fCurrentRange;
    }

    /**
     * @return This signal's current selected timestamp
     * @since 2.0
     */
    public ITmfTimestamp getCurrentTime() {
        return fCurrentTime;
    }

}
