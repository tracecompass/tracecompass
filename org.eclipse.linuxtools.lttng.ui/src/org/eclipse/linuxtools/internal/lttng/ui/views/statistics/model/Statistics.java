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

public class Statistics {
	/**
	 * <h4>Number of event</h4>
	 */
	public long nbEvents = 0;
	/**
	 * <h4>CPU time</h4>
	 * <p>Many events are excluded of the CPU time:
	 * <ul>
	 * 		<li>All events in MODE_UNKNOWN</li>
	 * 		<li>All events before a sched_schedule on a given CPU</li>
	 * 		<li>All events in a process after the process_exit</li>
	 * 		<li>Maybe some others</li>
	 * </ul>
	 */
	public long cpuTime = 0;
	/**
	 * <h4>Cumulative CPU time</h4>
	 * <p>Currently broken.</p>
	 */
	public long cumulativeCpuTime = 0;
	/**
	 * <h4>Elapsed time</h4>
	 * <p>Result validity in eclipse unknown.</p>
	 */
	public long elapsedTime = 0;
}
