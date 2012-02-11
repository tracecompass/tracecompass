/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

/**
 * <b><u>ITmfTimestamp</u></b>
 * <p>
 */
public interface ITmfTimestamp extends Cloneable, Comparable<ITmfTimestamp> {

    /**
     * @return the timestamp value (magnitude)
     */
    public long getValue();

    /**
     * @return the timestamp scale (exponent)
     */
    public int getScale();

    /**
     * @return the timestamp precision (measurement tolerance)
     */
    public int getPrecision();

    /**
     * Normalize (adjust scale and offset) of the timerstamp
     * 
     * @param offset the offset to apply to the timestamp value (after scaling)
     * @param scale the new timestamp scale
     * @return a new 'adjusted' ITmfTimestamp
     */
    public ITmfTimestamp normalize(long offset, int scale) throws ArithmeticException;

    /**
     * Compares [this] and [ts] within timestamp precision
     * 
     * @param ts the other timestamp
     * @param withinPrecision consider the precision when testing for equality
     * @return -1, 0 or 1 (less than, equals, greater than)
     */
    public int compareTo(ITmfTimestamp ts, boolean withinPrecision);

    /**
     * Returns the difference between [this] and [ts] as a timestamp 
     * 
     * @param ts the other timestamp
     * @return the time difference (this - other) as an ITmfTimestamp
     */
    public ITmfTimestamp getDelta(ITmfTimestamp ts);

    // Cloneable
    public ITmfTimestamp clone();
    
    // Comparable
    public int compareTo(ITmfTimestamp ts);

}
