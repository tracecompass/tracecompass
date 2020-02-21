/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.core.aspects;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * Counter aspect that can be grouped and associated to a given resource such as
 * a CPU, a thread, a disk, a GPU or a DSP.
 * <br><br>
 * The concatenation of the aspect's groups form a single entry in the state
 * system:
 * <pre>
 * {root}
 *   +- {group id}
 *       +- {group element}
 *           +- {group id}
 *               +- {group element}
 *                   +- ...
 * </pre>
 *
 * @author Matthew Khouzam
 * @since 3.1
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
