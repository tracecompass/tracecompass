/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.filter.model;

import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * The Filter tree root node
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterRootNode extends TmfFilterTreeNode {

    @SuppressWarnings("javadoc")
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

}
