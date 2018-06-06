/*******************************************************************************
 * Copyright (c) 2016, 2018 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.IOnDemandAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Project model element for individual on-demand analyses that apply to the
 * related trace.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
@NonNullByDefault
public abstract class TmfOnDemandAnalysisElement extends TmfProjectModelElement
        implements ITmfStyledProjectModelElement {

    /**
     * Checking if the analysis can run may take some time (running external
     * scripts, etc.) so we do this outside of the UI thread. The executor will
     * reserve one thread to check all on-demand analyses sequentially.
     */
    private static final ExecutorService EXECUTOR = checkNotNull(Executors.newFixedThreadPool(1));

    /**
     * Worker thread sent to the executor
     */
    private static class AnalysisChecker implements Runnable {

        private final TmfOnDemandAnalysisElement fElem;

        public AnalysisChecker(TmfOnDemandAnalysisElement elem) {
            fElem = elem;
        }

        @Override
        public void run() {
            ITmfTrace trace = fElem.getParent().getParent().getTrace();
            if (trace != null) {
                fElem.fCanRun = fElem.getAnalysis().canExecute(trace);
                fElem.refresh();
            }
        }
    }

    /**
     * Styler to strike-out the analysis when it is not available
     */
    private static final Styler STRIKED_OUT_STYLER = new Styler() {
        @Override
        public void applyStyles(@Nullable TextStyle textStyle) {
            if (textStyle != null) {
                textStyle.strikeout = true;
            }
        }
    };

    private final IOnDemandAnalysis fAnalysis;

    private volatile boolean fKnowIfCanRun = false;
    private volatile boolean fCanRun = false;

    /**
     * Constructor
     *
     * @param analysisName
     *            Name of the element
     * @param resource
     *            Workspace resource
     * @param parent
     *            Parent element, should be the "on-demand analyses" one
     * @param analysis
     *            The actual analysis represented by this element
     */
    protected TmfOnDemandAnalysisElement(String analysisName, IResource resource,
            TmfOnDemandAnalysesElement parent, IOnDemandAnalysis analysis) {
        super(analysisName, resource, parent);
        fAnalysis = analysis;
    }

    @Override
    public TmfOnDemandAnalysesElement getParent() {
        return checkNotNull((TmfOnDemandAnalysesElement) super.getParent());
    }

    @Override
    public abstract Image getIcon();

    @Override
    protected synchronized void refreshChildren() {
        /* No children, but determine if we can run or not. */
        if (!fKnowIfCanRun) {
            fKnowIfCanRun = true;
            EXECUTOR.execute(new AnalysisChecker(this));
        }
    }

    @Override
    public @Nullable Styler getStyler() {
        if (!fCanRun) {
            return STRIKED_OUT_STYLER;
        }
        return null;
    }

    /**
     * Get the analysis represented by this model element
     *
     * @return The real analysis
     */
    public IOnDemandAnalysis getAnalysis() {
        return fAnalysis;
    }

    /**
     * Return if the analysis can be run or not. Determines if we should enable
     * the run actions.
     *
     * @return If this analysis can run
     */
    public boolean canRun() {
        return fCanRun;
    }
}
