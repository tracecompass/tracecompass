/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Ensure backwards compatibility to Linux Tools
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.filter.xml.TmfFilterXMLParser;
import org.eclipse.tracecompass.tmf.core.filter.xml.TmfFilterXMLWriter;
import org.xml.sax.SAXException;

/**
 * Central filter manager
 *
 * @version 1.0
 * @author Patrick Tasse
 * @since 4.0
 */
public class FilterManager {

    private static final String SAVED_FILTERS_FILE_NAME = "saved_filters.xml"; //$NON-NLS-1$
    private static final String SAVED_FILTERS_PATH_NAME =
        Activator.getDefault().getStateLocation().addTrailingSeparator().append(SAVED_FILTERS_FILE_NAME).toString();

    /*
     * Legacy path to the XML definitions file (in Linux Tools)
     *  TODO Remove once we feel the transition phase is over.
     */
    private static final IPath SAVED_FILTERS_FILE_NAME_LEGACY =
            Activator.getDefault().getStateLocation().removeLastSegments(1)
                    .append("org.eclipse.linuxtools.tmf.ui") //$NON-NLS-1$
                    .append(SAVED_FILTERS_FILE_NAME);


    private static ITmfFilterTreeNode fRoot = new TmfFilterRootNode();
    static {

        File defaultFile = new File(SAVED_FILTERS_PATH_NAME);

        try {
            /*
             * If there is no file at the expected location, check the legacy
             * location instead.
             */
            if (!defaultFile.exists()) {
                File legacyFileCore = SAVED_FILTERS_FILE_NAME_LEGACY.toFile();
                if (legacyFileCore.exists()) {
                    ITmfFilterTreeNode root = new TmfFilterXMLParser(SAVED_FILTERS_FILE_NAME_LEGACY.toString()).getTree();
                    setSavedFilters(root.getChildren());
                }
            }
        } catch (FileNotFoundException e) {
        } catch (SAXException e) {
            Activator.logError("Error parsing saved filter xml file: " + SAVED_FILTERS_FILE_NAME_LEGACY, e); //$NON-NLS-1$
        } catch (IOException e) {
            Activator.logError("Error parsing saved filter xml file: " + SAVED_FILTERS_FILE_NAME_LEGACY, e); //$NON-NLS-1$
        }

        try {
            // Now load the filters from the current location
            fRoot = new TmfFilterXMLParser(SAVED_FILTERS_PATH_NAME).getTree();
        } catch (FileNotFoundException e) {
        } catch (SAXException e) {
            Activator.logError("Error parsing saved filter xml file: " + SAVED_FILTERS_PATH_NAME, e); //$NON-NLS-1$
        } catch (IOException e) {
            Activator.logError("Error parsing saved filter xml file: " + SAVED_FILTERS_PATH_NAME, e); //$NON-NLS-1$
        }
    }

    /**
     * Retrieve the currently saved filters
     *
     * @return The array of filters
     */
    public static @NonNull ITmfFilterTreeNode[] getSavedFilters() {
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
            Activator.logError("Error saving filter xml file: " + SAVED_FILTERS_PATH_NAME, e); //$NON-NLS-1$
        }
    }
}
