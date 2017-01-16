/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Refactor resource change listener
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;

/**
 * The implementation of TMF project model element.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfProjectElement extends TmfProjectModelElement {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    @Nullable private TmfTraceFolder fTraceFolder = null;
    @Nullable private TmfExperimentFolder fExperimentFolder = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * Creates the TMF project model element.
     *
     * @param name
     *            The name of the project.
     * @param project
     *            The project reference.
     * @param parent
     *            The parent element
     */
    public TmfProjectElement(String name, IProject project, ITmfProjectModelElement parent) {
        super(name, project, parent);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public IProject getResource() {
        return (IProject) super.getResource();
    }

    @Override
    public void addChild(ITmfProjectModelElement child) {
        super.addChild(child);
        if (child instanceof TmfTraceFolder) {
            fTraceFolder = (TmfTraceFolder) child;
            return;
        }
        if (child instanceof TmfExperimentFolder) {
            fExperimentFolder = (TmfExperimentFolder) child;
            return;
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the containing trace folder element.
     * @return the TMF trace folder element.
     */
    @Nullable
    public TmfTraceFolder getTracesFolder() {
        return fTraceFolder;
    }

    /**
     * Returns the containing experiment folder element.
     * @return the TMF experiment folder element.
     */
    @Nullable public TmfExperimentFolder getExperimentsFolder() {
        return fExperimentFolder;
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    protected void refreshChildren() {
        IProject project = getResource();

        // Get the children from the model
        Map<String, ITmfProjectModelElement> childrenMap = new HashMap<>();
        for (ITmfProjectModelElement element : getChildren()) {
            childrenMap.put(element.getResource().getName(), element);
        }

        // Add the model folder if the corresponding resource exists and is not
        // accounted for
        IFolder folder = project.getFolder(TmfTracesFolder.TRACES_RESOURCE_NAME);
        if (folder != null && folder.exists()) {
            String name = folder.getName();
            ITmfProjectModelElement element = childrenMap.get(name);
            if (element instanceof TmfTracesFolder) {
                childrenMap.remove(name);
            } else {
                element = new TmfTracesFolder(TmfTracesFolder.TRACES_RESOURCE_NAME, folder, this);
                addChild(element);
            }
            ((TmfTracesFolder) element).refreshChildren();
        }

        // Add the model folder if the corresponding resource exists and is not
        // accounted for
        folder = project.getFolder(TmfExperimentFolder.EXPER_RESOURCE_NAME);
        if (folder != null && folder.exists()) {
            String name = folder.getName();
            ITmfProjectModelElement element = childrenMap.get(name);
            if (element instanceof TmfExperimentFolder) {
                childrenMap.remove(name);
            } else {
                element = new TmfExperimentFolder(TmfExperimentFolder.EXPER_RESOURCE_NAME, folder, this);
                addChild(element);
            }
            ((TmfExperimentFolder) element).refreshChildren();
        }

        // Cleanup dangling children from the model
        for (ITmfProjectModelElement danglingChild : childrenMap.values()) {
            removeChild(danglingChild);
        }
    }

    @Override
    public TmfProjectElement getProject() {
        return this;
    }

    /**
     * @since 2.0
     */
    @Override
    public Image getIcon() {
        return null;
    }
}
