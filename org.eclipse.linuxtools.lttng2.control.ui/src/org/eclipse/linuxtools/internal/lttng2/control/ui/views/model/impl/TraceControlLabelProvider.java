/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.swt.graphics.Image;

/**
 * <p>
 * Label provider for trace control tree viewer.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceControlLabelProvider extends ColumnLabelProvider {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Image getImage(Object element) {
        if ((element != null) && (element instanceof ITraceControlComponent)) {
            return ((ITraceControlComponent) element).getImage();
        }
        return null;
    }

    @Override
    public String getText(Object element) {
        if ((element != null) && (element instanceof ITraceControlComponent)) {
            return ((ITraceControlComponent) element).getName();
        }
        return "";//$NON-NLS-1$
    }

    @Override
    public String getToolTipText(Object element) {
        if ((element != null) && (element instanceof ITraceControlComponent)) {
            return ((ITraceControlComponent) element).getToolTip();
        }
        return null;
    }
}
