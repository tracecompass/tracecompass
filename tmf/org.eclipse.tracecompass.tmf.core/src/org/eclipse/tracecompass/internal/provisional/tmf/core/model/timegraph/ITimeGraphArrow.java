/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph;

/**
 * Model of a arrow used in a time graph data provider.
 *
 * @since 3.2
 * @author Simon Delisle
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

}