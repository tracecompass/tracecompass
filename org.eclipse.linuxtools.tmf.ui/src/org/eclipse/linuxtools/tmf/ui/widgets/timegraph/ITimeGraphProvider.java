/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import java.util.Map;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphProvider.StateColor;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public interface ITimeGraphProvider {

    public abstract void setTimeGraphControl(TimeGraphControl timeGraphControl);

    public void drawItems(Rectangle bounds, ITimeDataProvider timeProvider, TimeGraphItem[] items, int topIndex, int nameSpace, GC gc);

    /**
     * Draws the item
     * 
     * @param item the item to draw
     * @param bounds the container rectangle
     * @param timeProvider the time provider
     * @param i the item index
     * @param nameSpace the name space
     * @param gc
     */
    public abstract void drawItem(TimeGraphItem item, Rectangle bounds, ITimeDataProvider timeProvider, int i, int nameSpace, GC gc);

    public abstract void drawState(TimeGraphColorScheme colors, ITimeEvent event, Rectangle rect, GC gc, boolean selected, boolean rectBound, boolean timeSelected);

    public abstract void drawState(TimeGraphColorScheme colors, int colorIdx, Rectangle rect, GC gc, boolean selected, boolean rectBound, boolean timeSelected);

    /**
     * Uses the abstract method getEventcolor to obtain an enum value and
     * convert it to an internal color index
     * 
     * @param event
     * @return the internal color index
     */
    public abstract int getEventColorVal(ITimeEvent event);

    /**
     * Select the color for the different internal variants of events.
     * 
     * @param event
     * @return the corresponding event color
     */
    public abstract StateColor getEventColor(ITimeEvent event);

    /**
     * This values is appended between braces to the right of Trace Name e.g.
     * Trace And Error Log [Board 17] or for a Thread trace e.g. State Server
     * [java.lang.Thread]
     * 
     * @param trace
     * @return the trace class name
     */
    public abstract String getTraceClassName(ITimeGraphEntry trace);

    public abstract String getEventName(ITimeEvent event);

    /**
     * Specify a Name for the event depending on its type or state e.g. blocked,
     * running, etc..
     * 
     * @param event
     * @param upper
     *            True return String value in Upper case
     * @param extInfo
     *            Verbose, add additional information if applicable
     * @return the event name
     */
    public abstract String getEventName(ITimeEvent event, boolean upper, boolean extInfo);

    public abstract String composeTraceName(ITimeGraphEntry trace, boolean inclState);

    public abstract String composeEventName(ITimeEvent event);

    public abstract Map<String, String> getEventHoverToolTipInfo(ITimeEvent event);

    /**
     * Provides the image icon for a given Event or Trace e.g. customize to use
     * different icons according to specific event /state combination
     * 
     * @param obj
     * @return the image icon
     */
    public abstract Image getItemImage(Object obj);

    public abstract String getStateName(StateColor color);

}