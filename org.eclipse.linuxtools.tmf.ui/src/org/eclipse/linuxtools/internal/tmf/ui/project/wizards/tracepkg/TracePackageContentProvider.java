/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider to display the content of a trace package in a tree
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageContentProvider implements ITreeContentProvider {

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof TracePackageElement[]) {
            return (TracePackageElement[]) inputElement;
        }
        return null;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return ((TracePackageElement) parentElement).getChildren();
    }

    @Override
    public Object getParent(Object element) {
        return ((TracePackageElement) element).getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        TracePackageElement traceTransferElement = (TracePackageElement) element;
        return traceTransferElement.getChildren() != null && traceTransferElement.getChildren().length > 0;
    }

}