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

package org.eclipse.tracecompass.internal.tmf.chart.ui.dialog;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Messages for the chart package
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$
    /** Title for the selection of data */
    public static @Nullable String ChartMakerDialog_AvailableData;
    /** Title for the selection of the chart type */
    public static @Nullable String ChartMakerDialog_ChartType;
    /** Title for the enabling logarithmic scale for the X axis */
    public static @Nullable String ChartMakerDialog_LogScaleX;
    /** Title for the enabling logarithmic scale for the Y axis */
    public static @Nullable String ChartMakerDialog_LogScaleY;
    /** Title for the options group */
    public static @Nullable String ChartMakerDialog_Options;
    /** Title for the series creator group */
    public static @Nullable String ChartMakerDialog_SeriesCreator;
    /** Title for choosing the data source for the X axis */
    public static @Nullable String ChartMakerDialog_XAxis;
    /** Title for choosing the data source for the Y axis */
    public static @Nullable String ChartMakerDialog_YAxis;
    /** Warning label that choices might be restricted when there is a selection */
    public static @Nullable String ChartMakerDialog_SelectionRestrictionWarning;
    /** Title for the series group */
    public static @Nullable String ChartMakerDialog_SelectedSeries;
    /** Title of the chark maker dialog */
    public static @Nullable String ChartMakerDialog_Title;
    /** Title for X series */
    public static @Nullable String ChartMakerDialog_XSeries;
    /** Title for Y series */
    public static @Nullable String ChartMakerDialog_YSeries;
    /** Title of a warning dialog */
    public static @Nullable String ChartMakerDialog_WarningConfirm;
    /** Message of the warning dialog when there is incompatible series */
    public static @Nullable String ChartMakerDialog_WarningIncompatibleSeries;

    /** Message of the warning dialog when there is incompatible series */
    public static @Nullable String ChartSeries_MultiSeriesTitle;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
