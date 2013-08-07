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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.property;

import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.BufferType;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the trace domain component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceDomainPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The trace domain 'name' property ID.
     */
    public static final String TRACE_DOMAIN_NAME_PROPERTY_ID = "trace.domain.name"; //$NON-NLS-1$
    /**
     *  The trace domain 'name' property name.
     */
    public static final String TRACE_DOMAIN_NAME_PROPERTY_NAME = Messages.TraceControl_DomainNamePropertyName;
    /**
     * The domain 'buffer type' property ID.
     */
    public static final String BUFFER_TYPE_PROPERTY_ID = "trace.domain.bufferType"; //$NON-NLS-1$
    /**
     * The domain 'buffer type' property name.
     */
    public static final String BUFER_TYPE_PROPERTY_NAME = Messages.TraceControl_BufferTypePropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The trace domain component which this property source is for.
     */
    private final TraceDomainComponent fDomain;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param component - the trace domain component
     */
    public TraceDomainPropertySource(TraceDomainComponent component) {
        fDomain = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (fDomain.getBufferType() == BufferType.BUFFER_TYPE_UNKNOWN) {
            return new IPropertyDescriptor[] {
                    new ReadOnlyTextPropertyDescriptor(TRACE_DOMAIN_NAME_PROPERTY_ID, TRACE_DOMAIN_NAME_PROPERTY_NAME) };
        }

        return new IPropertyDescriptor[] {
                new ReadOnlyTextPropertyDescriptor(TRACE_DOMAIN_NAME_PROPERTY_ID, TRACE_DOMAIN_NAME_PROPERTY_NAME),
                new ReadOnlyTextPropertyDescriptor(BUFFER_TYPE_PROPERTY_ID, BUFER_TYPE_PROPERTY_NAME) };
    }

    @Override
    public Object getPropertyValue(Object id) {
        if(BUFFER_TYPE_PROPERTY_ID.equals(id)){
            return fDomain.getBufferType().getInName();
        }

        if(TRACE_DOMAIN_NAME_PROPERTY_ID.equals(id)) {
            return fDomain.getName();
        }
        return null;
    }

}
