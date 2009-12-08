/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;


/**
 * <b><u>TmfTimestamp</u></b>
 * <p>
 * The fundamental time reference in the TMF.
 * <p>
 * It provides a generic timestamp implementation in its most basic form:
 * <ul>
 * <li>an unstructured integer value
 * <li>a time scale corresponding to the magnitude of the value wrt some
 * application-specific base unit (e.g. the second)
 * <li>a precision to indicate the error on the value (useful for comparing
 * timestamps in different scales). Default: 0.
 * </ul>
 * To allow synchronization of timestamps from different reference clocks,
 * there is a possibility to "adjust" the timestamp both by changing its scale
 * (traces of different scale) and by adding an offset to its value (clock
 * drift between traces).
 * <p>
 * Notice that the adjusted timestamp value could be negative e.g. for events
 * that occurred before t0 wrt the reference clock.
 * <p>
 * Finally, notice that timestamps are immutable.
 */
public class TmfTimestamp implements Cloneable {

	// ========================================================================
    // Attributes
    // ========================================================================

    protected long fValue; 		// The timestamp value
    protected byte fScale; 		// The time scale
    protected long fPrecision; 	// The value precision (tolerance)

    // ========================================================================
    // Constants
    // ========================================================================

    // The beginning and end of time
    public static final TmfTimestamp BigBang   = new TmfTimestamp(Long.MIN_VALUE, Byte.MAX_VALUE, 0);
    public static final TmfTimestamp BigCrunch = new TmfTimestamp(Long.MAX_VALUE, Byte.MAX_VALUE, 0);

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * Default constructor
     */
    public TmfTimestamp() {
        this(0, (byte) 0, 0);
    }

    /**
     * Simple constructor with value only
     */
    public TmfTimestamp(long value) {
        this(value, (byte) 0, 0);
    }

    /**
     * Simple constructor with value and scale
     * 
     * @param value
     * @param scale
     */
    public TmfTimestamp(long value, byte scale) {
        this(value, scale, 0);
    }

    /**
     * Constructor with value, scale and precision
     * 
     * @param value
     * @param scale
     * @param precision
     */
    public TmfTimestamp(long value, byte scale, long precision) {
        fValue = value;
        fScale = scale;
        fPrecision = Math.abs(precision);
    }

    /**
     * Copy constructor
     * 
     * @param other
     */
    public TmfTimestamp(TmfTimestamp other) {
    	assert(other != null);
        fValue = other.fValue;
        fScale = other.fScale;
        fPrecision = other.fPrecision;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return the timestamp value
     */
    public long getValue() {
        return fValue;
    }

    /**
     * @return the timestamp scale
     */
    public byte getScale() {
        return fScale;
    }

    /**
     * @return the timestamp value precision
     */
    public long getPrecision() {
        return fPrecision;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /**
     * Return a shifted and scaled timestamp.
     * 
     * Limitation: The scaling is limited to MAX_SCALING orders of magnitude.
     * The main reason is that the 64 bits value starts to lose any significance
     * meaning beyond that scale difference and it's not even worth the trouble
     * to switch to BigDecimal arithmetics.
     * 
     * @param offset
     *            - the shift value (in the same scale as newScale)
     * @param newScale
     *            - the new scale
     * @return The synchronized timestamp
     */

    /*
     * A java <code>long</code> has a maximum of 19 significant digits.
     * (-9,223,372,036,854,775,808 .. +9,223,372,036,854,775,807)
     * 
     * It is therefore useless to try to synchronize 2 timestamps whose
     * difference in scale exceeds that value.
     */
    private static int MAX_SCALING = 19;

    public TmfTimestamp synchronize(long offset, byte newScale) throws ArithmeticException {
        long newValue = fValue;
        long newPrecision = fPrecision;

        // Determine the scaling factor
        if (fScale != newScale) {
            int scaleDiff = Math.abs(fScale - newScale);
            // Let's try to be realistic...
            if (scaleDiff > MAX_SCALING) {
                throw new ArithmeticException("Scaling exception");
            }
            // Not pretty...
            long scalingFactor = 1;
            for (int i = 0; i < scaleDiff; i++) {
                scalingFactor *= 10;
            }
            if (newScale < fScale) {
                newValue *= scalingFactor;
                newPrecision *= scalingFactor;
            } else {
                newValue /= scalingFactor;
                newPrecision /= scalingFactor;
            }
        }

        return new TmfTimestamp(newValue + offset, newScale, newPrecision);
    }

    /**
     * Compute the adjustment, in the reference scale, needed to synchronize
     * this timestamp with a reference timestamp.
     * 
     * @param reference
     *            - the reference timestamp to synchronize with
     * @return the adjustment term in the reference time scale
     * @throws TmfNumericalException
     */
    public long getAdjustment(TmfTimestamp reference) throws ArithmeticException {
        TmfTimestamp ts = synchronize(0, reference.fScale);
        return reference.fValue - ts.fValue;
    }

    /**
     * Compare with another timestamp
     * 
     * @param other
     *            - the other timestamp
     * @param withinPrecision
     *            - indicates if precision is to be take into consideration
     * @return -1: this timestamp is lower
     *          0: timestamps are equal (within precision if requested)
     *          1: this timestamp is higher
     * @throws TmfNumericalException
     */
    public int compareTo(final TmfTimestamp other, boolean withinPrecision) {

    	// If values have the same time scale, perform the comparison
        if (fScale == other.fScale) {
            if (withinPrecision) {
                if ((fValue + fPrecision) < (other.fValue - other.fPrecision))
                    return -1;
                if ((fValue - fPrecision) > (other.fValue + other.fPrecision))
                    return 1;
                return 0;
            }
            return (fValue == other.fValue) ? 0 : (fValue < other.fValue) ? -1
                    : 1;
        }

        // If values have different time scales, adjust to the finest one and
        // then compare. If the scaling difference is too large, revert to
        // some heuristics. Hopefully, nobody will try to compare galactic and
        // quantic clock events...
        byte newScale = (fScale < other.fScale) ? fScale : other.fScale;
        try {
            TmfTimestamp ts1 = this.synchronize(0, newScale);
            TmfTimestamp ts2 = other.synchronize(0, newScale);
            return ts1.compareTo(ts2, withinPrecision);
        } catch (ArithmeticException e) {
            if ((fValue == 0) || (other.fValue == 0)) {
                return (fValue == other.fValue) ? 0
                        : (fValue < other.fValue) ? -1 : 1;
            }
            if ((fValue > 0) && (other.fValue > 0)) {
                return (fScale < other.fScale) ? -1 : 1;
            }
            if ((fValue < 0) && (other.fValue < 0)) {
                return (fScale > other.fScale) ? -1 : 1;
            }
            return (fValue < 0) ? -1 : 1;
        }
    }

	@Override
	public TmfTimestamp clone() {
		return new TmfTimestamp(this);
	}

    @Override
    public boolean equals(Object other) {
        if (other instanceof TmfTimestamp)
            return compareTo((TmfTimestamp) other, false) == 0;
        return super.equals(other);
    }

    @Override
    public String toString() {
    	return "[TmfTimestamp(" + fValue + "," + fScale + "," + fPrecision + ")]";
    }

}