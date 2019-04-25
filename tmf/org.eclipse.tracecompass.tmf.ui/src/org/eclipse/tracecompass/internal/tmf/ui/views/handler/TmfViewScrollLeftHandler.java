/*******************************************************************************
* Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.views.handler;

import org.eclipse.tracecompass.internal.tmf.ui.views.ITmfTimeNavigationProvider;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * TMF view left scroll handler.
 *
 * @author Matthew Khouzam
 * @author Bernd Hufmann
 */
public class TmfViewScrollLeftHandler extends TmfViewBaseHandler {

    @Override
    public void execute(TmfView view) {
        ITmfTimeNavigationProvider navigator = view.getAdapter(ITmfTimeNavigationProvider.class);
        if (navigator != null) {
            navigator.horizontalScroll(true);
        }
    }
}
