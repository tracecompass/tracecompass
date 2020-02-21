/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexis Cabana-Loriaux - Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts;

/**
 * Interface used to take control of a {@link TmfPieChartViewer} as part of the
 * State design pattern. Thus it is closely related with the TmfPieChartViewer
 *
 * @author Alexis Cabana-Loriaux
 * @since 2.0
 */
interface IPieChartViewerState {

    /**
     * To be called when the current selection has changed
     *
     * @param context
     *            The context in which to apply the changes
     */
    void newSelection(final TmfPieChartViewer context);

    /**
     * To be called when the current selection changes to "empty"
     *
     * @param context
     *            The context in which to apply the changes
     */
    void newEmptySelection(final TmfPieChartViewer context);

    /**
     * To be called when there are new global entries to show
     *
     * @param context
     *            The context in which to apply the changes
     */
    void newGlobalEntries(final TmfPieChartViewer context);
}
