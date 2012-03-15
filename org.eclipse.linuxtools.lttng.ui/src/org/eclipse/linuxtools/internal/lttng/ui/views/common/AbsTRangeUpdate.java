/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Alvaro Sanchez-Leon - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.common;

import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.state.AbsStateProcessing;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

public abstract class AbsTRangeUpdate extends AbsStateProcessing implements
		ILttngEventProcessor {

    private static final long MINORBITS = 20;
    
	// ========================================================================
	// General methods
	// =======================================================================
    
    /**
     * Get the mkdev node id<br>
     * <br>
     * This is an implementation of a KERNEL macro used in Lttv
     * 
     */
    public long getMkdevId(long major, long minor) {
        return (((major) << MINORBITS) | (minor));
    }

	/**
	 * Get the pixels per Nano second, either from active widgets or initialise
	 * with the experiment time range values
	 * 
	 * @param traceSt
	 * @param params
	 * 
	 * @return double
	 */
    protected double getPixelsPerNs(LttngTraceState traceSt, ParamsUpdater params) {
        double pixPerNs = params.getPixelsPerNs();
        if (pixPerNs == 0.0) {
            TmfTimeRange tsetRange = traceSt.getContext().getExperimentTimeWindow();
            
            long startTime = tsetRange.getStartTime().getValue();
            long endTime = tsetRange.getEndTime().getValue();
            long delta = endTime - startTime;
            
            if (delta > 0) {
                pixPerNs = (double) params.getWidth() / (double) delta;
                params.setPixelsPerNs(pixPerNs);
            }
        }
        return pixPerNs;
	}

}