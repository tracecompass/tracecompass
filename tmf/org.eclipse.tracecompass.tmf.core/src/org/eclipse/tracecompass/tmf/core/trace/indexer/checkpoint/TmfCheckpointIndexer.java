/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Update way of broadcasting of TmfTraceUpdatedSignal
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.Messages;
import org.eclipse.tracecompass.internal.tmf.core.TmfCoreTracer;
import org.eclipse.tracecompass.internal.tmf.core.trace.indexer.TmfMemoryIndex;
import org.eclipse.tracecompass.tmf.core.component.TmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceKnownSize;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;

/**
 * A simple indexer that manages the trace index as an array of trace
 * checkpoints. Checkpoints are stored in memory at fixed intervals (event rank)
 * in ascending timestamp order.
 * <p>
 * The goal being to access a random trace event reasonably fast from the user's
 * standpoint, picking the right interval value becomes a trade-off between
 * speed and memory usage (a shorter inter-event interval is faster but requires
 * more checkpoints).
 * <p>
 * Locating a specific checkpoint is trivial for both rank (rank % interval) and
 * timestamp (bsearch in the array). *
 *
 * @see ITmfTrace
 * @see ITmfEvent
 *
 * @author Francois Chouinard
 */
public class TmfCheckpointIndexer implements ITmfTraceIndexer {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The event trace to index */
    protected final ITmfTrace fTrace;

    /** The interval between checkpoints */
    private final int fCheckpointInterval;

    /** The event trace to index */
    private boolean fIsIndexing;

    /**
     * The trace index. It is composed of checkpoints taken at intervals of
     * fCheckpointInterval events.
     */
    protected final ITmfCheckpointIndex fTraceIndex;

    /**
     * The indexing request
     */
    private ITmfEventRequest fIndexingRequest = null;

    /** Whether or not the index was built once */
    private boolean fBuiltOnce;

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Basic constructor that uses the default trace block size as checkpoints
     * intervals
     *
     * @param trace
     *            the trace to index
     */
    public TmfCheckpointIndexer(final ITmfTrace trace) {
        this(trace, TmfEventProvider.DEFAULT_BLOCK_SIZE);
    }

    /**
     * Full trace indexer
     *
     * @param trace
     *            the trace to index
     * @param interval
     *            the checkpoints interval
     */
    public TmfCheckpointIndexer(final ITmfTrace trace, final int interval) {
        fTrace = trace;
        fCheckpointInterval = interval;
        fTraceIndex = createIndex(trace);
        fIsIndexing = false;
    }

    /**
     * Creates the index instance. Classes extending this class can override
     * this to provide a different index implementation.
     *
     * @param trace
     *            the trace to index
     * @return the index
     */
    protected ITmfCheckpointIndex createIndex(final ITmfTrace trace) {
        return new TmfMemoryIndex(trace);
    }

    @Override
    public void dispose() {
        if ((fIndexingRequest != null) && !fIndexingRequest.isCompleted()) {
            fIndexingRequest.cancel();
        }

        fTraceIndex.dispose();
    }

    // ------------------------------------------------------------------------
    // ITmfTraceIndexer - isIndexing
    // ------------------------------------------------------------------------

    @Override
    public boolean isIndexing() {
        return fIsIndexing;
    }

    // ------------------------------------------------------------------------
    // ITmfTraceIndexer - buildIndex
    // ------------------------------------------------------------------------

