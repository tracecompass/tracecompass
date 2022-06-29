/*******************************************************************************
 * Copyright (c) 2012, 2016, 2022 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add message to exceptions
 *   Marco Miller - Extract to this class
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

class HistoryTreeBackendIterator implements Iterator<@NonNull ITmfStateInterval> {
    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(HistoryTreeBackendIterator.class);

    private final @NonNull IHistoryTree fSht;
    private final IntegerRangeCondition fQuarks;
    private final TimeRangeCondition fTimes;
    private final boolean fReverse;
    private final @NonNull FlowScopeLog fParentLog;
    private final Deque<Integer> fSeqNumberQueue;

    private Iterator<@NonNull HTInterval> intervalQueue = Collections.emptyIterator();

    HistoryTreeBackendIterator(@NonNull IHistoryTree sht, IntegerRangeCondition quarks, TimeRangeCondition times, boolean reverse, @NonNull FlowScopeLog parentLog) {
        fSht = sht;
        fQuarks = quarks;
        fTimes = times;
        fReverse = reverse;
        fParentLog = parentLog;
        fSeqNumberQueue = new ArrayDeque<>(Collections.singleton(fSht.getRootNode().getSequenceNumber()));
    }

    @Override
    public boolean hasNext() {
        while (!intervalQueue.hasNext() && !fSeqNumberQueue.isEmpty()) {
            try {
                HTNode currentNode = fSht.readNode(fSeqNumberQueue);
                /*
                 * Compute reduced conditions here to reduce complexity in
                 * queuing operations.
                 */
                TimeRangeCondition subTimes = fTimes.subCondition(currentNode.getNodeStart(), currentNode.getNodeEnd());
                /*
                 * During the SHT construction, the bounds of the children are
                 * not final, so we may have queued some nodes which don't
                 * overlap the query.
                 */
                if (fQuarks.intersects(currentNode.getMinQuark(), currentNode.getMaxQuark()) && subTimes != null) {
                    if (currentNode.getNodeType() == HTNode.NodeType.CORE) {
                        // Queue the relevant children nodes for BFS.
                        ((ParentNode) currentNode).queueNextChildren2D(fQuarks, subTimes, fSeqNumberQueue, fReverse);
                    }
                    intervalQueue = currentNode.iterable2D(fQuarks, subTimes).iterator();
                }
            } catch (ClosedChannelException e) {
                try (FlowScopeLog closedChannelLog = new FlowScopeLogBuilder(LOGGER, Level.FINER,
                        "HistoryTreeBackendIterator:query2D:channelClosed").setParentScope(fParentLog).build()) { //$NON-NLS-1$
                    return false;
                }
            }
        }
        boolean hasNext = intervalQueue.hasNext();
        if (!hasNext) {
            try (FlowScopeLog noNext = new FlowScopeLogBuilder(LOGGER, Level.FINER,
                    "HistoryTreeBackendIterator:query2D:iteratorEnd").setParentScope(fParentLog).build()) { //$NON-NLS-1$
            }
        }
        return intervalQueue.hasNext();
    }

    @Override
    public ITmfStateInterval next() {
        return intervalQueue.next();
    }
}
