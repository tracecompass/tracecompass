/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.messages;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class RemoteMessages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.remote.ui.messages.messages"; //$NON-NLS-1$

    public static String RemoteConnection_MissingRemoteServicesProviderError;

    public static String RemoteFetchLogWizard_Title;
    public static String RemoteFetchLogWizardPage_Description;
    public static String RemoteFetchLogWizardPage_ManageProfileLabel;
    public static String RemoteFetchLogWizardPage_NodesLabel;
    public static String RemoteFetchLogWizardPage_Title;
    public static String RemoteFetchLogWizardRemotePage_CollapseAll;
    public static String RemoteFetchLogWizardRemotePage_ConnectionError;
    public static String RemoteFetchLogWizardRemotePage_Description;
    public static String RemoteFetchLogWizardRemotePage_ExpandAll;
    public static String RemoteFetchLogWizardRemotePage_MissingConnectionInformation;
    public static String RemoteFetchLogWizardRemotePage_OpeningConnectionTo;
    public static String RemoteFetchLogWizardRemotePage_Title;
    public static String RemoteProfilesPreferencePage_AddButton;
    public static String RemoteProfilesPreferencePage_BrowseButton;
    public static String RemoteProfilesPreferencePage_ConnectionNodeNameLabel;
    public static String RemoteProfilesPreferencePage_ConnectionNodeURILabel;
    public static String RemoteProfilesPreferencePage_CopyAction;
    public static String RemoteProfilesPreferencePage_CutAction;
    public static String RemoteProfilesPreferencePage_DefaultConnectionNodeName;
    public static String RemoteProfilesPreferencePage_DefaultConnectionNodeURI;
    public static String RemoteProfilesPreferencePage_DefaultProfileName;
    public static String RemoteProfilesPreferencePage_DeleteAction;
    public static String RemoteProfilesPreferencePage_DetailsPanelLabel;
    public static String RemoteProfilesPreferencePage_DuplicateConnectionNodeNameError;
    public static String RemoteProfilesPreferencePage_DuplicateProfileNameError;
    public static String RemoteProfilesPreferencePage_EmptyFilePatternError;
    public static String RemoteProfilesPreferencePage_EmptyNodeNameError;
    public static String RemoteProfilesPreferencePage_InvalidNodeName;
    public static String RemoteProfilesPreferencePage_EmptyNodeURIError;
    public static String RemoteProfilesPreferencePage_EmptyProfileNameError;
    public static String RemoteProfilesPreferencePage_EmptyRootPathError;
    public static String RemoteProfilesPreferencePage_ErrorWritingProfile;
    public static String RemoteProfilesPreferencePage_ExportButton;
    public static String RemoteProfilesPreferencePage_ExportFileDialogTitle;
    public static String RemoteProfilesPreferencePage_ExtraConnectionNodeError;
    public static String RemoteProfilesPreferencePage_FilePatternLabel;
    public static String RemoteProfilesPreferencePage_ImportButton;
    public static String RemoteProfilesPreferencePage_ImportFileDialogTitle;
    public static String RemoteProfilesPreferencePage_InvalidFilePatternError;
    public static String RemoteProfilesPreferencePage_InvalidHostOrPortError;
    public static String RemoteProfilesPreferencePage_InvalidNodeURIError;
    public static String RemoteProfilesPreferencePage_MissingConnectionNodeError;
    public static String RemoteProfilesPreferencePage_MissingTraceError;
    public static String RemoteProfilesPreferencePage_MissingTraceGroupError;
    public static String RemoteProfilesPreferencePage_MissingUserInfoError;
    public static String RemoteProfilesPreferencePage_MoveDownButton;
    public static String RemoteProfilesPreferencePage_MoveUpButton;
    public static String RemoteProfilesPreferencePage_NewConnectionNode;
    public static String RemoteProfilesPreferencePage_NewTraceAction;
    public static String RemoteProfilesPreferencePage_NewTraceGroupAction;
    public static String RemoteProfilesPreferencePage_PasteAction;
    public static String RemoteProfilesPreferencePage_ProfileNameLabel;
    public static String RemoteProfilesPreferencePage_RecursiveButton;
    public static String RemoteProfilesPreferencePage_RemoveButton;
    public static String RemoteProfilesPreferencePage_RootPathLabel;
    public static String RemoteProfilesPreferencePage_TraceTypeLabel;
    public static String RemoteProfilesPreferencePage_UnsupportedURISchemeError;

    public static String RemoteImportTracesOperation_DownloadTask;
    public static String RemoteImportTracesOperation_ImportFailure;
    public static String RemoteImportTracesOperation_DetectingTraceType;
    public static String RemoteImportConnectionNodeElement_NodeNameNullError;
    public static String RemoteImportConnectionNodeElement_InvalidUriString;
    public static String RemoteImportConnectionNodeElement_ConnectionFailure;
    public static String RemoteImportConnectionNodeElement_UriNullError;
    public static String RemoteImportTracesOperation_ImportDialogInvalidTracingProject;

    public static String RemoteGenerateManifest_GenerateProfileManifestError;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, RemoteMessages.class);
    }

    private RemoteMessages() {
    }

}
