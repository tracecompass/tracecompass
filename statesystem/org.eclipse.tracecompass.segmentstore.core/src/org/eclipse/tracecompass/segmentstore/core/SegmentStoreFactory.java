/*******************************************************************************
 * Copyright (c) 2016 Polytechnique
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Loïc Prieur-Drevon - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.segmentstore.core.arraylist.ArrayListStore;
import org.eclipse.tracecompass.internal.segmentstore.core.arraylist.LazyArrayListStore;
import org.eclipse.tracecompass.internal.segmentstore.core.segmentHistoryTree.HistoryTreeSegmentStore;
import org.eclipse.tracecompass.internal.segmentstore.core.treemap.TreeMapStore;

/**
 * Factory to create Segment Stores.
 *
 * Since segment stores are meant to be accessed using the {@link ISegmentStore}
 * interface, you can use this factory to instantiate new ones.
 *
 * @author Loïc Prieur-Drevon
 * @param <E>
 *            The type of segment held in this store
 * @since 1.1
 */
public final class SegmentStoreFactory<E> {
    /**
     * Flags to determine the type of SegmentStore to use.
     */
    public enum SegmentStoreType {
        /**
         * Segment Store should be as fast as possible to build and read.
         * Performance of individual operations may be slower, but overall the
         * speed should be faster.
         */
        Fast,
        /**
         * Segment Store based should have predictable performance. This does
         * not mean it's faster, it should not give a surprise random slow down
         * though. If it slows down, it will be on the {@link ISegment} that
         * slows it down.
         */
        Stable,
        /**
         * Segment Store should contain no duplicate segments
         */
        Distinct,
        /**
         * Segment store that doesn't have to reside entirely in memory, ideal
         * for very large stores, the performance are not as high as the other
         * segment stores. These kind of stores should be created using the
         * {@link SegmentStoreFactory#createOnDiskSegmentStore(Path, IHTIntervalReader)}
         * factory method
         *
         * @since 2.0
         */
        OnDisk
    }

    private SegmentStoreFactory() {
        // Do nothing
    }

    /**
     * New SegmentStore factory method
     *
     * @param segmentTypes
     *            Flags used to determine the type of Segment Store that will be
     *            created
     *
     * @return a new {@link ISegmentStore}
     */
    public static <E extends ISegment> ISegmentStore<E> createSegmentStore(@Nullable SegmentStoreType... segmentTypes) {
        Set<@NonNull SegmentStoreType> segments = getListOfFlags(segmentTypes);
        if (segments.contains(SegmentStoreType.Distinct)) {
            return createTreeMapStore();
        }
        if (segments.contains(SegmentStoreType.Stable)) {
            return createArrayListStore();
        }
        // default option is the fastest
        return createLazyArrayListStore();

    }

    /**
     * New SegmentStore factory method to create store from an array of Objects
     *
     * @param segmentTypes
     *            Flags used to determine the type of Segment Store that will be
     *            created
     * @param array
     *            the {@link Object} array we want the returned segment store to
     *            contain, {@link Object} are only inserted if they extend
     *            {@link ISegment}
     * @return an {@link ISegmentStore} containing the {@link ISegment}s from
     *         array.
     */
    public static <E extends ISegment> ISegmentStore<E> createSegmentStore(Object[] array, SegmentStoreType... segmentTypes) {
        Set<@NonNull SegmentStoreType> segments = getListOfFlags(segmentTypes);
        if (segments.contains(SegmentStoreType.Distinct)) {
            ISegmentStore<E> store = createTreeMapStore();
            for (Object elem : array) {
                if (elem instanceof ISegment) {
                    store.add((E) elem); // warning from type, it should be fine
                }
            }
            return store;
        }
        if (segments.contains(SegmentStoreType.Stable)) {
            return new ArrayListStore<>(array);
        }
        // default option is the fastest
        return new LazyArrayListStore<>(array);
    }

    /**
     * SegmentStore factory method that creates a segment store on disk
     *
     * @param segmentFile
     *            The file where to store the segments
     * @param segmentReader
     *            The factory to read the segments from a safe byte buffer
     *
     * @return an {@link ISegmentStore}
     * @throws IOException
     *             Exceptions when creating the segment store
     * @since 2.0
     * @deprecated Use the {@link #createOnDiskSegmentStore(Path, IHTIntervalReader, int)} instead
     */
    @Deprecated
    public static <E extends ISegment> ISegmentStore<E> createOnDiskSegmentStore(Path segmentFile, IHTIntervalReader<E> segmentReader) throws IOException {
        return createOnDiskSegmentStore(segmentFile, segmentReader, 1);
    }

    /**
     * SegmentStore factory method that creates a segment store on disk
     *
     * @param segmentFile
     *            The file where to store the segments
     * @param segmentReader
     *            The factory to read the segments from a safe byte buffer
     * @param version
     *            The version number of the segment reader/writer
     *
     * @return an {@link ISegmentStore}
     * @throws IOException
     *             Exceptions when creating the segment store
     * @since 2.1
     */
    public static <E extends ISegment> ISegmentStore<E> createOnDiskSegmentStore(Path segmentFile, IHTIntervalReader<E> segmentReader, int version) throws IOException {
        return new HistoryTreeSegmentStore<>(segmentFile, segmentReader, version);
    }

    private static Set<@NonNull SegmentStoreType> getListOfFlags(SegmentStoreType... segmentTypes) {
        Set<@NonNull SegmentStoreType> segments = new HashSet<>();
        for(@Nullable SegmentStoreType segmentType : segmentTypes ) {
            if(segmentType != null) {
                segments.add(segmentType);
            }
        }
        return segments;
    }

    /**
     * New {@link TreeMapStore} factory method
     *
     * @return the new Segment Store
     */
    private static <E extends ISegment> ISegmentStore<E> createTreeMapStore() {
        return new TreeMapStore<>();
    }

    /**
     * New {@link ArrayListStore} factory method
     *
     * @return the new Segment Store
     */
    private static <E extends ISegment> ISegmentStore<E> createArrayListStore() {
        return new ArrayListStore<>();
    }

    /**
     * New {@link LazyArrayListStore} factory method
     *
     * @return the new Segment Store
     */
    private static <E extends ISegment> ISegmentStore<E> createLazyArrayListStore() {
        return new LazyArrayListStore<>();
    }

}
