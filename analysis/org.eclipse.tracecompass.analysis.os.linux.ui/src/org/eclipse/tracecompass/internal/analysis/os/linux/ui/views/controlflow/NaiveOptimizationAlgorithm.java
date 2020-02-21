/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadEntryModel;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

/**
 * Optimization algorithm, this approach bubbles up the elements that have more
 * connections together. This effectively will give good results with a
 * complexity of O(n) where n is the number of transitions. It may solve
 * non-optimally for very tightly coupled cliques.
 *
 * @author Matthew Khouzam
 * @author Samuel Gagnon
 */
public final class NaiveOptimizationAlgorithm implements Function<Collection<ILinkEvent>, Map<Integer, Long>> {

    /**
     * Get the scheduling column order by arrows
     *
     * @param arrows
     *            the list of visible links
     * @return the list of weights, by thread ID
     */
    @Override
    public Map<Integer, Long> apply(Collection<ILinkEvent> arrows) {
        /*
         * "transitions" contains the count of every arrows between two tids
         * (Pair<Integer, Integer>). For constructing the Pair, we always put
         * the smallest tid first
         */
        Multiset<Pair<Integer, Integer>> transitions = HashMultiset.<Pair<Integer, Integer>> create();

        /*
         * We iterate in arrows to count the number of transitions between every
         * pair (tid,tid) in the current view
         */
        for (ILinkEvent arrow : arrows) {
            ITimeGraphEntry from = arrow.getEntry();
            ITimeGraphEntry to = arrow.getDestinationEntry();
            int fromTid = getTid(from);
            int toTid = getTid(to);
            if (fromTid >= 0 && toTid >= 0 && fromTid != toTid) {
                Pair<Integer, Integer> key = new Pair<>(Math.min(fromTid, toTid), Math.max(fromTid, toTid));
                transitions.add(key);
            }
        }

        /*
         * We now have a transition count for every pair (tid,tid). The next
         * step is to sort every pair according to its count in decreasing order
         */
        List<Pair<Integer, Integer>> sortedTransitionsByCount = Multisets.copyHighestCountFirst(transitions).asList();

        /*
         * Next, we find the order in which we want to display our threads. We
         * simply iterate in every pair (tid,tid) in orderedTidList. Each time
         * we see a new tid, we add it at the end of orderedTidList. This way,
         * threads with lots of transitions will be grouped in the top. While
         * very naive, this algorithm is fast, simple and gives decent results.
         */
        Map<Integer, Long> orderedTidMap = new LinkedHashMap<>();
        long pos = 0;
        for (Pair<Integer, Integer> threadPair : sortedTransitionsByCount) {
            if (orderedTidMap.get(threadPair.getFirst()) == null) {
                orderedTidMap.put(threadPair.getFirst(), pos);
                pos++;
            }
            if (orderedTidMap.get(threadPair.getSecond()) == null) {
                orderedTidMap.put(threadPair.getSecond(), pos);
                pos++;
            }
        }

        return orderedTidMap;
    }

    private static int getTid(ITimeGraphEntry entry) {
        ThreadEntryModel model = ControlFlowView.getThreadEntryModel(entry);
        return model == null ? -1 : model.getThreadId();
    }
}