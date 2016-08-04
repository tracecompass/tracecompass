/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.flamegraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.AggregatedCalledFunction;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphContentProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.Lists;

/**
 * Content provider for the flame graph view
 *
 * @author Sonia Farrah
 *
 */
public class FlameGraphContentProvider implements ITimeGraphContentProvider {

    private List<FlamegraphDepthEntry> fFlameGraphEntries = new ArrayList<>();
    private long fThreadDuration;
    private ITmfTrace fActiveTrace;

    /**
     * Parse the aggregated tree created by the callGraphAnalysis and creates
     * the event list (functions) for each entry (depth)
     *
     * @param firstNode
     *            The first node of the aggregation tree
     * @param childrenEntries
     *            The list of entries for one thread
     * @param timestampStack
     *            A stack used to save the functions timeStamps
     */
    private void setData(AggregatedCalledFunction firstNode, List<FlamegraphDepthEntry> childrenEntries, Deque<Long> timestampStack) {
        for (int i = 0; i < firstNode.getMaxDepth(); i++) {
            if (i >= childrenEntries.size()) {
                FlamegraphDepthEntry entry = new FlamegraphDepthEntry(String.valueOf(i), 0, fActiveTrace.getEndTime().toNanos() - fActiveTrace.getStartTime().toNanos(), i);
                childrenEntries.add(entry);
            }
        }
        FlamegraphDepthEntry firstEntry = NonNullUtils.checkNotNull(childrenEntries.get(0));
        firstEntry.addEvent(new FlamegraphEvent(firstEntry, timestampStack.peek(), firstNode));
        // Build the event list for next entries (next depth)
        addEvent(firstNode, childrenEntries, timestampStack);
    }

    /**
     * Build the events list for an entry (depth), then creates recursively the
     * events for the next entries. This parses the aggregation tree starting
     * from the bottom. This uses a stack to save the timestamp for each
     * function. Once we save a function's timestamp we'll use it to create the
     * callees events.
     *
     * @param node
     *            The node of the aggregation tree
     * @param childrenEntries
     *            The list of entries for one thread
     * @param timestampStack
     *            A stack used to save the functions timeStamps
     */
    private void addEvent(AggregatedCalledFunction node, List<FlamegraphDepthEntry> childrenEntries, Deque<Long> timestampStack) {
        if (node.hasChildren()) {
            node.getChildren().stream()
                    .sorted(Comparator.comparingLong(AggregatedCalledFunction::getDuration))
                    .forEach(child -> {
                        addEvent(child, childrenEntries, timestampStack);
                    });
            node.getChildren().stream().forEach(child -> {
                timestampStack.pop();
            });
        }
        FlamegraphDepthEntry entry = NonNullUtils.checkNotNull(childrenEntries.get(node.getDepth()));
        // Create the event corresponding to the function using the caller's
        // timestamp
        entry.addEvent(new FlamegraphEvent(entry, timestampStack.peek(), node));
        timestampStack.push(timestampStack.peek() + node.getDuration());
    }

    @Override
    public boolean hasChildren(Object element) {
        return !fFlameGraphEntries.isEmpty();
    }

    @Override
    public ITimeGraphEntry[] getElements(Object inputElement) {
        fFlameGraphEntries.clear();
        // Get the root of each thread
        fActiveTrace = TmfTraceManager.getInstance().getActiveTrace();
        if (fActiveTrace == null) {
            return new ITimeGraphEntry[0];
        }

        if (inputElement instanceof List<?>) {
            List<?> threadNodes = (List<?>) inputElement;
            for (Object object : threadNodes) {
                if (object instanceof AggregatedCalledFunction) {
                    buildChildrenEntries((AggregatedCalledFunction) object);
                }
            }
        }
        // Reverse the order of threads
        fFlameGraphEntries = Lists.reverse(fFlameGraphEntries);
        return fFlameGraphEntries.toArray(new ITimeGraphEntry[fFlameGraphEntries.size()]);
    }

    /**
     * Build the entry list for one thread
     *
     * @param threadNode
     *            The node of the aggregation tree
     */
    private void buildChildrenEntries(AggregatedCalledFunction threadNode) {
        FlamegraphDepthEntry threadEntry = new FlamegraphDepthEntry("", 0, fActiveTrace.getEndTime().toNanos() - fActiveTrace.getStartTime().toNanos(), fFlameGraphEntries.size()); //$NON-NLS-1$
        List<FlamegraphDepthEntry> childrenEntries = new ArrayList<>();
        fThreadDuration = 0L;
        // Sort children by duration
        threadNode.getChildren().stream()
                .sorted(Comparator.comparingLong(AggregatedCalledFunction::getDuration))
                .forEach(rootFunction -> {
                    Deque<Long> timestampStack = new ArrayDeque<>();
                    timestampStack.push(fThreadDuration);
                    setData(rootFunction, childrenEntries, timestampStack);
                    fThreadDuration += rootFunction.getDuration();
                    timestampStack.pop();
                });
        childrenEntries.forEach(child -> {
            if (child != null) {
                threadEntry.addChild(child);
            }
        });
        threadEntry.setName(threadNode.getSymbol().toString());
        fFlameGraphEntries.add(threadEntry);
    }

    @Override
    public ITimeGraphEntry[] getChildren(Object parentElement) {
        return fFlameGraphEntries.toArray(new TimeGraphEntry[fFlameGraphEntries.size()]);
    }

    @Override
    public ITimeGraphEntry getParent(Object element) {
        // Do nothing
        return null;
    }

    @Override
    public void dispose() {
        // Do nothing
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Do nothing
    }

}
