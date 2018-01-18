/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.module;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.module.TmfXmlAnalysisOutputSource.ViewType;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.ui.analysis.TmfAnalysisViewOutput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Class overriding the default analysis view output for XML views. These views
 * may have labels defined in the XML element and those label will be used as
 * the name of the view
 *
 * @author Geneviève Bastien
 *
 *         TODO: We shouldn't have to do a new class here, we should be able to
 *         set the name in the parent instead
 */
public class TmfXmlViewOutput extends TmfAnalysisViewOutput {

    private String fLabel = null;

    /**
     * Constructor
     *
     * @param viewid
     *            id of the view to display as output
     */
    public TmfXmlViewOutput(String viewid) {
        this(viewid, ViewType.TIME_GRAPH_VIEW);
    }

    /**
     * Constructor
     *
     * @param viewid
     *            id of the view to display as output
     * @param viewType
     *            type of view this output is for
     */
    public TmfXmlViewOutput(String viewid, @NonNull ViewType viewType) {
        super(viewid);
    }

    @Override
    public String getName() {
        if (fLabel == null) {
            return super.getName();
        }
        return fLabel;
    }

    @Override
    protected IViewPart openView() throws PartInitException {
        final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();

        return activePage.showView(getViewId(), getName(), IWorkbenchPage.VIEW_ACTIVATE);
    }

    @Override
    public void setOutputProperty(@NonNull String key, String value, boolean immediate) {
        super.setOutputProperty(key, value, immediate);
        /* Find the label of the view */
        if (key.equals(TmfXmlStrings.XML_OUTPUT_DATA)) {
            String[] idFile = value.split(TmfXmlAnalysisOutputSource.DATA_SEPARATOR);
            String label = (idFile.length > 2) ? idFile[2] : ""; //$NON-NLS-1$

            if (label.isEmpty()) {
                return;
            }

            fLabel = label;
        }
    }
}
