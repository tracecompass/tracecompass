/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.xy;

import java.util.Map;

import org.eclipse.tracecompass.tmf.core.viewmodel.ICommonXAxisModel;

/**
 * This is the XY model interface returned by data providers. This model is
 * immutable and is used by viewers. In this model, there is no information
 * about color, style or chart type (bar, scatter, line, etc.). It contains only
 * data.
 * <br/><br/>
 *
 * Basically, CommonXAxis stands for a collection of Y values that share the
 * same array of X values. For example, if we take the points [1, 33]; [1, 34];
 * [1, 35]; [2, 43]; [2, 44]; [2, 45]. There are 6 points and 3 series: <br/>
 * <br/>
 * Series 1: points [1, 33] ; [2, 43] give ySeries = [33, 43]<br/>
 * Series 2: points [1, 34] ; [2, 44] give ySeries = [34, 44]<br/>
 * Series 3: points [1, 35] ; [2, 45] give ySeries = [35, 45]<br/>
 * All series share [1, 2] as X values
 * <br/><br/>
 *
 * Unlike {@link ICommonXAxisModel}, this interface returns a collection of
 * {@link IYModel}, a minimal model for Y values that contains only data. No
 * informations about UI properties
 *
 * @author Yonni Chen
 * @since 4.0
 */
public interface ITmfCommonXAxisModel extends ITmfXyModel {

    /**
     * Get the X values
     *
     * @return The x values
     * @since 5.0
     */
    default long[] getXValues() {
        return getXAxis();
    }

    /**
     * Get the X values
     *
     * @return The x values
     * @deprecated Use getXValues instead
     */
    @Deprecated
    long[] getXAxis();

    /**
     * Get the collection of {@link IYModel}
     *
     * @return the collection of Y values.
     */
    Map<String, IYModel> getYData();
}
