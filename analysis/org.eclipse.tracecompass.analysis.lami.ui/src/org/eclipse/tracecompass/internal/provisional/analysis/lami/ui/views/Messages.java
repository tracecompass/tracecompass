/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the package
 *
 * @noreference Messages class
 */
@NonNullByDefault({})
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String LamiReportView_ActivateTableAction_ButtonName;
    public static String LamiReportView_ActivateTableAction_ButtonTooltip;

    public static String LamiReportView_ToggleAction_ButtonNamePrefix;
    public static String LamiReportView_ToggleAction_ButtonTooltip;

    public static String LamiReportView_NewCustomBarChart;
    public static String LamiReportView_NewCustomScatterChart;
    public static String LamiReportView_ClearAllCustomViews;
    public static String LamiReportView_LogScale;
    public static String LamiReportView_SelectColumnForX;
    public static String LamiReportView_SelectColumnsForCategories;
    public static String LamiReportView_SelectColumnsForSeries;
    public static String LamiReportView_Custom;

    public static String LamiSeriesDialog_creation;
    public static String LamiSeriesDialog_add;
    public static String LamiSeriesDialog_chart_options;
    public static String LamiSeriesDialog_delete;
    public static String LamiSeriesDialog_selectionRestrictionWarning;
    public static String LamiSeriesDialog_serie_creator;
    public static String LamiSeriesDialog_series;
    public static String LamiSeriesDialog_x_axis;
    public static String LamiSeriesDialog_x_values;
    public static String LamiSeriesDialog_y_axis;
    public static String LamiSeriesDialog_y_values;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
