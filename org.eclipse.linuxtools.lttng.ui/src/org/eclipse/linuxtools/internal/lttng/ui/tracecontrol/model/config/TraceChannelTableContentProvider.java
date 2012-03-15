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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannels;

/**
 * <b><u>TraceChannelTableContentProvider</u></b>
 * <p>
 *  IStructuredContentProvider implementation for TableViewers in order to provide the content of a table 
 *  used for displaying and configuring trace channel information.  
 * </p>
 */
public class TraceChannelTableContentProvider implements IStructuredContentProvider {
    
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
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof TraceChannels) {
            return ((TraceChannels)inputElement).values().toArray();
        }
        return null;
    }
}
