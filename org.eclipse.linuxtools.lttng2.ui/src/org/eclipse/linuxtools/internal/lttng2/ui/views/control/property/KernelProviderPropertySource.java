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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.property;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.KernelProviderComponent;
import org.eclipse.linuxtools.tmf.core.util.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the kernl provider component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class KernelProviderPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The kernel provider 'name' property ID.
     */
    public static final String KERNEL_PROVIDER_NAME_PROPERTY_ID = "ust.provider.name"; //$NON-NLS-1$
    /**
     *  The kernel provider 'name' property name.
     */
    public static final String KERNEL_PROVIDER_NAME_PROPERTY_NAME = Messages.TraceControl_ProviderNamePropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The kernel provider component which this property source is for.
     */
    private KernelProviderComponent fProvider;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param component - the kernel provider component
     */
    public KernelProviderPropertySource(KernelProviderComponent component) {
        fProvider = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new ReadOnlyTextPropertyDescriptor(KERNEL_PROVIDER_NAME_PROPERTY_ID, KERNEL_PROVIDER_NAME_PROPERTY_NAME)};
    }

    @Override
    public Object getPropertyValue(Object id) {
        if(KERNEL_PROVIDER_NAME_PROPERTY_ID.equals(id)) {
            return fProvider.getName();
        }
        return null;
    }

}
