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

import org.eclipse.linuxtools.tmf.event.TmfData;
import org.eclipse.linuxtools.tmf.request.ITmfRequestHandler;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;

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
public abstract class TmfProvider<T extends TmfData> extends TmfComponent implements ITmfRequestHandler<T> {

	private Class<T> fType;

	// ------------------------------------------------------------------------
	// Constructors (enforce that a type be supplied) 
	// ------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private TmfProvider() {
	}

	@SuppressWarnings("unused")
	private TmfProvider(TmfProvider<T> other) {
	}

	protected TmfProvider(Class<T> type) {
		fType = type;
		register();
	}

	@Override
	public void register() {
		super.register();
		TmfProviderManager.register(fType, this);
	}

	@Override
	public void deregister() {
		TmfProviderManager.deregister(fType, this);
		super.deregister();
	}

	// ------------------------------------------------------------------------
	// ITmfRequestHandler
	// ------------------------------------------------------------------------

	// TODO: Request coalescing, filtering, result dispatching

	public void processRequest(final TmfDataRequest<T> request, boolean waitForCompletion) {

		//Process the request 
		processDataRequest(request);

		// Wait for completion if needed
    	if (waitForCompletion) {
			request.waitForCompletion();
		}
	}

	protected void processDataRequest(final TmfDataRequest<T> request) {

		// Process the request
		Thread thread = new Thread() {

			@Override
			public void run() {

				// Extract the generic information
				int blockSize   = request.getBlockize();
				int nbRequested = request.getNbRequested();
			 
				// Create the result buffer
				Vector<T> result = new Vector<T>();
				int nbRead = 0;

				// Initialize the execution
				ITmfContext context = setContext(request);

				// Get the ordered events
				T data = getNext(context);
				while (data != null && !request.isCancelled() && nbRead < nbRequested && !isCompleted(request, data))
				{
					result.add(data);
					if (++nbRead % blockSize == 0) {
						pushData(request, result);
					}
					// To avoid an unnecessary read passed the last data requested
					if (nbRead < nbRequested)
						data = getNext(context);
				}
				pushData(request, result);
				request.done();
			}
		};
		thread.start();
	}

	/**
	 * Format the result data and forwards it to the requester.
	 * Note: after handling, the data is *removed*.
	 * 
	 * @param request
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	private void pushData(TmfDataRequest<T> request, Vector<T> data) {
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
	 * application specific and will be updated by getNext().
	 * 
	 * @param request
	 * @return
	 */
	public abstract ITmfContext setContext(TmfDataRequest<T> request);
	
	/**
	 * Return the next piece of data based on the context supplied. The context
	 * would typically be updated for the subsequent read.
	 * 
	 * @param context
	 * @return
	 */
	public abstract T getNext(ITmfContext context);

	/**
	 * Checks if the data meets the request completion criteria.
	 * 
	 * @param request
	 * @param data
	 * @return
	 */
	public abstract boolean isCompleted(TmfDataRequest<T> request, T data);

}
