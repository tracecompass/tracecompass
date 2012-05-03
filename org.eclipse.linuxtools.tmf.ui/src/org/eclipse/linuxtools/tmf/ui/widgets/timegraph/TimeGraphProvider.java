/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *   Patrick Tasse - Refactoring
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import java.util.Map;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public abstract class TimeGraphProvider implements ITimeGraphProvider {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    public enum StateColor {
        GREEN, DARK_BLUE, RED, GOLD, ORANGE, GRAY, BLACK, DARK_GREEN, DARK_YELLOW, MAGENTA3, PURPLE1, PINK1, AQUAMARINE, LIGHT_BLUE, CADET_BLUE, OLIVE;

        private String stateName;

        StateColor() {
            String undef = "Undefined"; //$NON-NLS-1$
            this.stateName = undef;
        }

        public String getStateName() {
            return stateName;
        }

        public void setStateName(String stateName) {
            this.stateName = stateName;
        }
    }

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    protected TimeGraphControl fTimeGraphControl;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public void setTimeGraphControl(TimeGraphControl timeGraphControl) {
        fTimeGraphControl = timeGraphControl;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#drawItems(org.eclipse.swt.graphics.Rectangle, org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider, org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider.Item[], int, int, org.eclipse.swt.graphics.GC)
     */
    public void drawItems(Rectangle bounds, ITimeDataProvider timeProvider, TimeGraphItem[] items, int topIndex, int nameSpace, GC gc) {
        fTimeGraphControl.drawItems(bounds, timeProvider, items, topIndex, nameSpace, gc);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#drawItem(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider.Item, org.eclipse.swt.graphics.Rectangle, org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider, int, int, org.eclipse.swt.graphics.GC)
     */
    @Override
    public void drawItem(TimeGraphItem item, Rectangle bounds, ITimeDataProvider timeProvider, int i, int nameSpace, GC gc) {
        fTimeGraphControl.drawItem(item, bounds, timeProvider, i, nameSpace, gc);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#drawState(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TraceColorScheme, org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent, org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.GC, boolean, boolean, boolean)
     */
    @Override
    public void drawState(TimeGraphColorScheme colors, ITimeEvent event,
            Rectangle rect, GC gc, boolean selected, boolean rectBound,
            boolean timeSelected) {
        int colorIdx = getEventColorVal(event);
        drawState(colors, colorIdx, rect, gc, selected, rectBound, timeSelected);

    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#drawState(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TraceColorScheme, int, org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.GC, boolean, boolean, boolean)
     */
    @Override
    public void drawState(TimeGraphColorScheme colors, int colorIdx,
            Rectangle rect, GC gc, boolean selected, boolean rectBound,
            boolean timeSelected) {
        fTimeGraphControl.drawState(colors, colorIdx, rect, gc, selected, rectBound, timeSelected);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getEventColorVal(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent)
     */
    @Override
    public int getEventColorVal(ITimeEvent event) {
        StateColor colors = getEventColor(event);
        if (colors == StateColor.GREEN) {
            return TimeGraphColorScheme.GREEN_STATE;
        } else if (colors == StateColor.DARK_BLUE) {
            return TimeGraphColorScheme.DARK_BLUE_STATE;
        } else if (colors == StateColor.RED) {
            return TimeGraphColorScheme.RED_STATE;
        } else if (colors == StateColor.GOLD) {
            return TimeGraphColorScheme.GOLD_STATE;
        } else if (colors == StateColor.ORANGE) {
            return TimeGraphColorScheme.ORANGE_STATE;
        } else if (colors == StateColor.GRAY) {
            return TimeGraphColorScheme.GRAY_STATE;
        } else if (colors == StateColor.DARK_GREEN) {
            return TimeGraphColorScheme.DARK_GREEN_STATE;
        } else if (colors == StateColor.DARK_YELLOW) {
            return TimeGraphColorScheme.DARK_YELLOW_STATE;
        } else if (colors == StateColor.MAGENTA3) {
            return TimeGraphColorScheme.MAGENTA3_STATE;
        } else if (colors == StateColor.PURPLE1) {
            return TimeGraphColorScheme.PURPLE1_STATE;
        } else if (colors == StateColor.PINK1) {
            return TimeGraphColorScheme.PINK1_STATE;
        } else if (colors == StateColor.AQUAMARINE) {
            return TimeGraphColorScheme.AQUAMARINE_STATE;
        } else if (colors == StateColor.LIGHT_BLUE) {
            return TimeGraphColorScheme.LIGHT_BLUE_STATE;
        } else if (colors == StateColor.CADET_BLUE) {
            return TimeGraphColorScheme.CADET_BLUE_STATE_SEL;
        } else if (colors == StateColor.OLIVE) {
            return TimeGraphColorScheme.OLIVE_STATE;
        }

        return TimeGraphColorScheme.BLACK_STATE;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getEventColor(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent)
     */
    @Override
    public abstract StateColor getEventColor(ITimeEvent event);

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getTraceClassName(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry)
     */
    @Override
    public abstract String getTraceClassName(ITimeGraphEntry trace);

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getEventName(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent)
     */
    @Override
    public String getEventName(ITimeEvent event) {
        return getEventName(event, true, false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getEventName(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent, boolean, boolean)
     */
    @Override
    public abstract String getEventName(ITimeEvent event, boolean upper,
            boolean extInfo);

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#composeTraceName(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry, boolean)
     */
    @Override
    public String composeTraceName(ITimeGraphEntry trace, boolean inclState) {
        String name = trace.getName();
        String threadClass = getTraceClassName(trace);
        if (threadClass != null && threadClass.length() > 0) {
            name += " [" + threadClass + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#composeEventName(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent)
     */
    @Override
    public String composeEventName(ITimeEvent event) {
        String name = event.getEntry().getName();
        String threadClass = getTraceClassName(event.getEntry());
        if (threadClass != null && threadClass.length() > 0) {
            name += " [" + threadClass + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        name += " (" + getEventName(event, false, true) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getEventHoverToolTipInfo(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent)
     */
    @Override
    public abstract Map<String, String> getEventHoverToolTipInfo(ITimeEvent event);

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getItemImage(java.lang.Object)
     */
    @Override
    public Image getItemImage(Object obj) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getStateName(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TmfTimeAnalysisProvider.StateColor)
     */
    @Override
    public abstract String getStateName(StateColor color);

}