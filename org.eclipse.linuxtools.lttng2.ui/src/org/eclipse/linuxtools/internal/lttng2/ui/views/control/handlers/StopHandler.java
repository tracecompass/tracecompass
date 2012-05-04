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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;

/**
 * <b><u>StopHandler</u></b>
 * <p>
 * Command handler implementation to stop one or more trace sessions.
 * </p>
 */
public class StopHandler extends ChangeSessionStateHandler {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.ChangeSessionStateHandler#getNewState()
     */
    @Override
    public TraceSessionState getNewState() {
        return TraceSessionState.INACTIVE;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.ChangeSessionStateHandler#changeState(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void changeState(TraceSessionComponent session, IProgressMonitor monitor) throws ExecutionException {
        session.stopSession(monitor);
    }
}
