/*******************************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Class for project elements of type compound analysis modules.
 *
 * This element aggregates analyses with the same type that come from various
 * traces contained in an experiment. This allows to show trace analyses and
 * their views under the experiment's view element.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public class TmfAggregateAnalysisElement extends TmfAnalysisElement {

    private final @NonNull Set<TmfAnalysisElement> fContainedAnalyses = new HashSet<>();
    private final @NonNull TmfCommonProjectElement fExperimentParent;

    /**
     * Constructor
     *
     * @param experiment
     *            The element to use for experiment activation.
     *
     * @param module
     *            The analysis module helper.
     *            This helper is used in super and acts as a delegate
     *            helper representing all contained analyses elements.
     */
    protected TmfAggregateAnalysisElement(@NonNull TmfCommonProjectElement experiment, @NonNull TmfAnalysisElement module) {
        super (module.getName(), module.getResource(), module.getParent(), module.getAnalysisHelper());
        fExperimentParent = experiment;
        fContainedAnalyses.add(module);
    }

    @Override
    protected synchronized void refreshChildren() {
        // refresh all children analysis as well
        for (TmfAnalysisElement analysis : fContainedAnalyses) {
            analysis.refreshChildren();
        }
        super.refreshChildren();
    }

    @Override
    public boolean canExecute() {
        for (TmfAnalysisElement analysis : fContainedAnalyses) {
            if (analysis.canExecute()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add an analysis element that is combined in the compound element.
     *
     * @param element
     *          analysis element to add
     */
    public void addAnalyses(@NonNull TmfAnalysisElement element) {
        fContainedAnalyses.add(element);
    }

    /**
     * Remove an analysis element that is combined in the compound element.
     *
     * @param element
     *            analysis element to remove
     */
    public void removeAnalyses(@NonNull TmfAnalysisElement element) {
        fContainedAnalyses.remove(element);
    }

    /**
     * Checks if aggregated list is empty or not
     *
     * @return <code>true<code> if empty else <code>false</code>
     */
    public boolean isEmpty() {
        return fContainedAnalyses.isEmpty();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Gets the help message for this analysis
     *
     * @return The help message
     */
    @Override
    public String getHelpMessage() {
        Set<String> messages = new HashSet<>();
        for (TmfAnalysisElement analysis : fContainedAnalyses) {
            messages.add(analysis.getHelpMessage());
        }
        if (!messages.isEmpty()) {
            return String.join(",", messages); //$NON-NLS-1$
        }
        return super.getHelpMessage();
    }

    /**
     * Make sure the trace this analysis is associated to is the currently
     * selected one
     */
    @Override
    public void activateParentTrace() {
        TmfOpenTraceHelper.openTraceFromElement(fExperimentParent);
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        Set<TmfAnalysisElement> analyses = fContainedAnalyses;
        List<IPropertyDescriptor> descs = new ArrayList<>();
        for (TmfAnalysisElement analysis : analyses) {
            if (analysis.canExecute()) {
                descs.add(new ReadOnlyTextPropertyDescriptor(analysis.getResource(), analysis.getParent().getParent().getLabelText()));
            }
        }
        return descs.toArray(new IPropertyDescriptor[0]);
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (id instanceof IResource) {
            Set<TmfAnalysisElement> analyses = fContainedAnalyses;
            for (TmfAnalysisElement tmfAnalysisElement : analyses) {
                if (id.equals(tmfAnalysisElement.getResource())) {
                    return tmfAnalysisElement;
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasChildren() {
        for (TmfAnalysisElement analysis : fContainedAnalyses) {
            if (analysis.hasChildren()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ITmfProjectModelElement> getChildren() {
        List<ITmfProjectModelElement> children = new ArrayList<>();
        for (TmfAnalysisElement analysis : fContainedAnalyses) {
            if (analysis.hasChildren()) {
                for (ITmfProjectModelElement output : analysis.getChildren()) {
                    if (output instanceof TmfAnalysisOutputElement) {
                        TmfAnalysisOutputElement newOutput = new TmfAnalysisOutputElement(output.getName(), output.getResource(), this, ((TmfAnalysisOutputElement) output).getOutput());
                        children.add(newOutput);
                    }
                }
                return children;
            }
        }
        return children;
    }

    @Override
    public TmfViewsElement getParent() {
        return fExperimentParent.getChildElementViews();
    }
}
