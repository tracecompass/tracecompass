/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.histogram;

import java.util.Arrays;

/**
 * <b><u>HistogramScaledData</u></b>
 * <p>
 * Convenience class/struct for scaled histogram data.
 */
public class HistogramScaledData {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    public static final int OUT_OF_RANGE_BUCKET = -1;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    public int fWidth;
    public int fHeight;
    public int[] fData;
    public long fBucketDuration;
    public long fMaxValue;
    public int fCurrentBucket;
    public int fLastBucket;
    public double fScalingFactor;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public HistogramScaledData(int width, int height) {
        fWidth = width;
        fHeight = height;
        fData = new int[width];
        Arrays.fill(fData, 0);
        fBucketDuration = 1;
        fMaxValue = 0;
        fCurrentBucket = 0;
        fLastBucket = 0;
        fScalingFactor = 1;
    }

    public HistogramScaledData(HistogramScaledData other) {
        fWidth = other.fWidth;
        fHeight = other.fHeight;
        fData = Arrays.copyOf(other.fData, fWidth);
        fBucketDuration = other.fBucketDuration;
        fMaxValue = other.fMaxValue;
        fCurrentBucket = other.fCurrentBucket;
        fLastBucket = other.fLastBucket;
        fScalingFactor = other.fScalingFactor;
    }

}