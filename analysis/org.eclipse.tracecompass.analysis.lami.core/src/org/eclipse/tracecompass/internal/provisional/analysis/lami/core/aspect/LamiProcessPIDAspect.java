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
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiProcess;

/**
 * Aspect for process PID
 *
 * @author Philippe Proulx
 */
public class LamiProcessPIDAspect extends LamiGenericAspect {

    /**
     * Constructor
     *
     * @param colName
     *            Column name
     * @param colIndex
     *            Column index
     */
    public LamiProcessPIDAspect(String colName, int colIndex) {
        super(colName + " (PID)", null, colIndex, false, false); //$NON-NLS-1$
    }

    @Override
    public @Nullable String resolveString(LamiTableEntry entry) {
        LamiData data = entry.getValue(getColIndex());
        if (data instanceof LamiProcess) {
            Long pid = ((LamiProcess) data).getPID();

            if (pid == null) {
                return null;
            }

            return pid.toString();
        }
        /* Could be null, unknown, etc. */
        return data.toString();
    }

    @Override
    public @Nullable Number resolveNumber(LamiTableEntry entry) {
        LamiData data = entry.getValue(getColIndex());
        if (data instanceof LamiProcess) {
            Long pid = ((LamiProcess) data).getPID();
            return pid;
        }

        return null;
    }

    @Override
    public Comparator<LamiTableEntry> getComparator() {
        return LamiComparators.getLongComparator(this::resolveNumber);
    }
}
