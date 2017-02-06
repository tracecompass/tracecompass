/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francis Giraldeau - Initial implementation and API
 *   Geneviève Bastien - Fixes and improvements
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.synchronization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.MathContext;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Fast linear timestamp transform.
 *
 * Reduce the use of BigDecimal for an interval of time where the transform can
 * be computed only with integer math. By rearranging the linear equation
 *
 * f(t) = fAlpha * t + fBeta
 *
 * to
 *
 * f(t) = (fAlphaLong * (t - ts)) / m + fBeta + c
 *
 * where fAlphaLong = fAlpha * m, and c is the constant part of the slope
 * product.
 *
 * The slope applies to a relative time reference instead of absolute timestamp
 * from epoch. The constant part of the slope for the interval is added to beta.
 * It reduces the width of slope and timestamp to 32-bit integers, and the
 * result fits a 64-bit value. Using standard integer arithmetic yield speedup
 * compared to BigDecimal, while preserving precision. Depending of rounding,
 * there may be a slight difference of +/- 3ns between the value computed by the
 * fast transform compared to BigDecimal. The timestamps produced are indepotent
 * (transforming the same timestamp will always produce the same result), and
 * the timestamps are monotonic.
 *
 * The number of bits available for the cache range is variable. The variable
 * alphaLong must be a 32-bit value. We reserve 30-bit for the decimal part to
 * reach the nanosecond precision. If the slope is greater than 1.0, the shift
 * is reduced to avoid overflow. It reduces the useful cache range, but the
 * result is correct even for large (1e9) slope.
 *
 * @author Francis Giraldeau <francis.giraldeau@gmail.com>
 *
 */
public class TmfTimestampTransformLinearFast implements ITmfTimestampTransformInvertible {

    private static final long serialVersionUID = 2398540405078949740L;

    private static final int INTEGER_BITS = 32;
    private static final int DECIMAL_BITS = 30;
    private static final HashFunction HASHER = Hashing.goodFastHash(32);
    private static final MathContext MC = MathContext.DECIMAL128;

    private final @NonNull BigDecimal fAlpha;
    private final @NonNull BigDecimal fBeta;
    private final long fAlphaLong;
    private final long fDeltaMax;
    private final int fDeltaBits;
    private final int fHashCode;

    private transient long fOffset;
    private transient long fRangeStart;
    private transient long fScaleMiss;
    private transient long fScaleHit;

    /**
     * Default constructor, equivalent to the identity.
     */
    public TmfTimestampTransformLinearFast() {
        this(BigDecimal.ONE, BigDecimal.ZERO);
    }

    /**
     * Constructor with alpha and beta
     *
     * @param alpha
     *            The slope of the linear transform
     * @param beta
     *            The initial offset of the linear transform
     */
    public TmfTimestampTransformLinearFast(final double alpha, final double beta) {
        this(BigDecimal.valueOf(alpha), BigDecimal.valueOf(beta));
    }

    /**
     * Constructor with alpha and beta as BigDecimal
     *
     * @param alpha
     *            The slope of the linear transform (must be in the range
     *            [1e-9, 1e9]
     * @param beta
     *            The initial offset of the linear transform
     */
    public TmfTimestampTransformLinearFast(final @NonNull BigDecimal alpha, final @NonNull BigDecimal beta) {
        /*
         * Validate the slope range:
         *
         * - Negative slope means timestamp goes backward wrt another computer,
         *   and this would violate the basic laws of physics.
         *
         * - A slope smaller than 1e-9 means the transform result will always be
         *   truncated to zero nanosecond.
         *
         * - A slope larger than Integer.MAX_VALUE is too large for the
         *   nanosecond scale.
         *
         * Therefore, a valid slope must be in the range [1e-9, 1e9]
         */
        if (alpha.compareTo(BigDecimal.valueOf(1e-9)) < 0 ||
                alpha.compareTo(BigDecimal.valueOf(1e9)) > 0) {
            throw new IllegalArgumentException("The slope alpha must in the range [1e-9, 1e9]"); //$NON-NLS-1$
        }
        fAlpha = alpha;
        fBeta = beta;

        /*
         * The result of (fAlphaLong * delta) must be at most 64-bit value.
         * Below, we compute the number of bits usable to represent the delta.
         * Small fAlpha (close to one) have greater range left for delta (at
         * most 30-bit). For large fAlpha, we reduce the delta range. If fAlpha
         * is close to ~1e9, then the delta size will be zero, effectively
         * recomputing the result using the BigDecimal for each transform.
         *
         * Long.numberOfLeadingZeros(fAlpha.longValue()) returns the number of
         * zero bits of the integer part of the slope. Then, fIntegerBits is
         * subtracted, which returns the number of bits usable for delta. This
         * value is bounded in the interval of [0, 30], because the delta can't
         * be negative, and we handle at most nanosecond precision, or 2^30. One
         * bit for each operand is reserved for the sign (Java enforce signed
         * arithmetics), such that
         *
         * bitsof(fDeltaBits) + bitsof(fAlphaLong) = 62 + 2 = 64
         */
        int width = Long.numberOfLeadingZeros(fAlpha.longValue()) - INTEGER_BITS;
        fDeltaBits = Math.max(Math.min(width, DECIMAL_BITS), 0);
        fDeltaMax = 1 << fDeltaBits;
        fAlphaLong = fAlpha.multiply(BigDecimal.valueOf(fDeltaMax), MC).longValue();
        rescale(0);
        fScaleMiss = 0;
        fScaleHit = 0;
        fHashCode = HASHER.newHasher()
                .putDouble(fAlpha.doubleValue())
                .putDouble(fBeta.doubleValue())
                .putLong(fAlphaLong)
                .hash()
                .asInt();
    }

