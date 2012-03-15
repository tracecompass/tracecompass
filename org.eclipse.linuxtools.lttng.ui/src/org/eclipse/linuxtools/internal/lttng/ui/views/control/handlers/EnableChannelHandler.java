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
package org.eclipse.linuxtools.internal.lttng.ui.views.control.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.TraceDomainComponent;

/**
 * <b><u>EnableChannelHandler</u></b>
 * <p>
 * Command handler implementation to enable one or more trace channels per session and domain.
 * </p>
 */
public class EnableChannelHandler extends ChangeChannelStateHandler {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng.ui.views.control.handlers.BaseChangeChannelStateHandler#getNewState()
     */
    @Override
    protected TraceEnablement getNewState() {
        return TraceEnablement.ENABLED;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng.ui.views.control.handlers.BaseChangeChannelStateHandler#changeState(org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.TraceDomainComponent, java.util.List, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void changeState(TraceDomainComponent domain, List<String> channelNames, IProgressMonitor monitor) throws ExecutionException {
        domain.enableChannels(channelNames, null, monitor);
    }
}
