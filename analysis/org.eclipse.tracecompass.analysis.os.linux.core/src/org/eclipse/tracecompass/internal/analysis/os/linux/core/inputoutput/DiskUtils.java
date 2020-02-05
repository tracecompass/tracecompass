/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput;

import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.Disk;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

/**
 * Utility methods common to all disk data providers
 *
 * @author Geneviève Bastien
 */
public final class DiskUtils {

    private DiskUtils() {
        // Nothing to do
    }

    /**
     * Get the name of the disk from the state system
     *
     * @param ss
     *            The state system
     * @param diskQuark
     *            The base quark of the disk
     * @return The name of the disk
     */
    public static String getDiskName(ITmfStateSystem ss, Integer diskQuark) {
        ITmfStateInterval interval = StateSystemUtils.queryUntilNonNullValue(ss, diskQuark, ss.getStartTime(), ss.getCurrentEndTime());
        if (interval != null) {
            return String.valueOf(interval.getValue());
        }
        int devNum = Integer.parseInt(ss.getAttributeName(diskQuark));
        return Disk.extractDeviceIdString(devNum);
    }

}
