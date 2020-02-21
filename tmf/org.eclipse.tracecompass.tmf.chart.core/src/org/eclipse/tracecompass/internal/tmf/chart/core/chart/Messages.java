/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.core.chart;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the chart package
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$
    /**
     * Title of a bar chart
     */
    public static String Chart_EnumBarChart;
    /**
     * Title of a scatter chart
     */
    public static String Chart_EnumPieChart;
    /**
     * Title of a pie chart
     */
    public static String Chart_EnumScatterChart;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
