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
package org.eclipse.linuxtools.internal.lttng2.control.stubs.dialogs;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs.IEnableChannelDialog;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;

/**
 * Create channel dialog stub implementation.
 */
@SuppressWarnings("javadoc")
public class EnableChannelDialogStub implements IEnableChannelDialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private TraceDomainComponent fDomain;
    private ChannelInfo fChannelInfo;
    private boolean fIsKernel;

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
    public void setIsKernel(boolean isKernel) {
        fIsKernel = isKernel;
    }

    @Override
    public IChannelInfo getChannelInfo() {
        return fChannelInfo;
    }

    @Override
    public void setDomainComponent(TraceDomainComponent domain) {
        fDomain = domain;
        if (fDomain != null) {
            fIsKernel = fDomain.isKernel();
        }
    }

    @Override
    public int open() {
        return 0;
    }

    @Override
    public boolean isKernel() {
        return fIsKernel;
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