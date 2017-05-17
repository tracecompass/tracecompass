/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.module.TmfXmlAnalysisOutputSource;

/**
 * Class that manages information about a latency view for pattern analysis: its
 * title, the analysis ID, etc.
 *
 * @author Jean-Christian Kouame
 *
 */
public class XmlLatencyViewInfo extends AbstractXmlViewInfo {

    private static final String XML_LATENCY_VIEW_ANALYSIS_ID_PROPERTY = "XmlLatencyAnalysisId"; //$NON-NLS-1$
    private static final String XML_LATENCY_VIEW_LABEL_PROPERTY = "XmlLatencyViewLabel"; //$NON-NLS-1$

    private @Nullable String fAnalysisId = null;
    private @Nullable String fLabel = null;
    // If true, properties were set but not saved to persistent storage
    private boolean fIsDirty = false;

    /**
     * Constructor
     *
     * @param viewId
     *            The ID of the view
     */
    public XmlLatencyViewInfo(String viewId) {
        super(viewId);

        IDialogSettings settings = getPersistentPropertyStore();
        fAnalysisId = settings.get(XML_LATENCY_VIEW_ANALYSIS_ID_PROPERTY);
        fLabel = settings.get(XML_LATENCY_VIEW_LABEL_PROPERTY);
    }

    /**
     * Get the analysis ID this view is for
     *
     * @return The analysis ID this view
     */
    public String getViewAnalysisId() {
        return fAnalysisId;
    }

    /**
     * Get the view label
     *
     * @return The view label
     */
    public String getLabel() {
        return fLabel;
    }

    @Override
    public synchronized void setName(String name) {
        super.setName(name);
        if (fIsDirty) {
            savePersistentData();
        } else {
            IDialogSettings settings = getPersistentPropertyStore();
            fAnalysisId = settings.get(XML_LATENCY_VIEW_ANALYSIS_ID_PROPERTY);
            fLabel = settings.get(XML_LATENCY_VIEW_LABEL_PROPERTY);
        }
    }

    /**
     * Set the data for this view and retrieves from it the analysis ID of the
     * pattern analysis this view belongs to and the view label.
     *
     * @param data
     *            A string of the form "XML analysis ID" +
     *            {@link TmfXmlAnalysisOutputSource#DATA_SEPARATOR} +
     *            "latency view label"
     */
    @Override
    public void setViewData(String data) {
        String[] idFile = data.split(TmfXmlAnalysisOutputSource.DATA_SEPARATOR);
        fAnalysisId = (idFile.length > 0) ? idFile[0] : null;
        fLabel = (idFile.length > 1) ? idFile[1] : null;
        String viewSubsectionName = getName();
        if (viewSubsectionName != null) {
            savePersistentData();
        } else {
            fIsDirty = true;
        }
    }

    @Override
    protected void savePersistentData() {
        IDialogSettings settings = getPersistentPropertyStore();

        settings.put(XML_LATENCY_VIEW_ANALYSIS_ID_PROPERTY, fAnalysisId);
        settings.put(XML_LATENCY_VIEW_LABEL_PROPERTY, fLabel);
    }
}
