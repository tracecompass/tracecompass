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
package org.eclipse.linuxtools.lttng.ui.views.control.property;

import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * <b><u>TargetNodePropertySource</u></b>
 * <p>
 * Property source implementation for the target node component.
 * </p>
 */
public class TargetNodePropertySource implements IPropertySource {

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
     *  The node name property name. 
     */
    public static final String TARGET_NODE_NAME_PROPERTY_NAME = "Name"; //$NON-NLS-1$
    /**
     * The node address property name.
     */
    public static final String TARGET_NODE_ADDRESS_PROPERTY_NAME = "Address"; //$NON-NLS-1$
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The node component which this property source is for. 
     */
    final private TargetNodeComponent fTargetNode;
    
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
     * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
     */
    @Override
    public Object getEditableValue() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
     */
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new TextPropertyDescriptor(TARGET_NODE_NAME_PROPERTY_ID, TARGET_NODE_NAME_PROPERTY_NAME),
                new TextPropertyDescriptor(TARGET_NODE_ADDRESS_PROPERTY_ID, TARGET_NODE_ADDRESS_PROPERTY_NAME),
        };
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
     */
    @Override
    public Object getPropertyValue(Object id) {
        if(TARGET_NODE_NAME_PROPERTY_ID.equals(id)) {
            return fTargetNode.getName();
        }
        if (TARGET_NODE_ADDRESS_PROPERTY_ID.equals(id)) {
            return fTargetNode.getHostName();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang.Object)
     */
    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
     */
    @Override
    public void resetPropertyValue(Object id) {
        
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object, java.lang.Object)
     */
    @Override
    public void setPropertyValue(Object id, Object value) {
    }

}
