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

package org.eclipse.linuxtools.tmf.component;

import java.lang.reflect.Array;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.eclipse.linuxtools.tmf.Tracer;
import org.eclipse.linuxtools.tmf.event.TmfData;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.request.TmfCoalescedDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfRequestExecutor;
import org.eclipse.linuxtools.tmf.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfStartSynchSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;

/**
 * <b><u>TmfProvider</u></b>
 * <p>
 * The TmfProvider<T> is a provider for a data of type <T>.
 * <p>
 * This abstract class implements the housekeeking methods to register/
 * deregister the event provider and to handle generically the event requests.
 * <p>
 * The concrete class can either re-implement processRequest() entirely or
 * just implement the hooks (initializeContext() and getNext()).
 * <p>
 * TODO: Add support for providing multiple data types.
 */
public abstract class TmfDataProvider<T extends TmfData> extends TmfComponent implements ITmfDataProvider<T> {

	// ------------------------------------------------------------------------
	// Constants
	// ------------------------------------------------------------------------

//	private static final ITmfDataRequest.ExecutionType SHORT = ITmfDataRequest.ExecutionType.SHORT;
//	private static final ITmfDataRequest.ExecutionType LONG  = ITmfDataRequest.ExecutionType.LONG;
	
	// ------------------------------------------------------------------------
	// 
	// ------------------------------------------------------------------------

	final protected Class<T> fType;
	final protected boolean  fLogData;
	final protected boolean  fLogError;

	public static final int DEFAULT_QUEUE_SIZE = 1000;
	protected final int fQueueSize;
	protected final BlockingQueue<T> fDataQueue;
	protected final TmfRequestExecutor fExecutor;

	private int fSignalDepth = 0;
    private final Object fLock = new Object();

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	
	public TmfDataProvider(String name, Class<T> type) {
		this(name, type, DEFAULT_QUEUE_SIZE);
	}

	protected TmfDataProvider(String name, Class<T> type, int queueSize) {
		super(name);
		fType = type;
		fQueueSize = queueSize;
        fDataQueue = (fQueueSize > 1) ? new LinkedBlockingQueue<T>(fQueueSize) : new SynchronousQueue<T>();

        fExecutor = new TmfRequestExecutor();
		fSignalDepth = 0;

		fLogData  = Tracer.isEventTraced();
		fLogError = Tracer.isErrorTraced();

		TmfProviderManager.register(fType, this);
//		if (Tracer.isComponentTraced()) Tracer.traceComponent(this, "started");
}
	
	public TmfDataProvider(TmfDataProvider<T> other) {
        super(other);
        fType = other.fType;
        fQueueSize = other.fQueueSize;
        fDataQueue = (fQueueSize > 1) ? new LinkedBlockingQueue<T>(fQueueSize) : new SynchronousQueue<T>();

        fExecutor = new TmfRequestExecutor();
        fSignalDepth = 0;

        fLogData  = Tracer.isEventTraced();
        fLogError = Tracer.isErrorTraced();
	}
	
	@Override
	public void dispose() {
		TmfProviderManager.deregister(fType, this);
		fExecutor.stop();
		super.dispose();
//		if (Tracer.isComponentTraced()) Tracer.traceComponent(this, "stopped");
	}

	public int getQueueSize() {
		return fQueueSize;
	}

	public Class<?> getType() {
		return fType;
	}

	// ------------------------------------------------------------------------
	// ITmfRequestHandler
	// ------------------------------------------------------------------------

	public void sendRequest(final ITmfDataRequest<T> request) {
		synchronized(fLock) {
			if (fSignalDepth > 0) {
				coalesceDataRequest(request);
			} else {
				dispatchRequest(request);
			}
		}
	}

	/**
	 * This method queues the coalesced requests.
	 * 
	 * @param thread
	 */
	public void fireRequests() {
		synchronized(fLock) {
			for (TmfDataRequest<T> request : fPendingCoalescedRequests) {
				dispatchRequest(request);
			}
			fPendingCoalescedRequests.clear();
		}
	}

