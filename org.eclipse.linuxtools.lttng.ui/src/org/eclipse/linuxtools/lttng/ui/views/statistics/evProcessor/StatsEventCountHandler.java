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

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.StateStrings.Events;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsTreeNode;

class StatsEventCountHandler extends AbstractStatsEventHandler {
	
	public StatsEventCountHandler(Events eventType) {
		super(eventType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing#process(org.eclipse.linuxtools.lttng.event.LttngEvent, org.eclipse.linuxtools.lttng.state.model.LttngTraceState)
	 */
	public boolean process(LttngEvent event, LttngTraceState traceState) {		
		StatisticsTreeNode root = getStatisticsTree(event);
		
		String[][] paths = getRelevantPaths(event, traceState);
		
		for (String[] path : paths) {
			StatisticsTreeNode node = root.getOrCreateChildFromPath(path); 
			
			increaseNbEvents(node);
		}
		
		String[][] eventTypesPaths = getRelevantEventTypesPaths(event, traceState);
		
		for (String[] path : eventTypesPaths) {
			StatisticsTreeNode node = root.getOrCreateChildFromPath(path); 
			
			increaseNbEvents(node);
		}
		
		return false;
	}

}