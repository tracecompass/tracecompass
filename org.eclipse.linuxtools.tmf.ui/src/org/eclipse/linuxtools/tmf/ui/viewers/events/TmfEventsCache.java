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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

public class TmfEventsCache {

	public class CachedEvent {
		TmfEvent event;
		long rank;

		public CachedEvent (TmfEvent event, long rank) {
			this.event = event;
			this.rank = rank;
		}
	}

    private CachedEvent[] fCache;
    private int fCacheStartIndex = 0;
    private int fCacheEndIndex   = 0;

    private ITmfTrace<?> fTrace;
    private TmfEventsTable fTable;
    private ITmfFilter fFilter;
    private ArrayList<Integer> fFilterIndex = new ArrayList<Integer>(); // contains the event rank at each 'cache size' filtered events

    public TmfEventsCache(int cacheSize, TmfEventsTable table) {
    	fCache = new CachedEvent[cacheSize];
    	fTable = table;
    }
    
    public void setTrace(ITmfTrace<?> trace) {
    	fTrace = trace;
    	clear();
    }
    
    public void clear() {
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
    
    public CachedEvent getEvent(int index) {
        if ((index >= fCacheStartIndex) && (index < fCacheEndIndex)) {
            int i = index - fCacheStartIndex;
            return fCache[i];
        }
        populateCache(index);
    	return null;
    }

    public CachedEvent peekEvent(int index) {
        if ((index >= fCacheStartIndex) && (index < fCacheEndIndex)) {
            int i = index - fCacheStartIndex;
            return fCache[i];
        }
    	return null;
    }
    
    public synchronized void storeEvent(TmfEvent event, long rank, int index) {
    	if (fCacheStartIndex == fCacheEndIndex) {
    		fCacheStartIndex = index;
    		fCacheEndIndex = index;
    	}
    	if (index == fCacheEndIndex) {
    		int i = index - fCacheStartIndex;
    		if (i < fCache.length) {
    			fCache[i] = new CachedEvent(event.clone(), rank);
    			fCacheEndIndex++;
    		}
    	}
    	if (fFilter != null && index % fCache.length == 0) {
    		int i = index / fCache.length;
    		fFilterIndex.add(i, new Integer((int) rank));
    	}
    }
    
    @SuppressWarnings("unchecked")
    public int getFilteredEventIndex(final long rank) {
    	int current;
    	int startRank;
    	TmfDataRequest<TmfEvent> request;
    	synchronized (this) {
    		int start = 0;
    		int end = fFilterIndex.size();
    		
    		if (fCacheEndIndex - fCacheStartIndex > 1) {
    			if (rank < fCache[0].rank) {
    				end = fCacheStartIndex / fCache.length + 1;
    			} else if (rank > fCache[fCacheEndIndex - fCacheStartIndex - 1].rank) {
    				start = fCacheEndIndex / fCache.length;
    			} else {
    				for (int i = 0; i < fCacheEndIndex - fCacheStartIndex; i++) {
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
    		startRank = fFilterIndex.get(current);
    	}
    	
    	final int index = current * fCache.length;
    	
    	class DataRequest<T extends TmfEvent> extends TmfDataRequest<T> {
    		int fRank;
    		int fIndex;
    		
    		DataRequest(Class<T> dataType, int start, int nbRequested) {
    			super(dataType, start, nbRequested);
    			fRank = start;
    			fIndex = index;
    		}
    		
			@Override
			public void handleData(T event) {
				super.handleData(event);
				if (isCancelled()) return;
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
    	
    	request = new DataRequest<TmfEvent>(TmfEvent.class, startRank, TmfDataRequest.ALL_DATA);
		((ITmfDataProvider<TmfEvent>) fTrace).sendRequest(request);
		try {
			request.waitForCompletion();
			return ((DataRequest<TmfEvent>) request).getFilteredIndex();
		} catch (InterruptedException e) {
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
                if (index >= fCacheStartIndex && index < (fCacheStartIndex + fCache.length)) {
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
            	
                TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(TmfEvent.class, startIndex, nbRequested) {
                    private int count = 0;
                    private long rank = startIndex;
                    @Override
                    public void handleData(TmfEvent event) {
                        // If the job is canceled, cancel the request so waitForCompletion() will unlock
                        if (monitor.isCanceled()) {
                            cancel();
                            return;
                        }
                        super.handleData(event);
                        if (event != null) {
                        	if ((fFilter == null || fFilter.matches(event)) && skipCount-- <= 0) {
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
                        } else if (fFilter != null && count >= fTable.getTable().getItemCount() - 3) { // -1 for header row, -2 for top and bottom filter status rows
                        	cancel();
                        }
                        rank++;
                    }
                };

                ((ITmfDataProvider<TmfEvent>) fTrace).sendRequest(request);
                try {
                    request.waitForCompletion();
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
