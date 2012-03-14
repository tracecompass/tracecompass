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

package org.eclipse.linuxtools.internal.lttng.ui.views.resources.model;

import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeViewerProvider;
import org.eclipse.linuxtools.internal.lttng.ui.views.common.ParamsUpdater;

public class ResourcesTimeRangeViewerProvider extends TimeRangeViewerProvider {

	public ResourcesTimeRangeViewerProvider(ParamsUpdater paramsUpdater) {
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
		boolean isInBlockDevice = bdevStateToColor.containsValue(color);
		boolean isInSoftIrq = softIrqStateToColor.containsValue(color);
		boolean isInTrap = trapStateToColor.containsValue(color);
		boolean isInIrq = irqStateToColor.containsValue(color);
		boolean isInCpu = cpuStateToColor.containsValue(color);
		if (isInCpu) {
			return findObject(color, cpuStateToColor);
		} else if (isInIrq) {
			return findObject(color, irqStateToColor);
		} else if (isInTrap) {
			return findObject(color, trapStateToColor);
		} else if (isInSoftIrq) {
			return findObject(color, softIrqStateToColor);
		} else if (isInBlockDevice) {
			return findObject(color, bdevStateToColor);
		}
		return super.getStateName(color);
	}

}
