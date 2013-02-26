/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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

/**
 * Interface for the time graph widget provider
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public interface ITimeGraphPresentationProvider {

    /** State table index for an invisible event
     * @since 2.0
     */
    public final int INVISIBLE = -1;

    /** State table index for a transparent event (only borders drawn)
     * @since 2.0
     */
    public final int TRANSPARENT = -2;

    /**
     * Returns the name of state types.
     *
     * @return the name of state types
     */
    public String getStateTypeName();

   /**
    * Returns the name of state type depending on the given entry.
    * Note that this overwrites the name which is return by getStateTypeName().
    *
    * @param entry
    *           the entry
    * @return the name of state type depending on the given entry or null.
    * @since 2.0
    */
   public String getStateTypeName(ITimeGraphEntry entry);

    /**
     * Returns table of states with state name to state color relationship.
     *
     * @return table of states with color and name
     *
     * @see #getStateTableIndex
     */
    public StateItem[] getStateTable();

    /**
     * Returns the index in the state table corresponding to this time event.
     * The index should correspond to a state in the state table,
     * otherwise the color SWT.COLOR_BLACK will be used.
     * If the index returned is TRANSPARENT, only the event borders will be drawn.
     * If the index returned is INVISIBLE or another negative, the event will not be drawn.
     *
     * @param event the time event
     * @return the corresponding state table index
     *
     * @see #getStateTable
     * @see #TRANSPARENT
     * @see #INVISIBLE
     */
    public int getStateTableIndex(ITimeEvent event);

    /**
     * Called after drawing the control
     *
     * @param bounds
     *            The drawing rectangle
     * @param gc
     *            The graphics context
     */
    public void postDrawControl(Rectangle bounds, GC gc);

    /**
     * Called after drawing an entry
     *
     * @param entry
     *            the entry that was drawn
     * @param bounds
     *            the drawing rectangle
     * @param gc
     *            the graphics context
     */
    public void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc);

    /**
     * Called after drawing an event
     *
     * @param event
     *            the event that was drawn
     * @param bounds
     *            the drawing rectangle
     * @param gc
     *            the graphics context
     */
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc);

    /**
     * Returns the height of this item. This value is ignored if the time graph has a fixed item height.
     *
     * @param entry the entry
     * @return the item height
     *
     * @see TimeGraphViewer#setItemHeight
     */
    public int getItemHeight(ITimeGraphEntry entry);

    /**
     * Provides the image icon for a given entry.
     *
     * @param entry the entry
     * @return the image icon
     */
    public Image getItemImage(ITimeGraphEntry entry);

    /**
     * Returns the name of this event.
     *
     * @param event
     *            The event
     * @return The event name
     */
    public String getEventName(ITimeEvent event);

    /**
     * Returns a map of name and value providing additional information
     * to display in the tool tip for this event.
     *
     * @param event the time event
     * @return a map of tool tip information
     */
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event);

    /**
     * Returns a map of name and value providing additional information
     * to display in the tool tip for this event.
     *
     * @param event the time event
     * @param hoverTime the time corresponding to the mouse hover position
     * @return a map of tool tip information
     *
     * @since 2.0
     */
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime);

}