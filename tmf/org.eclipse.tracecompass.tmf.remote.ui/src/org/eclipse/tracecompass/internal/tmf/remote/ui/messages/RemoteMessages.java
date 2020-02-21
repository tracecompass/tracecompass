/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.messages;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for remote UI plug-in.
 */
public class RemoteMessages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.remote.ui.messages.messages"; //$NON-NLS-1$

    /** Title for remote fetch log wizard */
    public static String RemoteFetchLogWizard_Title;
    /** Description of remote fetch wizard page */
    public static String RemoteFetchLogWizardPage_Description;
    /** Label for manage profile button */
    public static String RemoteFetchLogWizardPage_ManageProfileLabel;
    /** Label for remote nodes list */
    public static String RemoteFetchLogWizardPage_NodesLabel;
    /** Title of remote fetch wizard page */
    public static String RemoteFetchLogWizardPage_Title;
    /** Label of collapse all button */
    public static String RemoteFetchLogWizardRemotePage_CollapseAll;
    /** Error string for connection errors */
    public static String RemoteFetchLogWizardRemotePage_ConnectionError;
    /** Description of remote fetch wizard remote page */
    public static String RemoteFetchLogWizardRemotePage_Description;
    /** Label of expand all button */
    public static String RemoteFetchLogWizardRemotePage_ExpandAll;
    /** Error missing connection information */
    public static String RemoteFetchLogWizardRemotePage_MissingConnectionInformation;
    /** Message for connection job */
    public static String RemoteFetchLogWizardRemotePage_OpeningConnectionTo;
    /** Title of remote fetch wizard remote page*/
    public static String RemoteFetchLogWizardRemotePage_Title;
    /** Project group label */
    public static String RemoteFetchLogWizardRemotePage_ImportDialogProjectsGroupName;
    /** Error String for no project selected*/
    public static String RemoteFetchLogWizardRemotePage_NoProjectSelectedError;
    /** Error string for invalid tracing project */
    public static String RemoteFetchLogWizardRemotePage_InvalidTracingProject;
    /** Options group label */
    public static String RemoteFetchLogWizardRemotePage_OptionsGroupName;
    /** Create experiment button name */
    public static String RemoteFetchLogWizardRemotePage_CreateExperimentName;
    /** Error string when experiment already exists*/
    public static String RemoteFetchLogWizardRemotePage_ErrorExperimentAlreadyExists;
    /** The error message when a resource in experiment folder already exists */
    public static String RemoteFetchLogWizardRemotePage_ErrorResourceAlreadyExists;
    /** The error message when an experiment name is invalid */
    public static String RemoteFetchLogWizardRemotePage_ErrorExperimentNameInvalid;
    /** The error message when no experiment name was entered */
    public static String RemoteFetchLogWizardRemotePage_ErrorEmptyExperimentName;
    /** Label of add button in remote preference page */
    public static String RemoteProfilesPreferencePage_AddButton;
    /** Label of browse button in remote preference page */
    public static String RemoteProfilesPreferencePage_BrowseButton;
    /** Label of connection node name label */
    public static String RemoteProfilesPreferencePage_ConnectionNodeNameLabel;
    /** Label of connection node URI label */
    public static String RemoteProfilesPreferencePage_ConnectionNodeURILabel;
    /** Label of copy button in remote preference page */
    public static String RemoteProfilesPreferencePage_CopyAction;
    /** Label of cut button in remote preference page */
    public static String RemoteProfilesPreferencePage_CutAction;
    /** Default connection node name */
    public static String RemoteProfilesPreferencePage_DefaultConnectionNodeName;
    /** Default connection node URI */
    public static String RemoteProfilesPreferencePage_DefaultConnectionNodeURI;
    /** Default profile name */
    public static String RemoteProfilesPreferencePage_DefaultProfileName;
    /** Label of delete button in remote preference page */
    public static String RemoteProfilesPreferencePage_DeleteAction;
    /** Label of details pane in remote preference page */
    public static String RemoteProfilesPreferencePage_DetailsPanelLabel;
    /** Error message for duplicate connection node name */
    public static String RemoteProfilesPreferencePage_DuplicateConnectionNodeNameError;
    /** Error message for duplicate profile name */
    public static String RemoteProfilesPreferencePage_DuplicateProfileNameError;
    /** Error message for empty file pattern */
    public static String RemoteProfilesPreferencePage_EmptyFilePatternError;
    /** Error message for empty node name */
    public static String RemoteProfilesPreferencePage_EmptyNodeNameError;
    /** Error message for invalid node name */
    public static String RemoteProfilesPreferencePage_InvalidNodeName;
    /** Error message for empty node URI */
    public static String RemoteProfilesPreferencePage_EmptyNodeURIError;
    /** Error message for empty profile name */
    public static String RemoteProfilesPreferencePage_EmptyProfileNameError;
    /** Error message for empty root path */
    public static String RemoteProfilesPreferencePage_EmptyRootPathError;
    /** Error message for errors during writing of profiles to disk */
    public static String RemoteProfilesPreferencePage_ErrorWritingProfile;
    /** Label of export button in remote preference page */
    public static String RemoteProfilesPreferencePage_ExportButton;
    /** Title of export file dialog */
    public static String RemoteProfilesPreferencePage_ExportFileDialogTitle;
    /** Label for file pattern label */
    public static String RemoteProfilesPreferencePage_FilePatternLabel;
    /** Label of import button in remote preference page */
    public static String RemoteProfilesPreferencePage_ImportButton;
    /** Title for import file dialog */
    public static String RemoteProfilesPreferencePage_ImportFileDialogTitle;
    /** Error message for invalid file pattern */
    public static String RemoteProfilesPreferencePage_InvalidFilePatternError;
    /** Error message for invalid host or port */
    public static String RemoteProfilesPreferencePage_InvalidHostOrPortError;
    /** Error message for invalid node URI */
    public static String RemoteProfilesPreferencePage_InvalidNodeURIError;
    /** Error message for missing connection node */
    public static String RemoteProfilesPreferencePage_MissingConnectionNodeError;
    /** Error message for missing trace node */
    public static String RemoteProfilesPreferencePage_MissingTraceError;
    /** Error message for missing trace group */
    public static String RemoteProfilesPreferencePage_MissingTraceGroupError;
    /** Error message for missing user info */
    public static String RemoteProfilesPreferencePage_MissingUserInfoError;
    /** Label of move down button in remote preference page */
    public static String RemoteProfilesPreferencePage_MoveDownButton;
    /** Label of move up button in remote preference page */
    public static String RemoteProfilesPreferencePage_MoveUpButton;
    /** Label for new connection node menu item */
    public static String RemoteProfilesPreferencePage_NewConnectionNode;
    /** Label for new trace menu item */
    public static String RemoteProfilesPreferencePage_NewTraceAction;
    /** Label for new trace group menu item */
    public static String RemoteProfilesPreferencePage_NewTraceGroupAction;
    /** Label for paste menu item */
    public static String RemoteProfilesPreferencePage_PasteAction;
    /** Label for profile name */
    public static String RemoteProfilesPreferencePage_ProfileNameLabel;
    /** Label for button recursive */
    public static String RemoteProfilesPreferencePage_RecursiveButton;
    /** Label for remove button in remote preference page */
    public static String RemoteProfilesPreferencePage_RemoveButton;
    /** Label for root path label */
    public static String RemoteProfilesPreferencePage_RootPathLabel;
    /** Label for trace type combo */
    public static String RemoteProfilesPreferencePage_TraceTypeLabel;
    /** Error message for unsupported URI scheme error*/
    public static String RemoteProfilesPreferencePage_UnsupportedURISchemeError;

    /** Message for download task */
    public static String RemoteImportTracesOperation_DownloadTask;
    /** Error message for import failure */
    public static String RemoteImportTracesOperation_ImportFailure;
    /** Message for detection of trace type task */
    public static String RemoteImportTracesOperation_DetectingTraceType;
    /** Error message for an null node name */
    public static String RemoteImportConnectionNodeElement_NodeNameNullError;
    /** Error message for invalid URI string during import operation */
    public static String RemoteImportConnectionNodeElement_InvalidUriString;
    /** Error message for connection error during import operation */
    public static String RemoteImportConnectionNodeElement_ConnectionFailure;
    /** Error message for a null URI during import operation */
    public static String RemoteImportConnectionNodeElement_UriNullError;
    /** Error message for an invalid tracing project during import operation */
    public static String RemoteImportTracesOperation_ImportDialogInvalidTracingProject;
    /** Error message for errors during generation of the profile manifest*/
    public static String RemoteGenerateManifest_GenerateProfileManifestError;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, RemoteMessages.class);
    }

    private RemoteMessages() {
    }

}
