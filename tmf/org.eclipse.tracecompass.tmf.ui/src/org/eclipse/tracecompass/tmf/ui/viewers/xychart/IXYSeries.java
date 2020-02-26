/**********************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xychart;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Color;

/**
 * Series interface
 *
 * @author Matthew Khouzam
 * @since 6.0
 */
public interface IXYSeries {

    /**
     * Get X series
     *
     * @return the X series
     */
    double[] getXSeries();

    /**
     * Get Y series
     *
     * @return the Y series
     */
    double[] getYSeries();

    /**
     * Get the series ID
     *
     * @return the series ID
     */
    String getId();

    /**
     * Is the series visible?
     *
     * @return is the series visible
     */
    boolean isVisible();

    /**
     * Get the color for the series
     *
     * @return the color
     */
    @Nullable Color getColor();
}
