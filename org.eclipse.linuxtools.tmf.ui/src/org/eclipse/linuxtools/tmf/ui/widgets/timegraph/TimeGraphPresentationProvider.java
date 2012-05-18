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

public abstract class TimeGraphPresentationProvider implements ITimeGraphPresentationProvider {

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

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#postDrawItems(org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.GC)
     */
    @Override
    public void postDrawControl(Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#postDrawItem(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry, org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.GC)
     */
    @Override
    public void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#drawState(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent, org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.GC)
     */
    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

//    /* (non-Javadoc)
//     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getEventColorTable()
//     */
//    @Override
//    public RGB[] getEventColorTable() {
//        return null;
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getEventNameTable()
//     */
//    @Override
//    public String[] getEventNameTable() {
//        return null;
//    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getStateItems()
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
    public int getEventTableIndex(ITimeEvent event) {
        return 0;
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
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getTraceClassName(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry)
     */
    @Override
    public String getTraceClassName(ITimeGraphEntry trace) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider#getEventName(org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent)
     */
    @Override
    public abstract String getEventName(ITimeEvent event);

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
        name += " (" + getEventName(event) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
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

}