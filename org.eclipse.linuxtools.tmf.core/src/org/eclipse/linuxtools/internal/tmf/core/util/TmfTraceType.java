/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.util;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class TmfTraceType {

    // Extension point ID
    public static final String TMF_TRACE_TYPE_ID = "org.eclipse.linuxtools.tmf.tracetype"; //$NON-NLS-1$

    // Extension point elements
    public static final String CATEGORY_ELEM = "category"; //$NON-NLS-1$
    public static final String TYPE_ELEM = "type"; //$NON-NLS-1$
    public static final String DEFAULT_EDITOR_ELEM = "defaultEditor"; //$NON-NLS-1$
    public static final String EVENTS_TABLE_TYPE_ELEM = "eventsTableType"; //$NON-NLS-1$

    // Extension point attributes
    public static final String ID_ATTR = "id"; //$NON-NLS-1$
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$
    public static final String CATEGORY_ATTR = "category"; //$NON-NLS-1$
    public static final String TRACE_TYPE_ATTR = "trace_type"; //$NON-NLS-1$
    public static final String EVENT_TYPE_ATTR = "event_type"; //$NON-NLS-1$
    public static final String ICON_ATTR = "icon"; //$NON-NLS-1$
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    public static String getCategoryName(String categoryId) {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(TMF_TRACE_TYPE_ID);
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(CATEGORY_ELEM) && categoryId.equals(element.getAttribute(ID_ATTR))) {
                return element.getAttribute(NAME_ATTR);
            }
        }
        return ""; //$NON-NLS-1$
    }

    public static IConfigurationElement[] getTypeElements() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(TMF_TRACE_TYPE_ID);
        List<IConfigurationElement> typeElements = new LinkedList<IConfigurationElement>();
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(TYPE_ELEM)) {
                typeElements.add(element);
            }
        }
        return typeElements.toArray(new IConfigurationElement[typeElements.size()]);
    }
}
