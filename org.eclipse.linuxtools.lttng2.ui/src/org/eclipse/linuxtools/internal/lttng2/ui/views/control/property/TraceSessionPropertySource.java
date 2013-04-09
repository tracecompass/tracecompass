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
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.linuxtools.tmf.core.util.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the trace session component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceSessionPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The trace session name property ID.
     */
    public static final String TRACE_SESSION_NAME_PROPERTY_ID = "trace.session.name"; //$NON-NLS-1$
    /**
     * The trace session path property ID.
     */
    public static final String TRACE_SESSION_PATH_PROPERTY_ID = "trace.session.path"; //$NON-NLS-1$
    /**
     * The trace session state ID.
     */
    public static final String TRACE_SESSION_STATE_PROPERTY_ID = "trace.session.state"; //$NON-NLS-1$
    /**
     *  The trace session name property name.
     */
    public static final String TRACE_SESSION_NAME_PROPERTY_NAME = Messages.TraceControl_SessionNamePropertyName;
    /**
     *  The trace session path property name.
     */
    public static final String TRACE_SESSION_PATH_PROPERTY_NAME = Messages.TraceControl_SessionPathPropertyName;
    /**
     * The trace session state property name.
     */
    public static final String TRACE_SESSION_STATE_PROPERTY_NAME = Messages.TraceControl_StatePropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The session component which this property source is for.
     */
    private final TraceSessionComponent fSession;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param component - the session component
     */
    public TraceSessionPropertySource(TraceSessionComponent component) {
        fSession = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new ReadOnlyTextPropertyDescriptor(TRACE_SESSION_NAME_PROPERTY_ID, TRACE_SESSION_NAME_PROPERTY_NAME),
                new ReadOnlyTextPropertyDescriptor(TRACE_SESSION_PATH_PROPERTY_ID, TRACE_SESSION_PATH_PROPERTY_NAME),
                new ReadOnlyTextPropertyDescriptor(TRACE_SESSION_STATE_PROPERTY_ID, TRACE_SESSION_STATE_PROPERTY_NAME)};
    }

    @Override
    public Object getPropertyValue(Object id) {
        if(TRACE_SESSION_NAME_PROPERTY_ID.equals(id)) {
            return fSession.getName();
        }
        if(TRACE_SESSION_PATH_PROPERTY_ID.equals(id)) {
            return fSession.getSessionPath();
        }
        if (TRACE_SESSION_STATE_PROPERTY_ID.equals(id)) {
            return fSession.getSessionState().name();
        }
        return null;
    }
}
