/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
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
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
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

    private CallStackView fView;

    private Integer fAverageCharWidth;

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
     * @since 2.0
     */
    public CallStackPresentationProvider() {
    }

    /**
     * Sets the call stack view
     *
     * @param view
     *            The call stack view that will contain the time events
     * @since 2.0
     */
    public void setCallStackView(CallStackView view) {
        fView = view;
    }

    @Override
    public String getStateTypeName(ITimeGraphEntry entry) {
        return Messages.CallStackPresentationProvider_Thread;
    }

    @Override
    public StateItem[] getStateTable() {
        final float saturation = 0.6f;
        final float brightness = 0.6f;
        StateItem[] stateTable = new StateItem[NUM_COLORS + 1];
        stateTable[0] = new StateItem(State.MULTIPLE.rgb, State.MULTIPLE.toString());
        for (int i = 0; i < NUM_COLORS; i++) {
            RGB rgb = new RGB(i, saturation, brightness);
            stateTable[i + 1] = new StateItem(rgb, State.EXEC.toString());
        }
        return stateTable;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof CallStackEvent) {
            CallStackEvent callStackEvent = (CallStackEvent) event;
            return callStackEvent.getValue() + 1;
        } else if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return State.MULTIPLE.ordinal();
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof CallStackEvent) {
            CallStackEntry entry = (CallStackEntry) event.getEntry();
            ITmfStateSystem ss = entry.getStateSystem();
            try {
                ITmfStateValue value = ss.querySingleState(event.getTime(), entry.getQuark()).getStateValue();
                if (!value.isNull()) {
                    return fView.getFunctionName(entry.getTrace(), entry.getProcessId(), event.getTime(), value);
                }
            } catch (AttributeNotFoundException e) {
                Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
            } catch (TimeRangeException e) {
                Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
            } catch (StateSystemDisposedException e) {
                /* Ignored */
            }
            return null;
        }
        return State.MULTIPLE.toString();
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        if (fAverageCharWidth == null) {
            fAverageCharWidth = gc.getFontMetrics().getAverageCharWidth();
        }
        if (bounds.width <= fAverageCharWidth) {
            return;
        }
        if (!(event instanceof CallStackEvent)) {
            return;
        }
        CallStackEntry entry = (CallStackEntry) event.getEntry();
        ITmfStateSystem ss = entry.getStateSystem();
        try {
            ITmfStateValue value = ss.querySingleState(event.getTime(), entry.getQuark()).getStateValue();
            if (!value.isNull()) {
                String name = fView.getFunctionName(entry.getTrace(), entry.getProcessId(), event.getTime(), value);
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
                Utils.drawText(gc, name, bounds.x, bounds.y, bounds.width, bounds.height, true, true);
            }
        } catch (AttributeNotFoundException e) {
            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
        } catch (TimeRangeException e) {
            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
    }

}
