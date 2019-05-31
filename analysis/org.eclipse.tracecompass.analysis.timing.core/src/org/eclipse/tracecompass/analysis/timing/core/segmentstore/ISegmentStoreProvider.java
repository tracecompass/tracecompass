/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.IContentSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Segment store provider. Useful to populate views.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public interface ISegmentStoreProvider {

    /**
     * Add a listener for the viewers
     *
     * @param listener
     *                     listener for each type of viewer
     */
    void addListener(IAnalysisProgressListener listener);

    /**
     * Remove listener for the viewers
     *
     * @param listener
     *                     listener for each type of viewer
     */
    void removeListener(IAnalysisProgressListener listener);

    /**
     * Return the pre-defined set of segment aspects exposed by this analysis.
     *
     * It should not be null, but could be empty.
     *
     * @return The segment aspects for this analysis
     */
    Iterable<ISegmentAspect> getSegmentAspects();

    /**
     * Returns the result in a from the analysis in a ISegmentStore
     *
     * @return Results from the analysis in a ISegmentStore
     */
    @Nullable ISegmentStore<ISegment> getSegmentStore();

    /**
     * Get the map of segment data to filter on, using the provider's aspect and
     * the segment's own data
     *
     * @param provider
     *            The segment store provider
     * @param segment
     *            The segment to get the data from
     * @return A multimap of key, values that represent the data of this segment
     * @since 5.0
     */
    static Multimap<String, String> getFilterInput(ISegmentStoreProvider provider, ISegment segment) {
        Multimap<String, String> map = HashMultimap.create();
        for (ISegmentAspect aspect : provider.getSegmentAspects()) {
            Object resolve = aspect.resolve(segment);
            if (resolve != null) {
                map.put(aspect.getName(), String.valueOf(resolve));
            }
        }
        if (segment instanceof IContentSegment) {
            // Add the content of the segment as key, value to filter on
            Map<String, @NonNull ?> content = ((IContentSegment) segment).getContent();
            for (Entry<String, ?> val : content.entrySet()) {
                map.put(val.getKey(), String.valueOf(val.getValue()));
            }
        }
        return map;
    }

}