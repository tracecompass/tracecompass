/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * This implementation of <code>ITreeContentProvider</code> handles the case
 * where the viewer input is an unchanging array or collection of elements.
 * The elements do not have children.
 *
 * @author Patrick Tasse
 * @since 3.2
 */
public class ArrayTreeContentProvider implements ITreeContentProvider {

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return null;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return false;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Object[]) {
            return (Object[]) inputElement;
        }
        if (inputElement instanceof Collection) {
            return ((Collection<?>) inputElement).toArray();
        }
        return new Object[0];
    }

}
