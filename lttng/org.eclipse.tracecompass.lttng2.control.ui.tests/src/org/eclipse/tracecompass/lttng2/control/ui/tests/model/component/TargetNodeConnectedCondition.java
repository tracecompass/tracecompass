/**********************************************************************
 * Copyright (c) 2016 Ericsson
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
 **********************************************************************/
package org.eclipse.tracecompass.lttng2.control.ui.tests.model.component;

import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.tmf.ui.tests.shared.IWaitCondition;

/**
 * Condition that waits for a target node to be in connected state.
 */
public class TargetNodeConnectedCondition implements IWaitCondition {

    TargetNodeComponent fNode;

    /**
     * Constructor.
     *
     * @param node
     *            The target node
     */
    public TargetNodeConnectedCondition(TargetNodeComponent node) {
        fNode = node;
    }

    @Override
    public boolean test() throws Exception {
        return fNode.getTargetNodeState() == TargetNodeState.CONNECTED;
    }

    @Override
    public String getFailureMessage() {
        return NLS.bind("Target Node {0} did not reach CONNECTED state. State is ", fNode.getName(), fNode.getTargetNodeState().toString());
    }

}
