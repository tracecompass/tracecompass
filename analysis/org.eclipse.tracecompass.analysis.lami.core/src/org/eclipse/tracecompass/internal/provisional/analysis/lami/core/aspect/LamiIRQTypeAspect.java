/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiData;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiIRQ;

/**
 * Aspect for IRQ type, indicating if it is a hardware IRQ or software IRQ.
 *
 * @author Philippe Proulx
 */
public class LamiIRQTypeAspect extends LamiTableEntryAspect {

    private final int fColIndex;

    /**
     * Constructor
     *
     * @param colName
     *            Column name
     * @param colIndex
     *            Column index
     */
    public LamiIRQTypeAspect(String colName, int colIndex) {
        super(colName + " (" + Messages.LamiAspect_Type +')', null); //$NON-NLS-1$
        fColIndex = colIndex;
    }

    @Override
    public boolean isContinuous() {
        return false;
    }

    @Override
    public boolean isTimeStamp() {
        return false;
    }

    @Override
    public @Nullable String resolveString(LamiTableEntry entry) {
        LamiData data = entry.getValue(fColIndex);
        if (data instanceof LamiIRQ) {
            LamiIRQ irq = (LamiIRQ) data;

            switch (irq.getType()) {
            case HARD:
                return Messages.LamiIRQTypeAspect_HardwareIRQ;

            case SOFT:
                return Messages.LamiIRQTypeAspect_SoftIRQ;

            default:
                return "?"; //$NON-NLS-1$
            }
        }
        /* Could be null, unknown, etc. */
        return data.toString();
    }

    @Override
    public @Nullable Double resolveDouble(LamiTableEntry entry) {
        return null;
    }

    @Override
    public Comparator<LamiTableEntry> getComparator() {
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
