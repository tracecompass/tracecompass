/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
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
 *   Patrick Tasse - Update filter nodes
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.filter;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode.Type;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTraceTypeNode;

/**
 * This is the Label Provider for our Filter Tree
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 */
public class FilterTreeLabelProvider implements ILabelProvider {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final String QUOTE = "\""; //$NON-NLS-1$
    private static final String SPACE_QUOTE = " \""; //$NON-NLS-1$
    private static final String NOT = "NOT "; //$NON-NLS-1$

    @Override
    public void addListener(ILabelProviderListener listener) {
        // Do nothing
    }

    @Override
    public void dispose() {
        // Do nothing
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // Do nothing
    }

    @Override
    public Image getImage(Object element) {
        return null;
    }

    @Override
    public String getText(Object element) {
        StringBuilder label = new StringBuilder();

        if (element instanceof TmfFilterNode) {

            TmfFilterNode node = (TmfFilterNode) element;
            label.append(node.getNodeName()).append(' ').append(node.getFilterName() != null &&
                    !node.getFilterName().isEmpty() ? node.getFilterName() : Messages.FilterTreeLabelProvider_FilterNameHint);

        } else if (element instanceof TmfFilterTraceTypeNode) {

            TmfFilterTraceTypeNode node = (TmfFilterTraceTypeNode) element;
            label.append(node.isNot() ? NOT : EMPTY_STRING).append("WITH ").append(node.getNodeName()).append(' ').append((node.getName() != null ? node.getName() : Messages.FilterTreeLabelProvider_TraceTypeHint)); //$NON-NLS-1$

        } else if (element instanceof TmfFilterAndNode) {

            TmfFilterAndNode node = (TmfFilterAndNode) element;
            label.append((node.isNot() ? NOT : EMPTY_STRING)).append(node.getNodeName());

        } else if (element instanceof TmfFilterOrNode) {

            TmfFilterOrNode node = (TmfFilterOrNode) element;
            label.append(node.isNot() ? NOT : EMPTY_STRING).append(node.getNodeName());

        } else if (element instanceof TmfFilterContainsNode) {

            TmfFilterContainsNode node = (TmfFilterContainsNode) element;
            label.append(node.isNot() ? NOT : EMPTY_STRING)
            .append(node.getEventAspect() != null ? node.getAspectLabel(false) : Messages.FilterTreeLabelProvider_AspectHint)
            .append(' ').append(node.getNodeName()).append(node.getValue() != null ?
                    new StringBuilder().append(SPACE_QUOTE).append(node.getValue()).append(QUOTE).toString() : EMPTY_STRING);

        } else if (element instanceof TmfFilterEqualsNode) {

            TmfFilterEqualsNode node = (TmfFilterEqualsNode) element;
            label.append(node.isNot() ? NOT : EMPTY_STRING)
            .append(node.getEventAspect() != null ? node.getAspectLabel(false) : Messages.FilterTreeLabelProvider_AspectHint)
            .append(' ').append(node.getNodeName())
            .append(node.getValue() != null ? new StringBuilder().append(SPACE_QUOTE).append(node.getValue()).append(QUOTE).toString() : EMPTY_STRING);

        } else if (element instanceof TmfFilterMatchesNode) {

            TmfFilterMatchesNode node = (TmfFilterMatchesNode) element;
            label.append(node.isNot() ? NOT : EMPTY_STRING)
            .append(node.getEventAspect() != null ? node.getAspectLabel(false) : Messages.FilterTreeLabelProvider_AspectHint)
            .append(' ').append(node.getNodeName())
            .append(node.getRegex() != null ? new StringBuilder().append(SPACE_QUOTE).append(node.getRegex()).append(QUOTE).toString() : EMPTY_STRING);

        } else if (element instanceof TmfFilterCompareNode) {

            TmfFilterCompareNode node = (TmfFilterCompareNode) element;
            label.append(node.isNot() ? NOT : EMPTY_STRING)
            .append(node.getEventAspect() != null ? node.getAspectLabel(false) : Messages.FilterTreeLabelProvider_AspectHint)
            .append(node.getResult() < 0 ? " <" : (node.getResult() > 0 ? " >" : " =")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            .append(node.getType() == Type.ALPHA ? SPACE_QUOTE : node.getType() == Type.TIMESTAMP ? " [" : ' ') //$NON-NLS-1$
            .append(node.hasValidValue() ? node.getValue() : Messages.FilterTreeLabelProvider_ValueHint)
            .append(node.getType() == Type.ALPHA ? '\"' : node.getType() == Type.TIMESTAMP ? ']' : EMPTY_STRING);

        }
        return label.toString();
    }

}
