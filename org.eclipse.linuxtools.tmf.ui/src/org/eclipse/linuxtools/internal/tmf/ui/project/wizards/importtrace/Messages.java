/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Bernd Hufmann - Add ImportTraceWizard messages
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace;

import org.eclipse.osgi.util.NLS;

/**
 * The messages for import trace wizards.
 * @author Matthew Khouzam
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace.messages"; //$NON-NLS-1$

    // Import Trace Wizard
    /**
     * The dialog title of the import trace wizard
     */
    public static String ImportTraceWizard_DialogTitle;
    /**
     * The title of the file system within the import trace wizard
     */
    public static String ImportTraceWizard_FileSystemTitle;
    /**
     * The title of the the import trace wizard page.
     */
    public static String ImportTraceWizard_ImportTrace;
    /**
     * The label of the directory location (import trace wizard)
    */
    public static String ImportTraceWizard_DirectoryLocation;
    /**
     * The title of the select trace directory dialog (import trace wizard)
     */
    public static String ImportTraceWizard_SelectTraceDirectoryTitle;
    /**
     * The message of the select trace directory dialog (import trace wizard)
     */
    public static String ImportTraceWizard_SelectTraceDirectoryMessage;
    /**
     * The title of the trace type label (import trace wizard)
     */
    public static String ImportTraceWizard_TraceType;
    /**
     * The label of the overwrite checkbox (import trace wizard)
     */
    public static String ImportTraceWizard_OverwriteExistingTrace;
    /**
     * The label of the checkbox to create a link to the trace in workspace (import trace wizard)
     */
    public static String ImportTraceWizard_CreateLinksInWorkspace;
    /**
     * The label of the checkbox to preserve the folder structure of selected the traces in workspace (import trace wizard)
     */
    public static String ImportTraceWizard_PreserveFolderStructure;
    /**
     * The error message for invalid trace directory (import trace wizard)
     */
    public static String ImportTraceWizard_InvalidTraceDirectory;
    /**
     * The error message when a trace validation failed (import trace wizard).
     */
    public static String ImportTraceWizard_TraceValidationFailed;
    /**
     * The error message when a trace already exists in project (import trace wizard).
     */
    public static String ImportTraceWizard_TraceAlreadyExists;
    /**
     * The title of rename button for import configuration dialog.
     */
    public static String ImportTraceWizard_ImportConfigurationRename;
    /**
     * The title of rename all button for import configuration dialog.
     */
    public static String ImportTraceWizard_ImportConfigurationRenameAll;
    /**
     * The title of overwrite button for import configuration dialog.
     */
    public static String ImportTraceWizard_ImportConfigurationOverwrite;
    /**
     * The title of overwrite all button for import configuration dialog.
     */
    public static String ImportTraceWizard_ImportConfigurationOverwriteAll;
    /**
     * The title of skip button for import configuration dialog.
     */
    public static String ImportTraceWizard_ImportConfigurationSkip;
    /**
     * The title of skip all button for import configuration dialog.
     */
    public static String ImportTraceWizard_ImportConfigurationSkipAll;
    /**
     * The error message when trace source is empty (import trace wizard).
     */
    public static String ImportTraceWizard_SelectTraceSourceEmpty;
    /**
     * The error message when no trace is selected (import trace wizard).
     */
    public static String ImportTraceWizard_SelectTraceNoneSelected;
    /**
     * The error message when an error occurred during import operation.
     */
    public static String ImportTraceWizard_ImportProblem;
    /**
     * The error message if destination directory is a virtual folder.
     */
    public static String ImportTraceWizard_CannotImportFilesUnderAVirtualFolder;
    /**
     * The error message if destination directory is a virtual folder (for a link).
     */
    public static String ImportTraceWizard_HaveToCreateLinksUnderAVirtualFolder;
    /**
     * The label string of the browse button.
     */
    public static String ImportTraceWizard_BrowseButton;
    /**
     * The information label string.
     */
    public static String ImportTraceWizard_Information;
    /**
     * The label of the checkbox to import unrecognized trace files
     */
    public static String ImportTraceWizard_ImportUnrecognized;
    /**
     * The message when the import operation was cancelled.
     */
    public static String ImportTraceWizard_ImportOperationCancelled;
    /**
     * The message when the trace type is not found.
     */
    public static String ImportTraceWizard_TraceTypeNotFound;
    /**
     * The import operation task name.
     */
    public static String ImportTraceWizard_ImportOperationTaskName;
    /**
     * The label to indicate that trace type auto detection shall be used.
     */
    public static String ImportTraceWizard_AutoDetection;


    // Batch Import Wizard
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
    /**
     * @since 2.2
     */
    public static String ImportTraceWizardScanPagebyte;

    /**
     * @since 2.2
     */
    public static String ImportTraceWizardScanPageGigabyte;

    /**
     * @since 2.2
     */
    public static String ImportTraceWizardScanPageKilobyte;

    /**
     * @since 2.2
     */
    public static String ImportTraceWizardScanPageMegabyte;

    public static String ImportTraceWizardScanPageRenameError;
    /**
     * @since 2.2
     */
    public static String ImportTraceWizardScanPageSelectAtleastOne;

    /**
     * @since 2.2
     */
    public static String ImportTraceWizardScanPageSize;
    public static String ImportTraceWizardSelectAll;
    /**
     * @since 2.2
     */
    public static String ImportTraceWizardScanPageTerabyte;

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
