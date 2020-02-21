/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.segmentstore.core.segmentHistoryTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;

import com.google.common.collect.Iterables;

/**
 * Segment store that saves segments in a history tree.
 *
 * This base class is where most if not all the functionality of the segment
 * store should be implemented. The implementation to use is
 * {@link HistoryTreeSegmentStore} which is just a concrete class that resolves
 * the node type for the segment store. This class can be extended in the unit
 * tests with stub history trees and nodes to test its specific functionalities.
 *
 * @author Loic Prieur-Drevon
 * @author Geneviève Bastien
 * @param <E>
 *            type of {@link ISegment}
 */
public class HistoryTreeSegmentStore<E extends ISegment> implements ISegmentStore<E> {

    // TODO: these values were taken from the state system implementation. Maybe
    // they are not adequate for segments stores. Do some benchmarks
    private static final int MAX_CHILDREN = 50;
    private static final int BLOCK_SIZE = 64 * 1024;
    /**
     * The history tree that sits underneath.
     */
    private final SegmentHistoryTree<E> fSht;

    /** Indicates if the history tree construction is done */
    private volatile boolean fFinishedBuilding = false;

    /**
     * Constructor for new history files. Use this when creating a new history
     * from scratch.
     *
     * @param newStateFile
     *            The filename/location where to store the state history (Should
     *            end in .ht)
     * @param factory
     *            Factory to read history tree objects from the backend
     * @param version
     *            The version number of the reader/writer
     * @throws IOException
     *             Thrown if we can't create the file for some reason
     */
    public HistoryTreeSegmentStore(Path newStateFile,
            IHTIntervalReader<E> factory, int version) throws IOException {
        fSht = createHistoryTree(newStateFile, factory, version);
    }


    /**
     * Create a history tree from an existing file
     *
     * @param treeFile
     *            Filename/location of the history we want to load
     * @param intervalReader
     *            Factory to read history tree objects from the backend
     * @param version
     *            The version number of the reader/writer
     * @return The new history tree
     * @throws IOException
     *             If we can't read the file, if it doesn't exist, is not
     *             recognized, or if the version of the file does not match the
     *             expected providerVersion.
     */
    private SegmentHistoryTree<E> createHistoryTree(Path treeFile, IHTIntervalReader<@NonNull E> intervalReader, int version) throws IOException {
        try {
            if (Files.exists(treeFile)) {
                SegmentHistoryTree<E> sht = new SegmentHistoryTree<>(NonNullUtils.checkNotNull(treeFile.toFile()), version, intervalReader);
                fFinishedBuilding = true;
                return sht;
            }
        } catch (IOException e) {
            /**
             * Couldn't create the history tree with this file, just fall back
             * to a new history tree
             */
        }

        return new SegmentHistoryTree<>(NonNullUtils.checkNotNull(treeFile.toFile()),
                BLOCK_SIZE,
                MAX_CHILDREN,
                version,
                0,
                intervalReader);
    }

    /**
     * Get the History Tree built by this backend.
     *
     * @return The history tree
     */
    public SegmentHistoryTree<E> getSHT() {
        return fSht;
    }

    /**
     * Get the start time of the history tree
     *
     * @return the start time of the SHT
     */
    public long getStartTime() {
        return getSHT().getTreeStart();
    }

    /**
     * Get the end time of the history tree
     *
     * @return the end time of the SHT
     */
    public long getEndTime() {
        return getSHT().getTreeEnd();
    }

    /**
     * Tell the SHT that all segments have been inserted and to write latest
     * branch to disk.
     *
     * @param endTime
     *            the time at which to close latest branch and tree
     */
    public void finishedBuilding(long endTime) {
        getSHT().closeTree(endTime);
        fFinishedBuilding = true;
    }

    @Override
    public void close(boolean deleteFiles) {
        if (deleteFiles) {
            removeFiles();
        } else {
            finishedBuilding(getEndTime());
        }
    }

