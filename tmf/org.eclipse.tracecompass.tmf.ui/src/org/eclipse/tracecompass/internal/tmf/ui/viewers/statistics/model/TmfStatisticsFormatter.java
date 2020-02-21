/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Add percentages to the label provider
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model;

import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfBaseColumnDataProvider.StatsColumn;

/**
 * Class that format data for cells in the statistics view.
 *
 * @author Vincent Perot
 */
public final class TmfStatisticsFormatter {

    /**
     * Formatter for the column data
     */
    private static final NumberFormat FORMATTER = NumberFormat.getNumberInstance(Locale.getDefault());

    TmfStatisticsFormatter() {
        // Nothing to construct.
    }

    /**
     * Generate the string for display in a cell.
     *
     * @param node
     *            Current node.
     * @param config
     *            Configuration between total and partial.
     * @return The formatted string ready for display.
     */
    public static String toColumnData(TmfStatisticsTreeNode node, StatsColumn config) {

        long eventValue = 0;

        switch (config) {

        case TOTAL:
            eventValue = node.getValues().getTotal();
            break;

        case PARTIAL:
            eventValue = node.getValues().getPartial();
            break;

        // Other values are illegal.
        // $CASES-OMITTED$
        default:
            throw new IllegalArgumentException();
        }

        return FORMATTER.format(eventValue);
    }

    /**
     * Format the percentage according to user settings.
     *
     * @param percentage
     *            the percentage to format
     * @return The formated percentage as a string.
     */
    public static String toPercentageText(double percentage) {

        // The cast to long is needed because the formatter cannot truncate the number.
        double truncPercentage = ((long) (1000.0 * percentage)) / 10.0;

        String percentageString = String.format("%s%s%s", "  ", FORMATTER.format(truncPercentage), " %     "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return percentageString;
    }
}