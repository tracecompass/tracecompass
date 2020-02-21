/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.views;

/**
 * Interface with a method for time zooming in time-based views.
 *
 * @author Bernd Hufmann
 *
 */
public interface ITmfTimeZoomProvider {
    /**
     * Method to implement to zoom in or out operation.
     *
     * @param zoomIn
     *            true for zoom-in else for zoom-out
     * @param useMousePosition
     *            true for zoom centered on mouse position, false for zoom
     *            centered on the middle of current selection range
     */
    void zoom(boolean zoomIn, boolean useMousePosition);
}