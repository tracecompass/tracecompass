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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;

/**
 * <b><u>TraceControlContentProvider</u></b>
 * <p>
 * Tree content provider implementation for trace control view.
 * </p>
 */
public class TraceControlContentProvider implements ITreeContentProvider {
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
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
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        
        if (parentElement instanceof ITraceControlComponent) {
            return ((ITraceControlComponent)parentElement).getChildren();
        }
        return new Object[0];
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        if (element instanceof ITraceControlComponent) {
            return ((ITraceControlComponent)element).getParent();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof ITraceControlComponent) {
            return ((ITraceControlComponent)element).hasChildren();
        }
        return false;
    }
}
