/**********************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import org.eclipse.tracecompass.tmf.core.model.ITimeElement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Model of a arrow used in a time graph data provider.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public interface ITimeGraphArrow extends ITimeElement {

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

    @Override
    default Multimap<String, Object> getMetadata() {
        return HashMultimap.create();
    }
}