/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.util.Collections;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;

/**
 * <b><u>TmfTraceIndexer</u></b>
 * <p>
 * A simple trace indexer that builds an array of trace checkpoints. Checkpoints
 * are stored at fixed intervals (event rank) in ascending timestamp order.
 * <p>
 * The goal being to access a random trace event reasonably fast from the user's
 * standpoint, picking the right interval value becomes a trade-off between speed
 * and memory usage (a shorter inter-event interval is faster but requires more
 * checkpoints).
 * <p>
 * Locating a specific checkpoint is trivial for both rank (rank % interval) and
 * timestamp (bsearch in the array).
 */
public class TmfTraceIndexer<T extends ITmfTrace<ITmfEvent>> implements ITmfTraceIndexer<T> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The event trace to index
     */
    private final ITmfTrace<ITmfEvent> fTrace;

    /**
     * The interval between checkpoints
     */
    protected final int fCheckpointInterval;

    /**
     * The trace index. It is composed of checkpoints taken at intervals of
     * fCheckpointInterval events.
     */
    protected final Vector<TmfCheckpoint> fTraceIndex;

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Basic constructor that uses the default trace block size as checkpoints
     * intervals
     * 
     * @param trace the trace to index
     */
    public TmfTraceIndexer(final ITmfTrace<ITmfEvent> trace) {
        this(trace, TmfTrace.DEFAULT_BLOCK_SIZE);
    }

    /**
     * Full trace indexer
     * 
     * @param trace the trace to index
     * @param interval the checkpoints interval
     */
    public TmfTraceIndexer(final ITmfTrace<ITmfEvent> trace, final int interval) {
        fTrace = trace;
        fCheckpointInterval = interval;
        fTraceIndex = new Vector<TmfCheckpoint>();
    }

    // ------------------------------------------------------------------------
    // ITmfTraceIndexer
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#indexTrace(boolean)
     * 
     * The index is a list of contexts that point to events at regular interval
     * (rank-wise) in the trace. After it is built, the index can be used to
     * quickly access any event by rank or timestamp (using seekIndex()).
     * 
     * The index is built simply by reading the trace
     */
    @Override
    public void buildIndex(final boolean waitForCompletion) {

        // The monitoring job
        final Job job = new Job("Indexing " + fTrace.getName() + "...") { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                while (!monitor.isCanceled()) {
                    try {
                        Thread.sleep(100);
                    } catch (final InterruptedException e) {
                        return Status.OK_STATUS;
                    }
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();

        // Clear the checkpoints
        fTraceIndex.clear();

        // Build a background request for all the trace data. The index is
        // updated as we go by getNextEvent().
        final ITmfEventRequest<ITmfEvent> request = new TmfEventRequest<ITmfEvent>(ITmfEvent.class, TmfTimeRange.ETERNITY,
                TmfDataRequest.ALL_DATA, fCheckpointInterval, ITmfDataRequest.ExecutionType.BACKGROUND)
                {
            ITmfTimestamp startTime = null;
            ITmfTimestamp lastTime = null;

            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                if (event != null) {
                    final ITmfTimestamp timestamp = event.getTimestamp();
                    if (startTime == null) {
                        startTime = timestamp.clone();
                    }
                    lastTime = timestamp.clone();

                    // Update the trace status at regular intervals
                    if ((getNbRead() % fCheckpointInterval) == 0) {
                        updateTraceStatus();
                    }
                }
            }

            @Override
            public void handleSuccess() {
                updateTraceStatus();
            }

            @Override
            public void handleCompleted() {
                job.cancel();
                super.handleCompleted();
            }

            private void updateTraceStatus() {
                if (getNbRead() != 0) {
                    notifyListeners(startTime, lastTime);
                }
            }
                };

                // Submit the request and wait for completion if required
                fTrace.sendRequest(request);
                if (waitForCompletion) {
                    try {
                        request.waitForCompletion();
                    } catch (final InterruptedException e) {
                    }
                }
    }


    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTraceIndexer#seekIndex(org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp)
     */
    @Override
    public ITmfContext seekIndex(final ITmfTimestamp timestamp) {
        // Adjust the timestamp if needed
        ITmfTimestamp ts = timestamp;
        if (ts == null) {
            ts = TmfTimestamp.BIG_BANG;
        }

        // First, find the right checkpoint
        int index = Collections.binarySearch(fTraceIndex, new TmfCheckpoint(ts, null));

        // In the very likely case that the checkpoint was not found, bsearch
        // returns its negated would-be location (not an offset...). From that
        // index, we can then position the stream and get figure out the context.
        if (index < 0) {
            index = Math.max(0, -(index + 2));
        }

        // Position the trace at the checkpoint
        ITmfLocation<?> location;
        synchronized (fTraceIndex) {
            if (!fTraceIndex.isEmpty()) {
                if (index >= fTraceIndex.size()) {
                    index = fTraceIndex.size() - 1;
                }
                location = fTraceIndex.elementAt(index).getLocation();
            } else {
                location = null;
            }
        }
        final ITmfContext context = fTrace.seekLocation(location);
        context.setRank(index * fCheckpointInterval);

        return context;
    }

    @Override
    public ITmfContext seekIndex(final long rank) {

        // Position the stream at the previous checkpoint
        int index = (int) rank / fCheckpointInterval;
        ITmfLocation<?> location;
        synchronized (fTraceIndex) {
            if (fTraceIndex.isEmpty()) {
                location = null;
            } else {
                if (index >= fTraceIndex.size()) {
                    index = fTraceIndex.size() - 1;
                }
                location = fTraceIndex.elementAt(index).getLocation();
            }
        }

        final ITmfContext context = fTrace.seekLocation(location);
        final long pos = index * fCheckpointInterval;
        context.setRank(pos);

        return context;
    }

    private void notifyListeners(final ITmfTimestamp startTime, final ITmfTimestamp endTime) {
        fTrace.broadcast(new TmfTraceUpdatedSignal(fTrace, fTrace, new TmfTimeRange(startTime, endTime)));
    }

    @Override
    public void updateIndex(final ITmfContext context, final long rank, final ITmfTimestamp timestamp) {
        if ((rank % fCheckpointInterval) == 0) {
            // Determine the table position
            final long position = rank / fCheckpointInterval;
            // Add new entry at proper location (if empty)
            if (fTraceIndex.size() == position) {
                final ITmfLocation<?> location = context.getLocation().clone();
                fTraceIndex.add(new TmfCheckpoint(timestamp.clone(), location));
                // System.out.println(getName() + "[" + (fCheckpoints.size() - 1) + "] " + timestamp + ", " + location.toString());
            }
        }
    }

}