    @Override
    public void buildIndex(final long offset, final TmfTimeRange range, final boolean waitForCompletion) {

        long indexingOffset = offset;
        TmfTimeRange indexingTimeRange = range;

        // Don't do anything if we are already indexing
        synchronized (fTraceIndex) {
            if (fIsIndexing) {
                return;
            }
            fIsIndexing = true;
        }

        // Restore previously built index values
        if (!fTraceIndex.isCreatedFromScratch() && !fBuiltOnce && fTraceIndex.getNbEvents() > 0) {
            indexingOffset = fTraceIndex.getNbEvents();
            indexingTimeRange = new TmfTimeRange(fTraceIndex.getTimeRange().getStartTime(), TmfTimestamp.BIG_CRUNCH);
            TmfCoreTracer.traceIndexer("restoring index. nbEvents: " + fTraceIndex.getNbEvents() + " time range: " + fTraceIndex.getTimeRange()); //$NON-NLS-1$ //$NON-NLS-2$
            // Set some trace attributes that depends on indexing
            TmfTraceUpdatedSignal signal = new TmfTraceUpdatedSignal(this, fTrace, new TmfTimeRange(fTraceIndex.getTimeRange().getStartTime(), fTraceIndex.getTimeRange().getEndTime()), indexingOffset);
            fTrace.broadcast(signal);
        }

        TmfCoreTracer.traceIndexer("buildIndex. offset: " + indexingOffset + " (requested " + offset + ")" + " time range: " + range); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        // The monitoring job
        TmfIndexingJob job = new TmfIndexingJob("Indexing " + fTrace.getName() + "..."); //$NON-NLS-1$ //$NON-NLS-2$
        job.setSystem(fBuiltOnce);
        fBuiltOnce = true;
        job.schedule();

        // Build a background request for all the trace data. The index is
        // updated as we go by readNextEvent().
        fIndexingRequest = new TmfEventRequest(ITmfEvent.class,
                indexingTimeRange, indexingOffset, ITmfEventRequest.ALL_DATA,
                ITmfEventRequest.ExecutionType.BACKGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                // Update the trace status at regular intervals
                if ((getNbRead() % fCheckpointInterval) == 0) {
                    updateTraceStatus();
                }
            }

            @Override
            public void handleSuccess() {
                updateTraceStatus();
            }

            @Override
            public void handleCompleted() {
                job.cancel();
                fTraceIndex.setTimeRange(fTrace.getTimeRange());
                fTraceIndex.setNbEvents(fTrace.getNbEvents());
                super.handleCompleted();
                fIsIndexing = false;
                TmfCoreTracer.traceIndexer("Build index request completed. nbEvents: " + fTraceIndex.getNbEvents() + " time range: " + fTraceIndex.getTimeRange()); //$NON-NLS-1$ //$NON-NLS-2$
            }

            @Override
            public void fail(Exception e) {
                super.fail(e);
                job.setException(e);
            }

            private void updateTraceStatus() {
                if (fTrace.getNbEvents() > 0) {
                    signalNewTimeRange(fTrace.getStartTime(), fTrace.getEndTime());
                }
            }
        };

