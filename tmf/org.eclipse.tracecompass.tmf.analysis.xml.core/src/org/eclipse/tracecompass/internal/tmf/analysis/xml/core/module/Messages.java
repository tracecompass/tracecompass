/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized messages for the XML analysis module package
 *
 * @author Geneviève Bastien
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.messages"; //$NON-NLS-1$

    /** Property name for file */
    public static String XmlModuleHelper_PropertyFile;

    /** Property name for type */
    public static String XmlModuleHelper_PropertyType;

    /** Error opening XML file */
    public static String XmlUtils_ErrorOpeningFile;
    /** Error copying XML file to workspace folder */
    public static String XmlUtils_ErrorCopyingFile;
    /** Error accessing file */
    public static String XmlUtils_ErrorAccessIO;
    /** IO exception */
    public static String XmlUtils_ExceptionIO;
    /** Error getting analysis modules */
    public static String XmlUtils_ErrorAnalysisModules;
    /** Error cannot find file */
    public static String XmlUtils_ErrorNotFound;
    /** Error fail analyses ID */
    public static String XmlUtils_ErrorAnalysesID;
    /** Error saving preferences */
    public static String XmlUtils_ErrorSavingPreferences;
    /** XML parse error */
    public static String XmlUtils_XmlParseError;
    /** Error occurred while validating XML */
    public static String XmlUtils_XmlValidateError;
    /** XML validation error */
    public static String XmlUtils_XmlValidationError;
    /** XSD validation error */
    public static String XmlUtils_XsdValidationError;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
