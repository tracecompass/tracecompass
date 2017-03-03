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

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.module.TmfXmlAnalysisOutputSource;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * Class that manages information about a view: its title, the file, etc.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class XmlViewInfo extends AbstractXmlViewInfo {

    private static final String XML_VIEW_ID_PROPERTY = "XmlViewId"; //$NON-NLS-1$
    private static final String XML_VIEW_FILE_PROPERTY = "XmlViewFile"; //$NON-NLS-1$

    /* Initialization is completed when name is set */
    private final CountDownLatch fInitialized = new CountDownLatch(1);
    /** This is the ID of the view described in the XML file */
    private @Nullable String fId = null;
    private @Nullable String fFilePath = null;
    // If true, properties were set but not saved to persistent storage
    private boolean fIsDirty = false;

    /**
     * Constructor
     *
     * @param viewId
     *            The ID of the view
     */
    public XmlViewInfo(String viewId) {
        super(viewId);
        /* Cannot get the properties yet, need to wait for the name */
    }

    /**
     * Waits for the view info's initialization to be completed. This happens
     * once the name has been set
     *
     * @return Whether the initialization has completed successfully
     */
    public boolean waitForInitialization() {
        try {
            fInitialized.await();
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    /**
     * Set the data for this view and retrieves from it the view ID and the file
     * path of the XML element this view uses.
     *
     * @param data
     *            A string of the form "XML view ID" +
     *            {@link TmfXmlAnalysisOutputSource#DATA_SEPARATOR} +
     *            "path of the file containing the XML element"
     */
    @Override
    public synchronized void setViewData(String data) {
        String[] idFile = data.split(TmfXmlAnalysisOutputSource.DATA_SEPARATOR);
        fId = (idFile.length > 0) ? idFile[0] : null;
        fFilePath = (idFile.length > 1) ? idFile[1] : null;
        String viewName = getName();
        if (viewName != null) {
            savePersistentData();
        } else {
            fIsDirty = true;
        }
    }

    @Override
    public synchronized void setName(String name) {
        super.setName(name);
        if (fIsDirty) {
            savePersistentData();
        } else {
            IDialogSettings settings = getPersistentPropertyStore();
            fId = settings.get(XML_VIEW_ID_PROPERTY);
            fFilePath = settings.get(XML_VIEW_FILE_PROPERTY);
        }
        fInitialized.countDown();
    }

    @Override
    protected void savePersistentData() {
        IDialogSettings settings = getPersistentPropertyStore();

        settings.put(XML_VIEW_ID_PROPERTY, fId);
        settings.put(XML_VIEW_FILE_PROPERTY, fFilePath);
    }

    /**
     * Retrieve the XML element corresponding to the view
     *
     * @param xmlTag
     *            The XML tag corresponding to the view element (the type of
     *            view)
     * @return The view {@link Element}
     */
    public @Nullable Element getViewElement(String xmlTag) {
        String id = fId;
        if (id == null) {
            return null;
        }
        Element viewElement = TmfXmlUtils.getElementInFile(fFilePath, xmlTag, id);
        return viewElement;
    }

    /**
     * Get the view title from the header information of the XML view element.
     *
     * @param viewElement
     *            The XML view element from which to get the title
     * @return The view title
     */
    public @Nullable String getViewTitle(Element viewElement) {
        List<Element> heads = TmfXmlUtils.getChildElements(viewElement, TmfXmlStrings.HEAD);

        String title = null;
        if (!heads.isEmpty()) {
            Element head = heads.get(0);
            /* Set the title of this view from the label in the header */
            List<Element> labels = TmfXmlUtils.getChildElements(head, TmfXmlStrings.LABEL);
            for (Element label : labels) {
                if (!label.getAttribute(TmfXmlStrings.VALUE).isEmpty()) {
                    title = label.getAttribute(TmfXmlStrings.VALUE);
                }
                break;
            }
        }
        return title;
    }

    /**
     * Get the list of analysis IDs this view is for, as listed in the header of
     * the XML element
     *
     * @param viewElement
     *            The XML view element from which to get the analysis IDs
     * @return The list of all analysis IDs this view is for
     */
    public Set<String> getViewAnalysisIds(Element viewElement) {
        List<Element> heads = TmfXmlUtils.getChildElements(viewElement, TmfXmlStrings.HEAD);

        Set<String> analysisIds = new HashSet<>();
        if (!heads.isEmpty()) {
            Element head = heads.get(0);

            /* Get the application analysis from the view's XML header */
            List<Element> applicableAnalysis = TmfXmlUtils.getChildElements(head, TmfXmlStrings.ANALYSIS);
            for (Element oneAnalysis : applicableAnalysis) {
                analysisIds.add(oneAnalysis.getAttribute(TmfXmlStrings.ID));
            }
        }
        return analysisIds;
    }
}
