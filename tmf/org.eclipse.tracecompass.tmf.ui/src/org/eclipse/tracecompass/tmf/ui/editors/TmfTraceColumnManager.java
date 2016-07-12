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

import org.eclipse.tracecompass.internal.tmf.ui.editors.TmfTableColumnUtils;

/**
 * This class manages the column settings associated with a trace type.
 *
 * @author Patrick Tasse
 * @since 1.0
 */
public class TmfTraceColumnManager {

    /**
     * Get the latest saved table column order for the specified trace type.
     * Returns null if no column order is set.
     *
     * @param traceTypeId
     *            the trace type id
     * @return the table column order, or null
     */
    public static int[] loadColumnOrder(String traceTypeId) {
        return TmfTableColumnUtils.loadColumnOrder(traceTypeId);
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
        TmfTableColumnUtils.saveColumnOrder(traceTypeId, columnOrder);
    }

    /**
     * Clears the table column order for the specified trace type.
     *
     * @param traceTypeId
     *            the trace type id
     */
    public static void clearColumnOrder(String traceTypeId) {
        TmfTableColumnUtils.clearColumnOrder(traceTypeId);
    }
}
