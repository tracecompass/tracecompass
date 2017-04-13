/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.interval;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;

/**
 * The StateInterval represents the "state" a particular attribute was in, at a
 * given time. It is the main object being returned from queries to the state
 * system.
 *
 * @author Alexandre Montplaisir
 */
public final class TmfStateInterval implements ITmfStateInterval {

    private final long start;
    private final long end;
    private final int attribute;
    private final @NonNull ITmfStateValue sv;

    /**
     * Construct an interval from its given parameters
     *
     * @param start
     *            Start time
     * @param end
     *            End time
     * @param attribute
     *            Attribute linked to this interval
     * @param sv
     *            State value this interval will contain
     */
    public TmfStateInterval(long start, long end, int attribute,
            @NonNull ITmfStateValue sv) {
        this.start = start;
        this.end = end;
        this.attribute = attribute;
        this.sv = sv;
    }

    @Override
    public long getStartTime() {
        return start;
    }

    @Override
    public long getEndTime() {
        return end;
    }

    @Override
    public int getAttribute() {
        return attribute;
    }

    @Override
    public ITmfStateValue getStateValue() {
        return sv;
    }

    @Override
    public boolean intersects(long timestamp) {
        if (start <= timestamp) {
            if (end >= timestamp) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return new ToStringBuilder(this)
            .append("start", start) //$NON-NLS-1$
            .append("end", end) //$NON-NLS-1$
            .append("key", attribute) //$NON-NLS-1$
            .append("value", sv.toString()) //$NON-NLS-1$
            .toString();
    }

}
