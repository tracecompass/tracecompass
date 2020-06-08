/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.xy;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataType;

/**
 * Represent a XY Axis description
 *
 * @author Simon Delisle
 * @since 5.0
 */
public class TmfXYAxisDescription {
    private String fLabel;
    private String fUnit;
    private DataType fDataType;

    /**
     * Constructor
     *
     * @param label
     *            Label for the axis
     * @param unit
     *            Unit type
     */
    public TmfXYAxisDescription(String label, String unit) {
        this(label, unit, DataType.NUMBER);
    }

    /**
     * Constructor
     *
     * @param label
     *            Label for the axis
     * @param unit
     *            Unit type
     * @param dataType
     *            The type of data this series represents
     * @since 6.1
     */
    public TmfXYAxisDescription(String label, String unit, DataType dataType) {
        super();
        fLabel = label;
        fUnit = unit;
        fDataType = dataType;
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
     * Get the units for this data type. It will be appended to the data
     *
     * @return Unit type
     */
    public String getUnit() {
        return fUnit;
    }

    /**
     * Get the type of data this represents
     *
     * @return The type of data this axis represents
     * @since 6.1
     */
    public DataType getDataType() {
        return fDataType;
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
        TmfXYAxisDescription other = (TmfXYAxisDescription) obj;
        return fLabel.equals(other.getLabel())
                && fUnit.equals(other.getUnit())
                && Objects.equals(fDataType, other.fDataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fLabel, fUnit, fDataType);
    }
}
