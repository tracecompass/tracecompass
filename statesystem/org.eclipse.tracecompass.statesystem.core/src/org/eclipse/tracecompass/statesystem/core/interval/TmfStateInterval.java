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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * The StateInterval represents the "state" a particular attribute was in, at a
 * given time. It is the main object being returned from queries to the state
 * system.
 *
 * @author Alexandre Montplaisir
 */
public final class TmfStateInterval implements ITmfStateInterval {

    private final long fStart;
    private final long fEnd;
    private final int fAttribute;
    private final @Nullable Object fValue;

    /**
    * Construct an interval from its given parameters
    *
    * @param start
    *            Start time
    * @param end
    *            End time
    * @param attribute
    *            Attribute linked to this interval
    * @param value
    *            {@link Object} this interval will contain
     * @since 3.1
    */
   public TmfStateInterval(long start, long end, int attribute, @Nullable Object value) {
        fStart = start;
        fEnd = end;
        fAttribute = attribute;
        fValue = value;
    }

    @Override
    public long getStartTime() {
        return fStart;
    }

    @Override
    public long getEndTime() {
        return fEnd;
    }

    @Override
    public int getAttribute() {
        return fAttribute;
    }

    @Override
    public ITmfStateValue getStateValue() {
        return TmfStateValue.newValue(fValue);
    }

    @Override
    public boolean intersects(long timestamp) {
        return fStart <= timestamp && fEnd >= timestamp;
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return new ToStringBuilder(this)
            .append("start", fStart) //$NON-NLS-1$
            .append("end", fEnd) //$NON-NLS-1$
            .append("key", fAttribute) //$NON-NLS-1$
            .append("value", String.valueOf(fValue)) //$NON-NLS-1$
            .toString();
    }

}
