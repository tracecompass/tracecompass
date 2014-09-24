/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yuriy Vashchuk - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.filter;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterCompareNode.Type;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterEventTypeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.swt.graphics.Image;

/**
 * This is the Label Provider for our Filter Tree
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 */
public class FilterTreeLabelProvider implements ILabelProvider {

    @Override
    public void addListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub
    }

    @Override
    public Image getImage(Object element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getText(Object element) {
        String label = null;

        if (element instanceof TmfFilterNode) {

            TmfFilterNode node = (TmfFilterNode) element;
            label = node.getNodeName() + " " + node.getFilterName(); //$NON-NLS-1$

        } else if (element instanceof TmfFilterEventTypeNode) {

            TmfFilterEventTypeNode node = (TmfFilterEventTypeNode) element;
            label = "WITH " + node.getNodeName() + (node.getName() != null ? " " + node.getName() : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        } else if (element instanceof TmfFilterAndNode) {

            TmfFilterAndNode node = (TmfFilterAndNode) element;
            label = (node.isNot() ? "NOT " : "") + node.getNodeName(); //$NON-NLS-1$ //$NON-NLS-2$

        } else if (element instanceof TmfFilterOrNode) {

            TmfFilterOrNode node = (TmfFilterOrNode) element;
            label = (node.isNot() ? "NOT " : "") + node.getNodeName(); //$NON-NLS-1$ //$NON-NLS-2$

        } else if (element instanceof TmfFilterContainsNode) {

            TmfFilterContainsNode node = (TmfFilterContainsNode) element;
            label = (node.isNot() ? "NOT " : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (node.getField() != null ? node.getField() + " " : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    node.getNodeName() +
                    (node.getValue() != null && node.getValue().length() > 0 ? " \"" + node.getValue() + "\"" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        } else if (element instanceof TmfFilterEqualsNode) {

            TmfFilterEqualsNode node = (TmfFilterEqualsNode) element;
            label = (node.isNot() ? "NOT " : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (node.getField() != null ? node.getField() + " " : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    node.getNodeName() +
                    (node.getValue() != null && node.getValue().length() > 0 ? " \"" + node.getValue() + "\"" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        } else if (element instanceof TmfFilterMatchesNode) {

            TmfFilterMatchesNode node = (TmfFilterMatchesNode) element;
            label = (node.isNot() ? "NOT " : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (node.getField() != null ? node.getField() + " " : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    node.getNodeName() +
                    (node.getRegex() != null && node.getRegex().length() > 0 ? " \"" + node.getRegex() + "\"" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        } else if (element instanceof TmfFilterCompareNode) {

            TmfFilterCompareNode node = (TmfFilterCompareNode) element;
            label = (node.isNot() ? "NOT " : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (node.getField() != null ? node.getField() + " " : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (node.getResult() < 0 ? "<" : (node.getResult() > 0 ? ">" : "=")) + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    (node.getValue() != null && node.getValue().length() > 0 ?
                            (node.getType() == Type.ALPHA ? " \"" + node.getValue() + "\"" : //$NON-NLS-1$ //$NON-NLS-2$
                                    (node.getType() == Type.TIMESTAMP ? " [" + node.getValue() + "]" : //$NON-NLS-1$ //$NON-NLS-2$
                                            " " + node.getValue())) : ""); //$NON-NLS-1$//$NON-NLS-2$

        }
        return label;
    }

}
