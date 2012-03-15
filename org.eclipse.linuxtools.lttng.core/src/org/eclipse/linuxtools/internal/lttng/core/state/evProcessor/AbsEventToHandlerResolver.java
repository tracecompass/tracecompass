/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@ail.com) - Initial API and implementation
 * 	 Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.core.state.evProcessor;

import java.util.Set;

import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;

/**
 * @author alvaro
 * 
 */
public abstract class AbsEventToHandlerResolver implements
		IEventToHandlerResolver, ITransEventProcessor {

	Long fbeforeEventCount = 0L;
	Long fstateUpdateCount = 0L;
	Long filteredOutEventsCount = 0L;

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.IEventToHandlerResolver#getBeforeProcessor(java.lang.String)
	 */
	@Override
	public abstract ILttngEventProcessor getBeforeProcessor(String eventType);

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.IEventToHandlerResolver#getAfterProcessor(java.lang.String)
	 */
	@Override
	public abstract ILttngEventProcessor getAfterProcessor(String eventType);

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.IEventToHandlerResolver#getfinishProcessor()
	 */
	@Override
	public abstract ILttngEventProcessor getfinishProcessor();

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.IEventToHandlerResolver#getStateUpdaterProcessor(java.lang.String)
	 */
	@Override
	public abstract ILttngEventProcessor getStateUpdaterProcessor(
			String eventType);

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.ILttngEventProcessor#process(org.eclipse.linuxtools.lttng.event.LttngEvent, org.eclipse.linuxtools.lttng.state.model.LttngTraceState)
	 */
	@Override
	public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
		if (trcEvent instanceof LttngSyntheticEvent) {

			// prepare to dispatch synthetic events to its corresponding handler
			LttngSyntheticEvent synEvent = (LttngSyntheticEvent) trcEvent;
			ILttngEventProcessor processor = null;
			String eventType = synEvent.getMarkerName();

			switch (synEvent.getSynType()) {
				case STARTREQ: {
					// Status indicators do not contain a valid marker name
					reset();
					return false;
				}
	
				case BEFORE: {
					processor = getBeforeProcessor(eventType);
					// increment event count only for one sequence indicator,
					// Note: BEFORE is selected to be used as an indicator to
					// prevent duplicated updates in the state system
					incrementBeforeEventCount();
					break;
				}
	
				case UPDATE: {
					processor = getStateUpdaterProcessor(eventType);
					incrementStateUpdateCount();
					break;
				}
	
				case AFTER: {
					processor = getAfterProcessor(eventType);
					break;
				}
				
				case ENDREQ: {
					processor = getfinishProcessor();
					TraceDebug.debug("EndRequest satus received:"); //$NON-NLS-1$
					break;
				}
					
				default:
					// Nothing to do
					break;

			}
			
			// For BEFORE/UPDATE/AFTER
			// TODO: Implement filter of events not associated to this trace
			// Make sure the event received is associated to this trace
			// handling context, Implementing a trace compare for each event
			// is not acceptable due to performance, and a reference check
			// may not be feasible since there are trace clones used either
			// to build the state system check points or UI requests.

			// if (traceSt != null && trcEvent.getParentTrace() !=
			// traceSt.getContext().getTraceIdRef()) {
			// // increment the number of events filtered out
			// filteredOutEventsCount++;
			// return false;
			// }

			if (processor != null) {
				processor.process(trcEvent, traceSt);
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.evProcessor.IBaseEventProcessor#process
	 * (org.eclipse.linuxtools.tmf.event.TmfEvent,
	 * org.eclipse.linuxtools.lttng.state.model.LttngTraceState)
	 */
	@Override
	public void process(TmfEvent tmfEvent, LttngTraceState traceSt) {
		if (tmfEvent == null) {
			return;
		}

		if (!(tmfEvent instanceof LttngSyntheticEvent)) {
			TraceDebug
					.debug("The event received is not an instance of LttngSyntheticEvent and can not be processed"); //$NON-NLS-1$
			return;
		}

		LttngSyntheticEvent trcEvent = (LttngSyntheticEvent) tmfEvent;

		process(trcEvent, traceSt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.IBaseEventProcessor#
	 * getEventsNotHandled()
	 */
	public Set<String> getEventsNotHandled() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.ITransEventProcessor#
	 * getEventCount()
	 */
	@Override
	public Long getBeforeEventCount() {
		return fbeforeEventCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.ITransEventProcessor#
	 * getStateUpdateCount()
	 */
	@Override
	public Long getStateUpdateCount() {
		return fstateUpdateCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.ITransEventProcessor#
	 * getFilteredOutEventCount()
	 */
	@Override
	public Long getFilteredOutEventCount() {
		return filteredOutEventsCount;
	}

	/**
	 * <p>
	 * Initialise counter values, e.g before new requests
	 * </p>
	 */
	protected void reset() {
		fbeforeEventCount = 0L;
		fstateUpdateCount = 0L;
		filteredOutEventsCount = 0L;
	}

	/**
	 * Multi-threading not expected
	 */
	protected void incrementBeforeEventCount() {
		fbeforeEventCount++;
	}

	/**
	 * Multi-threading not expected
	 */
	protected void incrementStateUpdateCount() {
		fstateUpdateCount++;
	}
}
