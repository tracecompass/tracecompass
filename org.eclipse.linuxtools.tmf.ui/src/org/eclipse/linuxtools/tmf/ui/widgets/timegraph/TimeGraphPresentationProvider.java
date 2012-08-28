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

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getStateTypeName()
     */
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
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getStateTable()
     */
    @Override
    public StateItem[] getStateTable() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getEventTableIndex(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent)
     */
    @Override
    public int getStateTableIndex(ITimeEvent event) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#postDrawControl(org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.GC)
     */
    @Override
    public void postDrawControl(Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#postDrawEntry(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry, org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.GC)
     */
    @Override
    public void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#postDrawEvent(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent, org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.GC)
     */
    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getItemHeight(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry)
     */
    @Override
    public int getItemHeight(ITimeGraphEntry entry) {
        return DEFAULT_ITEM_HEIGHT;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getItemImage(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry)
     */
    @Override
    public Image getItemImage(ITimeGraphEntry entry) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getEventName(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent)
     */
    @Override
    public String getEventName(ITimeEvent event) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getEventHoverToolTipInfo(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent)
     */
    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        return null;
    }

}