/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statesystem;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.ui.views.statesystem.TmfStateSystemExplorer.AttributeEntry;
import org.eclipse.tracecompass.tmf.ui.views.statesystem.TmfStateSystemExplorer.ModuleEntry;
import org.eclipse.tracecompass.tmf.ui.views.statesystem.TmfStateSystemExplorer.StateSystemEntry;
import org.eclipse.tracecompass.tmf.ui.views.statesystem.TmfStateSystemExplorer.TraceEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;

/**
 * Presentation Provider for the state system time graph view.
 *
 * @author Loic Prieur-Drevon
 */
class StateSystemPresentationProvider extends TimeGraphPresentationProvider {

    /** Number of colors used for State system time events */
    public static final int NUM_COLORS = 18;

    private static final StateItem[] STATE_TABLE = new StateItem[NUM_COLORS + 1];
    private static final float SATURATION = 0.6f;
    private static final float BRIGHTNESS = 0.6f;
    static {
        Arrays.setAll(STATE_TABLE, i -> new StateItem(new RGB(20 * i, SATURATION, BRIGHTNESS)));
        // Set the last one to grey.
        STATE_TABLE[NUM_COLORS] = new StateItem(new RGB(192, 192, 192));
    }

    /**
     * Minimum width of a displayed state below which we will not print any text
     * into it. It corresponds to the average width of 1 char, plus the width of
     * the ellipsis characters.
     */
    private Integer fMinimumBarWidth;

    @Override
    public StateItem[] getStateTable() {
        return STATE_TABLE;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof StateSystemEvent) {
            StateSystemEvent stateSystemEvent = (StateSystemEvent) event;
            Object value = stateSystemEvent.getInterval().getValue();
            if (value != null) {
                return Math.floorMod(value.hashCode() * 20, NUM_COLORS);
            }
            // grey
            return NUM_COLORS;
        } else if (event.getEntry() instanceof AttributeEntry) {
            return TRANSPARENT;
        }
        return INVISIBLE;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof StateSystemEvent) {
            Object object = ((StateSystemEvent) event).getInterval().getValue();
            return object != null ? object.getClass().getSimpleName() : Messages.TypeNull;
        }
        return null;
    }

    @Override
    public String getStateTypeName(ITimeGraphEntry entry) {
        if (entry instanceof TraceEntry) {
            return Messages.TraceEntry_StateTypeName;
        } else if (entry instanceof ModuleEntry) {
            return Messages.ModuleEntry_StateTypeName;
        } else if(entry instanceof StateSystemEntry) {
            return Messages.StateSystemEntry_StateTypeName;
        }
        return Messages.AttributeEntry_StateTypeName;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        Map<String, String> retMap = new LinkedHashMap<>();
        if (event instanceof StateSystemEvent) {
            StateSystemEvent ssEvent = (StateSystemEvent) event;
            AttributeEntry entry = (AttributeEntry) event.getEntry();

            Object value = ssEvent.getInterval().getValue();
            if (value != null) {
                retMap.put(Messages.ValueColumnLabel, value.toString());
            }

            int quark = ssEvent.getInterval().getAttribute();
            retMap.put(Messages.QuarkColumnLabel, Integer.toString(quark));

            ITmfStateSystem ss = TmfStateSystemExplorer.getStateSystem(entry);
            if (ss != null) {
                retMap.put(Messages.AttributePathColumnLabel, ss.getFullAttributePath(entry.getQuark()));
            }
        } else if (event instanceof TimeEvent) {
            ITimeGraphEntry entry = event.getEntry();
            if (entry instanceof StateSystemEntry) {
                ModuleEntry moduleEntry = (ModuleEntry) entry.getParent();
                ITmfAnalysisModuleWithStateSystems module = moduleEntry.getModule();
                if (module instanceof TmfAbstractAnalysisModule) {
                    retMap.putAll(((TmfAbstractAnalysisModule) module).getProperties());
                }
            } else if (entry instanceof ModuleEntry) {
                ITmfAnalysisModuleWithStateSystems module = ((ModuleEntry) entry).getModule();
                retMap.put(Messages.ModuleHelpText, module.getHelpText());
                retMap.put(Messages.ModuleIsAutomatic, Boolean.toString(module.isAutomatic()));
            }
        }

        return retMap;
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        if (!(event instanceof StateSystemEvent)) {
            return;
        }

        if (fMinimumBarWidth == null) {
            fMinimumBarWidth = gc.getFontMetrics().getAverageCharWidth() + gc.stringExtent(Utils.ELLIPSIS).x;
        }
        Object value = ((StateSystemEvent) event).getInterval().getValue();
        if (bounds.width <= fMinimumBarWidth || value == null) {
            /*
             * Don't print anything if we cannot at least show one character and
             * ellipses.
             */
            return;
        }

        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
        Utils.drawText(gc, value.toString(), bounds.x, bounds.y, bounds.width, bounds.height, true, true);
    }

}
