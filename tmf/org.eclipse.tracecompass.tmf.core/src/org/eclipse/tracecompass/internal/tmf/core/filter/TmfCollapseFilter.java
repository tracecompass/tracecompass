/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.core.filter;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.collapse.ITmfCollapsibleEvent;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;

/**
 * Stateful filter that compares consecutive events for collapsing feature.
 *
 * Usage of this class in conjunction with other {@link ITmfFilterTreeNode}
 * filters is not supported. Will throw {@link UnsupportedOperationException}
 * in that case.
 *
 * @author Bernd Hufmann
 */
public class TmfCollapseFilter implements ITmfFilterTreeNode {

    private static final String COLLAPSE_NODE_NAME = "Collapse"; //$NON-NLS-1$

    private ITmfCollapsibleEvent fPrevEvent = null;

    @Override
    public boolean matches(ITmfEvent event) {

        if (fPrevEvent != null) {
            if (event instanceof ITmfCollapsibleEvent) {
                boolean isCollapsible = fPrevEvent.isCollapsibleWith(event);
                fPrevEvent = (ITmfCollapsibleEvent) event;
                if (isCollapsible) {
                    return false;
                }
            } else {
                fPrevEvent = null;
            }
        } else {
            if (event instanceof ITmfCollapsibleEvent) {
                fPrevEvent = (ITmfCollapsibleEvent) event;
            }
        }
        return true;
    }

    @Override
    public ITmfFilterTreeNode getParent() {
        return null;
    }

    @Override
    public String getNodeName() {
        return COLLAPSE_NODE_NAME;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public int getChildrenCount() {
        return 0;
    }

    @Override
    public @NonNull ITmfFilterTreeNode[] getChildren() {
        return new @NonNull ITmfFilterTreeNode[0];
    }

    @Override
    public ITmfFilterTreeNode getChild(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITmfFilterTreeNode remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITmfFilterTreeNode removeChild(ITmfFilterTreeNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int addChild(ITmfFilterTreeNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITmfFilterTreeNode replaceChild(int index, ITmfFilterTreeNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParent(ITmfFilterTreeNode parent) {
        // Do nothing
    }

    @Override
    public List<String> getValidChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return COLLAPSE_NODE_NAME;
    }

    @Override
    public String toString(boolean explicit) {
        return COLLAPSE_NODE_NAME + " [" + fPrevEvent + ']'; //$NON-NLS-1$
    }

    @Override
    public ITmfFilterTreeNode clone() {
        return new TmfCollapseFilter();
    }
}
