/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.xy;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represent a XY Axis model
 *
 * @author Simon Delisle
 * @since 4.3
 */
public class TmfXYAxis {
    private String fLabel;
    private String fUnit;

    /**
     * Constructor
     *
     * @param label
     *            Label for the axis
     * @param unit
     *            Unit type
     */
    public TmfXYAxis(String label, String unit) {
        super();
        fLabel = label;
        fUnit = unit;
    }

    /**
     * Get the axis label
     *
     * @return Label
     */
    public String getLabel() {
        return fLabel;
    }

    /**
     * Get the unit type
     *
     * @return Unit type
     */
    public String getUnit() {
        return fUnit;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfXYAxis other = (TmfXYAxis) obj;
        return fLabel.equals(other.getLabel())
                && fUnit.equals(other.getUnit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fLabel, fUnit);
    }
}
