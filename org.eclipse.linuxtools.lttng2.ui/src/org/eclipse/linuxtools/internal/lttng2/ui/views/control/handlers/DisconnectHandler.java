/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TargetNodeState;

/**
 * <b><u>DisconnectHandler</u></b>
 * <p>
 * Command handler implementation to disconnect from a target host.
 * </p>
 */
public class DisconnectHandler extends BaseNodeHandler {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.BaseNodeHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        fLock.lock();
        try {
            ITraceControlComponent root = fTargetNode.getParent();
            fTargetNode.disconnect();
            root.removeChild(fTargetNode);
        } finally {
            fLock.unlock();
        }
        return null;
    }

   /*
    * (non-Javadoc)
    * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.BaseNodeHandler#isEnabled()
    */
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
