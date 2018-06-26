/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;

/**
 * Interface for a marker time event that includes a category, a color and
 * an optional label.
 *
 * @since 2.0
 */
public interface IMarkerEvent extends ITimeEvent {

    /** Bookmarks marker category */
    @NonNull String BOOKMARKS = checkNotNull(Messages.MarkerEvent_Bookmarks);

    /**
     * Get this marker's category.
     *
     * @return The category
     */
    String getCategory();

    /**
     * Get this marker's color.
     *
     * @return The color
     */
    RGBA getColor();

    /**
     * Returns true if the marker is drawn in foreground, and false otherwise.
     *
     * @return true if the marker is drawn in foreground, and false otherwise.
     */
    boolean isForeground();
}
