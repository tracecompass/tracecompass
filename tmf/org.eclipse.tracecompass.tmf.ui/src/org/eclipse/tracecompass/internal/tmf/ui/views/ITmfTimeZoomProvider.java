/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
     * Method to implement to zoom in or out from current position
     *
     * @param zoomIn
     *          true for zoom-in else for zoom-out
     */
    void zoom(boolean zoomIn);
}