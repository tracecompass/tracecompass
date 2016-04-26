/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.IOnDemandAnalysisReport;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Project model element for the "Reports" element, which lists the analysis
 * reports that were generated for this trace.
 *
 * It acts like a directory for the reports, where each one can be opened or
 * deleted.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfReportsElement extends TmfProjectModelElement {

    /**
     * Element of the resource path
     */
    public static final String PATH_ELEMENT = ".reports"; //$NON-NLS-1$

    private static final String ELEMENT_NAME = Messages.TmfReportsElement_Name;

    private final BiMap<String, IOnDemandAnalysisReport> fCurrentReports = HashBiMap.create();

    /**
     * Constructor
     *
     * @param resource
     *            The resource to be associated with this element
     * @param parent
     *            The parent element
     */
    protected TmfReportsElement(IResource resource, TmfCommonProjectElement parent) {
        super(ELEMENT_NAME, resource, parent);
    }

    @Override
    public TmfCommonProjectElement getParent() {
        /* Type enforced at constructor */
        return (TmfCommonProjectElement) super.getParent();
    }

    @Override
    public Image getIcon() {
        return TmfProjectModelIcons.REPORTS_ICON;
    }

    @Override
    protected void refreshChildren() {
        /* No children at the moment */
    }

    /**
     * Add a new report under this element.
     *
     * @param report
     *            The report to add
     */
    public void addReport(IOnDemandAnalysisReport report) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IPath nodePath = getResource().getFullPath();

        /* Append #2,#3,... to the name if needed */
        String reportDisplayName = report.getName();
        int i = 1;
        while (fCurrentReports.containsKey(reportDisplayName)) {
            i++;
            reportDisplayName = report.getName() + " #" + i; //$NON-NLS-1$
        }

        fCurrentReports.put(reportDisplayName, report);

        IFolder analysisRes = checkNotNull(root.getFolder(nodePath.append(reportDisplayName)));
        TmfReportElement elem = new TmfReportElement(reportDisplayName, analysisRes, this, report);
        addChild(elem);
        refresh();
    }

    /**
     * Remove a report from under this element.
     *
     * @param report
     *            The report to remove
     */
    public void removeReport(IOnDemandAnalysisReport report) {
        String displayName = fCurrentReports.inverse().get(report);
        fCurrentReports.values().remove(report);

        ITmfProjectModelElement elementToRemove = getChildren().stream()
                .filter(elem -> elem.getName().equals(displayName))
                .findFirst().orElse(null);
        removeChild(elementToRemove);
        refresh();
    }
}
