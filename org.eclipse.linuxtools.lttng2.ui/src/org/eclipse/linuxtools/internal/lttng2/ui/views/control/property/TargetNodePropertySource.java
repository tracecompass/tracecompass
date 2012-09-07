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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.property;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the target node component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TargetNodePropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The node name property ID.
     */
    public static final String TARGET_NODE_NAME_PROPERTY_ID = "target.node.name"; //$NON-NLS-1$
    /**
     * The node address property ID.
     */
    public static final String TARGET_NODE_ADDRESS_PROPERTY_ID = "target.node.address"; //$NON-NLS-1$
    /**
     * The state property ID.
     */
    public static final String TARGET_NODE_STATE_PROPERTY_ID = "target.node.state"; //$NON-NLS-1$
    /**
     * The node version property ID.
     */
    public static final String TARGET_NODE_VERSION_PROPERTY_ID = "target.node.version"; //$NON-NLS-1$

    /**
     *  The node name property name.
     */
    public static final String TARGET_NODE_NAME_PROPERTY_NAME = Messages.TraceControl_HostNamePropertyName;
    /**
     * The node address property name.
     */
    public static final String TARGET_NODE_ADDRESS_PROPERTY_NAME = Messages.TraceControl_HostAddressPropertyName;
    /**
     * The state address property name.
     */
    public static final String TARGET_NODE_STATE_PROPERTY_NAME = Messages.TraceControl_StatePropertyName;
    /**
     * The node version property name.
     */
    public static final String TARGET_NODE_VERSION_PROPERTY_NAME = Messages.TraceControl_VersionPropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The node component which this property source is for.
     */
    private final TargetNodeComponent fTargetNode;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param component - the node component
     */
    public TargetNodePropertySource(TargetNodeComponent component) {
        fTargetNode = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.BasePropertySource#getPropertyDescriptors()
     */
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new TextPropertyDescriptor(TARGET_NODE_NAME_PROPERTY_ID, TARGET_NODE_NAME_PROPERTY_NAME),
                new TextPropertyDescriptor(TARGET_NODE_ADDRESS_PROPERTY_ID, TARGET_NODE_ADDRESS_PROPERTY_NAME),
                new TextPropertyDescriptor(TARGET_NODE_STATE_PROPERTY_ID, TARGET_NODE_STATE_PROPERTY_NAME),
                new TextPropertyDescriptor(TARGET_NODE_VERSION_PROPERTY_ID, TARGET_NODE_VERSION_PROPERTY_NAME)};
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.BasePropertySource#getPropertyValue(java.lang.Object)
     */
    @Override
    public Object getPropertyValue(Object id) {
        if(TARGET_NODE_NAME_PROPERTY_ID.equals(id)) {
            return fTargetNode.getName();
        }
        if (TARGET_NODE_ADDRESS_PROPERTY_ID.equals(id)) {
            return fTargetNode.getHostName();
        }
        if (TARGET_NODE_STATE_PROPERTY_ID.equals(id)) {
            return fTargetNode.getTargetNodeState().name();
        }
        if (TARGET_NODE_VERSION_PROPERTY_ID.equals(id)) {
            return fTargetNode.getNodeVersion();
        }
        return null;
    }
}
