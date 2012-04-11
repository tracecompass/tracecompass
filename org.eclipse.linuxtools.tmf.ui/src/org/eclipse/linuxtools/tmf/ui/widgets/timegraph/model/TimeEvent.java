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


public abstract class TimeEvent implements ITimeEvent {
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tlf.widgets.timeAnalysis.model.TmTaEventI#getTrace()
	 */
	@Override
	public abstract ITmfTimeAnalysisEntry getEntry();
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tlf.widgets.timeAnalysis.model.TmTaEventI#getTime()
	 */
	@Override
	public abstract long getTime();
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tlf.widgets.timeAnalysis.model.TmTaEventI#getDuration()
	 */
	@Override
	public long getDuration() {
		return -1;
	}
}