    //-------------------------------------------------------------------------
    // Main timestamp computation
    //-------------------------------------------------------------------------

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return TmfTimestamp.create(transform(timestamp.getValue()), timestamp.getScale());
    }

    @Override
    public long transform(long timestamp) {
        long delta = timestamp - fRangeStart;
        if (delta >= fDeltaMax || delta < 0) {
            /*
             * Rescale if we exceed the safe range.
             *
             * If the same timestamp is transform with two different fStart
             * reference, they may not produce the same result. To avoid this
             * problem, align fStart on a deterministic boundary.
             *
             * TODO: use exact math arithmetic to detect overflow when switching to Java 8
             */
            rescale(timestamp);
            delta = Math.abs(timestamp - fRangeStart);
            fScaleMiss++;
        } else {
            fScaleHit++;
        }
        return ((fAlphaLong * delta) >> fDeltaBits) + fOffset;
    }

    private void rescale(long timestamp) {
        fRangeStart = timestamp - (timestamp % fDeltaMax);
        fOffset = BigDecimal.valueOf(fRangeStart).multiply(fAlpha, MC).add(fBeta, MC).longValue();
    }

    //-------------------------------------------------------------------------
    // Transform composition
    //-------------------------------------------------------------------------

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        if (composeWith.equals(TmfTimestampTransform.IDENTITY)) {
            /* If composing with identity, just return this */
            return this;
        } else if (composeWith instanceof TmfTimestampTransformLinearFast) {
            /* If composeWith is a linear transform, add the two together */
            TmfTimestampTransformLinearFast ttl = (TmfTimestampTransformLinearFast) composeWith;
            BigDecimal newAlpha = fAlpha.multiply(ttl.getAlpha(), MC);
            BigDecimal newBeta = fAlpha.multiply(ttl.getBeta(), MC).add(fBeta);
            /* Don't use the factory to make sure any further composition will
             * be performed on the same object type */
            return new TmfTimestampTransformLinearFast(newAlpha, newBeta);
        } else {
            /*
             * We do not know what to do with this kind of transform, just
             * return this
             */
            return this;
        }
    }

    @Override
    public ITmfTimestampTransform inverse() {
        return new TmfTimestampTransformLinearFast(BigDecimal.ONE.divide(fAlpha, MC),
                BigDecimal.valueOf(-1).multiply(fBeta).divide(fAlpha, MC));
    }

    //-------------------------------------------------------------------------
    // Getters and utility methods
    //-------------------------------------------------------------------------

    /**
     * A cache miss occurs when the timestamp is out of the range for integer
     * computation, and therefore requires using BigDecimal for re-scaling.
     *
     * @return number of misses
     */
    public long getCacheMisses() {
        return fScaleMiss;
    }

    /**
     * A scale hit occurs if the timestamp is in the range for fast transform.
     *
     * @return number of hits
     */
    public long getCacheHits() {
        return fScaleHit;
    }

    /**
     * Reset scale misses to zero
     */
    public void resetScaleStats() {
        fScaleMiss = 0;
        fScaleHit = 0;
    }

    /**
     * @return the slope alpha
     */
    public BigDecimal getAlpha() {
        return fAlpha;
    }

    /**
     * @return the offset beta
     */
    public BigDecimal getBeta() {
        return fBeta;
    }

    /**
     * The value delta max is the timestamp range where integer arithmetic is
     * used.
     *
     * @return the maximum delta
     */
    public long getDeltaMax() {
        return fDeltaMax;
    }

    @Override
    public String toString() {
        return String.format("%s [ slope = %s, offset = %s ]",  //$NON-NLS-1$
                getClass().getSimpleName(),
                fAlpha.toString(),
                fBeta.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TmfTimestampTransformLinearFast) {
            TmfTimestampTransformLinearFast other = (TmfTimestampTransformLinearFast) obj;
            return this.getAlpha().equals(other.getAlpha()) &&
                    this.getBeta().equals(other.getBeta());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fHashCode;
    }

    // Deserialization method, make sure there is a first scaling
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        rescale(0);
    }

}