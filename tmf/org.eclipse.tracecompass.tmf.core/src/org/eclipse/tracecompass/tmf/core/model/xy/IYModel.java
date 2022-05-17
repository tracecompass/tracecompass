/**********************************************************************
 * Copyright (c) 2017-2020 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.xy;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This represents a model for Y series of a XY chart. {@link IYModel} contains
 * strict minimum information.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public interface IYModel {

    /**
     * Get the Model's unique ID
     *
     * @return get the identifier for the entry that this model maps to.
     */
    long getId();

    /**
     * Get the name of the series, AKA, the name of that series to display
     *
     * @return The name
     */
    String getName();

    /**
     * Get the y values
     *
     * @return An array of y values
     */
    double[] getData();

    /**
     * Get the Y axis description for this model
     *
     * @return The Y axis description, or <code>null</code> if the description
     *         is not provided by the data provider
     * @since 6.1
     */
    default @Nullable TmfXYAxisDescription getYAxisDescription() {
        return null;
    }
}
