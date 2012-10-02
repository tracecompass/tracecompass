/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;

/**
 * Utility class for accessing TMF trace type extensions from the platform's extensions registry.
 *
 * @version 1.0
 * @author Patrick Tasse
 *
 */
public class TmfTraceType {

    /**
     *  Extension point ID
     */
    public static final String TMF_TRACE_TYPE_ID = "org.eclipse.linuxtools.tmf.ui.tracetype"; //$NON-NLS-1$

    /**
     *  Extension point element 'Category'
     */
    public static final String CATEGORY_ELEM = "category"; //$NON-NLS-1$
    /**
     *  Extension point element 'Type'
     */
    public static final String TYPE_ELEM = "type"; //$NON-NLS-1$
    /**
     * Extension point element 'Default editor'
     */
    public static final String DEFAULT_EDITOR_ELEM = "defaultEditor"; //$NON-NLS-1$
    /**
     * Extension point element 'Events table type'
     */
    public static final String EVENTS_TABLE_TYPE_ELEM = "eventsTableType"; //$NON-NLS-1$
    /**
     * Extension point element 'Statistics viewer type'
     *
     * @since 2.0
     */
    public static final String STATISTICS_VIEWER_ELEM = "statisticsViewerType"; //$NON-NLS-1$

    /**
     *  Extension point attribute 'ID'
     */
    public static final String ID_ATTR = "id"; //$NON-NLS-1$
    /**
     * Extension point attribute 'name'
     */
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$
    /**
     * Extension point attribute 'category'
     */
    public static final String CATEGORY_ATTR = "category"; //$NON-NLS-1$
    /**
     * Extension point attribute 'trace_type'
     */
    public static final String TRACE_TYPE_ATTR = "trace_type"; //$NON-NLS-1$
    /**
     * Extension point attribute 'event_type'
     */
    public static final String EVENT_TYPE_ATTR = "event_type"; //$NON-NLS-1$
    /**
     * Extension point attribute 'icon'
     */
    public static final String ICON_ATTR = "icon"; //$NON-NLS-1$
    /**
     * Extension point attribute 'class'
     */
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    /**
     * Retrieves the category name from the platform extension registry based on the category ID
     * @param categoryId The category ID
     * @return the category name or empty string if not found
     */
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

    /**
     * Retrieves and instantiates an element's object based on his plug-in
     * definition for a specific trace type.
     *
     * The element's object is instantiated using its 0-argument constructor.
     *
     * @param resource
     *            The resource where to find the information about the trace
     *            properties
     * @param element
     *            The name of the element to find under the trace type
     *            definition
     * @return a new Object based on his definition in plugin.xml, or null if no
     *         definition was found
     * @since 2.0
     */
    public static Object getTraceTypeElement(IResource resource, String element) {
        try {
            if (resource != null) {
                String traceType = resource.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                /*
                 * Search in the configuration if there is any viewer specified
                 * for this kind of trace type.
                 */
                for (IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                    if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(traceType)) {
                        IConfigurationElement[] viewerCE = ce.getChildren(element);
                        if (viewerCE.length != 1) {
                            break;
                        }
                        return viewerCE[0].createExecutableExtension(TmfTraceType.CLASS_ATTR);
                    }
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating the element from the resource", e); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Retrieves all configuration elements from the platform extension registry
     * for the trace type extension.
     *
     * @return an array of trace type configuration elements
     */
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
