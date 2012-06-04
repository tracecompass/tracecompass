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

public class TmfEventsCache {

    public static class CachedEvent {
        ITmfEvent event;
        long rank;

        public CachedEvent (ITmfEvent iTmfEvent, long rank) {
            this.event = iTmfEvent;
            this.rank = rank;
        }
    }

    private final CachedEvent[] fCache;
    private int fCacheStartIndex = 0;
    private int fCacheEndIndex   = 0;

    private ITmfTrace<?> fTrace;
    private final TmfEventsTable fTable;
    private ITmfFilter fFilter;
    private final List<Integer> fFilterIndex = new ArrayList<Integer>(); // contains the event rank at each 'cache size' filtered events

    public TmfEventsCache(int cacheSize, TmfEventsTable table) {
        fCache = new CachedEvent[cacheSize];
        fTable = table;
    }

    public void setTrace(ITmfTrace<?> trace) {
        fTrace = trace;
        clear();
    }

    public synchronized void clear() {
        fCacheStartIndex = 0;
        fCacheEndIndex = 0;
        fFilterIndex.clear();
    }

    public void applyFilter(ITmfFilter filter) {
        fFilter = filter;
        clear();
    }

    public void clearFilter() {
        fFilter = null;
        clear();
    }

    public synchronized CachedEvent getEvent(int index) {
        if ((index >= fCacheStartIndex) && (index < fCacheEndIndex)) {
            int i = index - fCacheStartIndex;
            return fCache[i];
        }
        populateCache(index);
        return null;
    }

    public synchronized CachedEvent peekEvent(int index) {
        if ((index >= fCacheStartIndex) && (index < fCacheEndIndex)) {
            int i = index - fCacheStartIndex;
            return fCache[i];
        }
        return null;
    }

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

    @SuppressWarnings("unchecked")
    public int getFilteredEventIndex(final long rank) {
        int current;
        int startRank;
        TmfDataRequest<ITmfEvent> request;
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

        class DataRequest<T extends ITmfEvent> extends TmfDataRequest<T> {
            ITmfFilter fFilter;
            int fRank;
            int fIndex;

            DataRequest(Class<T> dataType, ITmfFilter filter, int start, int nbRequested) {
                super(dataType, start, nbRequested);
                fFilter = filter;
                fRank = start;
                fIndex = index;
            }

            @Override
            public void handleData(T event) {
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

        request = new DataRequest<ITmfEvent>(ITmfEvent.class, filter, startRank, TmfDataRequest.ALL_DATA);
        ((ITmfDataProvider<ITmfEvent>) fTrace).sendRequest(request);
        try {
            request.waitForCompletion();
            return ((DataRequest<ITmfEvent>) request).getFilteredIndex();
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
            @SuppressWarnings("unchecked")
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

                TmfDataRequest<ITmfEvent> request = new TmfDataRequest<ITmfEvent>(ITmfEvent.class, startIndex, nbRequested) {
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

                ((ITmfDataProvider<ITmfEvent>) fTrace).sendRequest(request);
                try {
                    request.waitForCompletion();
                } catch (InterruptedException e) {
                    Activator.getDefault().logError("Wait for completion interrupted for populateCache ", e); //$NON-NLS-1$
                }

                fTable.cacheUpdated(true);

                // Flag the UI thread that the cache is ready
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                } else {
                    return Status.OK_STATUS;
                }
            }
        };
        //job.setSystem(true);
        job.setPriority(Job.SHORT);
        job.schedule();
    }

}
