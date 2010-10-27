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

package org.eclipse.linuxtools.tmf.request;

import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import org.eclipse.linuxtools.tmf.Tracer;
import org.eclipse.linuxtools.tmf.component.TmfThread;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest.ExecutionType;

/**
 * <b><u>TmfRequestExecutor</u></b>
 *
 * A simple, straightforward request executor.
 */
public class TmfRequestExecutor implements Executor {

	private final ExecutorService fExecutor;
	private final String fExecutorName;
	private final PriorityBlockingQueue<TmfThread> fRequestQueue = new PriorityBlockingQueue<TmfThread>(100, new Comparator<TmfThread>() {
		@Override
		public int compare(TmfThread o1, TmfThread o2) {
			if (o1.getExecType() == o2.getExecType())
				return 0;
			if (o1.getExecType() == ExecutionType.BACKGROUND)
				return 1;
			return -1;
		}
	});
	private Runnable fCurrentRequest;
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	
	public TmfRequestExecutor() {
		this(Executors.newSingleThreadExecutor());
	}

	public TmfRequestExecutor(ExecutorService executor) {
		fExecutor = executor;
		String canonicalName = fExecutor.getClass().getCanonicalName();
		fExecutorName = canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
		if (Tracer.isComponentTraced()) Tracer.trace(fExecutor + " created");
	}

	/**
	 * @return the number of pending requests
	 */
	public int getNbPendingRequests() {
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
	public boolean isTerminated() {
		return fExecutor.isTerminated();
	}
	
	/**
	 * Stops the executor
	 */
	public void stop() {
		fExecutor.shutdown();
		if (Tracer.isComponentTraced()) Tracer.trace(fExecutor + " terminated");
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
				    if (Tracer.isRequestTraced()) Tracer.trace("[REQ] Request finished");
				} finally {
					scheduleNext();
				}
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
			if (!isShutdown())
				fExecutor.execute(fCurrentRequest);
		}
	}

	// ------------------------------------------------------------------------
	// Object
	// ------------------------------------------------------------------------

	@Override
	public String toString() {
		return "[TmfRequestExecutor(" + fExecutorName + ")]";
	}

}
