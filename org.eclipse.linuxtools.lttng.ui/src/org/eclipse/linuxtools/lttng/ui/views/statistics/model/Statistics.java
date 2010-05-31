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

public class Statistics {
	public long nbEvents = 0;
	
	public long cpuTime = 0;
	
	public long cumulativeCpuTime = 0;
	
	public long elapsedTime = 0;
}
