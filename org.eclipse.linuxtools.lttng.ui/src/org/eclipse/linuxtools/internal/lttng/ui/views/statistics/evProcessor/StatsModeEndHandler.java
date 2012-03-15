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
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model.StatisticsData;

class StatsModeEndHandler extends AbstractStatsEventHandler {
	
	public StatsModeEndHandler(Events eventType) {
		super(eventType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing#process(org.eclipse.linuxtools.lttng.event.LttngEvent, org.eclipse.linuxtools.lttng.state.model.LttngTraceState)
	 */
	@Override
	public boolean process(LttngEvent event, LttngTraceState traceState) {
		StatisticsData tree = getStatisticsTree(traceState);
		tree.increase(event, traceState, StatisticsData.Values.CPU_TIME | StatisticsData.Values.ELAPSED_TIME | StatisticsData.Values.CUMULATIVE_CPU_TIME);
		return false;
	}

}