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

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <p>
 * Base property source implementation.
 * </p>
 *
 * @author Bernd Hufmann
 */
abstract public class BasePropertySource implements IPropertySource {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object getEditableValue() {
        return null;
    }

    @Override
    abstract public IPropertyDescriptor[] getPropertyDescriptors();

    @Override
    abstract public Object getPropertyValue(Object id);

    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
    }
}
