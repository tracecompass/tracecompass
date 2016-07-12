/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.editors;

import java.util.Arrays;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;

/**
 * Column table utils
 *
 * @author Matthew Khouzam
 *
 */
public final class TmfTableColumnUtils {

    private static final String ROOT_SECTION_NAME = "editor.eventstable.column"; //$NON-NLS-1$
    private static final String COLUMN_ORDER_SECTION_NAME = "order"; //$NON-NLS-1$
    private static final String COLUMN_WIDTH_SECTION_NAME = "width"; //$NON-NLS-1$
    private static final String COLUMN_RESIZABLE_SECTION_NAME = "resizable"; //$NON-NLS-1$

    private TmfTableColumnUtils() {
        // do nothing
    }

    /**
     * Loads the column orders
     *
     * @param traceTypeId
     *            the trace type ID
     * @return an integer array of the column order
     */
    public static int[] loadColumnOrder(String traceTypeId) {
        return loadIntSection(traceTypeId, COLUMN_ORDER_SECTION_NAME);
    }

    /**
     * Loads the column widths
     *
     * @param traceTypeId
     *            the trace type ID
     * @return an integer array of the column widths
     */
    public static int[] loadColumnWidth(String traceTypeId) {
        return loadIntSection(traceTypeId, COLUMN_WIDTH_SECTION_NAME);
    }

    /**
     * Loads the column resizablity
     *
     * @param traceTypeId
     *            the trace type ID
     * @return an integer array of the column resizablity
     */
    public static boolean[] loadColumnResizable(String traceTypeId) {
        return loadBoolSection(traceTypeId, COLUMN_RESIZABLE_SECTION_NAME);
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
        saveColumnElementInt(traceTypeId, COLUMN_ORDER_SECTION_NAME, columnOrder);
    }

    /**
     * Saves the table column width for the specified trace type. Passing a null
     * column width clears it.
     *
     * @param traceTypeId
     *            the trace type id
     * @param columnWidth
     *            the table column width
     */
    public static void saveColumnWidth(String traceTypeId, int[] columnWidth) {
        saveColumnElementInt(traceTypeId, COLUMN_WIDTH_SECTION_NAME, columnWidth);
    }

    /**
     * Saves the table column resizability for the specified trace type. Passing a null
     * column resizability clears it.
     *
     * @param traceTypeId
     *            the trace type id
     * @param columnResizability
     *            the table column resizability
     */
    public static void saveColumnResizability(String traceTypeId, boolean[] columnResizability) {
        saveColumnElementBoolean(traceTypeId, COLUMN_RESIZABLE_SECTION_NAME, columnResizability);
    }

    /**
     * Clears the table column visibility for the specified trace type.
     *
     * @param traceTypeId
     *            the trace type id
     */
    public static void clearColumnOrder(String traceTypeId) {
        clearColumnElement(traceTypeId, COLUMN_ORDER_SECTION_NAME);
    }

    /**
     * Clears the table column visibility for the specified trace type.
     *
     * @param traceTypeId
     *            the trace type id
     */
    public static void clearColumnWidth(String traceTypeId) {
        clearColumnElement(traceTypeId, COLUMN_WIDTH_SECTION_NAME);
    }

    /**
     * Clears the table column visibility for the specified trace type.
     *
     * @param traceTypeId
     *            the trace type id
     */
    public static void clearColumnResizable(String traceTypeId) {
        clearColumnElement(traceTypeId, COLUMN_RESIZABLE_SECTION_NAME);
    }

    private static boolean[] loadBoolSection(String traceTypeId, String sectionName) {
        String columnSection = loadSection(traceTypeId, sectionName);
        if (columnSection == null) {
            return null;
        }
        String[] array = columnSection.substring(1, columnSection.length() - 1).split(", "); //$NON-NLS-1$
        boolean[] columnValue = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            columnValue[i] = Boolean.valueOf(array[i]);
        }
        return columnValue;
    }

    private static int[] loadIntSection(String traceTypeId, String sectionName) {
        String columnSection = loadSection(traceTypeId, sectionName);
        if (columnSection == null) {
            return null;
        }
        String[] array = columnSection.substring(1, columnSection.length() - 1).split(", "); //$NON-NLS-1$
        int[] columnValue = new int[array.length];
        try {
            for (int i = 0; i < array.length; i++) {
                columnValue[i] = Integer.valueOf(array[i]);
            }
            return columnValue;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void saveColumnElementBoolean(String traceTypeId, String sectionName, boolean[] data) {
        if (traceTypeId == null) {
            return;
        }
        if (data == null) {
            clearColumnElement(traceTypeId, sectionName);
            return;
        }
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(ROOT_SECTION_NAME);
        if (section == null) {
            section = settings.addNewSection(ROOT_SECTION_NAME);
        }
        IDialogSettings columnSection = section.getSection(sectionName);
        if (columnSection == null) {
            columnSection = section.addNewSection(sectionName);
        }
        columnSection.put(traceTypeId, Arrays.toString(data));
    }

    private static void saveColumnElementInt(String traceTypeId, String sectionName, int[] data) {
        if (traceTypeId == null) {
            return;
        }
        if (data == null) {
            clearColumnElement(traceTypeId, sectionName);
            return;
        }
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(ROOT_SECTION_NAME);
        if (section == null) {
            section = settings.addNewSection(ROOT_SECTION_NAME);
        }
        IDialogSettings columnSection = section.getSection(sectionName);
        if (columnSection == null) {
            columnSection = section.addNewSection(sectionName);
        }
        columnSection.put(traceTypeId, Arrays.toString(data));
    }

    private static void clearColumnElement(String traceTypeId, final String sectionName) {
        if (traceTypeId == null) {
            return;
        }
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(ROOT_SECTION_NAME);
        if (section == null) {
            return;
        }
        IDialogSettings columnSection = section.getSection(sectionName);
        if (columnSection == null) {
            return;
        }
        columnSection.put(traceTypeId, (String) null);
    }

    private static String loadSection(String traceTypeId, String sectionName) {
        if (traceTypeId == null) {
            return null;
        }
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(ROOT_SECTION_NAME);
        if (section == null) {
            return null;
        }
        IDialogSettings columnSection = section.getSection(sectionName);
        if (columnSection == null) {
            return null;
        }
        String string = columnSection.get(traceTypeId);
        if (string == null || string.length() < 3) {
            return null;
        }
        if (string.charAt(0) != '[' || string.charAt(string.length() - 1) != ']') {
            return null;
        }
        return string;
    }

}
