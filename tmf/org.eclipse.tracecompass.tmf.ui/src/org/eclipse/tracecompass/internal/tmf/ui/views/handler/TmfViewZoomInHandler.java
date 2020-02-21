/*******************************************************************************
* Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
