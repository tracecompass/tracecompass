/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.uml2sd;

import org.eclipse.tracecompass.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.drawings.IColor;

/**
 * <p>
 * Listener interface for the time compression bar to notify about dela selection.
 * </p>
 *
 * @author sveyrier
 * @version 1.0
 */
public interface ITimeCompressionListener {

    /**
     * Notifies listeners about a selected delta.
     *
     * @param lifeline
     *            The current lifeline.
     * @param startEvent
     *            The start event selected.
     * @param nbEvent
     *            A number of events.
     * @param color
     *            The current color to use.
     */
    void deltaSelected(Lifeline lifeline, int startEvent, int nbEvent, IColor color);

}
