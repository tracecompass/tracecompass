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

package org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model;

import java.util.HashMap;
import java.util.Map;

public class StatisticsTreeRootFactory {
	
	// -----------------------------------------------------------------------
	// Data
	// -----------------------------------------------------------------------

	private static final Map<String, StatisticsData> fTreeInstances = new HashMap<String, StatisticsData>();
	
	// -----------------------------------------------------------------------
	// Methods
	// -----------------------------------------------------------------------

	/**
	 * Provide a statisticsTree instance per trace
	 * 
	 * @return
	 */
	public static StatisticsTreeNode getStatTreeRoot(String traceUniqueId) {
		return getStatTree(traceUniqueId).getOrCreate(StatisticsData.ROOT);
	}
	public static StatisticsData getStatTree(String traceUniqueId) {
		if(traceUniqueId == null)
			return null;
		
		StatisticsData tree = fTreeInstances.get(traceUniqueId);
		if(tree == null) {
			tree = new KernelStatisticsData(traceUniqueId); // NOTE
			fTreeInstances.put(traceUniqueId, tree);
		}
		return tree;
	}
	/**
	 * @param traceUniqueId
	 * @return
	 */
	public static boolean containsTreeRoot(String traceUniqueId) {
		return fTreeInstances.containsKey(traceUniqueId);
	}

	/**
	 * Remove previously registered statistics tree.
	 * @param traceUniqueId
	 */
	public static void removeStatTreeRoot(String traceUniqueId) {
		if (traceUniqueId != null && fTreeInstances.containsKey(traceUniqueId)) {
			fTreeInstances.remove(traceUniqueId);
		}
	}

	/**
	 * Remove all tree and root instances
	 */
	public static void removeAll() {
		fTreeInstances.clear();
	}
}
