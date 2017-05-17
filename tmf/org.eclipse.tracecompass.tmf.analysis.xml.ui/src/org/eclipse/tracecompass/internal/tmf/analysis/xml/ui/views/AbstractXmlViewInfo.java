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
import org.eclipse.jdt.annotation.Nullable;
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
    private @Nullable String fSubsectionName;

    /**
     * Constructor
     *
     * @param viewId
     *            The ID of the view
     */
    public AbstractXmlViewInfo(String viewId) {
        fViewId = viewId;
        fSubsectionName = null;
    }

    /**
     * Set the name of the view property subsection, which should correspond to a secondary ID
     *
     * @param name
     *            The name of the view property subsection
     */
    public void setName(@NonNull String name) {
        fSubsectionName = name;
    }

    /**
     * Get the name of the view property subsection, which should correspond to the view's secondary
     * ID. If this name is not null, it will never be null again
     *
     * @return The name of the view property subsection
     */
    protected @Nullable String getName() {
        return fSubsectionName;
    }

    /**
     * Get this view property section from the settings. The property section is
     * per view ID and view name if available.
     *
     * @return The property section
     */
    protected IDialogSettings getPersistentPropertyStore() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(fViewId);
        if (section == null) {
            section = settings.addNewSection(fViewId);
            if (section == null) {
                throw new IllegalStateException("The persistent property section could not be added " + fViewId); //$NON-NLS-1$
            }
        }
        if (fSubsectionName == null) {
            return section;
        }
        // FIXME: when a file is removed from TraceCompass, its view section should also be deleted
        IDialogSettings subSection = section.getSection(fSubsectionName);
        if (subSection == null) {
            subSection = section.addNewSection(fSubsectionName);
            if (subSection == null) {
                throw new IllegalStateException("The persistent property section could not be added: " + fSubsectionName); //$NON-NLS-1$
            }
        }
        return subSection;
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
