/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.callstack;

import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.callstack.CallStackStateProvider;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Presentation provider for the Call Stack view, based on the generic TMF
 * presentation provider.
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public class CallStackPresentationProvider extends TimeGraphPresentationProvider {

    /** Number of colors used for call stack events */
    public static final int NUM_COLORS = 360;

    private enum State {
        MULTIPLE (new RGB(100, 100, 100)),
        EXEC     (new RGB(0, 200, 0));

        private final RGB rgb;

        private State (RGB rgb) {
            this.rgb = rgb;
        }
    }

    @Override
    public String getStateTypeName() {
        // Empty string since no generic name
        return ""; //$NON-NLS-1$
    }

    @Override
    public String getStateTypeName(ITimeGraphEntry entry) {
        return ""; //$NON-NLS-1$
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
            ITmfStateSystem ss = entry.getTrace().getStateSystems().get(CallStackStateProvider.ID);
            try {
                ITmfStateInterval value = ss.querySingleState(event.getTime(), entry.getQuark());
                if (!value.getStateValue().isNull()) {
                    ITmfStateValue state = value.getStateValue();
                    return state.toString();
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
        if (bounds.width <= gc.getFontMetrics().getAverageCharWidth()) {
            return;
        }
        if (!(event instanceof CallStackEvent)) {
            return;
        }
        CallStackEntry entry = (CallStackEntry) event.getEntry();
        ITmfStateSystem ss = entry.getTrace().getStateSystems().get(CallStackStateProvider.ID);
        try {
            ITmfStateInterval value = ss.querySingleState(event.getTime(), entry.getQuark());
            if (!value.getStateValue().isNull()) {
                ITmfStateValue state = value.getStateValue();
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
                Utils.drawText(gc, state.toString(), bounds.x, bounds.y - 2, bounds.width, true, true);
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
