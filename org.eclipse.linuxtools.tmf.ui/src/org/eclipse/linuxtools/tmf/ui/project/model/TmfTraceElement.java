/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.linuxtools.tmf.TmfCorePlugin;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * <b><u>TmfTraceElement</u></b>
 * <p>
 */
public class TmfTraceElement extends TmfProjectModelElement implements IActionFilter, IPropertySource2 {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // Property keys
    public static final QualifiedName TRACEBUNDLE = new QualifiedName("org.eclipse.linuxtools.tmf", "tracetype.bundle"); //$NON-NLS-1$//$NON-NLS-2$
    public static final QualifiedName TRACETYPE = new QualifiedName("org.eclipse.linuxtools.tmf", "tracetype.id"); //$NON-NLS-1$//$NON-NLS-2$
    public static final QualifiedName TRACEICON = new QualifiedName("org.eclipse.linuxtools.tmf", "tracetype.icon"); //$NON-NLS-1$//$NON-NLS-2$

    // Extension point fields
    public static final String TYPE = "type"; //$NON-NLS-1$
    public static final String ID = "id"; //$NON-NLS-1$
    public static final String CATEGORY = "category"; //$NON-NLS-1$
    public static final String NAME = "name"; //$NON-NLS-1$
    public static final String TRACE_TYPE = "trace_type"; //$NON-NLS-1$
    public static final String EVENT_TYPE = "event_type"; //$NON-NLS-1$
    public static final String ICON = "icon"; //$NON-NLS-1$

    public static final String DEFAULT_EDITOR = "defaultEditor"; //$NON-NLS-1$
    public static final String EVENTS_TABLE_TYPE = "eventsTableType"; //$NON-NLS-1$
    public static final String CLASS = "class"; //$NON-NLS-1$

    // Other attributes
    public static final String BUNDLE = "bundle"; //$NON-NLS-1$
    public static final String IS_LINKED = "isLinked"; //$NON-NLS-1$

    // Property View stuff
    private static final String sfInfoCategory = "Info"; //$NON-NLS-1$
    private static final String sfName = "name"; //$NON-NLS-1$
    private static final String sfPath = "path"; //$NON-NLS-1$
    private static final String sfLocation = "location"; //$NON-NLS-1$
    private static final String sfEventType = "type"; //$NON-NLS-1$
    private static final String sfIsLinked = "linked"; //$NON-NLS-1$

    private static final TextPropertyDescriptor sfNameDescriptor = new TextPropertyDescriptor(sfName, sfName);
    private static final TextPropertyDescriptor sfPathDescriptor = new TextPropertyDescriptor(sfPath, sfPath);
    private static final TextPropertyDescriptor sfLocationDescriptor = new TextPropertyDescriptor(sfLocation, sfLocation);
    private static final TextPropertyDescriptor sfTypeDescriptor = new TextPropertyDescriptor(sfEventType, sfEventType);
    private static final TextPropertyDescriptor sfIsLinkedDescriptor = new TextPropertyDescriptor(sfIsLinked, sfIsLinked);

    private static final IPropertyDescriptor[] sfDescriptors = { sfNameDescriptor, sfPathDescriptor, sfLocationDescriptor,
            sfTypeDescriptor, sfIsLinkedDescriptor };

