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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Counter aspect that can be grouped and associated to a given resource such as
 * a CPU, a thread, a disk, a GPU or a DSP.
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class CounterAspect extends AbstractCounterAspect {

    private final Class<? extends ITmfEventAspect<?>>[] fGroups;

    /**
     * Counter aspect constructor
     *
     * @param fieldName
     *            the field to follow
     * @param label
     *            display name
     * @param groups
     *            the groups, empty means ungrouped
     */
    @SafeVarargs
    public CounterAspect(String fieldName, String label, Class<? extends ITmfEventAspect<?>>... groups) {
        super(fieldName, label);
        fGroups = Arrays.copyOf(groups, groups.length);
    }

    /**
     * Get the groups
     *
     * @return the groups
     */
    public Class<? extends ITmfEventAspect<?>>[] getGroups() {
        return fGroups;
    }

    @Override
    public int hashCode() {
        return (fGroups.length == 0) ? super.hashCode() : Objects.hash(Arrays.deepHashCode(fGroups), super.hashCode());
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
        return Arrays.deepEquals(fGroups, other.getGroups());
    }

}
