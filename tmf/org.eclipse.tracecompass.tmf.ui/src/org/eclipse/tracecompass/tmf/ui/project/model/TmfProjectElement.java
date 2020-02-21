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
 *   Patrick Tasse - Refactor resource change listener
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;

/**
 * The implementation of TMF project model element.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfProjectElement extends TmfProjectModelElement {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    static final String TRACECOMPASS_PROJECT_FILE = ".tracecompass"; //$NON-NLS-1$

    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private @Nullable TmfTraceFolder fTraceFolder = null;
    private @Nullable TmfExperimentFolder fExperimentFolder = null;
    private @Nullable IFolder fSupplFolder = null;

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

    /**
     * @return returns the supplementary folder
     *
     * @since 3.2
     */
    public IFolder getSupplementaryFolder() {
        return fSupplFolder;
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    protected synchronized void refreshChildren() {
        IProject project = getResource();

        // Get the children from the model
        Map<String, ITmfProjectModelElement> childrenMap = new HashMap<>();
        for (ITmfProjectModelElement element : getChildren()) {
            childrenMap.put(element.getResource().getName(), element);
        }

        TmfProjectConfig config = getFolderStructure(project);

        // Add the model folder if the corresponding resource exists and is not
        // accounted for
        IFolder folder = config.getTracesFolder();
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
        folder = config.getExperimentsFolder();
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

        fSupplFolder = config.getSupplementaryFolder();

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
        return TmfProjectModelPreferences.getProjectModelIcon();
    }

    @Override
    public String getLabelText() {
        return TmfProjectModelPreferences.getProjectModelLabel();
    }

    static void createFolderStructure(IContainer parent) throws CoreException {
        IFolder folder = parent.getFolder(new Path(TmfTracesFolder.TRACES_RESOURCE_NAME));
        createFolder(folder);

        folder = parent.getFolder(new Path(TmfExperimentFolder.EXPER_RESOURCE_NAME));
        createFolder(folder);

        // create folder for supplementary tracing files
        folder = parent.getFolder(new Path(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME));
        createFolder(folder);
    }

    static IFolder createFolderStructure(IProject project, IProject shadowProject) throws CoreException {
        if (shadowProject != null) {
            createFolderStructure(shadowProject);
        }
        IFolder parentFolder = project.getFolder(TRACECOMPASS_PROJECT_FILE);
        createFolder(parentFolder);
        return parentFolder;
    }

    static TmfProjectConfig getFolderStructure(IProject project) {
        TmfProjectConfig.Builder builder = new TmfProjectConfig.Builder();

        IFolder folder = project.getFolder(new Path(TmfTracesFolder.TRACES_RESOURCE_NAME));
        builder.setTracesFolder(folder);

        folder = project.getFolder(new Path(TmfExperimentFolder.EXPER_RESOURCE_NAME));
        builder.setExperimentsFolder(folder);

        // create folder for supplementary tracing files
        folder = project.getFolder(new Path(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME));
        builder.setSupplementaryFolder(folder);
        return builder.build();
    }

    private static void createFolder(IFolder folder) throws CoreException {
        if (!folder.exists()) {
            folder.create(true, true, null);
        }
    }

    static boolean showProjectRoot(IProject project) {
        IFolder traceCompassFile = project.getFolder(TRACECOMPASS_PROJECT_FILE);
        return traceCompassFile != null && traceCompassFile.exists();
    }
}
