/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density;

import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.swtchart.Chart;

/**
 * Base class for any mouse provider such as tool tip, zoom and selection providers.
 *
 * @author Bernd Hufmann
 * @author Marc-Andre Laperle
 */
public abstract class BaseMouseProvider {

    private AbstractSegmentStoreDensityViewer fDensityViewer;

    /**
     * Standard constructor.
     *
     * @param densityViewer
     *            The parent density viewer
     */
    public BaseMouseProvider(AbstractSegmentStoreDensityViewer densityViewer) {
        fDensityViewer = densityViewer;
    }

    /**
     * Method to register the provider to the viewer.
     */
    protected abstract void register();

    /**
     * Method to deregister the provider from the viewer.
     */
    protected abstract void deregister();

    /**
     * @return the density viewer
     */
    public AbstractSegmentStoreDensityViewer getDensityViewer() {
        return fDensityViewer;
    }

    /**
     * Returns the SWT chart reference
     *
     * @return SWT chart reference.
     */
    protected Chart getChart() {
        return fDensityViewer.getControl();
    }

}