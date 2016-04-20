/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.module.TmfXmlAnalysisOutputSource;

/**
 * Abstract class to manage information about a view for an XML analysis
 *
 * @author Jean-Christian Kouame
 */
public abstract class AbstractXmlViewInfo {

    private final String fViewId;

    /**
     * Constructor
     *
     * @param viewId
     *            The ID of the view
     */
    public AbstractXmlViewInfo(String viewId) {
        fViewId = viewId;
    }

    /**
     * Get this view property section from the settings. The property section is
     * per view ID.
     *
     * @return The property section
     */
    protected IDialogSettings getPersistentPropertyStore() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(fViewId);
        if (section == null) {
            section = settings.addNewSection(fViewId);
            if (section == null) {
                throw new IllegalStateException("The persistent property section could not be added"); //$NON-NLS-1$
            }
        }
        return section;
    }

    /**
     * Set the data for this view and retrieves from it the parameter the view needs
     *
     * @param data
     *            A string of the form "param1" +
     *            {@link TmfXmlAnalysisOutputSource#DATA_SEPARATOR} +
     *            "param2"
     */
    public abstract void setViewData(@NonNull String data);

    /**
     * Save this view persistent data into the settings. For example, user could
     * save the view ID or the label, ...
     */
    protected abstract void savePersistentData();
}
