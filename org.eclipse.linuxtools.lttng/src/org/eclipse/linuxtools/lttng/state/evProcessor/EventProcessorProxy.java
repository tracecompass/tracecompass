/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.state.evProcessor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.state.evProcessor.state.StateUpdateFactory;

/**
 * @author alvaro
 * 
 */
public class EventProcessorProxy {
	// ========================================================================
	// Data
	// =======================================================================
	private static EventProcessorProxy instance = null;
	private final Set<AbsEventProcessorFactory> processingFactories = new HashSet<AbsEventProcessorFactory>();

	
	// ========================================================================
	// Constructors
	// =======================================================================
	public EventProcessorProxy() {
		// Manual creation of State update factory
		addEventProcessorFactory(StateUpdateFactory.getInstance());
	}

	// ========================================================================
	// Methods
	// =======================================================================
	/**
	 * @return the processingFactories
	 */
	public Set<AbsEventProcessorFactory> getProcessingFactories() {
		return processingFactories;
	}

	/**
	 * Returns this singleton
	 * 
	 * @return
	 */
	public static EventProcessorProxy getInstance() {
		if (instance == null) {
			instance = new EventProcessorProxy();
		}

		return instance;
	}

	/**
	 * Register a factory of event handler methods, each factory provides a map
	 * to Before and After state update handlers
	 * 
	 * @param handlersFactory
	 */
	public void addEventProcessorFactory(
			AbsEventProcessorFactory handlersFactory) {
		if (handlersFactory != null) {
			//only add the listener if not already included
			if (!processingFactories.contains(handlersFactory)) {
				processingFactories.add(handlersFactory);
			}
		} else {
			TraceDebug
					.debug("An attempt to register a null factory has been detected");
		}
	}

	/**
	 * Remove a factory previously added with addEventProcessorFactory
	 * 
	 * @param handlersFactory
	 */
	public void removeEventProcessorFactory(
			AbsEventProcessorFactory handlersFactory) {
		if (handlersFactory != null) {
			processingFactories.remove(handlersFactory);
		} 
	}

}
