/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.widgets.piechart;

/**
 * Pie Slice for pie chart.
 *
 * @author Matthew Khouzam
 */
public class PieSlice {
    private final String fLabel;
    private final double fValue;
    private final String fID;

    /**
     * Pie slice contructor
     *
     * @param label
     *            the label to display
     * @param value
     *            the value of the item (self value)
     * @param id
     *            the ID of the slice
     */
    public PieSlice(String label, double value, String id) {
        fLabel = label;
        fValue = value;
        fID = id;
    }

    /**
     * Get the label
     *
     * @return the label
     */
    public String getLabel() {
        return fLabel;
    }

    /**
     * Get the value
     *
     * @return the value
     */
    public double getValue() {
        return fValue;
    }

    /**
     * Get the id
     *
     * @return the id
     */
    public String getID() {
        return fID;
    }
}
