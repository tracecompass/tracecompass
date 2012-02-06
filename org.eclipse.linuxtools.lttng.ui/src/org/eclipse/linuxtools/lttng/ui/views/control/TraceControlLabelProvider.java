/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.swt.graphics.Image;

/**
 * <b><u>TraceControlLabelProvider</u></b>
 * <p>
 * Label provider for trace control tree viewer.
 * </p>
 */
public class TraceControlLabelProvider extends ColumnLabelProvider {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
        if ((element != null) && (element instanceof ITraceControlComponent)) {
            return ((ITraceControlComponent) element).getImage();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        if ((element != null) && (element instanceof ITraceControlComponent)) {
            return ((ITraceControlComponent) element).getName();
        }
        return "";//$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
     */
    @Override
    public String getToolTipText(Object element) {
        if ((element != null) && (element instanceof ITraceControlComponent)) {
            return ((ITraceControlComponent) element).getToolTip();
        }
        return null;
    }
}