        // Submit the request and wait for completion if required
        fTrace.sendRequest(fIndexingRequest);
        if (waitForCompletion) {
            try {
                fIndexingRequest.waitForCompletion();
            } catch (final InterruptedException e) {
            }
        }
    }

    /**
     * Notify the interested parties that the trace time range has changed
     *
     * @param startTime
     *            the new start time
     * @param endTime
     *            the new end time
     */
    private void signalNewTimeRange(final @NonNull ITmfTimestamp startTime, final @NonNull ITmfTimestamp endTime) {
        fTrace.broadcast(new TmfTraceUpdatedSignal(fTrace, fTrace, new TmfTimeRange(startTime, endTime), fTrace.getNbEvents()));
    }

    // ------------------------------------------------------------------------
    // ITmfTraceIndexer - updateIndex
    // ------------------------------------------------------------------------

    @Override
    public synchronized void updateIndex(final ITmfContext context, final ITmfTimestamp timestamp) {
        if ((context.getRank() % fCheckpointInterval) == 0) {
            // Determine the table position
            final long position = context.getRank() / fCheckpointInterval;
            // Add new entry at proper location (if empty)
            if (fTraceIndex.size() == position) {
                TmfCheckpoint checkpoint = new TmfCheckpoint(timestamp, context.getLocation(), position);
                TmfCoreTracer.traceIndexer("Inserting checkpoint: " + checkpoint); //$NON-NLS-1$
                fTraceIndex.insert(checkpoint);
            }
        }
    }

    // ------------------------------------------------------------------------
    // ITmfTraceIndexer - seekIndex
    // ------------------------------------------------------------------------

    @Override
    public synchronized ITmfContext seekIndex(final ITmfTimestamp timestamp) {

        // A null timestamp indicates to seek the first event
        if (timestamp == null) {
            return fTrace.seekEvent(0);
        }

        // Find the checkpoint at or before the requested timestamp.
        // In the very likely event that the timestamp is not at a checkpoint
        // boundary, bsearch will return index = (- (insertion point + 1)).
        // It is then trivial to compute the index of the previous checkpoint.
        long index = fTraceIndex.binarySearch(new TmfCheckpoint(timestamp, null, 0));
        if (index < 0) {
            index = Math.max(0, -(index + 2));
        } else {
            // If timestamp was in the list, use previous index to be able to
            // find the
            // first event with the same timestamp before the checkpoint
            index = Math.max(0, index - 1);
        }

        // Position the trace at the checkpoint
        return restoreCheckpoint(index);
    }

    @Override
    public ITmfContext seekIndex(final long rank) {

        // A rank < 0 indicates to seek the first event
        if (rank < 0) {
            return fTrace.seekEvent(0);
        }

        // Find the checkpoint at or before the requested rank.
        final int index = (int) rank / fCheckpointInterval;

        // Position the trace at the checkpoint
        return restoreCheckpoint(index);
    }

    /**
     * Position the trace at the given checkpoint
     *
     * @param checkpointIndex
     *            the checkpoint index
     * @return the corresponding context
     */
    private ITmfContext restoreCheckpoint(final long checkpointIndex) {
        ITmfLocation location = null;
        long index = 0;
        synchronized (fTraceIndex) {
            if (!fTraceIndex.isEmpty()) {
                index = checkpointIndex;
                if (index >= fTraceIndex.size()) {
                    index = fTraceIndex.size() - 1;
                }
                ITmfCheckpoint checkpoint = fTraceIndex.get(index);
                TmfCoreTracer.traceIndexer("Restored checkpoint: " + checkpoint); //$NON-NLS-1$
                if (checkpoint == null) {
                    return fTrace.seekEvent((ITmfLocation) null);
                }
                location = checkpoint.getLocation();
            }
        }
        final ITmfContext context = fTrace.seekEvent(location);
        context.setRank(index * fCheckpointInterval);
        return context;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the trace index
     */
    protected ITmfCheckpointIndex getTraceIndex() {
        return fTraceIndex;
    }

    private final class TmfIndexingJob extends Job {
        private Exception fException = null;
        private final ITmfTraceKnownSize fTraceWithSize;

        private TmfIndexingJob(String name) {
            super(name);
            fTraceWithSize = (fTrace instanceof ITmfTraceKnownSize) ? (ITmfTraceKnownSize) fTrace : null;
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            int alreadyDone = 0;
            SubMonitor subMonitor = SubMonitor.convert(monitor);
            if (fTraceWithSize != null) {
                subMonitor.beginTask("", fTraceWithSize.size()); //$NON-NLS-1$
            } else {
                subMonitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
            }
            while (!monitor.isCanceled()) {
                try {
                    long prevNbEvents = fTrace.getNbEvents();
                    Thread.sleep(250);
                    long nbEvents = fTrace.getNbEvents();
                    if (fTraceWithSize != null) {
                        final int done = fTraceWithSize.progress();
                        subMonitor.setWorkRemaining(fTraceWithSize.size() - done);
                        subMonitor.worked(done - alreadyDone);
                        alreadyDone = done;
                    }
                    setName(Messages.TmfCheckpointIndexer_Indexing + ' ' + fTrace.getName() + " (" + String.format("%,d", nbEvents) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    // setName doesn't refresh the UI, setTaskName does
                    long rate = (nbEvents - prevNbEvents) * 4;
                    subMonitor.setTaskName(String.format("%,d", rate) + " " + Messages.TmfCheckpointIndexer_EventsPerSecond); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (final InterruptedException e) {
                    return Status.OK_STATUS;
                }
            }
            subMonitor.done();
            monitor.done();
            return fException != null ? new Status(IStatus.ERROR, Activator.PLUGIN_ID, fException.getMessage(), fException) : Status.OK_STATUS;
        }

        public void setException(Exception e) {
            fException = e;
        }

    }
}