    static {
        sfNameDescriptor.setCategory(sfInfoCategory);
        sfPathDescriptor.setCategory(sfInfoCategory);
        sfLocationDescriptor.setCategory(sfInfoCategory);
        sfTypeDescriptor.setCategory(sfInfoCategory);
        sfIsLinkedDescriptor.setCategory(sfInfoCategory);
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // This trace type ID as defined in plugin.xml
    private String fTraceTypeId = null;

    // ------------------------------------------------------------------------
    // Static initialization
    // ------------------------------------------------------------------------

    // The mapping of available trace type IDs to their corresponding configuration element
    private static final Map<String, IConfigurationElement> sfTraceTypeAttributes = new HashMap<String, IConfigurationElement>();
    private static final Map<String, IConfigurationElement> sfTraceCategories = new HashMap<String, IConfigurationElement>();

    // Initialize statically at startup
    public static void init() {
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfCorePlugin.TMF_TRACE_TYPE_ID);
        for (IConfigurationElement ce : config) {
            String attribute = ce.getName();
            if (attribute.equals(TYPE)) {
                String traceTypeId = ce.getAttribute(ID);
                sfTraceTypeAttributes.put(traceTypeId, ce);
            } else if (attribute.equals(CATEGORY)) {
                String categoryId = ce.getAttribute(ID);
                sfTraceCategories.put(categoryId, ce);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TmfTraceElement(String name, IResource trace, TmfTraceFolder parent) {
        this(name, trace, (TmfProjectModelElement) parent);
    }

    public TmfTraceElement(String name, IResource trace, TmfExperimentElement parent) {
        this(name, trace, (TmfProjectModelElement) parent);
    }

    private TmfTraceElement(String name, IResource trace, TmfProjectModelElement parent) {
        super(name, trace, parent);
        parent.addChild(this);
        refreshTraceType();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public String getTraceType() {
        return fTraceTypeId;
    }

    public void refreshTraceType() {
        try {
            fTraceTypeId = getResource().getPersistentProperty(TRACETYPE);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public ITmfTrace<?> instantiateTrace() {
        try {
            if (fTraceTypeId != null) {
                IConfigurationElement ce = sfTraceTypeAttributes.get(fTraceTypeId);
                ITmfTrace<?> trace = (ITmfTrace<?>) ce.createExecutableExtension(TRACE_TYPE);
                return trace;
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TmfEvent instantiateEvent() {
        try {
            if (fTraceTypeId != null) {
                IConfigurationElement ce = sfTraceTypeAttributes.get(fTraceTypeId);
                TmfEvent event = (TmfEvent) ce.createExecutableExtension(EVENT_TYPE);
                return event;
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getEditorId() {
        if (fTraceTypeId != null) {
            IConfigurationElement ce = sfTraceTypeAttributes.get(fTraceTypeId);
            IConfigurationElement[] defaultEditorCE = ce.getChildren(DEFAULT_EDITOR);
            if (defaultEditorCE.length == 1) {
                return defaultEditorCE[0].getAttribute(ID);
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // IActionFilter
    // ------------------------------------------------------------------------

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (name.equals(IS_LINKED)) {
            boolean isLinked = getResource().isLinked();
            return Boolean.toString(isLinked).equals(value);
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // TmfTraceElement
    // ------------------------------------------------------------------------

    @Override
    public TmfProjectElement getProject() {
        if (getParent() instanceof TmfTraceFolder) {
            TmfTraceFolder folder = (TmfTraceFolder) getParent();
            TmfProjectElement project = (TmfProjectElement) folder.getParent();
            return project;
        }
        if (getParent() instanceof TmfExperimentElement) {
            TmfExperimentElement experiment = (TmfExperimentElement) getParent();
            TmfExperimentFolder folder = (TmfExperimentFolder) experiment.getParent();
            TmfProjectElement project = (TmfProjectElement) folder.getParent();
            return project;
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // IPropertySource2
    // ------------------------------------------------------------------------

    @Override
    public Object getEditableValue() {
        return null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return sfDescriptors;
    }

    @Override
    public Object getPropertyValue(Object id) {

        if (sfName.equals(id))
            return getName();

        if (sfPath.equals(id))
            return getPath().toString();

        if (sfLocation.equals(id))
            return getLocation().toString();

        if (sfIsLinked.equals(id))
            return Boolean.valueOf(getResource().isLinked()).toString();

        if (sfEventType.equals(id)) {
            if (fTraceTypeId != null) {
                IConfigurationElement ce = sfTraceTypeAttributes.get(fTraceTypeId);
                return (ce != null) ? (getCategory(ce) + " : " + ce.getAttribute(NAME)) : ""; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return null;
    }

    private String getCategory(IConfigurationElement ce) {
        String categoryId = ce.getAttribute(CATEGORY);
        if (categoryId != null) {
            IConfigurationElement category = sfTraceCategories.get(categoryId);
            if (category != null && !category.equals("")) { //$NON-NLS-1$
                return category.getAttribute(NAME);
            }
        }
        return "[no category]"; //$NON-NLS-1$
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
    }

    @Override
    public boolean isPropertyResettable(Object id) {
        return false;
    }

    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

}
