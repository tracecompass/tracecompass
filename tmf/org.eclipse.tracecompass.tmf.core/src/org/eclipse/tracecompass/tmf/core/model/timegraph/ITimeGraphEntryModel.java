/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.Map;

import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Represents a time graph entry model. These objects are typically returned by
 * {@link ITimeGraphDataProvider#fetchTree}. The entry may or may not have a
 * {@link ITimeGraphRowModel} associated to it.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public interface ITimeGraphEntryModel extends ITmfTreeDataModel, IElementResolver {

    /**
     * Gets the entry start time
     *
     * @return Start Time
     */
    long getStartTime();

    /**
     * Gets the entry end time
     *
     * @return End time
     */
    long getEndTime();

    /**
     * Returns true if the entry has a row model, or false if it is a blank entry
     * with no associated states.
     *
     * @return true if the entry has a row model
     */
    default boolean hasRowModel() {
        return true;
    }

    @Override
    default Multimap<String, Object> getMetadata() {
        return HashMultimap.create();
    }

    /**
     * @since 5.2
     */
    @Override
    @Deprecated
    default Map<String, String> computeData() {
        return IElementResolver.super.computeData();
    }
}
