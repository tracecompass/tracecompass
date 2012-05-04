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

package org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl;

import org.eclipse.linuxtools.internal.lttng2.core.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IUstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.UstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.UstProviderPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <b><u>UstProviderComponent</u></b>
 * <p>
 * Implementation of the UST provider component.
 * </p>
 */
public class UstProviderComponent extends TraceControlComponent {
    
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */
    public static final String USTL_PROVIDER_ICON_FILE = "icons/obj16/targets.gif"; //$NON-NLS-1$
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The UST provider information.
     */
    private IUstProviderInfo fProviderInfo = null;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor 
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */    
    public UstProviderComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(USTL_PROVIDER_ICON_FILE);
        setToolTip(Messages.TraceControl_ProviderDisplayName);
        fProviderInfo = new UstProviderInfo(name);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Sets the UST provider information to the given value. 
     * @param providerInfo - the provider information to set
     */
    public void setUstProvider(IUstProviderInfo providerInfo) {
        fProviderInfo = providerInfo;
        IBaseEventInfo[] events = providerInfo.getEvents();
        for (int i = 0; i < events.length; i++) {
            BaseEventComponent component  = new BaseEventComponent(events[i].getName(), this);
            component.setEventInfo(events[i]);
            addChild(component);
        }
        setName(getName() + " [PID=" + fProviderInfo.getPid() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * @return the process ID of the UST provider.
     */
    public int getPid() {
        return fProviderInfo.getPid();
    }

    /**
     * Sets the process ID of the UST provider to the given value.
     * @param pid - process ID to set
     */
    public void setPid(int pid) {
        fProviderInfo.setPid(pid);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceControlComponent#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new UstProviderPropertySource(this);
        }
        return null;
    } 
 
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
}


