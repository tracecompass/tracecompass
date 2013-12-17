/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisOutput;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.osgi.framework.Bundle;

/**
 * Class for project elements of type analysis modules
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfAnalysisElement extends TmfProjectModelElement {

    private final String fAnalysisId;

    /**
     * Constructor
     *
     * @param name
     *            Name of the analysis
     * @param resource
     *            The resource
     * @param parent
     *            Parent of the analysis
     * @param id
     *            The analysis module id
     */
    protected TmfAnalysisElement(String name, IResource resource, ITmfProjectModelElement parent, String id) {
        super(name, resource, parent);
        fAnalysisId = id;
        refreshOutputs();
    }

    private void refreshOutputs() {
        List<TmfAnalysisOutputElement> outputs = getAvailableOutputs();

        /* Remove children */
        getChildren().clear();

        /* Add the children again */
        for (TmfAnalysisOutputElement module : outputs) {
            addChild(module);
        }

    }

    /**
     * Get the list of analysis elements
     *
     * @return Array of analysis elements
     */
    public List<TmfAnalysisOutputElement> getAvailableOutputs() {
        List<TmfAnalysisOutputElement> list = new ArrayList<>();

        IAnalysisModuleHelper helper = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (helper == null) {
            return list;
        }

        /** Get base path for resource */
        IPath path = getProject().getTracesFolder().getPath();
        if (fResource instanceof IFolder) {
            path = ((IFolder) fResource).getFullPath();
        }

        /* We can get a list of available outputs once the analysis is instantiated when the trace is opened */
        ITmfProjectModelElement parent = getParent();
        if (parent instanceof TmfTraceElement) {
            ITmfTrace trace = ((TmfTraceElement) parent).getTrace();
            if (trace == null) {
                return list;
            }

            IAnalysisModule module = trace.getAnalysisModule(fAnalysisId);
            if (module == null) {
                return list;
            }

            for (IAnalysisOutput output : module.getOutputs()) {
                if (fResource instanceof IFolder) {
                    IFolder newresource = ResourcesPlugin.getWorkspace().getRoot().getFolder(path.append(output.getName()));
                    TmfAnalysisOutputElement out = new TmfAnalysisOutputElement(output.getName(), newresource, this, output);
                    list.add(out);
                }
            }
        }
        return list;
    }

    @Override
    public TmfProjectElement getProject() {
        return getParent().getProject();
    }

    /**
     * Gets the analysis id of this module
     *
     * @return The analysis id
     */
    public String getAnalysisId() {
        return fAnalysisId;
    }

    /**
     * Gets the help message for this analysis
     *
     * @return The help message
     */
    public String getHelpMessage() {
        IAnalysisModuleHelper helper = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (helper == null) {
            return new String();
        }

        return helper.getHelpText();
    }

    /**
     * Gets the icon file name for the analysis
     *
     * @return The analysis icon file name
     */
    public String getIconFile() {
        IAnalysisModuleHelper helper = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (helper == null) {
            return null;
        }
        return helper.getIcon();
    }

    /**
     * Gets the bundle this analysis is from
     *
     * @return The analysis bundle
     */
    public Bundle getBundle() {
        IAnalysisModuleHelper helper = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (helper == null) {
            return null;
        }
        return helper.getBundle();
    }

    /**
     * Make sure the trace this analysis is associated to is the currently selected one
     */
    public void activateParent() {
        ITmfProjectModelElement parent = getParent();

        if (parent instanceof TmfTraceElement) {
            TmfTraceElement traceElement = (TmfTraceElement) parent;
            TmfOpenTraceHelper.openTraceFromElement(traceElement);
        }
    }
}
