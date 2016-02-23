/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Project model element for the "Views" node.
 *
 * For now it contains the list of the standard analyses, with their outputs
 * (views) under each. The plan is to eventually only show the views under this
 * node, since the user cannot really interact with the analyses themselves.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfViewsElement extends TmfProjectModelElement {

    /**
     * Element of the resource path
     */
    public static final String PATH_ELEMENT = ".views"; //$NON-NLS-1$

    private static final String ELEMENT_NAME = Messages.TmfViewsElement_Name;

    /**
     * Constructor
     *
     * @param resource
     *            The resource to be associated with this element
     * @param parent
     *            The parent element
     */
    protected TmfViewsElement(IResource resource, TmfCommonProjectElement parent) {
        super(ELEMENT_NAME, resource, parent);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public TmfCommonProjectElement getParent() {
        /* Type enforced at constructor */
        return (TmfCommonProjectElement) super.getParent();
    }

    @Override
    protected void refreshChildren() {
        /* Refreshes the analysis under this trace */
        Map<String, TmfAnalysisElement> childrenMap = new HashMap<>();
        for (TmfAnalysisElement analysis : getParent().getAvailableAnalysis()) {
            childrenMap.put(analysis.getAnalysisId(), analysis);
        }

        TraceTypeHelper helper = TmfTraceType.getTraceType(getParent().getTraceType());

        Class<@NonNull ? extends ITmfTrace> traceClass = null;

        if (helper != null) {
            traceClass = helper.getTraceClass();
        }

        /* Remove all analysis and return */
        if (traceClass == null) {
            for (TmfAnalysisElement analysis : childrenMap.values()) {
                removeChild(analysis);
            }
            return;
        }

        IPath nodePath = getResource().getFullPath();

        /* Add all new analysis modules or refresh outputs of existing ones */
        for (IAnalysisModuleHelper module : TmfAnalysisManager.getAnalysisModules(traceClass).values()) {

            /* If the analysis is not a child of the trace, create it */
            TmfAnalysisElement analysis = childrenMap.remove(module.getId());
            if (analysis == null) {
                IFolder analysisRes = ResourcesPlugin.getWorkspace().getRoot().getFolder(nodePath.append(module.getId()));
                analysis = new TmfAnalysisElement(module.getName(), analysisRes, this, module);
                addChild(analysis);
            }
            analysis.refreshChildren();
        }

        /* Remove analysis that are not children of this trace anymore */
        for (TmfAnalysisElement analysis : childrenMap.values()) {
            removeChild(analysis);
        }
    }

    @Override
    public Image getIcon() {
        return TmfProjectModelIcons.VIEWS_ICON;
    }
}
