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
package org.eclipse.linuxtools.internal.lttng.ui.views.statistics.evProcessor;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Events;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model.StatisticsData;
import org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model.StatisticsTreeRootFactory;

abstract class AbstractStatsEventHandler implements ILttngEventProcessor {
	private Events fEventType;
	
	public AbstractStatsEventHandler(Events eventType) {
		super();
		fEventType = eventType;
	}

	/**
	 * @return root of of the tree for this experiment.
	 */
	protected StatisticsData getStatisticsTree(LttngTraceState trcState) {
		String experimentName = trcState.getContext().getExperimentName();
		return StatisticsTreeRootFactory.getStatTree(experimentName);
	}
	
	public Events getEventHandleType() {
		return fEventType;
	}
	
	protected void stepCount(LttngEvent event, LttngTraceState traceState) {
		StatisticsData tree = getStatisticsTree(traceState);
		tree.registerEvent(event, traceState);
	}

}