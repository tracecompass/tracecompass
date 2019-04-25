/*******************************************************************************
* Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.views.handler;

import org.eclipse.tracecompass.internal.tmf.ui.views.ITmfTimeZoomProvider;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Zoom-in handler for TMF views.
 *
 * @author Matthew Khouzam
 * @author Bernd Hufmann
 */
public class TmfViewZoomInHandler extends TmfViewBaseHandler {

    @Override
    public void execute(TmfView view) {
        ITmfTimeZoomProvider zoomer = view.getAdapter(ITmfTimeZoomProvider.class);
        if (zoomer != null) {
            zoomer.zoom(true, true);
        }
    }
}
