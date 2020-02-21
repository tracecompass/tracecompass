/*******************************************************************************
 * Copyright (c) 2010, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Yuriy Vashchuk - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.filter;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;

/**
 * This is the Content Provider of our tree
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 */
public class FilterTreeContentProvider implements ITreeContentProvider {

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ITmfFilterTreeNode) {
            return ((ITmfFilterTreeNode) parentElement).getChildren();
        }
        return new ITmfFilterTreeNode[0];
    }

    @Override
    public Object getParent(Object element) {
        return ((ITmfFilterTreeNode) element).getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        return ((ITmfFilterTreeNode) element).hasChildren();
    }

}
