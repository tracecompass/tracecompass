/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiData;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiIRQ;

/**
 * Aspect for the IRQ numbers.
 *
 * This resolves the IRQ number for a given table, so 0|timer would return 0.
 *
 * @author Philippe Proulx
 */
public class LamiIRQNumberAspect extends LamiGenericAspect {

    /**
     * Constructor
     *
     * @param colName
     *            Column name
     * @param colIndex
     *            Column index
     */
    public LamiIRQNumberAspect(String colName, int colIndex) {
        super(colName + " (#)", null, colIndex, false, false); //$NON-NLS-1$
    }

    @Override
    public @Nullable String resolveString(LamiTableEntry entry) {
        Number number = resolveNumber(entry);
        if (number == null) {
            return entry.getValue(getColIndex()).toString();
        }
        return String.valueOf(number);
    }

    @Override
    public @Nullable Number resolveNumber(LamiTableEntry entry) {
        LamiData data = entry.getValue(getColIndex());
        if (data instanceof LamiIRQ) {
            return (((LamiIRQ) data).getNumber());
        }

        return null;
    }

    @Override
    public Comparator<LamiTableEntry> getComparator() {
        return LamiComparators.getLongComparator(this::resolveNumber);
    }

}
