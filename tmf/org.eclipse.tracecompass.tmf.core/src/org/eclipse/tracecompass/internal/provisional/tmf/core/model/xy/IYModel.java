/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy;

import org.eclipse.tracecompass.tmf.core.viewmodel.IYSeries;

/**
 * This represents a model for Y series of a XY chart. Even if {@link IYSeries}
 * and this class share the same data, {@link IYSeries} is only used by viewers
 * as a ViewModel and contains UI informations such as color, style, etc.
 * {@link IYModel} contains strict minimum informations. It's highly recommended
 * to used this class for data providers instead of {@link IYSeries}.
 *
 * @author Yonni Chen
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
}
