/*******************************************************************************
 * Copyright (c) 2016, 2018 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.HashMap;
import java.util.List;
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
 * (views) under each. For experiments all analyses from children traces are
 * aggregated and shown under the "Views" node.
 *
 * The plan is to eventually only show the views under this node, since the
 * user cannot really interact with the analyses themselves.
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
    protected synchronized void refreshChildren() {
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

        TmfCommonProjectElement parent = getParent();

        if (parent instanceof TmfTraceElement) {
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
        } else if (parent != null) {
            /* In experiment case collect trace analyses in the aggregate analyses element */
            Map<String, TmfAggregateAnalysisElement> analysisMap = new HashMap<>();

            /* Add all new analysis modules or refresh outputs of existing ones */
            for (IAnalysisModuleHelper module : TmfAnalysisManager.getAnalysisModules(traceClass).values()) {

                /* If the analysis is not a child of the trace, create it */
                TmfAnalysisElement analysis = childrenMap.remove(module.getId());
                TmfAggregateAnalysisElement aggregateAnalysisElement = null;
                if (analysis == null) {
                    IFolder analysisRes = ResourcesPlugin.getWorkspace().getRoot().getFolder(nodePath.append(module.getId()));
                    analysis = new TmfAnalysisElement(module.getName(), analysisRes, this, module);
                    aggregateAnalysisElement = new TmfAggregateAnalysisElement(parent, analysis);
                    addChild(aggregateAnalysisElement);
                } else {
                    if (analysis instanceof TmfAggregateAnalysisElement) {
                        aggregateAnalysisElement = (TmfAggregateAnalysisElement) analysis;
                    } else {
                        aggregateAnalysisElement = new TmfAggregateAnalysisElement(parent, analysis);
                    }
                    removeChild(analysis);
                    addChild(aggregateAnalysisElement);
                }
                analysisMap.put(analysis.getAnalysisId(), aggregateAnalysisElement);
            }

            /* Now add all available trace analyses */
            for (TmfAnalysisElement analysis : getParent().getAvailableChildrenAnalyses()) {
                /* If the analysis is not a child of the trace, create it */
                TmfAnalysisElement a = childrenMap.remove(analysis.getAnalysisId());

                TmfAggregateAnalysisElement childAnalysis = null;

                if (a instanceof TmfAggregateAnalysisElement) {
                    childAnalysis = (TmfAggregateAnalysisElement) a;
                } else {
                    childAnalysis = analysisMap.get(analysis.getAnalysisId());
                }

                if (childAnalysis == null) {
                    childAnalysis = new TmfAggregateAnalysisElement(parent, analysis);
                    addChild(childAnalysis);
                } else {
                    childAnalysis.addAnalyses(analysis);
                }
                analysisMap.put(analysis.getAnalysisId(), childAnalysis);
            }

            /* Remove analysis that are not children of this trace anymore */
            for (TmfAnalysisElement analysis : childrenMap.values()) {
                removeChild(analysis);
            }
        }
    }

    @Override
    public Image getIcon() {
        return TmfProjectModelIcons.VIEWS_ICON;
    }

    /**
     * Remove children analysis from aggregated traces
     *
     * @param analysisElements
     *              list of analysis elements to remove
     *
     * @since 3.0
     */
    public void removeChildrenAnalysis(List<@NonNull TmfAnalysisElement> analysisElements) {
        for (TmfAnalysisElement tmfAnalysisElement : analysisElements) {
            TmfAggregateAnalysisElement aggrElement = getAggregateAnalysisElement(tmfAnalysisElement);
            if (aggrElement != null) {
                aggrElement.removeAnalyses(tmfAnalysisElement);
                if (aggrElement.isEmpty()) {
                    removeChild(aggrElement);
                }
            }
        }
    }

    private TmfAggregateAnalysisElement getAggregateAnalysisElement(TmfAnalysisElement element) {
        return getChildren().stream()
                .filter(TmfAggregateAnalysisElement.class::isInstance)
                .map(elem -> ((TmfAggregateAnalysisElement) elem))
                .filter(elem -> elem.getAnalysisHelper().getId().equals(element.getAnalysisHelper().getId()))
                .findFirst().orElse(null);
    }
}
