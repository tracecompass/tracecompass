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

	public static final int DEFAULT_BLOCK_SIZE = 5000;
	public static final int DEFAULT_QUEUE_SIZE = 1000;

	protected final int fQueueSize;
	protected final BlockingQueue<T> fDataQueue;
	protected final TmfRequestExecutor fExecutor;

	private int fSignalDepth = 0;
    private final Object fLock = new Object();

    private int fRequestPendingCounter = 0;

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

	@Override
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
	@Override
    public void fireRequest() {
		synchronized(fLock) {
			if (fRequestPendingCounter > 0) {
				return;
			}
			if (fPendingCoalescedRequests.size() > 0) {
				for (TmfDataRequest<T> request : fPendingCoalescedRequests) {
					dispatchRequest(request);
				}
				fPendingCoalescedRequests.clear();
			}
		}
	}
	
	/**
	 * Increments/decrements the pending requests counters and fires
	 * the request if necessary (counter == 0). Used for coalescing
	 * requests accross multiple TmfDataProvider.
	 * 
	 * @param isIncrement
	 */
    @Override
    public void notifyPendingRequest(boolean isIncrement) {
        synchronized(fLock) {
            if (isIncrement) {
                if (fSignalDepth > 0) {
                    fRequestPendingCounter++;
                }
            } else {
                if (fRequestPendingCounter > 0) {
                    fRequestPendingCounter--;
                }
                
                // fire request if all pending requests are received
                if (fRequestPendingCounter == 0) {
                    fireRequest();
                }
            }
        }
	}

	// ------------------------------------------------------------------------
	// Coalescing (primitive test...)
	// ------------------------------------------------------------------------

	protected Vector<TmfCoalescedDataRequest<T>> fPendingCoalescedRequests = new Vector<TmfCoalescedDataRequest<T>>();

	protected void newCoalescedDataRequest(ITmfDataRequest<T> request) {
		synchronized(fLock) {
			TmfCoalescedDataRequest<T> coalescedRequest = new TmfCoalescedDataRequest<T>(
					fType, request.getIndex(), request.getNbRequested(),request.getExecType());
			coalescedRequest.addRequest(request);
	        if (Tracer.isRequestTraced()) {
	        	Tracer.traceRequest(request, "coalesced with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
		        Tracer.traceRequest(coalescedRequest,  "added " + request.getRequestId()); //$NON-NLS-1$
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
				        Tracer.traceRequest(request, "coalesced with " + req.getRequestId()); //$NON-NLS-1$
				        Tracer.traceRequest(req,  "added " + request.getRequestId()); //$NON-NLS-1$
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
		if (request.getExecType() == ExecutionType.FOREGROUND)
			queueRequest(request);
		else
			queueBackgroundRequest(request, DEFAULT_BLOCK_SIZE, true);
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

				if (Tracer.isRequestTraced()) Tracer.traceRequest(request, "started"); //$NON-NLS-1$

				// Extract the generic information
				request.start();
				int nbRequested = request.getNbRequested();
				int nbRead = 0;

				// Initialize the execution
				ITmfContext context = armRequest(request);
				if (context == null) {
					request.cancel();
					return;
				}

				try {
					// Get the ordered events
					if (Tracer.isRequestTraced()) Tracer.trace("Request #" + request.getRequestId() + " is being serviced by " + provider.getName());  //$NON-NLS-1$//$NON-NLS-2$
					T data = getNext(context);
					if (Tracer.isRequestTraced()) Tracer.trace("Request #" + request.getRequestId() + " read first event"); //$NON-NLS-1$ //$NON-NLS-2$
					while (data != null && !isCompleted(request, data, nbRead))
					{
						if (fLogData) Tracer.traceEvent(provider, request, data);
						request.handleData(data);

						// To avoid an unnecessary read passed the last data requested
						if (++nbRead < nbRequested) {
							data = getNext(context);
							if (Tracer.isRequestTraced() && (data == null || data.isNullRef())) {
								Tracer.trace("Request #" + request.getRequestId() + " end of data");  //$NON-NLS-1$//$NON-NLS-2$
							}
						}
					}

					if (request.isCancelled()) {
						request.cancel();					    
					}
					else {
						request.done();
					}

					if (Tracer.isRequestTraced()) Tracer.traceRequest(request, "completed"); //$NON-NLS-1$
				}
				catch (Exception e) {
			        if (Tracer.isRequestTraced()) Tracer.traceRequest(request, "exception (failed)"); //$NON-NLS-1$
					request.fail();
				}
			}
		};

		fExecutor.execute(thread);

        if (Tracer.isRequestTraced()) Tracer.traceRequest(request, "queued"); //$NON-NLS-1$
	}

	// By default, same behavior as a foreground request
	protected void queueBackgroundRequest(final ITmfDataRequest<T> request, final int blockSize, boolean indexing) {
		queueRequest(request);
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
		  		fireRequest();
    		}
        }
	}

}
