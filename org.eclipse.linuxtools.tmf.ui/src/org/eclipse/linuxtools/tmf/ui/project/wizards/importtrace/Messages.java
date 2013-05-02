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

    public static String ImportTraceWizard_ImportProblem ;
    public static String ImportTraceWizardImportCaption;
    public static String ImportTraceWizardTraceDisplayName;
    public static String ImportTraceWizardLinkTraces;
    public static String ImportTraceWizardCopyTraces;
    public static String ImportTraceWizardOverwriteTraces;
    public static String ImportTraceWizardAddFile;
    public static String ImportTraceWizardAddDirectory;
    public static String ImportTraceWizardRemove;
    public static String ImportTraceWizardDirectoryCaption;
    public static String ImportTraceWizardDirectoryHint;
    public static String ImportTraceWizardScanPage_renameError;
    public static String ImportTraceWizardScanPage_SelectAtleastOne;
    public static String ImportTraceWizardSelectAll;
    public static String ImportTraceWizardPageScan_done;
    public static String ImportTraceWizardPageScan_scanning;
    public static String ImportTraceWizardPageSelectNone;
    public static String ImportTraceWizardPageSelectHint;
    public static String BatchImportTraceWizard_remove;
    public static String BatchImportTraceWizard_add;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
