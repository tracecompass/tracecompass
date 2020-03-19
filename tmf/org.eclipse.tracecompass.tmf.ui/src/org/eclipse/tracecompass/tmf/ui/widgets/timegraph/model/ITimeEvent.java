/*******************************************************************************
 * Copyright (c) 2009, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *   Patrick Tasse - Refactoring
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IElementResolver;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IMetadataStrings;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IPropertyCollection;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Interface for time events, for use in the timegraph view
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public interface ITimeEvent extends IPropertyCollection, IElementResolver {

    /**
     * Get the entry matching this time event.
     *
     * @return The time graph entry
     */
    ITimeGraphEntry getEntry();

    /**
     * Get the timestamp of this event.
     *
     * @return The event's time
     */
    long getTime();

    /**
     * @return
     * <list>
     * <li>-1: Considers duration to be from current event till the next</li>
     * <li>0: Duration is not relevant e.g. a Burst / no state associated</li>
     * <li>>0: Valid duration value specified</li>
     * </list>
     * <p>
     */
    long getDuration();

    /**
     * Get the label of this event.
     *
     * @return the event's label, or null if it has none
     * @since 4.1
     */
    default String getLabel() {
        return null;
    }

    /**
     * Split an event in two at the specified time and keep the part before the
     * split. If the time is smaller or equal to the event's start, the returned
     * event is null.
     *
     * @param splitTime
     *            the time at which the event is to be split
     * @return The part before the split time
     */
    ITimeEvent splitBefore(long splitTime);

    /**
     * Split an event in two at the specified time and keep the part after the
     * split. If the time is greater or equal to the event's end, the returned
     * event is null.
     *
     * @param splitTime
     *            the time at which the event is to be split
     * @return The part after the split time
     */
    ITimeEvent splitAfter(long splitTime);

    /**
     * @since 5.0
     */
    @Override
    default @NonNull Multimap<@NonNull String, @NonNull Object> getMetadata() {
        String entryName = getEntry().getName();
        return (entryName != null) ? ImmutableMultimap.of(IMetadataStrings.ENTRY_NAME_KEY, entryName) : ImmutableMultimap.of();
    }
}