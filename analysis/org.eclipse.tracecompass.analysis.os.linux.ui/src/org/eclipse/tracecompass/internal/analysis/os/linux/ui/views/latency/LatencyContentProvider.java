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
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import java.util.Arrays;
import java.util.Comparator;

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
public class LatencyContentProvider implements ISortingLazyContentProvider {

    /**
     * Array of all the segments in the segment store of the current trace
     */
    private @Nullable ISegment[] fSegmentArray = null;

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
        final ISegment[] segmentArray = fSegmentArray;
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
        if (newInput instanceof ISegmentStore<?>) {
            ISegmentStore<?> segmentStore = (ISegmentStore<?>) newInput;
            fSegmentArray = Iterables.toArray(segmentStore, ISegment.class);
            if (fComparator != null) {
                Arrays.sort(fSegmentArray, fComparator);
            }
        } else {
            fSegmentArray = null;
        }
    }

    @Override
    public void setSortOrder(@Nullable Comparator<?> comparator) {
        if (comparator == null) {
            return;
        }
        if (fSegmentArray == null) {
            return;
        }
        final TableViewer tableViewer = fTableViewer;
        if (tableViewer == null) {
            return;
        }
        fComparator = (Comparator<ISegment>) comparator;
        Arrays.sort(fSegmentArray, fComparator);
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
