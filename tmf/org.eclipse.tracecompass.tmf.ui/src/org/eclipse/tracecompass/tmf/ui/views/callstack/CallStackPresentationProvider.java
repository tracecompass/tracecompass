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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.RotatingPaletteProvider;
import org.eclipse.tracecompass.internal.tmf.core.callstack.provider.CallStackEntryModel;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.colors.RGBAUtil;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NamedTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
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
        STATE_TABLE = new StateItem[NUM_COLORS + 1];
        STATE_TABLE[0] = new StateItem(State.MULTIPLE.rgb, State.MULTIPLE.toString());
    }

    /**
     * Minimum width of a displayed state below which we will not print any text
     * into it. It corresponds to the average width of 1 char, plus the width of
     * the ellipsis characters.
     */
    private Integer fMinimumBarWidth;
    private @NonNull IPaletteProvider fPalette = new RotatingPaletteProvider.Builder().setNbColors(NUM_COLORS).build();

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
        // Do nothing
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
        // Do nothing
    }

    @Override
    public String getStateTypeName(ITimeGraphEntry entry) {
        if (entry instanceof TimeGraphEntry) {
            ITimeGraphEntryModel model = ((TimeGraphEntry) entry).getModel();
            if (model instanceof CallStackEntryModel) {
                int type = ((CallStackEntryModel) model).getStackLevel();
                if (type >= 0) {
                    return Messages.CallStackPresentationProvider_Thread;
                } else if (type == -1) {
                    return Messages.CallStackPresentationProvider_Process;
                }
            }
        }
        return null;
    }

    @Override
    public StateItem[] getStateTable() {
        if (STATE_TABLE[1] == null) {
            int i = 1;
            String exec = State.EXEC.toString();
            for (RGBAColor color : fPalette.get()) {
                STATE_TABLE[i] = new StateItem(RGBAUtil.fromRGBAColor(color).rgb, exec);
                i++;
            }
        }
        return STATE_TABLE;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof NamedTimeEvent) {
            NamedTimeEvent callStackEvent = (NamedTimeEvent) event;
            return Math.floorMod(callStackEvent.getValue(), fPalette.get().size()) + 1;
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
