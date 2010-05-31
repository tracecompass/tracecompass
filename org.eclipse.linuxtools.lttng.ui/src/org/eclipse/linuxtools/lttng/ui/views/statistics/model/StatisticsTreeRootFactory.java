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
package org.eclipse.linuxtools.lttng.ui.views.statistics.model;

import java.util.HashMap;
import java.util.Map;

public class StatisticsTreeRootFactory {
	// ========================================================================
	// Data
	// =======================================================================

	private static final Map<String, StatisticsTreeNode> rootInstances = new HashMap<String, StatisticsTreeNode>();
	
	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * Provide a statisticsTree instance per trace
	 * 
	 * @return
	 */
	public static StatisticsTreeNode getStatTreeRoot(String traceUniqueId) {
		if (traceUniqueId == null) {
			return null;
		}

		if (rootInstances.containsKey(traceUniqueId)) {
			return rootInstances.get(traceUniqueId);
		}
		
		StatisticsTreeNode tree = new StatisticsTreeNode(traceUniqueId);

		rootInstances.put(traceUniqueId, tree);
		
		return tree;
	}

	/**
	 * @param traceUniqueId
	 * @return
	 */
	public static boolean containsTreeRoot(String traceUniqueId) {
		return rootInstances.containsKey(traceUniqueId);
	}

	/**
	 * Remove previously registered statistics tree.
	 * @param traceUniqueId
	 */
	public static void removeStatTreeRoot(String traceUniqueId) {
		if (traceUniqueId != null && rootInstances.containsKey(traceUniqueId)) {
			rootInstances.remove(traceUniqueId);
		}
	}

	/**
	 * Remove all tree root instances
	 */
	public static void removeAll() {
		rootInstances.clear();
	}
}
