/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import org.eclipse.swt.graphics.Color;

/**
 * Interface for a marker time event that includes a color and optional label.
 *
 * @since 2.0
 */
public interface IMarkerEvent extends ITimeEvent {

    /**
     * Get this marker's label.
     *
     * @return The label, or null
     */
    String getLabel();

    /**
     * Get this marker's color.
     *
     * @return The color
     */
    Color getColor();

    /**
     * Returns true if the marker is drawn in foreground, and false otherwise.
     *
     * @return true if the marker is drawn in foreground, and false otherwise.
     */
    boolean isForeground();
}
