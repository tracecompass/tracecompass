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

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public interface ITimeGraphPresentationProvider {

    
    /**
     * Returns the name of state types.
     * 
     * @return the name of state types
     */
    public String getStateTypeName();
    
    /**
     * Called after drawing the control
     * 
     * @bounds the drawing rectangle
     * @param gc the graphics context
     */
    public void postDrawControl(Rectangle bounds, GC gc);

    /**
     * Called after drawing an entry
     * 
     * @param entry the entry that was drawn
     * @bounds the drawing rectangle
     * @param gc the graphics context
     */
    public void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc);

    /**
     * Called after drawing an event
     * 
     * @param event the event that was drawn
     * @bounds the drawing rectangle
     * @param gc the graphics context
     */
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc);


    /**
     * Returns table of states with state name to state color relationship
     * 
     * @return table of states with state name to state color relationship
     */
    public StateItem[] getStateTable();
    
    /**
     * Returns the index in the event color table and event name table corresponding to this time event.
     * The index should correspond to a RGB value in the event color table,
     * otherwise the color SWT.COLOR_BLACK will be used.
     * 
     * @param event the time event
     * @return the corresponding event table index
     * 
     * @see #getEventColorTable
     * @see #getEventNameTable
     */
    public int getEventTableIndex(ITimeEvent event);

    /**
     * Returns the height of this item. This value is ignored if the time graph has a fixed item height.
     * 
     * @return the entry height
     * 
     * @see TimeGraphViewer#setItemHeight
     */
    public int getItemHeight(ITimeGraphEntry entry);

    /**
     * This values is appended between braces to the right of Trace Name e.g.
     * Trace And Error Log [Board 17] or for a Thread trace e.g. State Server
     * [java.lang.Thread]
     * 
     * @param trace
     * @return the trace class name
     */
    public String getTraceClassName(ITimeGraphEntry trace);

    /**
     * Returns the name of this event.
     * 
     * @return the event name
     */
    public String getEventName(ITimeEvent event);

    public String composeTraceName(ITimeGraphEntry trace, boolean inclState);

    public String composeEventName(ITimeEvent event);

    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event);

    /**
     * Provides the image icon for a given Event or Trace e.g. customize to use
     * different icons according to specific event /state combination
     * 
     * @param obj
     * @return the image icon
     */
    public Image getItemImage(Object obj);

}