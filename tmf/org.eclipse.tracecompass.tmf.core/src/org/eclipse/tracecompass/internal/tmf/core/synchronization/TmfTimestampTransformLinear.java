/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.synchronization;

import java.math.BigDecimal;
import java.math.MathContext;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.tracecompass.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

/**
 * Class implementing a linear timestamp transform, with a slope and/or offset
 *
 * f(t) = alpha*t + beta
 *
 * @author Geneviève Bastien
 */
public class TmfTimestampTransformLinear implements ITmfTimestampTransformInvertible {

    /**
     * Generated serial UID
     */
    private static final long serialVersionUID = -4756608071358979461L;

    /**
     * Respectively the slope and offset and this linear equation.
     */
    private final @NonNull BigDecimal fAlpha;
    private final @NonNull BigDecimal fBeta;

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
        return TmfTimestamp.create(newvalue.longValue(), timestamp.getScale());
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
            /* Don't use the factory to make sure any further composition will
             * be performed on the same object type */
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
        return "TmfTimestampLinear [ slope = " + fAlpha.toString() + //$NON-NLS-1$
                ", offset = " + fBeta.toString() + //$NON-NLS-1$
                " ]"; //$NON-NLS-1$
    }

    @Override
    public ITmfTimestampTransform inverse() {
        return TimestampTransformFactory.createLinear(NonNullUtils.checkNotNull(BigDecimal.ONE.divide(fAlpha, fMc)), NonNullUtils.checkNotNull(BigDecimal.valueOf(-1).multiply(fBeta).divide(fAlpha, fMc)));
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
}
