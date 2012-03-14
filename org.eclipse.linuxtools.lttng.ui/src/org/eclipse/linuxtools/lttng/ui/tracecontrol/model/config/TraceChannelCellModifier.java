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
package org.eclipse.linuxtools.lttng.ui.tracecontrol.model.config;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource.TraceState;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannel;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.wizards.KernelTraceChannelConfigurationPage;
import org.eclipse.swt.widgets.TableItem;

/**
 * <b><u>TraceChannelCellModifier</u></b>
 * <p>
 *  ICellModifier implementation for TableViewers in order to modify cells of a table used for 
 *  for displaying and configuring trace channel information.  
 * </p>
 */
public class TraceChannelCellModifier implements ICellModifier {

    // ------------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------
    KernelTraceChannelConfigurationPage fConfigPage;

    // ------------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------
    
    /**
     * Constructor
     * 
     * @param configPage The trace configuration reference
     */
    public TraceChannelCellModifier(KernelTraceChannelConfigurationPage configPage) {
        fConfigPage = configPage;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean canModify(Object element, String property) {
        // Find the index of the column
        int columnIndex = fConfigPage.getColumnProperties().indexOf(property);

        switch (columnIndex) {
            case 0: // Name
                return false;
            case 1: // Enabled
            case 2: // Buffer Overwrite
            case 3: // SubbufNum
            case 4: // SubbufSize
                if ((fConfigPage.getTraceState() == TraceState.CREATED) || (fConfigPage.getTraceState() == TraceState.CONFIGURED)) {
                    return true;
                }
                break;
            case 5: // Channel Timer
                if (fConfigPage.isLocalTrace() && ((fConfigPage.getTraceState() == TraceState.CREATED) || (fConfigPage.getTraceState() == TraceState.CONFIGURED))) {
                    return true;
                }
                break;
            default:  
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
     */
    @Override
    public Object getValue(Object element, String property) {
        
        // Find the index of the column
        int columnIndex = fConfigPage.getColumnProperties().indexOf(property);

        Object result = null;
        TraceChannel chan = (TraceChannel) element;
        
        switch (columnIndex) {
            case 0: // Name
                result = chan.getName();
                break;
            case 1: // Enabled
                result = Boolean.valueOf(chan.isEnabled());
                break;
            case 2: // Buffer Overwrite
                result = Boolean.valueOf(chan.isChannelOverride());
                break;
            case 3: // SubbufNum
                result = String.valueOf(chan.getSubbufNum());
                break;
            case 4: // SubbufSize
                result = String.valueOf(chan.getSubbufSize());
                break;
            case 5: // Channel Timer
                result = String.valueOf(chan.getTimer());
                break;
            default:
                    result = "";  //$NON-NLS-1$
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
     */
    @Override
    public void modify(Object element, String property, Object value) {
        //  Find the index of the column
        int columnIndex = fConfigPage.getColumnProperties().indexOf(property);

        TableItem item = (TableItem) element;
        TraceChannel chan = (TraceChannel) item.getData();
        String valueString;

        switch (columnIndex) {
        case 0:
            chan.setName(((String) value).trim());
            break;
        case 1: // Name
            chan.setIsEnabled(((Boolean) value).booleanValue());
            break;
        case 2: 
            chan.setIsChannelOverride(((Boolean) value).booleanValue());
            break;
        case 3: // SubbufNum
            valueString = ((String) value).trim();
            if (valueString.length() == 0) {
                valueString = "0"; //$NON-NLS-1$
            }
            else if(TraceChannel.UNKNOWN_STRING.equals(valueString)) {
                chan.setSubbufNum(TraceChannel.UNKNOWN_VALUE);
            }
            else {
                chan.setSubbufNum(Integer.parseInt(valueString));
            }
            break;
        case 4: // SubbufSize
            valueString = ((String) value).trim();
            if (valueString.length() == 0) {
                valueString = "0"; //$NON-NLS-1$
            }
            else if(TraceChannel.UNKNOWN_STRING.equals(valueString)) {
                chan.setSubbufSize(TraceChannel.UNKNOWN_VALUE);
            }
            else {
                chan.setSubbufSize(Integer.parseInt(valueString));
            }
            break;
        case 5:  // Channel Timer
            valueString = ((String) value).trim();
            if (valueString.length() == 0) {
                valueString = "0"; //$NON-NLS-1$
            }
            else if(TraceChannel.UNKNOWN_STRING.equals(valueString)) {
                chan.setTimer(TraceChannel.UNKNOWN_VALUE);
            }
            else {
                chan.setTimer(Integer.parseInt(valueString));
            }
            break;
        default:
        }
        fConfigPage.refresh();
    }
}
