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

    private final @Nullable LamiGraphRange fInternalRange;
    private final @Nullable LamiGraphRange fExternalRange;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

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
    public LamiTimeStampFormat(String pattern, @Nullable LamiGraphRange internalRange, @Nullable LamiGraphRange externalRange) {
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
     * @return the external range definition
     */
    public @Nullable LamiGraphRange getExternalRange() {
        return fExternalRange;
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
