/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.viewers.events;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Implements basic UI support for TMF events.
 */
public class TmfEventAdapterFactory implements IAdapterFactory {

    private static Class<?>[] PROPERTIES = new Class[] {
        IPropertySource.class
    };

    @Override
    public Class<?>[] getAdapterList() {
        return PROPERTIES;
    }

    @Override
    public <T> T getAdapter(Object element, Class<T> adapterType) {
        ITmfEvent tmfEvent = (ITmfEvent) element;
        if (IPropertySource.class.equals(adapterType)) {
            return adapterType.cast(new TmfEventPropertySource(tmfEvent));
        }
        return null;
    }
}
