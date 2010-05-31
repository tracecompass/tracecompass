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

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.linuxtools.tmf.Tracer;

/**
 * <b><u>TmfRequestExecutor</u></b>
 *
 * A simple, straightforward request executor.
 */
public class TmfRequestExecutor implements Executor {

	private final ExecutorService fExecutor;
	private final String fExecutorName;
	private final Queue<Runnable> fRequestQueue = new LinkedBlockingQueue<Runnable>();
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
		Tracer.trace(fExecutor + " created");
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
	public boolean isShutdown() {
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
		Tracer.trace(fExecutor + " terminated");
	}
	
	// ------------------------------------------------------------------------
	// Operations
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	public synchronized void execute(final Runnable request) {
		Tracer.trace("Queueing request " + request);
		fRequestQueue.offer(new Runnable() {
			public void run() {
				try {
					Tracer.trace("Processing request " + request);
					request.run();
					Tracer.trace("Finishing request " + request);
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
