/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Godin (copelnug@gmail.com)  - Initial design and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.views.statistics.evProcessor;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Events;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model.StatisticsData;

/**
 * <h4>Handler for process exit event statistics</h4>
 */
public class StatsProcessExitHandler extends AbstractStatsEventHandler {
	/**
	 * <h4>Constructor</h4>
	 * <p>
	 * Define the LTT_EVENT_PROCESS_EXIT type.
	 * </p>
	 */
	public StatsProcessExitHandler() {
		super(Events.LTT_EVENT_PROCESS_EXIT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.ILttngEventProcessor#process(org.eclipse.linuxtools.lttng.event.LttngEvent, org.eclipse.linuxtools.lttng.state.model.LttngTraceState)
	 */
	@Override
	public boolean process(LttngEvent event, LttngTraceState traceState) {
		StatisticsData tree = getStatisticsTree(traceState);
		tree.process_exit(event, traceState);

		return false;
	}
}