    /**
     * delete the SHT files from disk
     */
    public void removeFiles() {
        getSHT().deleteFile();
    }

    @Override
    public void dispose() {
        if (fFinishedBuilding) {
            getSHT().closeFile();
        } else {
            /*
             * The build is being interrupted, delete the file we partially
             * built since it won't be complete, so shouldn't be re-used in the
             * future (.deleteFile() will close the file first)
             */
            getSHT().deleteFile();
        }
    }

    /**
     * Return the size of the history tree file
     *
     * @return The current size of the history file in bytes
     */
    public long getFileSize() {
        return getSHT().getFileSize();
    }

    @Override
    public boolean add(E interval) {
        getSHT().insert(interval);
        return true;
    }

    @Override
    public boolean addAll(@Nullable Collection<? extends E> c) {
        if (c == null) {
            return false;
        }
        c.forEach(interval -> add(interval));
        return true;
    }

    @Override
    public int size() {
        return getSHT().size();
    }

    @Override
    public boolean isEmpty() {
        return getSHT().isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        // narrow down search when object is a segment
        if (o instanceof ISegment) {
            ISegment seg = (ISegment) o;
            if (seg.getStart() < getStartTime() || seg.getEnd() > getEndTime()) {
                /* This segment cannot be in this SegmentStore */
                return false;
            }
            Iterable<@NonNull E> iterable = getIntersectingElements(seg.getStart());
            return Iterables.contains(iterable, seg);
        }
        return false;
    }

    @Override
    public boolean containsAll(@Nullable Collection<?> c) {
        if (c == null) {
            return false;
        }
        /*
         * Check that all elements in the collection are indeed ISegments, and
         * find their min end and max start time
         */
        long minEnd = Long.MAX_VALUE, maxStart = Long.MIN_VALUE;
        for (Object o : c) {
            if (o instanceof ISegment) {
                ISegment seg = (ISegment) o;
                if (seg.getStart() < getStartTime() || seg.getEnd() > getEndTime()) {
                    /* This segment cannot be in this SegmentStore */
                    return false;
                }
                minEnd = Math.min(minEnd, seg.getEnd());
                maxStart = Math.max(maxStart, seg.getStart());
            } else {
                return false;
            }
        }
        if (minEnd > maxStart) {
            /*
             * all segments intersect a common range, we just need to intersect
             * a time stamp in that range
             */
            minEnd = maxStart;
        }

        /* Iterate through possible segments until we have found them all */
        Iterator<@NonNull E> iterator = getIntersectingElements(minEnd, maxStart).iterator();
        int unFound = c.size();
        while (iterator.hasNext() && unFound > 0) {
            E seg = iterator.next();
            for (Object o : c) {
                if (Objects.equals(o, seg)) {
                    unFound--;
                }
            }
        }
        return unFound == 0;
    }

    @Override
    public @Nullable Iterator<E> iterator() {
        return getSHT().iterator();
    }

    @Override
    public Object @NonNull [] toArray() {
        throw new UnsupportedOperationException("This segment store can potentially cause OutOfMemoryExceptions"); //$NON-NLS-1$
    }

    @Override
    public <T> T @NonNull [] toArray(T @NonNull [] a) {
        throw new UnsupportedOperationException("This segment store can potentially cause OutOfMemoryExceptions"); //$NON-NLS-1$
    }

    @Override
    public void clear() {
        try {
            getSHT().cleanFile();
        } catch (IOException e) {
            throw new IllegalStateException("HT segment store: couldn't clear the HT file: " + e.getMessage()); //$NON-NLS-1$
        }
    }

    @Override
    public @NonNull Iterable<E> getIntersectingElements(long start, long end) {
        return getSHT().getIntersectingElements(start, end);
    }

    @Override
    public Iterable<E> getIntersectingElements(long start, long end, @Nullable Comparator<ISegment> order) {
        if (order == null) {
            return getSHT().getIntersectingElements(start, end);
        }
        return getSHT().getIntersectingElements(start, end, (Comparator<E>) order);

    }
}
