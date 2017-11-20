/*******************************************************************************
 * Copyright (c) 2013, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.callstack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NamedTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;

/**
 * Presentation provider for the Call Stack view, based on the generic TMF
 * presentation provider.
 *
 * @author Patrick Tasse
 */
public class CallStackPresentationProvider extends TimeGraphPresentationProvider {

    /** Number of colors used for call stack events */
    public static final int NUM_COLORS = 360;

    private static final StateItem[] STATE_TABLE;
    static {
        final float saturation = 0.6f;
        final float brightness = 0.6f;
        STATE_TABLE = new StateItem[NUM_COLORS + 1];
        STATE_TABLE[0] = new StateItem(State.MULTIPLE.rgb, State.MULTIPLE.toString());
        for (int i = 0; i < NUM_COLORS; i++) {
            RGB rgb = new RGB(i, saturation, brightness);
            STATE_TABLE[i + 1] = new StateItem(rgb, State.EXEC.toString());
        }
    }

    /**
     * Minimum width of a displayed state below which we will not print any text
     * into it. It corresponds to the average width of 1 char, plus the width of
     * the ellipsis characters.
     */
    private Integer fMinimumBarWidth;

    private enum State {
        MULTIPLE (new RGB(100, 100, 100)),
        EXEC     (new RGB(0, 200, 0));

        private final RGB rgb;

        private State (RGB rgb) {
            this.rgb = rgb;
        }
    }

    /**
     * Constructor
     *
     * @since 1.2
     */
    public CallStackPresentationProvider() {
    }

    /**
     * Sets the call stack view
     *
     * @param view
     *            The call stack view that will contain the time events
     * @since 1.2
     * @deprecated {@link CallStackPresentationProvider} no longer needs the
     *             reference to the {@link CallStackView}
     */
    @Deprecated
    public void setCallStackView(CallStackView view) {
    }

    @Override
    public String getStateTypeName(ITimeGraphEntry entry) {
        return Messages.CallStackPresentationProvider_Thread;
    }

    @Override
    public StateItem[] getStateTable() {
        return STATE_TABLE;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof NamedTimeEvent) {
            NamedTimeEvent callStackEvent = (NamedTimeEvent) event;
            return callStackEvent.getValue() + 1;
        } else if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return State.MULTIPLE.ordinal();
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof NamedTimeEvent) {
            return ((NamedTimeEvent) event).getLabel();
        }
        return State.MULTIPLE.toString();
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        if (!(event instanceof NamedTimeEvent)) {
            return;
        }

        if (fMinimumBarWidth == null) {
            fMinimumBarWidth = gc.getFontMetrics().getAverageCharWidth() + gc.stringExtent(Utils.ELLIPSIS).x;
        }
        if (bounds.width <= fMinimumBarWidth) {
            /*
             * Don't print anything if we cannot at least show one character and
             * ellipses.
             */
            return;
        }

        String label = ((NamedTimeEvent) event).getLabel();
        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
        Utils.drawText(gc, label, bounds.x, bounds.y, bounds.width, bounds.height, true, true);
    }

}
