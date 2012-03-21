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

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateChannelDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;

/**
 * Create channel dialog stub implementation. 
 */
public class CreateChannelDialogStub implements ICreateChannelDialog {
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private TraceDomainComponent fDomain;
    private ChannelInfo fChannelInfo;
    private boolean fIsKernel;
    
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    public CreateChannelDialogStub() {
        fChannelInfo = new ChannelInfo("mychannel"); //$NON-NLS-1$
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
    
    public void setChannelInfo(ChannelInfo info) {
        fChannelInfo = info;
    }
}