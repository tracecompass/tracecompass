/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Add support for event collapsing
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.tmf.core.filter.TmfCollapseFilter;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.component.ITmfEventProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * The generic TMF Events table events cache
 *
 * This can help avoid re-reading the trace when the user scrolls a window,
 * for example.
 *
 * @author Patrick Tasse
 */
public class TmfEventsCache {

    /**
     * The generic TMF Events table cached event.
     *
     * @author Patrick Tasse
     */
    public static class CachedEvent implements ITmfEvent {
        /**
         * Event reference.
         *
         * When {@link TmfCollapseFilter} is active then it's event reference
         * of the first event of repeated events.
         */
        ITmfEvent event;
        /**
         * Events rank.
         *
         * When {@link TmfCollapseFilter} is active then it's event rank of the
         * first event of repeated events.
         */
        long rank;
        /**
         * Number times event is repeated. Updated by using {@link TmfCollapseFilter}
         */
        long repeatCount;

        /**
         * Constructor for new cached events.
         *
         * @param iTmfEvent
         *            The original trace event
         * @param rank
         *            The rank of this event in the trace
         */
        public CachedEvent (ITmfEvent iTmfEvent, long rank) {
            this.event = iTmfEvent;
            this.rank = rank;
        }
        /**
         * @since 3.2
         */
        @Override
        public Object getAdapter(Class adapter) {
            return event.getAdapter(adapter);
        }
        /**
         * @since 3.2
         */
        @Override
        public ITmfTrace getTrace() {
            return event.getTrace();
        }
        /**
         * @since 3.2
         */
        @Override
        public long getRank() {
            return event.getRank();
        }
        /**
         * @since 3.2
         */
        @Override
        public ITmfTimestamp getTimestamp() {
            return event.getTimestamp();
        }
        /**
         * @since 3.2
         */
        @Override
        public String getSource() {
            return event.getSource();
        }
        /**
         * @since 3.2
         */
        @Override
        public ITmfEventType getType() {
            return event.getType();
        }
        /**
         * @since 3.2
         */
        @Override
        public ITmfEventField getContent() {
            return event.getContent();
        }
        /**
         * @since 3.2
         */
        @Override
        public String getReference() {
            return event.getReference();
        }
    }

    private final CachedEvent[] fCache;
    private final int fCacheSize;
    private int fCacheStartIndex = 0;
    private int fCacheEndIndex   = 0;

    private ITmfTrace fTrace;
    private final TmfEventsTable fTable;
    private ITmfFilter fFilter;
    private final List<Integer> fFilterIndex = new ArrayList<>(); // contains the event rank at each 'cache size' filtered events

    /**
     * Constructor for the event cache
     *
     * @param cacheSize
     *            The size of the cache, in number of events
     * @param table
     *            The Events table this cache will cover
     */
    public TmfEventsCache(int cacheSize, TmfEventsTable table) {
        fCacheSize = cacheSize;
        fCache = new CachedEvent[cacheSize * 2]; // the cache holds two blocks of cache size
        fTable = table;
    }

    /**
     * Assign a new trace to this events cache. This clears the current
     * contents.
     *
     * @param trace
     *            The trace to assign.
     */
    public void setTrace(ITmfTrace trace) {
        fTrace = trace;
        clear();
    }

    /**
     * Clear the current contents of this cache.
     */
    public synchronized void clear() {
        if (job != null && job.getState() != Job.NONE) {
            job.cancel();
        }
        Arrays.fill(fCache, null);
        fCacheStartIndex = 0;
        fCacheEndIndex = 0;
        fFilterIndex.clear();
    }

    /**
     * Apply a filter on this event cache. This clears the current cache
     * contents.
     *
     * @param filter
     *            The ITmfFilter to apply.
     */
    public void applyFilter(ITmfFilter filter) {
        fFilter = filter;
        clear();
    }

    /**
     * Clear the current filter on this cache. This also clears the current
     * cache contents.
     */
    public void clearFilter() {
        fFilter = null;
        clear();
    }

