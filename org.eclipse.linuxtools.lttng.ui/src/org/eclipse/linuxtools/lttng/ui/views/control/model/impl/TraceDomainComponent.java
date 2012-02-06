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
package org.eclipse.linuxtools.lttng.ui.views.control.model.impl;

import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponent;

/**
 * <b><u>TraceDomainComponent</u></b>
 * <p>
 * Implementation of the trace domain component.
 * </p>
 */
public class TraceDomainComponent extends TraceControlComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */
    public static final String TRACE_DOMAIN_ICON_FILE = "icons/obj16/domain.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The domain information.
     */
    private IDomainInfo fDomainInfo = null; 

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor 
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public TraceDomainComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_DOMAIN_ICON_FILE);
        setToolTip(Messages.TraceControl_DomainDisplayName);
        fDomainInfo = new DomainInfo(name);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Sets the domain information.
     * @param domainInfo - the domain information to set.
     */
    public void setDomainInfo(IDomainInfo domainInfo) {
        fDomainInfo = domainInfo;
        IChannelInfo[] channels = fDomainInfo.getChannels();
        for (int i = 0; i < channels.length; i++) {
            TraceChannelComponent channel = new TraceChannelComponent(channels[i].getName(), this);
            channel.setChannelInfo(channels[i]);
            addChild(channel);
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
}
