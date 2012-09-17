/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.request;

import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfThread;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;

/**
 * A simple, straightforward request executor.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfRequestExecutor implements Executor {

	private final ExecutorService fExecutor;
	private final String fExecutorName;
    private final PriorityBlockingQueue<TmfThread> fRequestQueue = new PriorityBlockingQueue<TmfThread>(
            100, new Comparator<TmfThread>() {
                @Override
                public int compare(TmfThread o1, TmfThread o2) {
                    if (o1.getExecType() == o2.getExecType()) {
                        return 0;
                    }
                    if (o1.getExecType() == ExecutionType.BACKGROUND) {
                        return 1;
                    }
                    return -1;
                }
            });
    private TmfThread fCurrentRequest;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfRequestExecutor() {
        this(Executors.newSingleThreadExecutor());
    }

    /**
     * Standard constructor
     *
     * @param executor The executor service to use
     */
    public TmfRequestExecutor(ExecutorService executor) {
        fExecutor = executor;
        String canonicalName = fExecutor.getClass().getCanonicalName();
        fExecutorName = canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
        if (TmfCoreTracer.isComponentTraced())
        {
            TmfCoreTracer.trace(fExecutor + " created"); //$NON-NLS-1$
        }
    }

	/**
	 * @return the number of pending requests
	 */
	public synchronized int getNbPendingRequests() {
		return fRequestQueue.size();
	}

	/**
	 * @return the shutdown state (i.e. if it is accepting new requests)
	 */
	public synchronized boolean isShutdown() {
		return fExecutor.isShutdown();
	}

	/**
	 * @return the termination state
	 */
	public synchronized boolean isTerminated() {
		return fExecutor.isTerminated();
	}

	/**
	 * Stops the executor
	 */
	public synchronized void stop() {
	    if (fCurrentRequest != null) {
	        fCurrentRequest.cancel();
	    }

	    while ((fCurrentRequest = fRequestQueue.poll()) != null) {
	        fCurrentRequest.cancel();
	    }

		fExecutor.shutdown();
		if (TmfCoreTracer.isComponentTraced())
         {
            TmfCoreTracer.trace(fExecutor + " terminated"); //$NON-NLS-1$
        }
	}

	// ------------------------------------------------------------------------
	// Operations
	// ------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	@Override
	public synchronized void execute(final Runnable requestThread) {
		fRequestQueue.offer(new TmfThread(((TmfThread) requestThread).getExecType()) {
			@Override
			public void run() {
				try {
					requestThread.run();
				} finally {
					scheduleNext();
				}
			}
            @Override
            public void cancel() {
                ((TmfThread) requestThread).cancel();
            }
		});
		if (fCurrentRequest == null) {
			scheduleNext();
		}
	}

	/**
	 * Executes the next pending request, if applicable.
	 */
	protected synchronized void scheduleNext() {
		if ((fCurrentRequest = fRequestQueue.poll()) != null) {
			if (!isShutdown()) {
                fExecutor.execute(fCurrentRequest);
            }
		}
	}

	// ------------------------------------------------------------------------
	// Object
	// ------------------------------------------------------------------------

	@Override
    @SuppressWarnings("nls")
	public String toString() {
		return "[TmfRequestExecutor(" + fExecutorName + ")]";
	}

}
