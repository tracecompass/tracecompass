/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;

/**
 * <p>
 * Command handler implementation to connect to a target host.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ConnectHandler extends BaseNodeHandler {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        fLock.lock();
        TargetNodeComponent node = null;
        try {
            node = fTargetNode;
        } finally {
            fLock.unlock();
        }
        if (node != null) {
            node.connect();
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        boolean isEnabled = false;
        fLock.lock();
        try {
            isEnabled = super.isEnabled();
            TargetNodeComponent node = fTargetNode;
            isEnabled &= ((node != null) && (node.getTargetNodeState() == TargetNodeState.DISCONNECTED));
        } finally {
            fLock.unlock();
        }
        return isEnabled;
     }
}
