/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiData;

/**
 * Base class for LAMI table aspects.
 *
 * @author Alexandre Montplaisir
 */
public class LamiGenericAspect extends LamiTableEntryAspect {

    private final int fColIndex;
    private final boolean fIsContinuous;
    private final boolean fIsTimeStamp;

    /**
     * Constructor
     *
     * @param aspectName
     *            Name of the aspect (name of the column in the UI)
     * @param units
     *            The units of this column
     * @param colIndex
     *            Index of this column
     * @param isContinuous
     *            If the contents of this column are numbers or not
     * @param isTimeStamp
     *            If the contents of this column are numerical timestamp or not
     */
    public LamiGenericAspect(String aspectName, @Nullable String units, int colIndex, boolean isContinuous, boolean isTimeStamp) {
        super(aspectName, units);
        fColIndex = colIndex;
        fIsContinuous = isContinuous;
        fIsTimeStamp = isTimeStamp;
    }

    @Override
    public boolean isContinuous() {
        return fIsContinuous;
    }

    @Override
    public boolean isTimeStamp() {
        return fIsTimeStamp;
    }

    @Override
    public @Nullable String resolveString(@NonNull LamiTableEntry entry) {
        return entry.getValue(fColIndex).toString();
    }

    @Override
    public @Nullable Number resolveNumber(@NonNull LamiTableEntry entry) {
        if (fIsContinuous) {
            try {
                LamiData value = entry.getValue(fColIndex);
                if (value.toString() != null) {
                    return Double.parseDouble(value.toString());
                }
            } catch (NumberFormatException e) {
                // Fallback to default value below
            }
        }
        return null;
    }

    /**
     * Get the column index
     *
     * @return the column index
     */
    protected int getColIndex() {
        return fColIndex;
    }

    @Override
    public Comparator<LamiTableEntry> getComparator() {
        if (isContinuous()) {
            return LamiComparators.getDoubleComparator(this::resolveNumber);
        }

        /* Use regular string comparison */
        return LamiComparators.getStringComparator(this::resolveString);
    }

}
