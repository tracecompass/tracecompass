/*******************************************************************************
* Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.views.handler;

import org.eclipse.tracecompass.tmf.ui.views.ITmfFilterableControl;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Time graph filter handler, launches filtering
 *
 * @author Matthew Khouzam
 */
public class TmfViewFilterHandler extends TmfViewBaseHandler {

    @Override
    public void execute(TmfView view) {
        if (view instanceof ITmfFilterableControl) {
            ((ITmfFilterableControl) view).getFilterAction().run();
        }
    }
}
