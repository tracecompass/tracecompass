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
 *   Patrick Tasse - Add support for folder elements
 *   Marc-Andre Laperle - Preserve folder structure on import
 *   Bernd Hufmann - Extract ImportTraceWizard messages
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
     * The error message when selecting of traces for an experiment fails.
     * @since 3.1
     */
    public static String SelectTracesWizardPage_SelectionError;
    /**
     * The task name for selecting of a trace for an experiment.
     * @since 3.1
     */
    public static String SelectTracesWizardPage_TraceSelectionTask;
    /**
     * The task name for removing of a trace for an experiment.
     * @since 3.1
     */
    public static String SelectTracesWizardPage_TraceRemovalTask;
    /**
     * The cancel message for the trace selection operation.
     * @since 3.1
     */
    public static String SelectTracesWizardPage_SelectionOperationCancelled;
    /**
     * The error message title.
     * @since 3.1
     */
    public static String SelectTracesWizardPage_InternalErrorTitle;
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
    /**
     * The title of the new folder dialog.
     * @since 3.0
     */
    public static String NewFolderDialog_DialogTitle;
    /**
     * The label of the new folder name field.
     * @since 3.0
     */
    public static String NewFolderDialog_FolderName;
    /**
     * The title of the rename folder dialog.
     * @since 3.0
     */
    public static String RenameFolderDialog_DialogTitle;
    /**
     * The label of the field of the current folder name.
     * @since 3.0
     */
    public static String RenameFolderDialog_FolderName;
    /**
     * The label of the field for entering the new folder name.
     * @since 3.0
     */
    public static String RenameFolderDialog_FolderNewName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
