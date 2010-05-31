/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model;

import java.util.Vector;

public interface ITmfTimeAnalysisEntry {
	
    public String getGroupName();

	public int getId();

	public String getName();

	public long getStartTime();

	public long getStopTime();

	public <T extends ITimeEvent>  Vector<T> getTraceEvents();
}
