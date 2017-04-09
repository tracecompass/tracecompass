/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.segmentstore.core.arraylist;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;

/**
 * Implementation of an {@link ISegmentStore} using one in-memory
 * {@link ArrayList}. This relatively simple implementation holds everything in
 * memory, and as such cannot contain too much data.
 *
 * The ArrayListStore itself is {@link Iterable}, and its iteration order will
 * be by ascending order of start times. For segments with identical start
 * times, the secondary comparator will be the end time. If even those are
 * equal, it will defer to the segments' natural ordering (
 * {@link ISegment#compareTo}).
 *
 * Removal operations are not supported.
 *
 * @param <E>
 *            The type of segment held in this store
 *
 * @author Matthew Khouzam
 */
public class ArrayListStore<@NonNull E extends ISegment> extends LazyArrayListStore<E> {

    /**
     * Constructor
     */
    public ArrayListStore() {
        /** Same constructor as {@link LazyArrayListStore} */
    }

    /**
     * Constructor
     *
     * @param array
     *            an array of elements to wrap in the segment store
     */
    public ArrayListStore(Object[] array) {
        super(array);
        sortStore();
    }

    @Override
    protected int getInsertionPoint(E value) {
        /*
         * For the ArrayListStore, insert new segments in the position that
         * maintains the list ordered along COMPARATOR.
         */
        int insertPoint = Collections.binarySearch(fStore, value, COMPARATOR);
        return insertPoint >= 0 ? insertPoint : -insertPoint - 1;
    }

    @Override
    protected void setDirtyIfNeeded(@NonNull E value) {
        /*
         * Do nothing, ArrayListStore is never dirty as we insert the segments
         * in sorted order.
         */
    }
}