	// ------------------------------------------------------------------------
	// Coalescing (primitive test...)
	// ------------------------------------------------------------------------

	protected Vector<TmfCoalescedDataRequest<T>> fPendingCoalescedRequests = new Vector<TmfCoalescedDataRequest<T>>();

	protected void newCoalescedDataRequest(ITmfDataRequest<T> request) {
		synchronized(fLock) {
			TmfCoalescedDataRequest<T> coalescedRequest =
				new TmfCoalescedDataRequest<T>(fType, request.getIndex(), request.getNbRequested(), request.getBlockize(), request.getExecType());
			coalescedRequest.addRequest(request);
	        if (Tracer.isRequestTraced()) {
	        	Tracer.traceRequest(request, "coalesced with " + coalescedRequest.getRequestId());
		        Tracer.traceRequest(coalescedRequest,  "added " + request.getRequestId());
	        }
			fPendingCoalescedRequests.add(coalescedRequest);
		}
	}

	protected void coalesceDataRequest(ITmfDataRequest<T> request) {
		synchronized(fLock) {
			for (TmfCoalescedDataRequest<T> req : fPendingCoalescedRequests) {
				if (req.isCompatible(request)) {
					req.addRequest(request);
			        if (Tracer.isRequestTraced()) {
				        Tracer.traceRequest(request, "coalesced with " + req.getRequestId());
				        Tracer.traceRequest(req,  "added " + request.getRequestId());
			        }
					return;
				}
			}
			newCoalescedDataRequest(request);
		}
	}

	// ------------------------------------------------------------------------
	// Request processing
	// ------------------------------------------------------------------------

	private void dispatchRequest(final ITmfDataRequest<T> request) {
		if (request.getExecType() == ExecutionType.SHORT)
			queueRequest(request);
		else
			queueLongRequest(request);
	}

	protected void queueRequest(final ITmfDataRequest<T> request) {

	    if (fExecutor.isShutdown()) {
	        request.cancel();
	        return;
	    }
	    
		final TmfDataProvider<T> provider = this;

		// Process the request
		TmfThread thread = new TmfThread(request.getExecType()) {

			@Override
			public void run() {

				if (Tracer.isRequestTraced()) Tracer.traceRequest(request, "started");

				// Extract the generic information
				request.start();
				int blockSize   = request.getBlockize();
				int nbRequested = request.getNbRequested();
			 
				// Create the result buffer
				Vector<T> result = new Vector<T>();
				int nbRead = 0;

				// Initialize the execution
				ITmfContext context = armRequest(request);
				if (context == null) {
					request.cancel();
					return;
				}

				try {
					// Get the ordered events
					if (Tracer.isRequestTraced()) Tracer.trace("Request #" + request.getRequestId() + " is being serviced by " + provider.getName());
					T data = getNext(context);
					if (Tracer.isRequestTraced()) Tracer.trace("Request #" + request.getRequestId() + " read first event");
					while (data != null && !isCompleted(request, data, nbRead))
					{
						if (fLogData) Tracer.traceEvent(provider, request, data);
						result.add(data);
						if (++nbRead % blockSize == 0) {
							pushData(request, result);
						}
						// To avoid an unnecessary read passed the last data requested
						if (nbRead < nbRequested) {
							data = getNext(context);
							if (Tracer.isRequestTraced() && (data == null || data.isNullRef())) {
								Tracer.trace("Request #" + request.getRequestId() + " end of data");
							}
						}
					}
					if (result.size() > 0) {
						pushData(request, result);
					}
					request.done();

					if (Tracer.isRequestTraced()) Tracer.traceRequest(request, "completed");
				}
				catch (Exception e) {
			        if (Tracer.isRequestTraced()) Tracer.traceRequest(request, "exception (failed)");
					request.fail();
//					e.printStackTrace();
				}
			}
		};

		fExecutor.execute(thread);

        if (Tracer.isRequestTraced()) Tracer.traceRequest(request, "queued");
	}

