/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Loïc Prieur-Drevon - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;

/**
 * An abstract time graph view where only the visible elements are queried. This
 * largely reduces the amount of processing to do on views with large numbers of
 * entries.
 *
 * @deprecated Please use {@link AbstractTimeGraphView} directly
 *
 * @since 3.1
 * @author Loïc Prieur-Drevon
 */
@Deprecated
public abstract class AbstractVirtualTimeGraphView extends AbstractTimeGraphView {

    /**
     * Constructor
     *
     * @param id
     *            The id of the view
     * @param pres
     *            The presentation provider
     */
    public AbstractVirtualTimeGraphView(String id, TimeGraphPresentationProvider pres) {
        super(id, pres);
    }

    /**
     * Inner class for the zoom thread
     *
     * @deprecated Please use {@link AbstractTimeGraphView.ZoomThread}
     */
    @Deprecated
    public class ZoomThreadVisible extends ZoomThread {

        /**
         * Constructor
         *
         * @param startTime
         *            the start time
         * @param endTime
         *            the end time
         * @param resolution
         *            the resolution
         */
        public ZoomThreadVisible(long startTime, long endTime, long resolution) {
            super(startTime, endTime, resolution);
        }

        @Override
        public void doRun() {
        }
    }
}
