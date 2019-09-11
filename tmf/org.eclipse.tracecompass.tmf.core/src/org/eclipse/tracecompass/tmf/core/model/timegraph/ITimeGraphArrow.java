/**********************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;

/**
 * Model of a arrow used in a time graph data provider.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public interface ITimeGraphArrow {

    /**
     * Gets the source {@link ITimeGraphEntryModel}'s ID
     *
     * @return Source ID
     */
    long getSourceId();

    /**
     * Gets the destination {@link ITimeGraphEntryModel}'s ID
     *
     * @return Destination ID
     */
    long getDestinationId();

    /**
     * Gets the start time
     *
     * @return The start time
     */
    long getStartTime();

    /**
     * Gets the duration
     *
     * @return The duration
     */
    long getDuration();

    /**
     * Gets the arrow value
     *
     * @return value associated to this arrow
     */
    int getValue();

    /**
     * Get the style associated with this state
     *
     * @return {@link OutputElementStyle} describing the style of this state
     * @since 5.1
     */
    default @Nullable OutputElementStyle getStyle() {
        return null;
    }
}