    /**
     * Get an event from the cache. If the cache does not contain the event,
     * a cache population request is triggered.
     *
     * @param index
     *            The index of this event in the cache
     * @return The cached event, or 'null' if the event is not in the cache
     */
    public synchronized CachedEvent getEvent(int index) {
        if ((index >= fCacheStartIndex) && (index < fCacheEndIndex)) {
            int i = index - fCacheStartIndex;
            return fCache[i];
        }
        populateCache(index);
        return null;
    }

    /**
     * Peek an event in the cache. Does not trigger cache population.
     *
     * @param index
     *            Index of the event to peek
     * @return The cached event, or 'null' if the event is not in the cache
     */
    public synchronized CachedEvent peekEvent(int index) {
        if ((index >= fCacheStartIndex) && (index < fCacheEndIndex)) {
            int i = index - fCacheStartIndex;
            return fCache[i];
        }
        return null;
    }

    /**
     * Add a trace event to the cache.
     *
     * @param event
     *            The original trace event to be cached
     * @param rank
     *            The rank of this event in the trace
     * @param index
     *            The index this event will occupy in the cache
     */
    public synchronized void storeEvent(ITmfEvent event, long rank, int index) {
        if (index == fCacheEndIndex) {
            int i = index - fCacheStartIndex;
            if (i < fCache.length) {
                fCache[i] = new CachedEvent(event, rank);
                fCacheEndIndex++;
            }
        }
        if ((fFilter != null) && ((index % fCacheSize) == 0)) {
            int i = index / fCacheSize;
            fFilterIndex.add(i, Integer.valueOf((int) rank));
        }
    }

    /**
     * Update event repeat count at index
     *
     * @param index
     *            The index this event occupies in the cache
     *
     * @since 3.2
     */
    public synchronized void updateCollapsedEvent(int index) {
        int i = index - fCacheStartIndex;
        if (i < fCache.length) {
            fCache[i].repeatCount++;
        }
    }

    /**
     * Get the cache index of an event from his rank in the trace. This will
     * take in consideration any filter that might be applied.
     *
     * @param rank
     *            The rank of the event in the trace
     * @return The position (index) this event should use once cached
     */
    public int getFilteredEventIndex(final long rank) {
        int current;
        int startRank;
        TmfEventRequest request;
        final ITmfFilter filter = fFilter;
        synchronized (this) {
            int start = 0;
            int end = fFilterIndex.size();

            if ((fCacheEndIndex - fCacheStartIndex) > 1) {
                if (rank < fCache[0].rank) {
                    end = (fCacheStartIndex / fCacheSize) + 1;
                } else if (rank > fCache[fCacheEndIndex - fCacheStartIndex - 1].rank) {
                    start = fCacheEndIndex / fCacheSize;
                } else {
                    for (int i = 0; i < (fCacheEndIndex - fCacheStartIndex); i++) {
                        if (fCache[i].rank >= rank) {
                            return fCacheStartIndex + i;
                        }
                    }
                    return fCacheEndIndex;
                }
            }

            current = (start + end) / 2;
            while (current != start) {
                if (rank < fFilterIndex.get(current)) {
                    end = current;
                    current = (start + end) / 2;
                } else {
                    start = current;
                    current = (start + end) / 2;
                }
            }
            startRank = fFilterIndex.size() > 0 ? fFilterIndex.get(current) : 0;
        }

        final int index = current * fCacheSize;

        class DataRequest extends TmfEventRequest {
            ITmfFilter requestFilter;
            int requestRank;
            int requestIndex;

            DataRequest(Class<? extends ITmfEvent> dataType, ITmfFilter reqFilter, int start, int nbRequested) {
                super(dataType, TmfTimeRange.ETERNITY, start, nbRequested,
                        TmfEventRequest.ExecutionType.FOREGROUND);
                requestFilter = reqFilter;
                requestRank = start;
                requestIndex = index;
            }

            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (isCancelled()) {
                    return;
                }
                if (requestRank >= rank) {
                    cancel();
                    return;
                }
                requestRank++;
                if (requestFilter.matches(event)) {
                    requestIndex++;
                }
            }

            public int getFilteredIndex() {
                return requestIndex;
            }
        }

