/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphEntryModelWeighted;

@NonNullByDefault
public class ResourcesEntryModelWeighted extends ResourcesEntryModel implements ITimeGraphEntryModelWeighted {

    private final long fMin;
    private final long fMax;

    /**
     * Constructor
     *
     * @param id
     *            unique Entry ID
     * @param parentId
     *            parent ID
     * @param labels
     *            entry labels
     * @param startTime
     *            start time for this entry
     * @param endTime
     *            end time for this entry
     * @param resourceId
     *            resource ID (IRQ or CPU number)
     * @param type
     *            type of resource (TRACE / CPU / IRQ / SOFT_IRQ)
     * @param min
     *            The minimum value of the states
     * @param max
     *            The maximum value of the states
     */
    public ResourcesEntryModelWeighted(long id, long parentId, List<String> labels, long startTime, long endTime, int resourceId, Type type, long min, long max) {
        super(id, parentId, labels, startTime, endTime, resourceId, type);
        fMin = min;
        fMax = max;
    }

    @Override
    public long getMin() {
        return fMin;
    }

    @Override
    public long getMax() {
        return fMax;
    }

}
