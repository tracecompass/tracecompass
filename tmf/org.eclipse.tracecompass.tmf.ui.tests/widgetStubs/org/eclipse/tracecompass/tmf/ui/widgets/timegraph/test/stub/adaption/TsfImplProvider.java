/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.test.stub.adaption;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.test.stub.model.EventImpl;

/**
 * Time Graph Presentation Provider Stub.
 */
public class TsfImplProvider extends TimeGraphPresentationProvider {

    // ========================================================================
    // Methods
    // ========================================================================
    @Override
    public int getStateTableIndex(ITimeEvent event) {
        return 0;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent revent) {
        Map<String, String> toolTipEventMsgs = new HashMap<>();
        if (revent instanceof EventImpl) {
            toolTipEventMsgs.put("Test Tip1", "Test Value tip1");
            toolTipEventMsgs.put("Test Tip2", "Test Value tip2");
        }

        return toolTipEventMsgs;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        String name = "Unknown";
        if (event instanceof EventImpl) {
            EventImpl devent = (EventImpl) event;
            name = devent.getType().toString();
        }
        return name;
    }
}
