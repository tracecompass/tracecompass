/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.btf.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.linuxtools.btf.core.event.BtfEvent;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Btf Event Adaptor Factory
 *
 * @author Matthew Khouzam
 */
public class BtfEventAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] PROPERTIES = new Class[] {
            IPropertySource.class
    };

    @Override
    public Class<?>[] getAdapterList() {
        return PROPERTIES;
    }

    @Override
    public Object getAdapter(Object element, Class key) {
        if (element instanceof BtfEvent && IPropertySource.class.equals(key)) {
            BtfEvent tmfEvent = (BtfEvent) element;
            return new BtfEventPropertySource(tmfEvent);
        }
        return null;
    }

}