	// By default, same behavior as a short request
	protected void queueLongRequest(final ITmfDataRequest<T> request) {
		queueRequest(request);
	}

	/**
	 * Format the result data and forwards it to the requester.
	 * Note: after handling, the data is *removed*.
	 * 
	 * @param request
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	protected void pushData(ITmfDataRequest<T> request, Vector<T> data) {
		synchronized(request) {
			if (!request.isCompleted()) {
				T[] result = (T[]) Array.newInstance(fType, data.size());
				data.toArray(result);
				request.setData(result);
				request.handleData();
				data.removeAllElements();
			}
		}
	}

	/**
	 * Initialize the provider based on the request. The context is
	 * provider specific and will be updated by getNext().
	 * 
	 * @param request
	 * @return an application specific context; null if request can't be serviced
	 */
	public abstract ITmfContext armRequest(ITmfDataRequest<T> request);
	public abstract T getNext(ITmfContext context);

//	public abstract void queueResult(T data);

	/**
	 * Return the next piece of data based on the context supplied. The context
	 * would typically be updated for the subsequent read.
	 * 
	 * @param context
	 * @return
	 */
//	private static final int TIMEOUT = 10000;
////	public abstract T getNext(ITmfContext context) throws InterruptedException;
////	private int getLevel = 0;
//	public T getNext(ITmfContext context) throws InterruptedException {
////		String name = Thread.currentThread().getName(); getLevel++;
////		System.out.println("[" + System.currentTimeMillis() + "] " + name + " " + (getLevel) + " getNext() - entering");
//		T data = fDataQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
//		if (data == null) {
////			if (Tracer.isErrorTraced()) Tracer.traceError(getName() + ": Request timeout on read");
//			throw new InterruptedException();
//		}
////		System.out.println("[" + System.currentTimeMillis() + "] " + name + " " + (getLevel) + " getNext() - leaving");
////		getLevel--;
//		return data;
//	}
//
//	/**
//	 * Makes the generated result data available for getNext()
//	 * 
//	 * @param data
//	 */
////	public abstract void queueResult(T data) throws InterruptedException;
////	private int putLevel = 0;
//	public void queueResult(T data) throws InterruptedException {
////		String name = Thread.currentThread().getName(); putLevel++;
////		System.out.println("[" + System.currentTimeMillis() + "] " + name + " " + (putLevel) + " queueResult() - entering");
//		boolean ok = fDataQueue.offer(data, TIMEOUT, TimeUnit.MILLISECONDS);
//		if (!ok) {
////			if (Tracer.isErrorTraced()) Tracer.traceError(getName() + ": Request timeout on write");
//			throw new InterruptedException();
//		}
////		System.out.println("[" + System.currentTimeMillis() + "] " + name + " " + (putLevel) + " queueResult() - leaving");
////		putLevel--;
//	}

	/**
	 * Checks if the data meets the request completion criteria.
	 * 
	 * @param request
	 * @param data
	 * @return
	 */
	public boolean isCompleted(ITmfDataRequest<T> request, T data, int nbRead) {
		return request.isCompleted() || nbRead >= request.getNbRequested() || data.isNullRef();
	}

	// ------------------------------------------------------------------------
	// Signal handlers
	// ------------------------------------------------------------------------

	@TmfSignalHandler
	public void startSynch(TmfStartSynchSignal signal) {
	    synchronized (fLock) {
	        fSignalDepth++;
	    }
	}

	@TmfSignalHandler
	public void endSynch(TmfEndSynchSignal signal) {
        synchronized (fLock) {
    		fSignalDepth--;
    		if (fSignalDepth == 0) {
    			fireRequests();
    		}
        }
	}

}
