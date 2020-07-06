/**********************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.statistics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * Generic number comparator to handle Numbers
 *
 * @author Matthew Khouzam
 */
public final class NumberComparator implements Comparator<Number> {
    @Override
    public int compare(Number o1, Number o2) {
        if (o1 instanceof Long && o2 instanceof Long) {
            return ((Long) o1).compareTo((Long) o2);
        } else if (o1 instanceof Double && o2 instanceof Double) {
            return ((Double) o1).compareTo((Double) o2);
        } else if (o1 instanceof Integer && o2 instanceof Integer) {
            return ((Integer) o1).compareTo((Integer) o2);
        } else if (o1 instanceof Float && o2 instanceof Float) {
            return ((Float) o1).compareTo((Float) o2);
        } else if (o1 instanceof Short && o2 instanceof Short) {
            return ((Short) o1).compareTo((Short) o2);
        } else if (o1 instanceof Byte && o2 instanceof Byte) {
            return ((Byte) o1).compareTo((Byte) o2);
        } else if (o1 instanceof BigInteger && o2 instanceof BigInteger) {
            return ((BigInteger) o1).compareTo((BigInteger) o2);
        } else if (o1 instanceof BigDecimal && o2 instanceof BigDecimal) {
            return ((BigDecimal) o1).compareTo((BigDecimal) o2);
        } else {
            throw new IllegalArgumentException("Cannot compare : " + o1.getClass().getCanonicalName() + " to " + o2.getClass().getCanonicalName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}