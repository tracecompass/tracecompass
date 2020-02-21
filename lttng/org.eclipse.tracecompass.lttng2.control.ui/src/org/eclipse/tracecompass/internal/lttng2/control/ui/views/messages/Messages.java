/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 *   Jonathan Rajotte - Updated for basic support of LTTng 2.6 machine interface
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the trace control package.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("javadoc")
public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.messages"; //$NON-NLS-1$

    // Failures
    public static String TraceControl_ConnectionFailure;
    public static String TraceControl_DisconnectionFailure;
    public static String TraceControl_CommandError;
    public static String TraceControl_LiveTraceElementError;
    public static String TraceControl_LiveTraceInitError;
    public static String TraceControl_UnexpectedCommandOutputFormat;
    public static String TraceControl_UnexpectedNameError;
    public static String TraceControl_UnexpectedPathError;
    public static String TraceControl_UnexpectedNumberOfElementError;
    public static String TraceControl_UnexpectedValueError;

    public static String TraceControl_UnsupportedVersionError;
    public static String TraceControl_GettingVersionError;

    // Xml parsing related failures
    public static String TraceControl_InvalidSchemaError;
    public static String TraceControl_XmlDocumentBuilderError;
    public static String TraceControl_XmlParsingError;
    public static String TraceControl_XmlValidationError;
    public static String TraceControl_XmlValidationWarning;

    // Xml machine interface failures
    public static String TraceControl_MiInvalidNumberOfElementError;
    public static String TraceControl_MiInvalidProviderError;
    public static String TraceControl_MiMissingRequiredError;
    public static String TraceControl_MiInvalidElementError;
    public static String TraceControl_MiIllegalValueError;

    // Commands
    public static String TraceControl_ErrorTitle;
    public static String TraceControl_RetrieveNodeConfigurationJob;
    public static String TraceControl_RetrieveNodeConfigurationFailure;
    public static String TraceControl_ListSessionFailure;
    public static String TraceControl_EclipseCommandFailure;
    public static String TraceControl_NewNodeCreateButtonText;

    public static String TraceControl_NewNodeCreationFailure;
    public static String TraceControl_CreateSessionJob;
    public static String TraceControl_CreateSessionFailure;

    public static String TraceControl_DestroySessionJob;
    public static String TraceControl_DestroySessionFailure;
    public static String TraceControl_DestroyConfirmationTitle;
    public static String TraceControl_DestroyConfirmationMessage;

    public static String TraceControl_ImportJob;
    public static String TraceControl_DownloadTask;
    public static String TraceControl_ImportFailure;

    public static String TraceControl_LoadJob;
    public static String TraceControl_LoadTask;
    public static String TraceControl_LoadFailure;

    public static String TraceControl_SaveJob;
    public static String TraceControl_SaveFailure;

    public static String TraceControl_ChangeSessionStateJob;
    public static String TraceControl_ChangeSessionStateFailure;

    public static String TraceControl_CreateChannelStateJob;
    public static String TraceControl_CreateChannelStateFailure;

    public static String EnableChannelDialog_DefaultMessage;


    public static String TraceControl_ChangeChannelStateJob;
    public static String TraceControl_ChangeChannelStateFailure;
    public static String TraceControl_ChangeEventStateJob;
    public static String TraceControl_ChangeEventStateFailure;
    public static String TraceControl_ChangeLoggerStateJob;
    public static String TraceControl_ChangeLoggerStateFailure;

    public static String TraceControl_EnableEventsJob;
    public static String TraceControl_EnableEventsFailure;
    public static String TraceControl_DisableEventsJob;

    public static String TraceControl_GetContextJob;
    public static String TraceControl_GetContextFailure;

    public static String TraceControl_AddContextJob;
    public static String TraceControl_AddContextFailure;

    public static String TraceControl_RecordSnapshotJob;
    public static String TraceControl_RecordSnapshotFailure;

    // Dialogs
    public static String TraceControl_NewDialogTitle;
    public static String TraceControl_NewNodeExistingConnectionGroupName;
    public static String TraceControl_NewNodeEditButtonName;
    public static String TraceControl_NewNodeComboToolTip;
    public static String TraceControl_NewNodeConnectionNameLabel;
    public static String TraceControl_NewNodeConnectionNameTooltip;
    public static String TraceControl_NewNodeHostNameLabel;
    public static String TraceControl_NewNodeHostNameTooltip;
    public static String TraceControl_NewNodePortLabel;
    public static String TraceControl_NewNodePortTooltip;
    public static String TraceControl_AlreadyExistsError;

    public static String TraceControl_CreateSessionDialogTitle;
    public static String TraceControl_CreateSessionDialogMessage;
    public static String TraceControl_CreateSessionNameLabel;
    public static String TraceControl_CreateSessionNameTooltip;
    public static String TraceControl_CreateSessionPathLabel;
    public static String TraceControl_CreateSessionPathTooltip;
    public static String TraceControl_CreateSessionNormalLabel;
    public static String TraceControl_CreateSessionNormalTooltip;
    public static String TraceControl_CreateSessionSnapshotLabel;
    public static String TraceControl_CreateSessionSnapshotTooltip;
    public static String TraceControl_CreateSessionLiveLabel;
    public static String TraceControl_CreateSessionLiveTooltip;

    public static String TraceControl_CreateSessionConfigureStreamingButtonText;
    public static String TraceControl_CreateSessionConfigureStreamingButtonTooltip;
    public static String TraceControl_CreateSessionNoStreamingButtonText;
    public static String TraceControl_CreateSessionNoStreamingButtonTooltip;
    public static String TraceControl_CreateSessionTracePathText;
    public static String TraceControl_CreateSessionTracePathTooltip;
    public static String TraceControl_CreateSessionLinkButtonText;
    public static String TraceControl_CreateSessionLinkButtonTooltip;
    public static String TraceControl_CreateSessionProtocolLabelText;
    public static String TraceControl_CreateSessionAddressLabelText;
    public static String TraceControl_CreateSessionPortLabelText;
    public static String TraceControl_CreateSessionControlUrlLabel;
    public static String TraceControl_CreateSessionDataUrlLabel;
    public static String TraceControl_CreateSessionCommonProtocolTooltip;
    public static String TraceControl_CreateSessionControlAddressTooltip;
    public static String TraceControl_CreateSessionControlPortTooltip;
    public static String TraceControl_CreateSessionProtocolTooltip;
    public static String TraceControl_CreateSessionDataAddressTooltip;
    public static String TraceControl_CreateSessionDataPortTooltip;
    public static String TraceControl_CreateSessionNoConsumertText;
    public static String TraceControl_CreateSessionNoConsumertTooltip;
    public static String TraceControl_CreateSessionDisableConsumertText;
    public static String TraceControl_CreateSessionDisableConsumertTooltip;
    public static String TraceControl_CreateSessionLiveConnectionLabel;
    public static String TraceControl_CreateSessionLiveConnectionUrlTooltip;
    public static String TraceControl_CreateSessionLiveConnectionPortTooltip;
    public static String TraceControl_CreateSessionLiveDelayLabel;
    public static String TraceControl_CreateSessionLiveDelayTooltip;

    public static String TraceControl_InvalidSessionNameError;
    public static String TraceControl_SessionAlreadyExistsError;
    public static String TraceControl_SessionPathAlreadyExistsError;
    public static String TraceControl_InvalidSessionPathError;
    public static String TraceControl_InvalidLiveDelayError;
    public static String TraceControl_FileSubSystemError;

    public static String TraceControl_EnableChannelDialogTitle;
    public static String TraceControl_EnableChannelNameLabel;
    public static String TraceControl_EnableChannelNameTooltip;
    public static String TraceControl_EnableChannelSubBufferSizeTooltip;
    public static String TraceControl_EnableChannelNbSubBuffersTooltip;
    public static String TraceControl_EnableChannelSwitchTimerTooltip;
    public static String TraceControl_EnableChannelReadTimerTooltip;
    public static String TraceControl_EnableChannelOutputTypeTooltip;
    public static String TraceControl_EnableChannelDiscardModeGroupName;
    public static String TraceControl_EnableChannelDiscardModeLabel;
    public static String TraceControl_EnableChannelDiscardModeTooltip;
    public static String TraceControl_EnableChannelOverwriteModeLabel;
    public static String TraceControl_EnableChannelOverwriteModeTooltip;
    public static String TraceControl_EnbleChannelMaxSizeTraceFilesTooltip;
    public static String TraceControl_EnbleChannelMaxNumTraceFilesTooltip;

    public static String TraceControl_InvalidChannelNameError;
    public static String TraceControl_ChannelAlreadyExistsError;

    public static String TraceControl_EnableEventsDialogTitle;
    public static String TraceControl_EnableEventsSessionGroupName;
    public static String TraceControl_EnableEventsChannelGroupName;
    public static String TraceControl_EnableEventsSessionsTooltip;
    public static String TraceControl_EnableEventsChannelsTooltip;
    public static String TraceControl_EnableEventsNoSessionError;
    public static String TraceControl_EnableEventsNoChannelError;

    public static String TraceControl_EnableLoggersDialogTitle;

    public static String TraceControl_EnableGroupSelectionName;
    public static String TraceControl_EnableEventsAllEventsLabel;
    public static String TraceControl_EnableEventsAllEventsTooltip;
    public static String TraceControl_EnableEventsSpecificEventGroupName;
    public static String TraceControl_EnableEventsSpecificLoggerGroupName;
    public static String TraceControl_EnableEventsSpecificEventTooltip;
    public static String TraceControl_EnableEventsSpecificLoggerTooltip;
    public static String TraceControl_EnableEventsExcludedEventGroupName;
    public static String TraceControl_EnableEventsExcludedEventLabel;
    public static String TraceControl_EnableEventsExcludedEventTooltip;
    public static String TraceControl_EnableEventsTracepointGroupName;
    public static String TraceControl_EnableEventsTracepointTreeTooltip;
    public static String TraceControl_EnableEventsTreeAllLabel;
    public static String TraceControl_EnableEventsSyscallName;
    public static String TraceControl_EnableEventsSyscallTooltip;
    public static String TraceControl_EnableEventsProbeGroupName;
    public static String TraceControl_EnableEventsNameLabel;
    public static String TraceControl_EnableEventsProbeEventNameTooltip;
    public static String TraceControl_EnableEventsProbeNameLabel;
    public static String TraceControl_EnableEventsProbeNameTooltip;
    public static String TraceControl_EnableEventsFucntionGroupName;
    public static String TraceControl_EnableEventsFunctionEventNameTooltip;
    public static String TraceControl_EnableEventsFunctionNameLabel;
    public static String TraceControl_EnableEventsLoggerGroupName;
    public static String TraceControl_EnableEventsLoggerTreeTooltip;

    public static String TraceControl_EnableEventsWildcardGroupName;
    public static String TraceControl_EnableEventsWildcardLabel;
    public static String TraceControl_EnableEventsWildcardTooltip;
    public static String TraceControl_EnableEventsLogLevelGroupName;
    public static String TraceControl_EnableEventsLogLevelTypeName;
    public static String TraceControl_EnableEventsLogLevelTypeTooltip;
    public static String TraceControl_EnableEventsLogLevelOnlyTypeName;
    public static String TraceControl_EnableEventsLogLevelOnlyTypeTooltip;
    public static String TraceControl_EnableEventsLogLevelTooltip;
    public static String TraceControl_EnableEventsLoglevelEventNameTooltip;
    public static String TraceControl_EnableEventsFilterGroupName;
    public static String TraceControl_EnableEventsFilterTooltip;

    public static String TraceControl_InvalidProbeNameError;
    public static String TraceControl_InvalidExcludedEventsError;
    public static String TraceControl_InvalidWildcardError;
    public static String TraceControl_InvalidLogLevelEventNameError;
    public static String TraceControl_InvalidLogLevel;
    public static String TraceControl_InvalidLogger;

    public static String TraceControl_AddContextDialogTitle;
    public static String TraceControl_AddContextAvailableContextsLabel;
    public static String TraceControl_AddContextAvailableContextsTooltip;
    public static String TraceControl_AddContextAllLabel;

    public static String TraceControl_ImportDialogStreamedTraceNotification;
    public static String TraceControl_ImportDialogStreamedTraceNotificationToggle;

    // Tree structure strings
    public static String TraceControl_KernelDomainDisplayName;
    public static String TraceControl_UstDisplayName;
    public static String TraceControl_UstGlobalDomainDisplayName;
    public static String TraceControl_UnknownDomainDisplayName;
    public static String TraceControl_JULDomainDisplayName;
    public static String TraceControl_LOG4JDomainDisplayName;
    public static String TraceControl_PythonDomainDisplayName;
    public static String TraceControl_AllSessionsDisplayName;
    public static String TraceControl_SessionDisplayName;
    public static String TraceControl_DomainDisplayName;
    public static String TraceControl_BufferTypeDisplayName;
    public static String TraceControl_ChannelDisplayName;
    public static String TraceControl_EventDisplayName;
    public static String TraceControl_LoggerDisplayName;
    public static String TraceControl_ProviderDisplayName;
    public static String TraceControl_KernelProviderDisplayName;
    public static String TraceControl_SharedBuffersDisplayName;
    public static String TraceControl_PerPidBuffersDisplayName;
    public static String TraceControl_PerPidBuffersTooltip;
    public static String TraceControl_PerUidBuffersDisplayName;
    public static String TraceControl_PerUidBuffersTooltip;

    // Property names
    public static String TraceControl_SessionNamePropertyName;
    public static String TraceControl_EventNamePropertyName;
    public static String TraceControl_EventTypePropertyName;
    public static String TraceControl_LogLevelPropertyName;
    public static String TraceControl_FieldsPropertyName;
    public static String TraceControl_FilterPropertyName;
    public static String TraceControl_ExcludePropertyName;
    public static String TraceControl_StatePropertyName;
    public static String TraceControl_VersionPropertyName;
    public static String TraceControl_DomainNamePropertyName;
    public static String TraceControl_BufferTypePropertyName;
    public static String TraceControl_ChannelNamePropertyName;
    public static String TraceControl_OpenConnectionTo;
    public static String TraceControl_LoggerNamePropertyName;
    public static String TraceControl_LoggerTypePropertyName;
    public static String TraceControl_LoggerDomainPropertyName;

    public static String TraceControl_OverwriteModePropertyName;
    public static String TraceControl_SubBufferSizePropertyName;
    public static String TraceControl_NbSubBuffersPropertyName;
    public static String TraceControl_SwitchTimerPropertyName;
    public static String TraceControl_NumberOfDiscardedEventsPropertyName;
    public static String TraceControl_NumberOfLostPacketsPropertyName;

    public static String TraceControl_ReadTimerPropertyName;
    public static String TraceControl_OutputTypePropertyName;
    public static String TraceControl_TraceFileCountPropertyName;
    public static String TraceControl_TraceFileSizePropertyName;
    public static String TraceControl_HostNamePropertyName;
    public static String TraceControl_HostAddressPropertyName;
    public static String TraceControl_SessionPathPropertyName;

    public static String TraceControl_SnapshotPathPropertyName;
    public static String TraceControl_SnapshotNamePropertyName;
    public static String TraceControl_SnapshotIdPropertyName;
    public static String TraceControl_ProviderNamePropertyName;
    public static String TraceControl_ProcessIdPropertyName;
    public static String TraceControl_ProbeAddressPropertyName;
    public static String TraceControl_ProbeOffsetPropertyName;
    public static String TraceControl_ProbeSymbolPropertyName;
    public static String TraceControl_MaxSizeTraceFilesPropertyName;
    public static String TraceControl_MaxNumTraceFilesPropertyName;
    public static String TraceControl_ConfigureMetadataChannelName;

    // Preferences
    public static String TraceControl_TracingGroupPreference;
    public static String TraceControl_LoggingPreference;
    public static String TraceControl_LogfilePath;
    public static String TraceControl_AppendLogfilePreference;

    public static String TraceControl_VerboseLevelsPreference;
    public static String TraceControl_VerboseLevelNonePreference;
    public static String TraceControl_VerboseLevelVerbosePreference;
    public static String TraceControl_VerboseLevelVeryVerbosePreference;
    public static String TraceControl_VerboseLevelVeryVeryVerbosePreference;

    public static String TraceControl_ExecuteScriptJob;
    public static String TraceControl_ExecuteScriptError;

    public static String TraceControl_ExecuteScriptDialogTitle;
    public static String TraceControl_ExecuteScriptBrowseText;
    public static String TraceControl_ExecuteScriptSelectLabel;

    public static String TraceControl_LoadDialogTitle;
    public static String TraceControl_SaveDialogTitle;

    public static String TraceControl_ForceButtonText;
    public static String TraceControl_ManageButtonText;
    public static String TraceControl_UnknownNode;

    public static String TraceControl_SelectProfileText;
    public static String TraceControl_LocalButtonText;
    public static String TraceControl_RemoteButtonText;

    public static String TraceControl_DeleteButtonText;
    public static String TraceControl_ImportButtonText;
    public static String TraceControl_ExportButtonText;

    public static String TraceControl_ImportProfileTitle;
    public static String TraceControl_ExportProfileTitle;

    public static String TraceControl_ProfileAlreadyExists;
    public static String TraceControl_OverwriteQuery;

    public static String TraceControl_DeleteProfileTitle;
    public static String TraceControl_DeleteQuery;

    public static String TraceControl_DefaultEventFilterString;
    public static String TraceControl_DefaultEventExcludeString;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
