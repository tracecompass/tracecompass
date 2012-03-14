/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Matthew Khouzam - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.views.controlflow.model;

import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeViewerProvider;
import org.eclipse.linuxtools.internal.lttng.ui.views.common.ParamsUpdater;

public class FlowTimeRangeViewerProvider extends TimeRangeViewerProvider {

	public FlowTimeRangeViewerProvider(ParamsUpdater paramsUpdater) {
		super(paramsUpdater);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeAnalysisProvider
	 * #getStateName(org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.
	 * TmfTimeAnalysisProvider.StateColor)
	 */
	@Override
	public String getStateName(StateColor color) {
		// Override to multiple instances of the widget, the same color can have
		// multiple meanings
		boolean isInProcess = procStateToColor.containsValue(color);
		
		if (isInProcess) {
			return findObject(color, procStateToColor);
		}
		return super.getStateName(color);
	}

	

}
