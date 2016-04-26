/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson, EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.table.ISortingLazyContentProvider;

/**
 * Content provider for the latency table viewers.
 *
 * @author France Lapointe Nguyen
 * @author Alexandre Montplaisir
 */
class LamiTableContentProvider implements ISortingLazyContentProvider {

    /**
     * Table viewer of the latency table viewer
     */
    private @Nullable TableViewer fTableViewer = null;

    private List<LamiTableEntry> fCurrentEntries;

    private @Nullable Comparator<LamiTableEntry> fComparator = null;

    /**
     * Flag to avoid recursive calls to {@link #updateElement} due to table
     * refreshes.
     */
    private volatile boolean fOngoingUpdate = false;

    /**
     * Constructor.
     */
    public LamiTableContentProvider() {
        fCurrentEntries = checkNotNull(Collections.EMPTY_LIST);
    }

    @Override
    public void updateElement(int index) {
        final TableViewer tableViewer = fTableViewer;
        final List<LamiTableEntry> entries = fCurrentEntries;
        if ((tableViewer != null) && (entries.size() > index) && !fOngoingUpdate) {
            fOngoingUpdate = true;
            tableViewer.replace(entries.get(index), index);
            fOngoingUpdate = false;
        }
    }

    @Override
    public void dispose() {
        fCurrentEntries = checkNotNull(Collections.EMPTY_LIST);
        fTableViewer = null;
        fComparator = null;
    }

    @Override
    public void inputChanged(@Nullable Viewer viewer, @Nullable Object oldInput, @Nullable Object newInput) {
        fTableViewer = (TableViewer) viewer;
        if (!(newInput instanceof List<?>)) {
            /*
             * Should be a List<BabeltraceTableEntry>, but may be null if it is
             * not yet set.
             */
            return;
        }
        @SuppressWarnings("unchecked")
        List<LamiTableEntry> entries = (List<LamiTableEntry>) newInput;

        /* Do a copy here so that we can sort it to our heart's content */
        fCurrentEntries = new ArrayList<>(entries);

        if (fComparator != null) {
            Collections.sort(fCurrentEntries, fComparator);
        }
    }

    @Override
    public void setSortOrder(@Nullable Comparator<?> comparator) {
        if (comparator == null) {
            return;
        }
        final TableViewer tableViewer = fTableViewer;
        if (tableViewer == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        Comparator<LamiTableEntry> entryComparator = (Comparator<LamiTableEntry>) comparator;
        fComparator = entryComparator;
        Collections.sort(fCurrentEntries, fComparator);
        tableViewer.refresh();
    }

    /**
     * Get the segment count
     *
     * @return the segment count
     */
    public int getNbEntries() {
        return fCurrentEntries.size();
    }

    /**
     * Get the index of a table entry.
     *
     * @param entry
     *            Entry to look for
     * @return the index of the table entry
     */
    public int getIndexOf(LamiTableEntry entry) {
        return fCurrentEntries.indexOf(entry);
    }
}
