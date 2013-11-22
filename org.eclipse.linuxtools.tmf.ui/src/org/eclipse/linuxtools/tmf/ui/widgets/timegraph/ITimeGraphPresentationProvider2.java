/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Add drawing helper methods
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.ITmfTimeGraphDrawingHelper;

/**
 * Extension of the ITimeGraphPresentationProvider interface to avoid API breakage
 *
 * @author Geneviève Bastien
 * @since 2.1
 * TODO: Add me to ITimeGraphPresentationProvider before the 3.0 release
 */
public interface ITimeGraphPresentationProvider2 extends ITimeGraphPresentationProvider {

    /**
     * Returns the drawing helper for this presentation provider.
     *
     * @return The drawing helper
     */
    ITmfTimeGraphDrawingHelper getDrawingHelper();

    /**
     * Sets this presentation provider's drawing helper.
     * This helper be needed to know where to draw items, get its coordinates
     * given a time, etc.
     *
     * @param helper
     *            The drawing helper
     */
    void setDrawingHelper(ITmfTimeGraphDrawingHelper helper);

    /**
     * Adds a color settings listener, to be notified when the presentation
     * provider's state colors change.
     *
     * @param listener
     *            The new listener for color settings changes
     */
    public void addColorListener(ITimeGraphColorListener listener);

    /**
     * Removes a color settings listener.
     *
     * @param listener
     *            The color settings listener to remove
     */
    public void removeColorListener(ITimeGraphColorListener listener);

}