        request = new DataRequest(ITmfEvent.class, filter, startRank, ITmfEventRequest.ALL_DATA);
        ((ITmfEventProvider) fTrace).sendRequest(request);
        try {
            request.waitForCompletion();
            return ((DataRequest) request).getFilteredIndex();
        } catch (InterruptedException e) {
            Activator.getDefault().logError("Filter request interrupted!", e); //$NON-NLS-1$
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // Event cache population
    // ------------------------------------------------------------------------

    // The event fetching job
    private Job job;
    private synchronized void populateCache(final int index) {

        /* Check if the current job will fetch the requested event:
         * 1. The job must exist
         * 2. It must be running (i.e. not completed)
         * 3. The requested index must be within the cache range
         *
         * If the job meets these conditions, we simply exit.
         * Otherwise, we create a new job but we might have to cancel
         * an existing job for an obsolete range.
         */
        if (job != null) {
            if (job.getState() != Job.NONE) {
                if ((index >= fCacheStartIndex) && (index < (fCacheStartIndex + fCache.length))) {
                    return;
                }
                // The new index is out of the requested range
                // Kill the job and start a new one
                job.cancel();
            }
        }

        // Populate the cache starting at the index that is one block less
        // of cache size than the requested index. The cache will hold two
        // consecutive blocks of cache size, centered on the requested index.
        fCacheStartIndex = Math.max(0, index - fCacheSize);
        fCacheEndIndex   = fCacheStartIndex;

        job = new Job("Fetching Events") { //$NON-NLS-1$
            private int startIndex = fCacheStartIndex;
            private int skipCount = 0;
            @Override
            protected IStatus run(final IProgressMonitor monitor) {

                int nbRequested;
                if (fFilter == null) {
                    nbRequested = fCache.length;
                } else {
                    nbRequested = ITmfEventRequest.ALL_DATA;
                    int i = startIndex / fCacheSize;
                    if (i < fFilterIndex.size()) {
                        skipCount = startIndex - (i * fCacheSize);
                        startIndex = fFilterIndex.get(i);
                    }
                }

                TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                        TmfTimeRange.ETERNITY,
                        startIndex,
                        nbRequested,
                        TmfEventRequest.ExecutionType.FOREGROUND) {
                    private int count = 0;
                    private long rank = startIndex;
                    @Override
                    public void handleData(ITmfEvent event) {
                        // If the job is canceled, cancel the request so waitForCompletion() will unlock
                        if (monitor.isCanceled()) {
                            cancel();
                            return;
                        }
                        super.handleData(event);
                        if (((fFilter == null) || fFilter.matches(event)) && (skipCount-- <= 0)) {
                            synchronized (TmfEventsCache.this) {
                                if (monitor.isCanceled()) {
                                    return;
                                }
                                fCache[count] = new CachedEvent(event, rank);
                                count++;
                                fCacheEndIndex++;
                            }
                            if (fFilter != null) {
                                fTable.cacheUpdated(false);
                            }
                        } else if (((fFilter != null) && !fFilter.matches(event)) && (skipCount <= 0)) { // TODO fix duplicated call to matches()
                            if ((count > 0) && (fFilter instanceof TmfCollapseFilter)) {
                                fCache[count - 1].repeatCount++;
                            }
                        }
                        if (count >= fCache.length) {
                            cancel();
                        } else if ((fFilter != null) && (count >= (fTable.getTable().getItemCount() - 3))) { // -1 for header row, -2 for top and bottom filter status rows
                            cancel();
                        }
                        rank++;
                    }
                };

                ((ITmfEventProvider) fTrace).sendRequest(request);
                try {
                    request.waitForCompletion();
                } catch (InterruptedException e) {
                    Activator.getDefault().logError("Wait for completion interrupted for populateCache ", e); //$NON-NLS-1$
                }

                fTable.cacheUpdated(true);

                // Flag the UI thread that the cache is ready
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                return Status.OK_STATUS;
            }
        };
        //job.setSystem(true);
        job.setPriority(Job.SHORT);
        job.schedule();
    }

}
