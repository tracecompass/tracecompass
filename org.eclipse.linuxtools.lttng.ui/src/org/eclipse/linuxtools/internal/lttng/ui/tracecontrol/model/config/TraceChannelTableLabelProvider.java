/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.model.config;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannel;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.swt.graphics.Image;

/**
 * <b><u>TraceChannelTableLabelProvider</u></b>
 * <p>
 *  ITableLabelProvider implementation for TableViewers in order to provide the labels of a table 
 *  used for displaying and configuring trace channel information.  
 * </p>
 */
public class TraceChannelTableLabelProvider implements ITableLabelProvider {
    
    // ------------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // Operations
    // -----------------------------------------------------------------------
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (element instanceof TraceChannel) {
            TraceChannel chan = (TraceChannel) element;
            switch (columnIndex) {
            case 1: 
                return getImage(chan.isEnabled() && chan.isEnabledStatusKnown());
            case 2: 
                return getImage(chan.isChannelOverride() && chan.isChannelOverrideStatusKnown());    
            default:
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof TraceChannel) {
            TraceChannel chan = (TraceChannel) element;
            switch (columnIndex) {
            case 0: 
                return chan.getName();
            case 1: 
                if (!chan.isEnabledStatusKnown())  {
                    return TraceChannel.UNKNOWN_STRING;
                }
                break;
            case 2:
                if (!chan.isChannelOverrideStatusKnown()) { 
                    return TraceChannel.UNKNOWN_STRING;
                }
                break;
            case 3:
                if (chan.getSubbufNum() == TraceChannel.UNKNOWN_VALUE) {
                    return TraceChannel.UNKNOWN_STRING;
                }
                return String.valueOf(chan.getSubbufNum());
            case 4: 
                if (chan.getSubbufSize() == TraceChannel.UNKNOWN_VALUE) {
                    return TraceChannel.UNKNOWN_STRING;
                }
                return String.valueOf(chan.getSubbufSize());
            case 5:
                if (chan.getTimer() == TraceChannel.UNKNOWN_VALUE) { 
                    return TraceChannel.UNKNOWN_STRING;
                }
                return String.valueOf(chan.getTimer());
            default:
                // fall through
            }
        }
        return null;
    }

    /*
     * Gets the checked or unchecked image.
     */
    private Image getImage(boolean isSelected) {
        if (isSelected) {
            return Activator.getDefault().getImage(Activator.ICON_ID_CHECKED);
        }
        return Activator.getDefault().getImage(Activator.ICON_ID_UNCHECKED);
    }
}
