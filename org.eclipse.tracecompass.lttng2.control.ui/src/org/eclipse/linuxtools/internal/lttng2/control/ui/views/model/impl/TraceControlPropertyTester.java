/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl;

import org.eclipse.core.expressions.PropertyTester;

/**
 *
 * Property Tester Implementation for Trace Control Components.
 *
 * @author Bernd Hufmann
 */
public class TraceControlPropertyTester extends PropertyTester {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String ADD_CONTEXT_SUPPORT_PROPERTY = "isAddContextOnEventSupported"; //$NON-NLS-1$


    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

        // Check if node supports adding contexts on event level.
        if (ADD_CONTEXT_SUPPORT_PROPERTY.equals(property)) {
            if ((receiver != null) && (receiver instanceof TraceEventComponent)) {
                TraceEventComponent event = (TraceEventComponent) receiver;
                TargetNodeComponent node = event.getTargetNode();
                return node.isContextOnEventSupported();
            }
        }
        return false;
    }
}
