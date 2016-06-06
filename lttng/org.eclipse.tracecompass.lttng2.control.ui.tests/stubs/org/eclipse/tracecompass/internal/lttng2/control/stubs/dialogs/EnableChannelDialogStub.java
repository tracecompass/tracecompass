/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.IEnableChannelDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;

/**
 * Create channel dialog stub implementation.
 */
@SuppressWarnings("javadoc")
public class EnableChannelDialogStub implements IEnableChannelDialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private TraceDomainComponent fDomainComponent;
    private ChannelInfo fChannelInfo;
    private TraceDomainType fDomain;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    public EnableChannelDialogStub() {
        fChannelInfo = new ChannelInfo("mychannel");
        fChannelInfo.setNumberOfSubBuffers(4);
        fChannelInfo.setOverwriteMode(true);
        fChannelInfo.setReadTimer(200);
        fChannelInfo.setSwitchTimer(100);
        fChannelInfo.setSubBufferSize(16384);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public IChannelInfo getChannelInfo() {
        return fChannelInfo;
    }

    @Override
    public void setDomainComponent(TraceDomainComponent domain) {
        fDomainComponent = domain;
        if (fDomainComponent != null) {
            fDomain = fDomainComponent.getDomain();
        }
    }

    @Override
    public int open() {
        return 0;
    }

    @Override
    public TraceDomainType getDomain() {
        return fDomain;
    }

    public void setDomain(TraceDomainType domain) {
        fDomain = domain;
    }

    @Override
    public void setHasKernel(boolean hasKernel) {
        // Do nothing
    }

    public void setChannelInfo(ChannelInfo info) {
        fChannelInfo = info;
    }

    @Override
    public void setTargetNodeComponent(TargetNodeComponent node) {
        // Do nothing
    }

    public void setBufferType (BufferType bufferType) {
        fChannelInfo.setBufferType(bufferType);
    }
}