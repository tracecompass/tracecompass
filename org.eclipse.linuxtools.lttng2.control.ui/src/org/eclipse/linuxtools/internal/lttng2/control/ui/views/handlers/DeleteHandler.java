/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;

/**
 * <p>
 * Command handler implementation to delete a target host.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class DeleteHandler extends BaseNodeHandler {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        fLock.lock();
        try {
            ITraceControlComponent root = fTargetNode.getParent();
            fTargetNode.removeAllChildren();
            fTargetNode.deregister();
            root.removeChild(fTargetNode);
        } finally {
            fLock.unlock();
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        boolean isEnabled = false;
        fLock.lock();
        try {
           isEnabled = (super.isEnabled() && (fTargetNode.getTargetNodeState() == TargetNodeState.DISCONNECTED));
        } finally {
            fLock.unlock();
        }
        return isEnabled;
     }
}
