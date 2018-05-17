/*******************************************************************************
* Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.timegraph.handlers;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;

/**
 * Time graph right scroll handler.
 *
 * @author Matthew Khouzam
 */
public class TimeGraphRightHandler extends TimeGraphBaseHandler {

    @Override
    public void execute(TimeGraphControl control) {
        control.horizontalScroll(false);
    }

}
