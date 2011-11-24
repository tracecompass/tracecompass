/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson
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

import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * <b><u>TmfRangeSynchSignal</u></b>
 * <p>
 */
public class TmfRangeSynchSignal extends TmfSignal {

    private final TmfTimeRange fCurrentRange;
    private final TmfTimestamp fCurrentTime;

    public TmfRangeSynchSignal(Object source, TmfTimeRange range, TmfTimestamp ts) {
        super(source);
        fCurrentRange = range;
        fCurrentTime = ts;
    }

    public TmfTimeRange getCurrentRange() {
        return fCurrentRange;
    }

    public TmfTimestamp getCurrentTime() {
        return fCurrentTime;
    }

}
