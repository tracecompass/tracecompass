/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.synchronization;

import java.math.BigDecimal;
import java.math.MathContext;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

/**
 * Class implementing a linear timestamp transform, with a slope and/or offset
 *
 * f(t) = alpha*t + beta
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfTimestampTransformLinear implements ITmfTimestampTransform {

    /**
     * Generated serial UID
     */
    private static final long serialVersionUID = -4756608071358979461L;

    /**
     * Respectively the slope and offset and this linear equation.
     *
     * FIXME: Maybe doubles will be enough, for the whole synchronization
     * package as well, I think BigDecimal is a remnant of past trials and
     * errors
     */
    private final BigDecimal fAlpha;
    private final BigDecimal fBeta;

    private static final MathContext fMc = MathContext.DECIMAL128;

    /**
     * Default constructor
     */
    public TmfTimestampTransformLinear() {
        fAlpha = BigDecimal.ONE;
        fBeta = BigDecimal.ZERO;
    }

    /**
     * Constructor with alpha and beta
     *
     * @param alpha
     *            The slope of the linear transform
     * @param beta
     *            The initial offset of the linear transform
     */
    public TmfTimestampTransformLinear(final double alpha, final double beta) {
        fAlpha = BigDecimal.valueOf(alpha);
        fBeta = BigDecimal.valueOf(beta);
    }

    /**
     * Constructor with alpha and beta in big decimal
     *
     * @param fAlpha2
     *            The slope of the linear transform
     * @param fBeta2
     *            The initial offset of the linear transform
     */
    public TmfTimestampTransformLinear(final BigDecimal fAlpha2, final BigDecimal fBeta2) {
        if (fAlpha2 != null) {
            fAlpha = fAlpha2;
        } else {
            fAlpha = BigDecimal.ONE;
        }
        if (fBeta2 != null) {
            fBeta = fBeta2;
        } else {
            fBeta = BigDecimal.ZERO;
        }
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        BigDecimal newvalue = BigDecimal.valueOf(timestamp.getValue()).multiply(fAlpha, fMc).add(fBeta);
        return new TmfTimestamp(timestamp, newvalue.longValue());
    }

    @Override
    public long transform(long timestamp) {
        BigDecimal t = BigDecimal.valueOf(timestamp).multiply(fAlpha, fMc).add(fBeta);
        return t.longValue();
    }

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        if (composeWith.equals(TmfTimestampTransform.IDENTITY)) {
            /* If composing with identity, just return this */
            return this;
        } else if (composeWith instanceof TmfTimestampTransformLinear) {
            /* If composeWith is a linear transform, add the two together */
            TmfTimestampTransformLinear ttl = (TmfTimestampTransformLinear) composeWith;
            BigDecimal newAlpha = fAlpha.multiply(ttl.fAlpha, fMc);
            BigDecimal newBeta = fAlpha.multiply(ttl.fBeta, fMc).add(fBeta);
            return new TmfTimestampTransformLinear(newAlpha, newBeta);
        } else {
            /*
             * We do not know what to do with this kind of transform, just
             * return this
             */
            return this;
        }
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof TmfTimestampTransformLinear) {
            TmfTimestampTransformLinear that = (TmfTimestampTransformLinear) other;
            result = ((that.fAlpha.equals(fAlpha)) && (that.fBeta.equals(fBeta)));
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (fBeta.multiply(fAlpha).intValue());
        return result;
    }

    @Override
    public String toString() {
        return "TmfTimestampLinear [ alpha = " + fAlpha.toString() + //$NON-NLS-1$
                ", beta = " + fBeta.toString() + //$NON-NLS-1$
                " ]"; //$NON-NLS-1$
    }

}
