/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards;

import org.eclipse.osgi.util.NLS;

/**
 * Message strings for TMF model handling.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.project.wizards.messages"; //$NON-NLS-1$

    /**
     * The dialog header of the new project wizard
     */
    public static String NewProjectWizard_DialogHeader;
    /**
     * The dialog message of the new project wizard
     */
    public static String NewProjectWizard_DialogMessage;
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
     * The error message for invalid trace directory (import trace wizard)
     */
    public static String ImportTraceWizard_InvalidTraceDirectory;
    /**
     * The error message when a trace validation failed (import trace wizard).
     */
    public static String ImportTraceWizard_TraceValidationFailed;
    /**
     * The error message when a trace already exists in project (import trace wizard).
     * @since 3.0
     */
    public static String ImportTraceWizard_TraceAlreadyExists;
    /**
     * The title of rename button for import configuration dialog.
     * @since 3.0
     */
    public static String ImportTraceWizard_ImportConfigurationRename;
    /**
     * The title of rename all button for import configuration dialog.
     * @since 3.0
     */
    public static String ImportTraceWizard_ImportConfigurationRenameAll;
    /**
     * The title of overwrite button for import configuration dialog.
     * @since 3.0
     */
    public static String ImportTraceWizard_ImportConfigurationOverwrite;
    /**
     * The title of overwrite all button for import configuration dialog.
     * @since 3.0
     */
    public static String ImportTraceWizard_ImportConfigurationOverwriteAll;
    /**
     * The title of skip button for import configuration dialog.
     * @since 3.0
     */
    public static String ImportTraceWizard_ImportConfigurationSkip;
    /**
     * The title of skip all button for import configuration dialog.
     * @since 3.0
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
     * @since 3.0
     */
    public static String ImportTraceWizard_ImportUnrecognized;
    /**
     * The message when the import operation was cancelled.
     * @since 3.0
     */
    public static String ImportTraceWizard_ImportOperationCancelled;
    /**
     * The message when the trace type is not found.
     * @since 3.0
     */
    public static String ImportTraceWizard_TraceTypeNotFound;
    /**
     * The import operation task name.
     * @since 3.0
     */
    public static String ImportTraceWizard_ImportOperationTaskName;
    /**
     * The label to indicate that trace type auto detection shall be used.
     * @since 3.0
     */
    public static String ImportTraceWizard_AutoDetection;
    /**
     *  The title of the select traces wizard.
     */
    public static String SelectTracesWizard_WindowTitle;
    /**
     * The column header for the traces (select traces wizard page).
     */
    public static String SelectTracesWizardPage_TraceColumnHeader;
    /**
     * The title of select traces wizard page.
     */
    public static String SelectTracesWizardPage_WindowTitle;
    /**
     * The description of the select traces wizard page.
     */
    public static String SelectTracesWizardPage_Description;
    /**
     * The error message when no name was entered in a dialog box (new trace or experiment dialog)
     */
    public static String Dialog_EmptyNameError;
    /**
     * The error message when name of trace or experiment already exists
     */
    public static String Dialog_ExistingNameError;
    /**
     * The title of the new experiment dialog.
     */
    public static String NewExperimentDialog_DialogTitle;
    /**
     * The label of the new experiment name field.
     */
    public static String NewExperimentDialog_ExperimentName;
    /**
     * The title of the rename experiment dialog.
     */
    public static String RenameExperimentDialog_DialogTitle;
    /**
     * The label of the field of the current experiment name.
     */
    public static String RenameExperimentDialog_ExperimentName;
    /**
     * The label of the field for entering the new experiment name.
     */
    public static String RenameExperimentDialog_ExperimentNewName;
    /**
     * The title of the copy experiment dialog.
     */
    public static String CopyExperimentDialog_DialogTitle;
    /**
     * The label of the field of the current experiment name.
     */
    public static String CopyExperimentDialog_ExperimentName;
    /**
     * The label of the field for entering the new experiment name.
     */
    public static String CopyExperimentDialog_ExperimentNewName;
    /**
     * The title of the rename trace dialog.
     */
    public static String RenameTraceDialog_DialogTitle;
    /**
     * The label of the field of the current trace name.
     */
    public static String RenameTraceDialog_TraceName;
    /**
     * The label of the field for entering the new trace name.
     */
    public static String RenameTraceDialog_TraceNewName;
    /**
     * The title of the copy trace dialog.
     */
    public static String CopyTraceDialog_DialogTitle;
    /**
     * The label of the field of the current trace name.
     */
    public static String CopyTraceDialog_TraceName;
    /**
     * The label of the field for entering the new trace name.
     */
    public static String CopyTraceDialog_TraceNewName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
