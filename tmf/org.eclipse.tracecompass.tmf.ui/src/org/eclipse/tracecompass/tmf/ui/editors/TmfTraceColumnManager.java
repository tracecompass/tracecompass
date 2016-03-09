/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.editors;

import java.util.Arrays;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;

/**
 * This class manages the column settings associated with a trace type.
 *
 * @author Patrick Tasse
 * @since 1.0
 */
public class TmfTraceColumnManager {

    private static final String ROOT_SECTION_NAME = TmfTraceColumnManager.class.getSimpleName();
    private static final String COLUMN_ORDER_SECTION_NAME = "column.order"; //$NON-NLS-1$

    /**
     * Get the latest saved table column order for the specified trace type.
     * Returns null if no column order is set.
     *
     * @param traceTypeId
     *            the trace type id
     * @return the table column order, or null
     */
    public static int[] loadColumnOrder(String traceTypeId) {
        if (traceTypeId == null) {
            return null;
        }
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(ROOT_SECTION_NAME);
        if (section == null) {
            return null;
        }
        IDialogSettings columnOrderSection = section.getSection(COLUMN_ORDER_SECTION_NAME);
        if (columnOrderSection == null) {
            return null;
        }
        String string = columnOrderSection.get(traceTypeId);
        if (string == null || string.length() < 3) {
            return null;
        }
        if (string.charAt(0) != '[' || string.charAt(string.length() - 1) != ']') {
            return null;
        }
        String[] array = string.substring(1, string.length() - 1).split(", "); //$NON-NLS-1$
        int[] columnOrder = new int[array.length];
        try {
            for (int i = 0; i < array.length; i++) {
                columnOrder[i] = Integer.valueOf(array[i]);
            }
            return columnOrder;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Saves the table column order for the specified trace type. Passing a null
     * column order clears it.
     *
     * @param traceTypeId
     *            the trace type id
     * @param columnOrder
     *            the table column order
     */
    public static void saveColumnOrder(String traceTypeId, int[] columnOrder) {
        if (traceTypeId == null) {
            return;
        }
        if (columnOrder == null) {
            clearColumnOrder(traceTypeId);
            return;
        }
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(ROOT_SECTION_NAME);
        if (section == null) {
            section = settings.addNewSection(ROOT_SECTION_NAME);
        }
        IDialogSettings columnOrderSection = section.getSection(COLUMN_ORDER_SECTION_NAME);
        if (columnOrderSection == null) {
            columnOrderSection = section.addNewSection(COLUMN_ORDER_SECTION_NAME);
        }
        columnOrderSection.put(traceTypeId, Arrays.toString(columnOrder));
    }

    /**
     * Clears the table column order for the specified trace type.
     *
     * @param traceTypeId
     *            the trace type id
     */
    public static void clearColumnOrder(String traceTypeId) {
        if (traceTypeId == null) {
            return;
        }
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(ROOT_SECTION_NAME);
        if (section == null) {
            return;
        }
        IDialogSettings columnOrderSection = section.getSection(COLUMN_ORDER_SECTION_NAME);
        if (columnOrderSection == null) {
            return;
        }
        columnOrderSection.put(traceTypeId, (String) null);
    }
}
