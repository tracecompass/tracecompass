/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
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

import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Provider class for the time graph provider
 *
 * @version 1.0
 * @author Patrick Tasse
 *
 */
public class TimeGraphPresentationProvider implements ITimeGraphPresentationProvider {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final int DEFAULT_ITEM_HEIGHT = 19;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public String getStateTypeName() {
        return Messages.TmfTimeLegend_TRACE_STATES;
    }

    /**
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getStateTypeName(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry)
     * @since 2.0
     */
    @Override
    public String getStateTypeName(ITimeGraphEntry entry) {
        return null;
    }

    @Override
    public StateItem[] getStateTable() {
        return null;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        return 0;
    }

    @Override
    public void postDrawControl(Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    @Override
    public void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    @Override
    public int getItemHeight(ITimeGraphEntry entry) {
        return DEFAULT_ITEM_HEIGHT;
    }

    @Override
    public Image getItemImage(ITimeGraphEntry entry) {
        return null;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        return null;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        return null;
    }

    /**
     * @since 2.0
     */
    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
        return getEventHoverToolTipInfo(event);
    }

}