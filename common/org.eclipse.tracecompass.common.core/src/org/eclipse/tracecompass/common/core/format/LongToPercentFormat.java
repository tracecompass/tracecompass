/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.format;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author Geneviève Bastien
 * @since 3.1
 */
public class LongToPercentFormat extends Format {

    /**
     * The maximum long value that represents 100%
     */
    public static final long MAX_PERCENT_VALUE = 1000000000L;

    /**
     * generated uid
     */
    private static final long serialVersionUID = -5576403724759807115L;

    private static final @NonNull Format INSTANCE = new LongToPercentFormat();
    private static final Format FORMAT = new DecimalFormat("##.######%"); //$NON-NLS-1$

    /**
     * Protected constructor
     */
    private LongToPercentFormat() {
        super();
    }

    /**
     * Returns the instance of this formatter
     *
     * @return The instance of this formatter
     */
    public static @NonNull Format getInstance() {
        return INSTANCE;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof Number) {
            Number num = (Number) obj;
            double value = num.doubleValue();
            return toAppendTo.append(FORMAT.format(value / MAX_PERCENT_VALUE));
        }
        return toAppendTo.append(obj);
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        Number number = NumberFormat.getInstance().parse(source, pos);
        if (number == null) {
            return null;
        }
        return number.doubleValue() * MAX_PERCENT_VALUE;
    }

}
