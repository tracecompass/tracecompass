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
 * Interface for a view to support zoom to selection.
 *
 * @author Bernd Hufmann
 *
 */
public interface ITmfZoomToSelectionProvider {

    /**
     * Zoom to selection
     */
    void zoomToSelection();
}
