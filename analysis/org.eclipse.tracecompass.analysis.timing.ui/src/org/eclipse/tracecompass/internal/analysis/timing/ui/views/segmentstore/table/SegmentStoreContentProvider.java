/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *   Bernd Hufmann - MOve abstract class to TMF
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.table;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNullContents;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.ui.viewers.table.ISortingLazyContentProvider;

import com.google.common.collect.Iterables;

/**
 * Content provider for the latency table viewers.
 *
 * @author France Lapointe Nguyen
 */
public class SegmentStoreContentProvider implements ISortingLazyContentProvider {

    /**
     * Array of all the segments in the segment store of the current trace
     */
    private ISegment @Nullable [] fSegmentArray = null;

    /**
     * Table viewer of the latency table viewer
     */
    private @Nullable TableViewer fTableViewer = null;

    /**
     * Segment comparator
     */
    private @Nullable Comparator<ISegment> fComparator = null;

    @Override
    public void updateElement(int index) {
        final TableViewer tableViewer = fTableViewer;
        final ISegment @Nullable [] segmentArray = fSegmentArray;
        if (tableViewer != null && segmentArray != null) {
            tableViewer.replace(segmentArray[index], index);
        }
    }

    @Override
    public void dispose() {
        fSegmentArray = null;
        fTableViewer = null;
        fComparator = null;
    }

    @Override
    public void inputChanged(@Nullable Viewer viewer, @Nullable Object oldInput, @Nullable Object newInput) {
        fTableViewer = (TableViewer) viewer;
        if (newInput instanceof Collection<?> || newInput instanceof ISegmentStore) {
            @SuppressWarnings("unchecked")
            Collection<ISegment> segmentStore = (Collection<@NonNull ISegment>) newInput;
            ISegment[] array = Iterables.toArray(segmentStore, ISegment.class);
            @NonNull ISegment[] checkedArray = checkNotNullContents(array);
            if (fComparator != null) {
                Arrays.sort(checkedArray, fComparator);
            }
            fSegmentArray = checkedArray;
        } else if (newInput instanceof ISegment[]) {
            /*
             * Ensure that there are no null elements in the array, so we can
             * set it back to fSegmentArray, which does not allow nulls.
             */
            @NonNull ISegment[] checkedArray = checkNotNullContents((@Nullable ISegment[]) newInput);
            if (fComparator != null) {
                Arrays.sort(checkedArray, fComparator);
            }
            fSegmentArray = checkedArray;
        } else {
            fSegmentArray = null;
        }
    }

    @Override
    public void setSortOrder(@Nullable Comparator<?> comparator) {
        @NonNull ISegment @Nullable [] segmentArray = fSegmentArray;
        if (comparator == null) {
            return;
        }
        if (segmentArray == null) {
            return;
        }
        final TableViewer tableViewer = fTableViewer;
        if (tableViewer == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        Comparator<ISegment> comp = (Comparator<ISegment>) comparator;
        fComparator = comp;
        Arrays.sort(segmentArray, fComparator);
        tableViewer.refresh();
    }

    /**
     * Get the segment count
     *
     * @return the segment count
     */
    public int getSegmentCount() {
        ISegment[] segmentArray = fSegmentArray;
        return (segmentArray == null ? 0 : segmentArray.length);
    }
}
