/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.IOnDemandAnalysisReport;

/**
 * Project model element containing a report, which is the result of the
 * execution of an on-demand analysis.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfReportElement extends TmfProjectModelElement {

    private final IOnDemandAnalysisReport fReport;

    /**
     * Constructor
     *
     * @param reportName
     *            Name of this report element
     * @param resource
     *            The resource to be associated with this element
     * @param parent
     *            The parent element
     * @param report
     *            The report object represented by this element
     */
    protected TmfReportElement(String reportName, IResource resource,
            TmfReportsElement parent,  IOnDemandAnalysisReport report) {
        super(reportName, resource, parent);
        fReport = report;
    }

    @Override
    public TmfReportsElement getParent() {
        /* Type enforced at constructor */
        return (TmfReportsElement) super.getParent();
    }

    @Override
    public Image getIcon() {
        return TmfProjectModelIcons.DEFAULT_REPORT_ICON;
    }

    @Override
    protected void refreshChildren() {
        /* No children */
    }

    /**
     * Get the report object of this element.
     *
     * @return The report
     */
    public IOnDemandAnalysisReport getReport() {
        return fReport;
    }
}
