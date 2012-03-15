/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.distribution.model;

/**
 * <b><u>VerDistributionData</u></b>
 * 
 * Implementation of DistributionData for vertical direction. 
 * <p>
 */
public class VerDistributionData extends DistributionData {
    
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    public VerDistributionData(int nbBuckets, int[][] buckets) {
        super(nbBuckets, buckets);
    }

    // ------------------------------------------------------------------------
    // Abstract function implementation
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.distribution.model.DistributionData#moveBuckets(int)
     */
    @Override
    protected void moveBuckets(int offset) {
        for (int j = 0; j < fNbBuckets; j++) {

            for(int i = fNbBuckets - 1; i >= offset; i--) {
                fBuckets[j][i] = fBuckets[j][i-offset]; 
            }

            for (int i = 0; i < offset; i++) {
                fBuckets[j][i] = 0;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.distribution.model.DistributionData#mergeBuckets()
     */
    @Override
    protected void mergeBuckets() {
        for (int x = 0; x < fNbBuckets; x++) {
            for (int i = 0; i < fNbBuckets / 2; i++) {
                fBuckets[x][i] = fBuckets[x][2 * i] + fBuckets[x][2 * i + 1];
            }
            for (int i = fNbBuckets / 2; i < fNbBuckets; i++) {
                fBuckets[x][i] = 0;
            }
        }
        fBucketDuration = fBucketDuration * 2;
        updateEndTime();
        fLastBucket = fNbBuckets / 2 - 1;
    }
}
