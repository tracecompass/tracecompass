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
package org.eclipse.linuxtools.lttng.ui.views.resources.evProcessor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng.state.StateStrings;
import org.eclipse.linuxtools.lttng.state.evProcessor.AbsEventProcessorFactory;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;

/**
 * Builds a Map from string event name to a processing handler object, the
 * processors implement the same interface to facilitate transparent methods
 * call,
 * 
 * The map key String is the entry point of the raw events. Using a hash speeds
 * up the resolution of the appropriate processor
 * 
 * @author alvaro
 * 
 */
public class ResourcesTRangeUpdateFactory extends AbsEventProcessorFactory {
    // ========================================================================
    // Data
    // =======================================================================
    private final Map<String, IEventProcessing> eventNametoBeforeProcessor = new HashMap<String, IEventProcessing>();
    private final Map<String, IEventProcessing> eventNametoAfterProcessor = new HashMap<String, IEventProcessing>();
	private ResourcesTRangeFinishUpdateHandler finishProcessor = null;
    private static ResourcesTRangeUpdateFactory instance = null;
    private ResourcesTRangeBeforeUpdateHandlers instantiateBeforeHandler = new ResourcesTRangeBeforeUpdateHandlers();
    private ResourcesTRangeAfterUpdateHandlers instantiateAfterHandler = new ResourcesTRangeAfterUpdateHandlers();
    
    
	private ResourcesTRangeUpdateFactory() {
        // Create one instance of each individual event handler and add the instance to the map

        // *** BEFORE HOOKS ***
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE.getInName(),
				instantiateBeforeHandler.getBeforeSchedChangeHandler());
        
        eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_ENTRY
				.getInName(), instantiateBeforeHandler
				.getBeforeExecutionModeTrap());
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_EXIT
				.getInName(), instantiateBeforeHandler
				.getBeforeExecutionModeTrap());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PAGE_FAULT_ENTRY.getInName(),
				instantiateBeforeHandler.getBeforeExecutionModeTrap());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PAGE_FAULT_EXIT.getInName(),
				instantiateBeforeHandler.getBeforeExecutionModeTrap());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY
						.getInName(), instantiateBeforeHandler
						.getBeforeExecutionModeTrap());
		eventNametoBeforeProcessor
				.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_EXIT
						.getInName(), instantiateBeforeHandler
						.getBeforeExecutionModeTrap());
        
        eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_ENTRY
				.getInName(), instantiateBeforeHandler
				.getBeforeExecutionModeIrq());
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_EXIT
				.getInName(), instantiateBeforeHandler
				.getBeforeExecutionModeIrq());
        
        eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SOFT_IRQ_RAISE.getInName(),
				instantiateBeforeHandler.getBeforeExecutionModeSoftIrq());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SOFT_IRQ_ENTRY.getInName(),
				instantiateBeforeHandler.getBeforeExecutionModeSoftIrq());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SOFT_IRQ_EXIT.getInName(),
				instantiateBeforeHandler.getBeforeExecutionModeSoftIrq());
        
        eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_REQUEST_ISSUE.getInName(),
				instantiateBeforeHandler.getBeforeBdevEvent());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_REQUEST_COMPLETE.getInName(),
				instantiateBeforeHandler.getBeforeBdevEvent());
        
        
        
        // *** AFTER HOOKS ***
		eventNametoAfterProcessor.put(
				StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE.getInName(),
				instantiateAfterHandler.getAfterSchedChangeHandler());

		finishProcessor = new ResourcesTRangeFinishUpdateHandler();
    }
    
    
    /**
     * 
     */
	public static AbsEventProcessorFactory getInstance() {
        if (instance == null) {
			instance = new ResourcesTRangeUpdateFactory();
        }
        return instance;
    }
    
    @Override
    public IEventProcessing getAfterProcessor(String eventType) {
        return eventNametoAfterProcessor.get(eventType);
    }

    @Override
    public IEventProcessing getBeforeProcessor(String eventType) {
        return eventNametoBeforeProcessor.get(eventType);
    }

	@Override
	public IEventProcessing getfinishProcessor() {
		return finishProcessor;
	}
}
