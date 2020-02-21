/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage;

import java.util.Objects;

import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageEntryModel;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;

/**
 * Represents an entry in the tree viewer of the CPU usage view. An entry is a
 * thread that occupied part of the CPU in the selected time range.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageEntry extends TmfGenericTreeEntry<CpuUsageEntryModel> {

    private final double fPercent;

    /**
     * Constructor
     *
     * @param model
     *            {@link CpuUsageEntryModel} from the data provider
     * @param percent
     *            The percentage CPU usage
     */
    public CpuUsageEntry(CpuUsageEntryModel model, double percent) {
        super(model);
        fPercent = percent;
    }

    /**
     * Get the percentage of time spent on CPU in the time interval represented by
     * this entry.
     *
     * @return The percentage of time spent on CPU
     */
    public double getPercent() {
        return fPercent;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            // reference equality, nullness, getName, children and model
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CpuUsageEntry other = (CpuUsageEntry) obj;
        return Objects.equals(fPercent, other.fPercent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fPercent);
    }
}
