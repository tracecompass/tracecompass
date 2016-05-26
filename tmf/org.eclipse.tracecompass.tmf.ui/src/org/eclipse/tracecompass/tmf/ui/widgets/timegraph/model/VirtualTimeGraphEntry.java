/*******************************************************************************
 * Copyright (c) 2017 Ericsson.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.Objects;

/**
 * Child class for {@link TimeGraphEntry} which holds an object describing the
 * resolution at which the last zoom on this entry was done.
 *
 * @author Loic Prieur-Drevon
 * @since 3.1
 */
public class VirtualTimeGraphEntry extends TimeGraphEntry {

    /**
     * Class to describe the sampling parameters for zooms on time graph views.
     *
     * @author Loic Prieur-Drevon
     * @since 3.1
     */
    public static final class Sampling {
        private final long fZoomStart;
        private final long fZoomEnd;
        private final long fResolution;

        /**
         * Constructor for a zoom sampling object
         * @param zoomStart the start time of the zoom
         * @param zoomEnd the end time of the zoom
         * @param resolution the resolution of the zoom
         */
        public Sampling(long zoomStart, long zoomEnd, long resolution) {
            fZoomStart = zoomStart;
            fZoomEnd = zoomEnd;
            fResolution = resolution;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fZoomStart, fZoomEnd, fResolution);
        }

        @Override
        public boolean equals(Object arg0) {
            if (arg0 == this) {
                return true;
            }
            if (arg0 == null) {
                return false;
            }
            if (arg0 instanceof Sampling) {
                Sampling other = (Sampling) arg0;
                return fZoomStart == other.fZoomStart && fZoomEnd == other.fZoomEnd && fResolution == other.fResolution;
            }
            return false;
        }

    }

    private Sampling fSampling;

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
     * Getter for the zoom sampling of this Entry
     *
     * @return the zoom parameters of the current zoom event sampling.
     */
    public Sampling getSampling() {
        return fSampling;
    }

    /**
     * Setter for the zoom sampling of this entry.
     *
     * @param sampling
     *            the new sampling parameters for this entry;
     * @return the previous sampling parameters for this entry
     */
    public Sampling setSampling(Sampling sampling) {
        Sampling prev = fSampling;
        fSampling = sampling;
        return prev;
    }

}
