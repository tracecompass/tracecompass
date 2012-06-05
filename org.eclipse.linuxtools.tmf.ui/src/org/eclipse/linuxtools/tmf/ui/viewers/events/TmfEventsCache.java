/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * The generic TMF Events table events cache
 *
 * This can help avoid re-reading the trace when the user scrolls a window,
 * for example.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfEventsCache {

    /**
     * The generic TMF Events table cached event
     *
     * @version 1.0
     * @author Patrick Tasse
     */
    public static class CachedEvent {
        ITmfEvent event;
        long rank;

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
    }

    private final CachedEvent[] fCache;
    private int fCacheStartIndex = 0;
    private int fCacheEndIndex   = 0;

    private ITmfTrace fTrace;
    private final TmfEventsTable fTable;
    private ITmfFilter fFilter;
    private final List<Integer> fFilterIndex = new ArrayList<Integer>(); // contains the event rank at each 'cache size' filtered events

    /**
     * Constructor for the event cache
     *
     * @param cacheSize
     *            The size of the cache, in number of events
     * @param table
     *            The Events table this cache will cover
     */
    public TmfEventsCache(int cacheSize, TmfEventsTable table) {
        fCache = new CachedEvent[cacheSize];
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
     * Get an event from the cache. This will remove the event from the cache.
     *
     * FIXME this does not currently remove the event!
     *
     * @param index
     *            The index of this event in the cache
     * @return The cached event, or 'null' if there is no event at that index
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
     * Read an event, but without removing it from the cache.
     *
     * @param index
     *            Index of the event to peek
     * @return A reference to the event, or 'null' if there is no event at this
     *         index
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
                fCache[i] = new CachedEvent(event.clone(), rank);
                fCacheEndIndex++;
            }
        }
        if ((fFilter != null) && ((index % fCache.length) == 0)) {
            int i = index / fCache.length;
            fFilterIndex.add(i, Integer.valueOf((int) rank));
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
        TmfDataRequest request;
        final ITmfFilter filter = fFilter;
        synchronized (this) {
            int start = 0;
            int end = fFilterIndex.size();

            if ((fCacheEndIndex - fCacheStartIndex) > 1) {
                if (rank < fCache[0].rank) {
                    end = (fCacheStartIndex / fCache.length) + 1;
                } else if (rank > fCache[fCacheEndIndex - fCacheStartIndex - 1].rank) {
                    start = fCacheEndIndex / fCache.length;
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

        final int index = current * fCache.length;

        class DataRequest extends TmfDataRequest {
            ITmfFilter fFilter;
            int fRank;
            int fIndex;

            DataRequest(Class<? extends ITmfEvent> dataType, ITmfFilter filter, int start, int nbRequested) {
                super(dataType, start, nbRequested);
                fFilter = filter;
                fRank = start;
                fIndex = index;
            }

            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (isCancelled()) {
                    return;
                }
                if (fRank >= rank) {
                    cancel();
                    return;
                }
                fRank++;
                if (fFilter.matches(event)) {
                    fIndex++;
                }
            }

            public int getFilteredIndex() {
                return fIndex;
            }
        }

        request = new DataRequest(ITmfEvent.class, filter, startRank, TmfDataRequest.ALL_DATA);
        ((ITmfDataProvider) fTrace).sendRequest(request);
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

        fCacheStartIndex = index;
        fCacheEndIndex   = index;

        job = new Job("Fetching Events") { //$NON-NLS-1$
            private int startIndex = index;
            private int skipCount = 0;
            @Override
            protected IStatus run(final IProgressMonitor monitor) {

                int nbRequested;
                if (fFilter == null) {
                    nbRequested = fCache.length;
                } else {
                    nbRequested = TmfDataRequest.ALL_DATA;
                    int i = index / fCache.length;
                    if (i < fFilterIndex.size()) {
                        startIndex = fFilterIndex.get(i);
                        skipCount = index - (i * fCache.length);
                    }
                }

                TmfDataRequest request = new TmfDataRequest(ITmfEvent.class, startIndex, nbRequested) {
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
                        if (event != null) {
                            if (((fFilter == null) || fFilter.matches(event)) && (skipCount-- <= 0)) {
                                synchronized (TmfEventsCache.this) {
                                    fCache[count] = new CachedEvent(event.clone(), rank);
                                    count++;
                                    fCacheEndIndex++;
                                }
                                if (fFilter != null) {
                                    fTable.cacheUpdated(false);
                                }
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

                ((ITmfDataProvider) fTrace).sendRequest(request);
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
