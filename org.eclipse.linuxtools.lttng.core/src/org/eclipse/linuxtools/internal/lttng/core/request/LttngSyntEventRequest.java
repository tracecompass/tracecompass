/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.request;

import java.util.Vector;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ITransEventProcessor;
import org.eclipse.linuxtools.tmf.core.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;

/**
 * This class is an extension of Data Request which includes specific references
 * i.e. a status listener to indicate the start and end of the request
 * 
 * @author alvaro
 * 
 */
public class LttngSyntEventRequest extends TmfEventRequest<LttngSyntheticEvent>
		implements ILttngSyntEventRequest {

	// ========================================================================
	// Data
	// =======================================================================
	private Vector<IRequestStatusListener> listeners = new Vector<IRequestStatusListener>();
	private Long feventCount = 0L;
	private boolean clearDataInd = false;
	private TmfTimeRange fExperimentTimeRange = null;
	private Object fsource = null;
	private final ITransEventProcessor fprocessor;
	private String fExperimentName = null; 
	

	// ========================================================================
	// Constructors
	// =======================================================================
    /**
     * @param range
     * @param offset
     * @param nbEvents
     * @param maxBlockSize
     * @param listener
     * @param experimentTimeRange
     * @param processor
     */
    public LttngSyntEventRequest(TmfTimeRange range, long offset, int nbEvents,
	            int maxBlockSize, IRequestStatusListener listener,
	            TmfTimeRange experimentTimeRange, ITransEventProcessor processor) {
	        
	        this(range, offset, nbEvents, maxBlockSize, listener, experimentTimeRange, processor, "", ExecutionType.FOREGROUND); //$NON-NLS-1$
	    }
	/**
	 * @param range
	 * @param offset
	 * @param nbEvents
	 * @param maxBlockSize
	 * @param listener
	 * @param experimentTimeRange
	 * @param processor
	 * @param experimentName 
	 */
	public LttngSyntEventRequest(TmfTimeRange range, long offset, int nbEvents,
			int maxBlockSize, IRequestStatusListener listener,
			TmfTimeRange experimentTimeRange, ITransEventProcessor processor, String experimentName) {
		
		this(range, offset, nbEvents, maxBlockSize, listener, experimentTimeRange, processor, experimentName, ExecutionType.FOREGROUND);
	}

    /**
     * @param range
     * @param offset
     * @param nbEvents
     * @param maxBlockSize
     * @param listener
     * @param experimentTimeRange
     * @param processor
     * @param experimentName
     * @param execType 
     */
	public LttngSyntEventRequest(TmfTimeRange range, long offset, int nbEvents,
			int maxBlockSize, IRequestStatusListener listener,
			TmfTimeRange experimentTimeRange, ITransEventProcessor processor, String experimentName,
			ExecutionType execType) {
		
		super(LttngSyntheticEvent.class, range, (int) offset, nbEvents, maxBlockSize, execType);
		fExperimentTimeRange = experimentTimeRange;
		fExperimentName = experimentName;
		addListener(listener);

		fprocessor = processor;
	}

	/**
	 * @param listener
	 */
	public void addListener(IRequestStatusListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * @param listener
	 */
	public void removeListener(IRequestStatusListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	// ========================================================================
	// Methods
	// =======================================================================
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.request.ILttngEventRequest#startRequestInd(org.eclipse.linuxtools.tmf.experiment.TmfExperiment, boolean)
	 */
	@Override
	public void startRequestInd(TmfEventProvider<LttngSyntheticEvent> provider) {
		// trigger the start to process this request
		provider.sendRequest(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.request.ILttngEventRequest#notifyCompletion()
	 */
	@Override
	public void notifyCompletion() {
		// Notify specific state views
		for (IRequestStatusListener listener : listeners) {
			listener.processingCompleted(new RequestCompletedSignal(this));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.request.ILttngEventRequest#notifyStarting()
	 */
	@Override
	public void notifyStarting() {
		for (IRequestStatusListener listener : listeners) {
			listener.processingStarted(new RequestStartedSignal(this));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.request.ILttngEventRequest#getExperimentTimeRange()
	 */
	@Override
	public TmfTimeRange getExperimentTimeRange() {
		return fExperimentTimeRange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.request.ILttngSyntEventRequest#setSynEventCount
	 * (java.lang.Long)
	 */
	@Override
	public synchronized void setSynEventCount(Long numOfEvents) {
		this.feventCount = numOfEvents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.request.ILttngSyntEventRequest#getEventCount
	 * ()
	 */
	@Override
	public synchronized Long getSynEventCount() {
		return feventCount;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.request.ILttngEventRequest#setclearDataInd(boolean)
	 */
	@Override
	public void setclearDataInd(boolean clearAllData) {
		this.clearDataInd = clearAllData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.request.ILttngEventRequest#isclearDataInd()
	 */
	@Override
	public boolean isclearDataInd() {
		return clearDataInd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.request.ILttngEventRequest#handleData()
	 */
	@Override
	public void handleData(LttngSyntheticEvent event) {
		super.handleData(event);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.tmf.request.TmfDataRequest#handleCompleted()
	 */
	@Override
	public void handleCompleted() {
		// notify listeners
		notifyCompletion();
		super.handleCompleted();
	}

	/**
	 * @return the source
	 */
	@Override
	public Object getSource() {
		return fsource;
	}

	/**
	 * @param source
	 */
	@Override
	public void setSource(Object source) {
		this.fsource = source;
	}

	/**
	 * @return the event processor associated to this request
	 */
	public ITransEventProcessor getProcessor() {
		return fprocessor;
	}

	@Override
    public String getExperimentName() {
	    return fExperimentName;
	}
	
}