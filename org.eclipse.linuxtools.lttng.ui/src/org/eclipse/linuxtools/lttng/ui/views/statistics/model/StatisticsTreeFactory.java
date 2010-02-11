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

public class StatisticsTreeFactory {
	// ========================================================================
	// Data
	// =======================================================================

	private static final Map<String, StatisticsTreeNode> instanceBook = new HashMap<String, StatisticsTreeNode>();
	
	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * Provide a statisticsTree instance per trace
	 * 
	 * @return
	 */
	public static StatisticsTreeNode getStatisticsTree(String traceUniqueId) {
		if (traceUniqueId == null) {
			return null;
		}

		if (instanceBook.containsKey(traceUniqueId)) {
			return instanceBook.get(traceUniqueId);
		}
		
		StatisticsTreeNode tree = new StatisticsTreeNode(traceUniqueId, new Statistics());

		instanceBook.put(traceUniqueId, tree);
		
		return tree;
	}

	/**
	 * Remove previously registered statistics tree.
	 * @param traceUniqueId
	 */
	public static void removeStatisticsTree(String traceUniqueId) {
		if (traceUniqueId != null && instanceBook.containsKey(traceUniqueId)) {
			instanceBook.remove(traceUniqueId);
		}
	}
}
