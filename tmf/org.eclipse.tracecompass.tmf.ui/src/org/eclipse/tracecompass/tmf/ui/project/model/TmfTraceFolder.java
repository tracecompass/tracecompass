/*******************************************************************************
 * Copyright (c) 2011, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

/**
 * Implementation of trace folder model element representing a trace folder in
 * the project.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfTraceFolder extends TmfProjectModelElement implements IActionFilter, IPropertySource2 {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String INFO_CATEGORY = "Info"; //$NON-NLS-1$
    private static final String NAME = "name"; //$NON-NLS-1$
    private static final String PATH = "path"; //$NON-NLS-1$
    private static final String LOCATION = "location"; //$NON-NLS-1$
    /** IsLinked attribute name. */
    private static final String IS_LINKED = "isLinked"; //$NON-NLS-1$
    private static final String IS_LINKED_PROPERTY = Messages.TmfTraceElement_IsLinked;

    private static final ReadOnlyTextPropertyDescriptor NAME_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(NAME, NAME);
    private static final ReadOnlyTextPropertyDescriptor PATH_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(PATH, PATH);
    private static final ReadOnlyTextPropertyDescriptor LOCATION_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(LOCATION, LOCATION);
    private static final ReadOnlyTextPropertyDescriptor IS_LINKED_DESCRIPTOR = new ReadOnlyTextPropertyDescriptor(IS_LINKED_PROPERTY, IS_LINKED_PROPERTY);

    private static final IPropertyDescriptor[] DESCRIPTORS = { NAME_DESCRIPTOR, PATH_DESCRIPTOR, LOCATION_DESCRIPTOR, IS_LINKED_DESCRIPTOR };

    static {
        NAME_DESCRIPTOR.setCategory(INFO_CATEGORY);
        PATH_DESCRIPTOR.setCategory(INFO_CATEGORY);
        LOCATION_DESCRIPTOR.setCategory(INFO_CATEGORY);
        IS_LINKED_DESCRIPTOR.setCategory(INFO_CATEGORY);
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor. Creates folder model element under the project.
     *
     * @param name
     *            The name of trace folder.
     * @param resource
     *            The folder resource.
     * @param parent
     *            The parent element (project).
     */
    public TmfTraceFolder(String name, IFolder resource, TmfProjectElement parent) {
        super(name, resource, parent);
    }

    /**
     * Constructor. Creates folder model element under another folder.
     *
     * @param name
     *            The name of trace folder.
     * @param resource
     *            The folder resource.
     * @param parent
     *            The parent element (folder).
     */
    public TmfTraceFolder(String name, IFolder resource, TmfTraceFolder parent) {
        super(name, resource, parent);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public IFolder getResource() {
        return (IFolder) super.getResource();
    }

    /**
     * @since 2.0
     */
    @Override
    protected synchronized void refreshChildren() {
        IFolder folder = getResource();

        // Get the children from the model
        Map<String, ITmfProjectModelElement> childrenMap = new HashMap<>();
        for (ITmfProjectModelElement element : getChildren()) {
            childrenMap.put(element.getResource().getName(), element);
        }

        try {
            IResource[] members = folder.members();
            for (IResource resource : members) {
                String name = resource.getName();
                boolean isFolder = resource instanceof IFolder &&
                        (TmfTraceType.getTraceTypeId(resource) == null);
                ITmfProjectModelElement element = childrenMap.get(name);
                if (isFolder && !(element instanceof TmfTraceFolder) && !(element instanceof TmfTraceElement)) {
                    if (TmfTraceType.isDirectoryTrace(resource.getLocationURI().getPath())) {
                        element = new TmfTraceElement(name, resource, this);
                    } else {
                        element = new TmfTraceFolder(name, (IFolder) resource, this);
                    }
                    addChild(element);
                } else if (!isFolder && !(element instanceof TmfTraceElement)) {
                    element = new TmfTraceElement(name, resource, this);
                    addChild(element);
                } else {
                    childrenMap.remove(name);
                }
                if (element != null) {
                    ((TmfProjectModelElement) element).refreshChildren();
                }
            }
        } catch (CoreException e) {
        }

        // Cleanup dangling children from the model
        for (ITmfProjectModelElement danglingChild : childrenMap.values()) {
            removeChild(danglingChild);
        }
    }

    /**
     * @since 2.0
     */
    @Override
    public Image getIcon() {
        return TmfProjectModelIcons.FOLDER_ICON;
    }

    /**
     * @since 2.0
     */
    @Override
    public String getLabelText() {
        int nbTraces = getTraces().size();
        if (nbTraces > 0) {
            return (getName() + " [" + nbTraces + ']'); //$NON-NLS-1$
        }
        return getName();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns a list of trace elements under the folder element, recursively.
     * @return list of trace model elements
     */
    public List<@NonNull TmfTraceElement> getTraces() {
        List<ITmfProjectModelElement> children = getChildren();
        List<@NonNull TmfTraceElement> traces = new ArrayList<>();
        for (ITmfProjectModelElement child : children) {
            if (child instanceof TmfTraceElement) {
                traces.add((TmfTraceElement) child);
            } else if (child instanceof TmfTraceFolder) {
                traces.addAll(((TmfTraceFolder) child).getTraces());
            }
        }
        return traces;
    }

    /**
     * Gets the traces elements under this folder containing the given resources
     *
     * @param resources
     *            resources to search for
     * @return list of trace elements
     * @since 2.0
     */
    public @NonNull List<TmfTraceElement> getTraceElements(@NonNull List<IResource> resources) {
        return resources.stream()
                .flatMap(resource -> getTraces().stream()
                        .filter(traceElement -> traceElement.getResource().equals(resource)))
                .distinct()
                .collect(Collectors.toList());
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
    // IPropertySource2
    // ------------------------------------------------------------------------

    @Override
    public Object getEditableValue() {
        return null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return Arrays.copyOf(DESCRIPTORS, DESCRIPTORS.length);
    }

    @Override
    public Object getPropertyValue(Object id) {

        if (NAME.equals(id)) {
            return getName();
        }

        if (PATH.equals(id)) {
            return getPath().toString();
        }

        if (LOCATION.equals(id)) {
            return getLocation().toString();
        }

        if (IS_LINKED_PROPERTY.equals(id)) {
            return Boolean.toString(getResource().isLinked());
        }

        return null;
    }

    @Override
    public void resetPropertyValue(Object id) {
        // Do nothing
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        // Do nothing
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
