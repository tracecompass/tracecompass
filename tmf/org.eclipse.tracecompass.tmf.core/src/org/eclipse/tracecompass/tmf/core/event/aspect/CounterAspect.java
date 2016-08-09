/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Counter aspect, an aspect that can be grouped. This can allow a counter to be
 * associated to a given resource such as a CPU, a thread, a disk, a GPU or a
 * DSP.
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class CounterAspect extends AbstractCounterAspect {

    /*
     * TODO: change for ITmfEventAspects<?>
     */
    private final String[] fGroupIds;

    /**
     * Counter aspect constructor
     *
     *
     * @param fieldName
     *            the field to follow
     * @param label
     *            display name
     * @param groupIds
     *            the grouping id, null or empty means ungrouped
     *
     *            TODO: Change for {@link ITmfEventAspect}
     */
    public CounterAspect(String fieldName, String label, String... groupIds) {
        super(fieldName, label);
        fGroupIds = groupIds;
    }

    /**
     * Get the grouping ids
     *
     * TODO: Change for {@link ITmfEventAspect}
     *
     * @return the grouping ids
     */
    public String[] getGroupIds() {
        return fGroupIds;
    }

    @Override
    public int hashCode() {
        String @NonNull [] groupIds = getGroupIds();
        if (groupIds.length == 0) {
            return super.hashCode();
        }
        return Objects.hash(Arrays.deepHashCode(groupIds), super.hashCode());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CounterAspect other = (CounterAspect) obj;
        return Arrays.deepEquals(fGroupIds, other.getGroupIds());
    }

}
