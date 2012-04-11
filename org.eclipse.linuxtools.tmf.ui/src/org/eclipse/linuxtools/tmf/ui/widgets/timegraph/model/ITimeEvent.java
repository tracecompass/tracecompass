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

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model;


public interface ITimeEvent {

	public ITmfTimeAnalysisEntry getEntry();

	public long getTime();

	/**
	 * @return
	 * <list>
	 * <li>-1: Considers duration to be from current event till the next</li>
	 * <li>0: Duration is not relevant e.g. a Burst / no state associated</li>
	 * <li>>0: Valid duration value specified</li>
	 * </list>
	 * <p>
	 */
	public long getDuration();

}