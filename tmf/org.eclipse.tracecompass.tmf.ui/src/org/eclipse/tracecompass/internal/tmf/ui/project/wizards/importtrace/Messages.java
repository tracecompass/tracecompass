/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Bernd Hufmann - Add ImportTraceWizard messages
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import org.eclipse.osgi.util.NLS;

/**
 * The messages for import trace wizards.
 * @author Matthew Khouzam
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.messages"; //$NON-NLS-1$

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
     * The label of the archive location (import trace wizard)
    */
    public static String ImportTraceWizard_ArchiveLocation;
    /**
     * The title of the select trace directory dialog (import trace wizard)
     */
    public static String ImportTraceWizard_SelectTraceDirectoryTitle;
    /**
     * The title of the select trace archive dialog (import trace wizard)
     */
    public static String ImportTraceWizard_SelectTraceArchiveTitle;
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
     * The label of the checkbox to create an experiment after importing traces (import trace wizard)
     */
    public static String ImportTraceWizard_CreateExperiment;
    /**
     * The error message for invalid trace directory (import trace wizard)
     */
    public static String ImportTraceWizard_InvalidTraceDirectory;
    /**
     * The error message when a trace validation failed (import trace wizard).
     */
    public static String ImportTraceWizard_TraceValidationFailed;
    /**
     * The title of message dialog (import trace wizard).
     */
    public static String ImportTraceWizard_MessageTitle;
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
     * The error message when the specified archive file is not valid.
     */
    public static String ImportTraceWizard_BadArchiveFormat;
    /**
     * The error message when no trace is selected (import trace wizard).
     */
    public static String ImportTraceWizard_SelectTraceNoneSelected;
    /**
     * The error message when an error occurred during import operation.
     */
    public static String ImportTraceWizard_ImportProblem;
    /**
     * The error message when an experiment already exists
     */
    public static String ImportTraceWizard_ErrorExperimentAlreadyExists;
    /**
     * The error message when a resource in experiment folder already exists
     */
    public static String ImportTraceWizard_ErrorResourceAlreadyExists;
    /**
     * The error message when an experiment name is invalid
     */
    public static String ImportTraceWizard_ErrorExperimentNameInvalid;
    /**
     * The error message when no experiment name was entered
     */
    public static String ImportTraceWizard_ErrorEmptyExperimentName;
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
     * The examine operation task name.
     */
    public static String ImportTraceWizard_ExamineOperationTaskName;
    /**
     * The import operation task name.
     */
    public static String ImportTraceWizard_ImportOperationTaskName;
    /**
     * The extract import operation task name
     */
    public static String ImportTraceWizard_ExtractImportOperationTaskName;
    /**
     * The label to indicate that trace type auto detection shall be used.
     */
    public static String ImportTraceWizard_AutoDetection;
    /**
     * The label of the checkbox to enable time range filtering
     */
    public static String ImportTraceWizard_TimeRangeOptionButton;
    /**
     * The Label of the field to enter start time.
     */
    public static String ImportTraceWizard_StartTime;
    /**
     * The Label of the field to enter end time.
     */
    public static String ImportTraceWizard_EndTime;
    /**
     * The time range filtering task name
     */
    public static String ImportTraceWizard_FilteringOperationTaskName;
    /**
     * The error message when the time range is invalid
     */
    public static String ImportTraceWizard_TimeRangeErrorMessage;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
