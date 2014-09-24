/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Adapter TreeViewers can use to interact with StatisticsTreeNode objects.
 *
 * @version 2.0
 * @since 2.0
 * @author Mathieu Denis
 * @see org.eclipse.jface.viewers.ITreeContentProvider
 */
public class TmfTreeContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getChildren(Object parentElement) {
        return ((TmfStatisticsTreeNode) parentElement).getChildren().toArray();
    }

    @Override
    public Object getParent(Object element) {
        return ((TmfStatisticsTreeNode) element).getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        return ((TmfStatisticsTreeNode) element).hasChildren();
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
