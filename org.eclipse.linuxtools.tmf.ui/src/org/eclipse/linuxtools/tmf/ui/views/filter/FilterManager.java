/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.filter;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.linuxtools.tmf.core.filter.xml.TmfFilterXMLParser;
import org.eclipse.linuxtools.tmf.core.filter.xml.TmfFilterXMLWriter;
import org.xml.sax.SAXException;

/**
 * Central filter manager
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class FilterManager {

    private static final String SAVED_FILTERS_FILE_NAME = "saved_filters.xml"; //$NON-NLS-1$
    private static final String SAVED_FILTERS_PATH_NAME =
        Activator.getDefault().getStateLocation().addTrailingSeparator().append(SAVED_FILTERS_FILE_NAME).toString();

    private static ITmfFilterTreeNode fRoot = new TmfFilterRootNode();
    static {
        try {
            fRoot = new TmfFilterXMLParser(SAVED_FILTERS_PATH_NAME).getTree();
        } catch (FileNotFoundException e) {
        } catch (SAXException e) {
            Activator.getDefault().logError("Error parsing saved filter xml file: " + SAVED_FILTERS_PATH_NAME, e); //$NON-NLS-1$
        } catch (IOException e) {
            Activator.getDefault().logError("Error parsing saved filter xml file: " + SAVED_FILTERS_PATH_NAME, e); //$NON-NLS-1$
        }
    }

    /**
     * Retrieve the currently saved filters
     *
     * @return The array of filters
     */
    public static ITmfFilterTreeNode[] getSavedFilters() {
        return fRoot.clone().getChildren();
    }

    /**
     * Set the passed filters as the currently saved ones.
     *
     * @param filters
     *            The filters to save
     */
    public static void setSavedFilters(ITmfFilterTreeNode[] filters) {
        fRoot = new TmfFilterRootNode();
        for (ITmfFilterTreeNode filter : filters) {
            fRoot.addChild(filter.clone());
        }
        try {
            TmfFilterXMLWriter writerXML = new TmfFilterXMLWriter(fRoot);
            writerXML.saveTree(SAVED_FILTERS_PATH_NAME);
        } catch (ParserConfigurationException e) {
            Activator.getDefault().logError("Error saving filter xml file: " + SAVED_FILTERS_PATH_NAME, e); //$NON-NLS-1$
        }
    }
}
