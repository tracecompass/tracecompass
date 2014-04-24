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

/**
 * <p>
 * Command handler implementation to disconnect from a target host.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class DisconnectHandler extends BaseNodeHandler {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        fLock.lock();
        try {
            fTargetNode.disconnect();
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
           isEnabled = super.isEnabled() && (fTargetNode.getTargetNodeState() == TargetNodeState.CONNECTED);
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}
