/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.statesystem.core.interval;

import java.util.Comparator;

/**
 * Comparator for ITmfStateInterval, using their *end times*. Making intervals
 * Comparable wouldn't be clear if it's using their start or end times (or maybe
 * even values), so separate comparators are provided.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public class TmfIntervalEndComparator implements Comparator<ITmfStateInterval> {

    @Override
    public int compare(ITmfStateInterval o1, ITmfStateInterval o2) {
        long e1 = o1.getEndTime();
        long e2 = o2.getEndTime();

        if (e1 < e2) {
            return -1;
        } else if (e1 > e2) {
            return 1;
        } else {
            return 0;
        }
    }

}
