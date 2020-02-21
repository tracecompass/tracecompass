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
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * The Filter tree root node
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterRootNode extends TmfFilterTreeNode {

    /** root node name */
    public static final String NODE_NAME = "ROOT"; //$NON-NLS-1$

    private static final String[] VALID_CHILDREN = {
        TmfFilterNode.NODE_NAME
    };

    /**
     * Default constructor
     */
    public TmfFilterRootNode() {
        super(null);
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        for (ITmfFilterTreeNode node : getChildren()) {
            if (! node.matches(event)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> getValidChildren() {
        return Arrays.asList(VALID_CHILDREN);
    }

    @Override
    public String toString(boolean explicit) {
        StringBuffer buf = new StringBuffer("root"); //$NON-NLS-1$
        if (getChildrenCount() > 0) {
            buf.append(' ');
            List<String> strings = new ArrayList<>();
            for (ITmfFilterTreeNode child : getChildren()) {
                strings.add(child.toString(explicit));
            }
            buf.append(strings.toString());
        }
        return buf.toString();
    }
}
