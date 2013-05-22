/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import org.eclipse.osgi.util.NLS;

/**
 * The messages for import trace wizards
 * @author Matthew Khouzam
 * @since 2.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.messages"; //$NON-NLS-1$

    public static String ImportTraceWizardImportProblem ;
    public static String ImportTraceWizardImportCaption;
    public static String ImportTraceWizardTraceDisplayName;
    public static String ImportTraceWizardLinkTraces;
    public static String ImportTraceWizardCopyTraces;
    public static String ImportTraceWizardOverwriteTraces;
    public static String ImportTraceWizardAddFile;
    public static String ImportTraceWizardAddDirectory;
    public static String ImportTraceWizardRemove;
    public static String ImportTraceWizardDirectoryTitle;
    public static String ImportTraceWizardDirectoryHint;
    public static String ImportTraceWizardScanPageRenameError;
    public static String ImportTraceWizardScanPageSelectAtleastOne;
    public static String ImportTraceWizardSelectAll;
    public static String ImportTraceWizardScanPageTitle;
    public static String ImportTraceWizardSelectTraceTypePageTitle;
    public static String ImportTraceWizardPageOptionsTitle;
    public static String ImportTraceWizardPageScanDone;
    public static String ImportTraceWizardPageScanScanning;
    public static String ImportTraceWizardPageSelectNone;
    public static String ImportTraceWizardPageSelectHint;
    public static String BatchImportTraceWizardRemove;
    public static String BatchImportTraceWizardAdd;
    public static String BatchImportTraceWizardErrorImportingTraceResource;

    public static String SharedSelectProject;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
