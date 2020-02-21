/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Describe a presentation value, associating to value to a label and color
 *
 * @author Geneviève Bastien
 */
public class DataDrivenPresentationState {

    private final String fValue;
    private final String fName;
    private final @Nullable String fColor;

    /**
     * Constructor
     *
     * @param value
     *            The value of this state, ie the expected value present in the
     *            state system
     * @param name
     *            The name for this state, ie the label to associate with the
     *            value
     * @param color
     *            An optional color to go with this state
     */
    public DataDrivenPresentationState(String value, String name, String color) {
        fValue = value;
        fName = name;
        fColor = color;
    }

    /**
     * Get the value of the presentation state
     *
     * @return The value
     */
    public String getValue() {
        return fValue;
    }

    /**
     * Get the label of this state
     *
     * @return The label or name of the state
     */
    public String getName() {
        return fName;
    }

    /**
     * Get the color for this state
     *
     * @return The color, or <code>null</code> if no color is defined
     */
    public @Nullable String getColor() {
        return fColor;
    }

}
