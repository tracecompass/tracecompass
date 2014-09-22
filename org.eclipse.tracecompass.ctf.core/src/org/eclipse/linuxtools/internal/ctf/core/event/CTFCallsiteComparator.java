/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Simon Delisle - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event;

import java.util.Comparator;

import org.eclipse.linuxtools.ctf.core.event.CTFCallsite;

/**
 * Comparator for CTFCallsite
 *
 * @author Simon Delisle
 * @since 3.0
 *
 */
public class CTFCallsiteComparator implements Comparator<CTFCallsite> {

    private static final long MASK32 = 0x00000000ffffffffL;

    /*
     * The callsites will be sorted by calling addresses. To do this we take IPs
     * (instruction pointers) and compare them. Java only supports signed
     * operation and since memory addresses are unsigned, we will convert the
     * longs into integers that contain the high and low bytes and compare them.
     */
    @Override
    public int compare(CTFCallsite o1, CTFCallsite o2) {
        /*
         * mask32 is 32 zeros followed by 32 ones, when we bitwise and this it
         * will return the lower 32 bits
         */

        long other = o2.getIp();
        /*
         * To get a high int: we downshift by 32 and bitwise and with the mask
         * to get rid of the sign
         *
         * To get the low int: we bitwise and with the mask.
         */
        long otherHigh = (other >> 32) & MASK32;
        long otherLow = other & MASK32;
        long ownHigh = (o1.getIp() >> 32) & MASK32;
        long ownLow = o1.getIp() & MASK32;
        /* are the high values different, if so ignore the lower values */
        if (ownHigh > otherHigh) {
            return 1;
        }
        if (ownHigh < otherHigh ) {
            return -1;
        }
        /* the high values are the same, compare the lower values */
        if (ownLow > otherLow) {
            return 1;
        }
        if (ownLow < otherLow) {
            return -1;
        }
        /* the values are identical */
        return 0;
    }

}
