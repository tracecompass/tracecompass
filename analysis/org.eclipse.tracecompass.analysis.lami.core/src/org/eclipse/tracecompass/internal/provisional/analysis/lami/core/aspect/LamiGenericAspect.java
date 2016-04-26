/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;

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
    public @Nullable Double resolveDouble(@NonNull LamiTableEntry entry) {
        if (fIsContinuous) {
            try {
                if (entry.getValue(fColIndex).toString() != null) {
                    return Double.parseDouble(entry.getValue(fColIndex).toString());
                }
            } catch (NumberFormatException e) {
                // Fallback to default value below
            }
        }
        return null;
    }

    @Override
    public Comparator<LamiTableEntry> getComparator() {
        if (isContinuous()) {
            return (o1, o2) -> {
                Double dO1 = resolveDouble(o1);
                Double dO2 = resolveDouble(o2);
                if (dO1 == null || dO2 == null) {
                    return 0;
                }

                return dO1.compareTo(dO2);
            };
        }

        /* Use regular string comparison */
        return (o1, o2) -> {
            String s1 = resolveString(o1);
            String s2 = resolveString(o2);

            if (s1 == null || s2 == null) {
                return 0;
            }

            return s1.compareTo(s2);
        };
    }

}
