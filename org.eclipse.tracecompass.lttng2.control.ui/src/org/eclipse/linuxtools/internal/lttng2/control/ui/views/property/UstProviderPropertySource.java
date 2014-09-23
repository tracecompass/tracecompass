/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.property;

import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.UstProviderComponent;
import org.eclipse.linuxtools.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the UST provider component.
 * </p>
 *
 * @author Bernd Hufmann
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

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new ReadOnlyTextPropertyDescriptor(UST_PROVIDER_NAME_PROPERTY_ID, UST_PROVIDER_NAME_PROPERTY_NAME),
                new ReadOnlyTextPropertyDescriptor(UST_PROVIDER_PID_PROPERTY_ID, UST_PROVIDER_PID_PROPERTY_NAME)};
    }

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
