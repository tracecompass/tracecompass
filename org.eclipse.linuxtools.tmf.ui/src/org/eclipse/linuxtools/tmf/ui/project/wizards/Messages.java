/*******************************************************************************
 * Copyright (c) 2011 Ericsson
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

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.project.wizards.messages"; //$NON-NLS-1$

    public static String NewProjectWizard_DialogHeader;
    public static String NewProjectWizard_DialogMessage;
    
    public static String ImportTraceWizard_DialogTitle;
    public static String ImportTraceWizard_FileSystemTitle;
    public static String ImportTraceWizard_ImportTrace;
    public static String ImportTraceWizard_DirectoryLocation;
    public static String ImportTraceWizard_SelectTraceDirectoryTitle;
    public static String ImportTraceWizard_SelectTraceDirectoryMessage;
    public static String ImportTraceWizard_TraceType;
    public static String ImportTraceWizard_OverwriteExistingTrace;
    public static String ImportTraceWizard_CreateLinksInWorkspace;
    public static String ImportTraceWizard_InvalidTraceDirectory;
    public static String ImportTraceWizard_TraceValidationFailed;
    public static String ImportTraceWizard_SelectTraceSourceEmpty;
    public static String ImportTraceWizard_SelectTraceNoneSelected;
    public static String ImportTraceWizard_ImportProblem;
    public static String ImportTraceWizard_CannotImportFilesUnderAVirtualFolder;
    public static String ImportTraceWizard_HaveToCreateLinksUnderAVirtualFolder;
    public static String ImportTraceWizard_BrowseButton;
    public static String ImportTraceWizard_Information;

    public static String SelectTracesWizard_WindowTitle;
	public static String SelectTracesWizardPage_TraceColumnHeader;
	public static String SelectTracesWizardPage_WindowTitle;
	public static String SelectTracesWizardPage_Description;

	public static String Dialog_EmptyNameError;
    public static String Dialog_ExistingNameError;

    public static String NewExperimentDialog_DialogTitle;
    public static String NewExperimentDialog_ExperimentName;

    public static String RenameExperimentDialog_DialogTitle;
    public static String RenameExperimentDialog_ExperimentName;
    public static String RenameExperimentDialog_ExperimentNewName;

    public static String CopyExperimentDialog_DialogTitle;
    public static String CopyExperimentDialog_ExperimentName;
    public static String CopyExperimentDialog_ExperimentNewName;

    public static String RenameTraceDialog_DialogTitle;
    public static String RenameTraceDialog_TraceName;
    public static String RenameTraceDialog_TraceNewName;

    public static String CopyTraceDialog_DialogTitle;
    public static String CopyTraceDialog_TraceName;
    public static String CopyTraceDialog_TraceNewName;

    static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
