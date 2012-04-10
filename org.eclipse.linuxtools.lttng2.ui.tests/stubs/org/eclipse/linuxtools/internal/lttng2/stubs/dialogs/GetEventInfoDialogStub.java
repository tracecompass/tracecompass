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
package org.eclipse.linuxtools.internal.lttng2.stubs.dialogs;

import java.util.Arrays;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IGetEventInfoDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;

/**
 * Get event information dialog stub implementation. 
 */
public class GetEventInfoDialogStub implements IGetEventInfoDialog {

    private TraceSessionComponent[] fSessions;
    
    @Override
    public TraceSessionComponent getSession() {
        return fSessions[0];
    }

    @Override
    public TraceChannelComponent getChannel() {
        return null;
    }

    @Override
    public void setIsKernel(boolean isKernel) {
    }

    @Override
    public void setSessions(TraceSessionComponent[] sessions) {
        if (sessions != null) {
            fSessions = Arrays.copyOf(sessions, sessions.length);
            return;
        }
        fSessions = null;
    }

    @Override
    public int open() {
        return 0;
    }
}

