/*******************************************************************************
 * Copyright (c) 2014 Inria
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Generoso Pagano, Inria - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.dialogs;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * A filter extending the <code>org.eclipse.ui.dialogs.PatternFilter<code>.
 *
 * It redefines the {@link #isElementVisible(Viewer, Object)}} method in order
 * to have a match on a node if: the node matches or one of the children matches
 * or one of the parents matches.
 *
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * @since 3.2
 */
public class TreePatternFilter extends PatternFilter {

    @Override
    public boolean isElementVisible(Viewer viewer, Object element) {
        return super.isElementVisible(viewer, element) || isChildMatch(viewer, element);
    }

    /**
     * Check if at least one of the parents of this element is a match with the
     * filter text.
     *
     * @param viewer
     *            the viewer that contains the element
     * @param element
     *            the tree element to check
     * @return true if the given element has a parent that matches the filter
     *         text
     */
    private boolean isChildMatch(Viewer viewer, Object element) {
        Object parent = ((ITreeContentProvider) ((AbstractTreeViewer) viewer).getContentProvider())
                .getParent(element);
        while (parent != null) {
            if (isLeafMatch(viewer, parent)) {
                return true;
            }
            parent = ((ITreeContentProvider) ((AbstractTreeViewer) viewer).getContentProvider())
                    .getParent(parent);
        }
        return false;
    }

}
