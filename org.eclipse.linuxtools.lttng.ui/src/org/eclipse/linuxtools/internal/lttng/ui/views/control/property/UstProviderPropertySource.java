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
package org.eclipse.linuxtools.internal.lttng.ui.views.control.property;

import org.eclipse.linuxtools.internal.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.UstProviderComponent;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * <b><u>UstProviderPropertySource</u></b>
 * <p>
 * Property source implementation for the UST provider component.
 * </p>
 */
public class UstProviderPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The UST provider 'name' property ID.
     */
    public static final String UST_PROVIDER_NAME_PROPERTY_ID = "ust.provider.name"; //$NON-NLS-1$
    /**
     * The UST provider 'PID' property ID.
     */
    public static final String UST_PROVIDER_PID_PROPERTY_ID = "ust.provider.pid"; //$NON-NLS-1$
    /**
     *  The UST provider 'name' property name. 
     */
    public static final String UST_PROVIDER_NAME_PROPERTY_NAME = Messages.TraceControl_ProviderNamePropertyName;
    /**
     * The UST provider 'type' property name.
     */
    public static final String UST_PROVIDER_PID_PROPERTY_NAME = Messages.TraceControl_ProcessIdPropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The UST provider component which this property source is for. 
     */
    private UstProviderComponent fUstProvider;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param component - the UST provider component
     */
    public UstProviderPropertySource(UstProviderComponent component) {
        fUstProvider = component;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng.ui.views.control.property.BasePropertySource#getPropertyDescriptors()
     */
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new TextPropertyDescriptor(UST_PROVIDER_NAME_PROPERTY_ID, UST_PROVIDER_NAME_PROPERTY_NAME),
                new TextPropertyDescriptor(UST_PROVIDER_PID_PROPERTY_ID, UST_PROVIDER_PID_PROPERTY_NAME)};
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng.ui.views.control.property.BasePropertySource#getPropertyValue(java.lang.Object)
     */
    @Override
    public Object getPropertyValue(Object id) {
        if(UST_PROVIDER_NAME_PROPERTY_ID.equals(id)) {
            return fUstProvider.getName();
        }
        if (UST_PROVIDER_PID_PROPERTY_ID.equals(id)) {
            return String.valueOf(fUstProvider.getPid());
        }
        return null;
    }

}
