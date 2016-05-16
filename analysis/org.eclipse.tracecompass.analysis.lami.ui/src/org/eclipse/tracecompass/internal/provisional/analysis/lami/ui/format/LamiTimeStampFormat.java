/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.format;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers.LamiGraphRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;

/**
 * Formatter for time stamps
 */
public class LamiTimeStampFormat extends Format {

    private static final int BIG_DECIMAL_DIVISION_SCALE = 22;

    private static final long serialVersionUID = 4285447886537779762L;

    private final TmfTimestampFormat fFormat;

    private @Nullable LamiGraphRange fInternalRange = null;
    private @Nullable LamiGraphRange fExternalRange = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The default constructor
     */
    public LamiTimeStampFormat() {
        fFormat = checkNotNull(TmfTimestampFormat.getDefaulTimeFormat());
    }

    /**
     * The base constructor
     *
     * @param internalRange
     *            The internal range used for graph representation
     * @param externalRange
     *            The external (real value) range shown to the user
     */
    public LamiTimeStampFormat(LamiGraphRange internalRange, LamiGraphRange externalRange) {
        fFormat = checkNotNull(TmfTimestampFormat.getDefaulTimeFormat());
        fInternalRange = internalRange;
        fExternalRange = externalRange;
    }

    /**
     * The normal constructor
     *
     * @param pattern
     *            The format pattern
     */
    public LamiTimeStampFormat(String pattern) {
        fFormat = new TmfTimestampFormat(pattern);
    }

    /**
     * The normal constructor
     *
     * @param pattern
     *            the format pattern
     * @param internalRange
     *            The internal range used for graph representation
     * @param externalRange
     *            The external (real value) range shown to the user
     */
    public LamiTimeStampFormat(String pattern, LamiGraphRange internalRange, LamiGraphRange externalRange) {
        fFormat = new TmfTimestampFormat(pattern);
        fInternalRange = internalRange;
        fExternalRange = externalRange;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @return the internal range definition
     */
    public @Nullable LamiGraphRange getInternalRange() {
        return fInternalRange;
    }

    /**
     * @param internalRange
     *            The internal range definition to be used by the formatter
     */
    public void setInternalRange(@Nullable LamiGraphRange internalRange) {
        fInternalRange = internalRange;
    }

    /**
     * @return the external range definition
     */
    public @Nullable LamiGraphRange getExternalRange() {
        return fExternalRange;
    }

    /**
     * @param externalRange
     *            The external range definition to be used by the formatter
     */
    public void setExternalRange(@Nullable LamiGraphRange externalRange) {
        fExternalRange = externalRange;
    }


    @Override
    public StringBuffer format(@Nullable Object obj, @Nullable StringBuffer toAppendTo, @Nullable FieldPosition pos) {
        if (obj != null && obj instanceof Number && toAppendTo != null) {
            @Nullable LamiGraphRange internalRange = fInternalRange;
            @Nullable LamiGraphRange externalRange = fExternalRange;
            if (internalRange == null || externalRange == null) {
                long time = ((Number)obj).longValue();
                return checkNotNull(toAppendTo.append(fFormat.format(time)));
            }

            if (internalRange.getDelta().compareTo(BigDecimal.ZERO) == 0 ||
                    externalRange.getDelta().compareTo(BigDecimal.ZERO) == 0) {
                return checkNotNull(toAppendTo.append(fFormat.format(externalRange.getMinimum().doubleValue())));
            }

            /* Find external value before formatting */
            BigDecimal externalValue = (new BigDecimal(obj.toString()))
                    .subtract(internalRange.getMinimum())
                    .multiply(externalRange.getDelta())
                    .divide(internalRange.getDelta(), BIG_DECIMAL_DIVISION_SCALE, BigDecimal.ROUND_DOWN)
                    .add(externalRange.getMinimum());

            return checkNotNull(toAppendTo.append(fFormat.format(externalValue.longValue())));
        }
        return new StringBuffer();
    }

    @Override
    public @Nullable Object parseObject(@Nullable String source, @Nullable ParsePosition pos) {
        return null;
    }

    /**
     * Get the pattern string of the format.
     *
     * @return the pattern string.
     */
    public String getPattern() {
        return fFormat.toPattern();
    }
}
