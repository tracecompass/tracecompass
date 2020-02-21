/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;

/**
 * <p>
 * Tree content provider implementation for trace control view.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceControlContentProvider implements ITreeContentProvider {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        // Do nothing
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Do nothing
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement) {

        if (parentElement instanceof ITraceControlComponent) {
            return ((ITraceControlComponent)parentElement).getChildren();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof ITraceControlComponent) {
            return ((ITraceControlComponent)element).getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof ITraceControlComponent) {
            return ((ITraceControlComponent)element).hasChildren();
        }
        return false;
    }
}
