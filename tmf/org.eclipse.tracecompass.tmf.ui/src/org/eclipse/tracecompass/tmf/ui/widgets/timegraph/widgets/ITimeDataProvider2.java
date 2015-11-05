/*****************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *****************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets;

/**
 * Extension to the ITimeDataProvider interface.
 *
 * @since 1.2
 */
public interface ITimeDataProvider2 extends ITimeDataProvider {

    /**
     * Updates the selection begin and end time and notifies the selection
     * listeners about the new selection range (if it has changed).
     * <p>
     * If ensureVisible is true, the window range will be centered either on the
     * selection begin time (if it has changed) or otherwise on the selection
     * end time, if and only if that time is outside of the current window. If
     * the window range is modified, the range listeners will be notified.
     *
     * @param beginTime
     *            the selection begin time
     * @param endTime
     *            the selection end time
     * @param ensureVisible
     *            if true, ensure visibility of the new selection range boundary
     * @since 1.2
     */
    void setSelectionRangeNotify(long beginTime, long endTime, boolean ensureVisible);

    /**
     * Updates the selection begin and end time.
     * <p>
     * If ensureVisible is true, the window range will be centered either on the
     * selection begin time (if it has changed) or otherwise on the selection
     * end time, if and only if that time is outside of the current window. If
     * the window range is modified, the range listeners will be notified.
     *
     * @param beginTime
     *            the selection begin time
     * @param endTime
     *            the selection end time
     * @param ensureVisible
     *            if true, ensure visibility of the new selection range boundary
     * @since 1.2
     */
    void setSelectionRange(long beginTime, long endTime, boolean ensureVisible);
}
