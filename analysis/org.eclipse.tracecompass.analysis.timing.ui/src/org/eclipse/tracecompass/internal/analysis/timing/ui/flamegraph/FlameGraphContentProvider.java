/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.flamegraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.AggregatedCalledFunction;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.ThreadNode;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphContentProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.Iterables;

/**
 * Content provider for the flame graph view
 *
 * @author Sonia Farrah
 *
 */
public class FlameGraphContentProvider implements ITimeGraphContentProvider {

    /**
     * remember the input to avoid recomputing for every refresh.
     */
    private Collection<?> fCurrentInput = null;
    private final List<FlamegraphDepthEntry> fFlameGraphEntries = new ArrayList<>();
    private SortOption fSortOption = SortOption.BY_NAME;
    private @NonNull Comparator<FlamegraphDepthEntry> fThreadComparator = Objects.requireNonNull(Comparator.comparing(FlamegraphDepthEntry::getName));

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof FlamegraphDepthEntry) {
            return ((FlamegraphDepthEntry) element).hasChildren();
        }
        return false;
    }

    @Override
    public ITimeGraphEntry[] getElements(Object inputElement) {
        if (!Objects.equals(fCurrentInput, inputElement)) {
            fFlameGraphEntries.clear();
            // Get the root of each thread
            if (inputElement instanceof Collection<?>) {
                Collection<?> threadNodes = (Collection<?>) inputElement;
                for (ThreadNode object : Iterables.filter(threadNodes, ThreadNode.class)) {
                    buildChildrenEntries(object);
                }
                fCurrentInput = threadNodes;
            } else {
                return new ITimeGraphEntry[0];
            }
        }

        // Sort the threads
        fFlameGraphEntries.sort(fThreadComparator);
        return fFlameGraphEntries.toArray(new ITimeGraphEntry[fFlameGraphEntries.size()]);
    }

    /**
     * Build the entry list for one thread
     *
     * @param threadNode
     *            The node of the aggregation tree
     */
    private void buildChildrenEntries(ThreadNode threadNode) {
        FlamegraphDepthEntry threadEntry = new FlamegraphDepthEntry(threadNode.getSymbol().toString(), 0, 0, fFlameGraphEntries.size(), threadNode.getId());
        for (AggregatedCalledFunction child : getSortedChildren(threadNode)) {
            addEntries(threadEntry, child);
        }
        fFlameGraphEntries.add(threadEntry);
    }

    /**
     * recursively add functions to the thread entry, incrementing its depth as
     * required, and adding each function to the list of events.
     *
     * @param threadEntry
     *            root thread entry for this call graph
     * @param function
     *            function to add to the call graph
     */
    private static void addEntries(FlamegraphDepthEntry threadEntry, AggregatedCalledFunction function) {
        // get the flame graph entry for the correct depth, create it if absent.
        int depth = function.getDepth();
        TimeGraphEntry depthEntry;
        if (threadEntry.getChildren().size() <= depth) {
            depthEntry = new FlamegraphDepthEntry(String.valueOf(depth), 0, 0, depth, depth);
            threadEntry.addChild(depthEntry);
        } else {
            depthEntry = threadEntry.getChildren().get(depth);
        }

        // also updates the depthEntry's end time.
        depthEntry.addEvent(new FlamegraphEvent(depthEntry, threadEntry.getEndTime(), function));

        for (AggregatedCalledFunction node : getSortedChildren(function)) {
            addEntries(threadEntry, node);
        }
        threadEntry.updateEndTime(depthEntry.getEndTime());
    }

    private static Iterable<@NonNull AggregatedCalledFunction> getSortedChildren(AggregatedCalledFunction function) {
        List<@NonNull AggregatedCalledFunction> children = new ArrayList<>(function.getChildren());
        children.sort(Comparator.comparingLong(AggregatedCalledFunction::getDuration));
        return children;
    }

    @Override
    public ITimeGraphEntry[] getChildren(Object parentElement) {
        if (parentElement instanceof FlamegraphDepthEntry) {
            List<@NonNull TimeGraphEntry> children = ((FlamegraphDepthEntry) parentElement).getChildren();
            return children.toArray(new TimeGraphEntry[children.size()]);
        }
        return new ITimeGraphEntry[0];
    }

    @Override
    public ITimeGraphEntry getParent(Object element) {
        if (element instanceof FlamegraphDepthEntry) {
            return ((FlamegraphDepthEntry) element).getParent();
        }
        return null;
    }

    @Override
    public void dispose() {
        fFlameGraphEntries.clear();
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Do nothing
    }

    /**
     * Get the sort option
     *
     * @return the sort option.
     */
    public SortOption getSortOption() {
        return fSortOption;
    }

    /**
     * Set the sort option for sorting the thread entries
     *
     * @param sortOption
     *            the sort option to set
     *
     */
    public void setSortOption(SortOption sortOption) {
        fSortOption = sortOption;
        switch (sortOption) {
        case BY_NAME:
            fThreadComparator = Objects.requireNonNull(Comparator.comparing(FlamegraphDepthEntry::getName));
            break;
        case BY_NAME_REV:
            fThreadComparator = Objects.requireNonNull(Comparator.comparing(FlamegraphDepthEntry::getName, Comparator.reverseOrder()));
            break;
        case BY_ID:
            fThreadComparator = Objects.requireNonNull(Comparator.comparingLong(FlamegraphDepthEntry::getId));
            break;
        case BY_ID_REV:
            fThreadComparator = Objects.requireNonNull(Comparator.comparing(FlamegraphDepthEntry::getId, Comparator.reverseOrder()));
            break;
        default:
            break;
        }
        fFlameGraphEntries.sort(fThreadComparator);
    }
}
