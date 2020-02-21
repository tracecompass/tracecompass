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

package org.eclipse.tracecompass.internal.tmf.ui.viewers.timegraph.handlers;

import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;

/**
 * Time graph filter handler, launches filtering
 *
 * @author Matthew Khouzam
 */
public class TimeGraphFilterHandler extends TimeGraphBaseHandler {

    @Override
    public void execute(AbstractTimeGraphView view) {
        view.getTimeEventFilterAction().run();
    }
}
