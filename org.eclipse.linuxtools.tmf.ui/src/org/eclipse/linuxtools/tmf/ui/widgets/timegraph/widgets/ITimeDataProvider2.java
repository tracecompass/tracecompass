/*****************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal, Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Added methods to save a time range selection
 *   Patrick Tasse - Support for range selection
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;


/**
 * Extension of the ITimeDateProvider interface to avoid API breakage
 *
 * @version 1.0
 * @TODO: Move these to the ITimeDateProvider interface when API 3.0 is reached
 * @since 2.1
 */
public interface ITimeDataProvider2 extends ITimeDataProvider {

    /**
     * Updates the selection begin and end time and notifies any registered
     * listeners about the new time range (if necessary)
     *
     * @param beginTime the selection begin time
     * @param endTime the selection end time
     */
    void setSelectionRangeNotify(long beginTime, long endTime);

    /**
     * Updates the selection begin and end time
     *
     * @param beginTime the selection begin time
     * @param endTime the selection end time
     */
    void setSelectionRange(long beginTime, long endTime);

    /**
     * @return The begin time of the current selection
     */
    long getSelectionBegin();

    /**
     * @return The end time of the current selection
     */
    long getSelectionEnd();

}
