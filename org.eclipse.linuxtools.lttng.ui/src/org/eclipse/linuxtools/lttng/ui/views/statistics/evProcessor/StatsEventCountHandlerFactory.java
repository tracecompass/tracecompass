/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Yann N. Dauphin     (dhaemon@gmail.com)  - Implementation for stats
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.statistics.evProcessor;

import org.eclipse.linuxtools.lttng.state.evProcessor.AbsEventProcessorFactory;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;

/**
 * Provide the handlers that will count the number of events and update the
 * appropriate tree.
 * 
 * Builds a Map from string event name to a processing handler object, the
 * processors implement the same interface to facilitate transparent methods
 * call,
 * 
 * The map key STring is the entry point of the raw events, using a hash speeds
 * up the resolution of the appropriate processor
 * 
 * @author yann
 * 
 */
public class StatsEventCountHandlerFactory extends AbsEventProcessorFactory{
	// ========================================================================
	// Data
	// ========================================================================
	private static StatsEventCountHandlerFactory instance = null;

	private final IEventProcessing handler = new StatsEventCountHandler(null);
	
	// ========================================================================
	// Public methods
	// =======================================================================
	/**
	 * The event processors are common to all traces an multiple instances will
	 * use more memory unnecessarily
	 * 
	 * @return
	 */
	public static AbsEventProcessorFactory getInstance() {
		if (instance == null) {
			instance = new StatsEventCountHandlerFactory();
		}
		return instance;
	}


	@Override
	public IEventProcessing getAfterProcessor(String eventType) {
		return handler;
	}

	@Override
	public IEventProcessing getBeforeProcessor(String eventType) {
		return null;
	}

	@Override
	public IEventProcessing getfinishProcessor() {
		// No finishing processor used
		return null;
	}
}
