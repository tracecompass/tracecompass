/*******************************************************************************
 * Copyright (c) 2017 Ericsson.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.logging.Logger;

import org.eclipse.tracecompass.common.core.log.TraceCompassLog;

/**
 * Child class for {@link TimeGraphEntry} which holds an object describing the
 * resolution at which the last zoom on this entry was done.
 *
 * @deprecated Please use {@link TimeGraphEntry} directly
 *
 * @author Loic Prieur-Drevon
 * @since 3.1
 */
@Deprecated
public class VirtualTimeGraphEntry extends TimeGraphEntry {

    private static final Logger LOGGER = TraceCompassLog.getLogger(VirtualTimeGraphEntry.class);

    /**
     * Class to describe the sampling parameters for zooms on time graph views.
     *
     * @deprecated Please use {@link TimeGraphEntry.Sampling}
     * @author Loic Prieur-Drevon
     * @since 3.1
     */
    @Deprecated
    public static final class Sampling extends TimeGraphEntry.Sampling {

        /**
         * Constructor for a zoom sampling object
         *
         * @param zoomStart
         *            the start time of the zoom
         * @param zoomEnd
         *            the end time of the zoom
         * @param resolution
         *            the resolution of the zoom
         */
        public Sampling(long zoomStart, long zoomEnd, long resolution) {
            super(zoomStart, zoomEnd, resolution);
        }
    }

    /**
     * Constructor for a VirtualTimeGraphEntry
     *
     * @param name
     *            The name of this entry
     * @param startTime
     *            The start time of this entry
     * @param endTime
     *            The end time of this entry
     */
    public VirtualTimeGraphEntry(String name, long startTime, long endTime) {
        super(name, startTime, endTime);
    }

    /**
     * Setter for the zoom sampling of this entry
     *
     * @param sampling
     *            The sampling to set
     * @return The previous sampling
     * @since 3.1
     */
    @Deprecated
    public Sampling setSampling(Sampling sampling) {
        LOGGER.severe("Invalid operation"); //$NON-NLS-1$
        return null;
    }

    /**
     * Getter for the zoom sampling of this entry
     *
     * @return The timegraph entry sampling
     *
     * @deprecated Please use {@link TimeGraphEntry#getSampling()}
     */
    @Override
    @Deprecated
    public Sampling getSampling() {
        LOGGER.severe("Invalid operation"); //$NON-NLS-1$
        return null;
    }
}
