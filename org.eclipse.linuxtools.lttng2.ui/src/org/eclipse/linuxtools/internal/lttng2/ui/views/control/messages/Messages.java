/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages;

import org.eclipse.osgi.util.NLS;

/**
 * <p>
 * Messages file for the trace control package.
 * </p>
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("javadoc")
final public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.messages"; //$NON-NLS-1$

    // Failures
    public static String TraceControl_ConnectionFailure;
    public static String TraceControl_DisconnectionFailure;
    public static String TraceControl_ExecutionCancelled;
    public static String TraceControl_ExecutionFailure;
    public static String TraceControl_ExecutionTimeout;
    public static String TraceControl_ShellNotConnected;

    public static String TraceControl_CommandShellError;
    public static String TraceControl_CommandError;
    public static String TraceControl_UnexpectedCommandOutputFormat;
    public static String TraceControl_UnexpectedNameError;
    public static String TraceControl_UnexpectedPathError;

    public static String TraceControl_UnsupportedVersionError;
    public static String TraceControl_GettingVersionError;

    // Commands
    public static String TraceControl_ErrorTitle;
    public static String TraceControl_RetrieveNodeConfigurationJob;
    public static String TraceControl_RetrieveNodeConfigurationFailure;
    public static String TraceControl_ListSessionFailure;
    public static String TraceControl_EclipseCommandFailure;
    public static String TraceControl_NewNodeCreationFailure;
    public static String TraceControl_CreateSessionJob;
    public static String TraceControl_CreateSessionFailure;

    public static String TraceControl_DestroySessionJob;
    public static String TraceControl_DestroySessionFailure;
    public static String TraceControl_DestroyConfirmationTitle;
    public static String TraceControl_DestroyConfirmationMessage;

    public static String TraceControl_ImportJob;
    public static String TraceControl_ImportFailure;

    public static String TraceControl_ChangeSessionStateJob;
    public static String TraceControl_ChangeSessionStateFailure;

    public static String TraceControl_CreateChannelStateJob;
    public static String TraceControl_CreateChannelStateFailure;

    public static String TraceControl_ChangeChannelStateJob;
    public static String TraceControl_ChangeChannelStateFailure;
    public static String TraceControl_ChangeEventStateJob;
    public static String TraceControl_ChangeEventStateFailure;

    public static String TraceControl_EnableEventsJob;
    public static String TraceControl_EnableEventsFailure;
    public static String TraceControl_DisableEventsJob;

    public static String TraceControl_GetContextJob;
    public static String TraceControl_GetContextFailure;

    public static String TraceControl_AddContextJob;
    public static String TraceControl_AddContextFailure;

    public static String TraceControl_AddCalibrateJob;
    public static String TraceControl_AddCalibrateFailure;

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
    public static String TraceControl_CreateSessionNameLabel;
    public static String TraceControl_CreateSessionNameTooltip;
    public static String TraceControl_CreateSessionPathLabel;
    public static String TraceControl_CreateSessionPathTooltip;

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

    public static String TraceControl_InvalidSessionNameError;
    public static String TraceControl_SessionAlreadyExistsError;
    public static String TraceControl_SessionPathAlreadyExistsError;
    public static String TraceControl_InvalidSessionPathError;
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

    public static String TraceControl_InvalidChannelNameError;
    public static String TraceControl_ChannelAlreadyExistsError;

    public static String TraceControl_EnableEventsDialogTitle;
    public static String TraceControl_EnableEventsSessionGroupName;
    public static String TraceControl_EnableEventsChannelGroupName;
    public static String TraceControl_EnableEventsSessionsTooltip;
    public static String TraceControl_EnableEventsChannelsTooltip;
    public static String TraceControl_EnableEventsNoSessionError;
    public static String TraceControl_EnableEventsNoChannelError;

    public static String TraceControl_EnableGroupSelectionName;
    public static String TraceControl_EnableEventsTracepointGroupName;
    public static String TraceControl_EnableEventsTracepointTreeTooltip;
    public static String TraceControl_EnableEventsTracepointTreeAllLabel;
    public static String TraceControl_EnableEventsSyscallName;
    public static String TraceControl_EnableEventsSyscallTooltip;
    public static String TraceControl_EnableEventsProbeGroupName;
    public static String TraceControl_EnableEventsEventNameLabel;
    public static String TraceControl_EnableEventsProbeEventNameTooltip;
    public static String TraceControl_EnableEventsProbeNameLabel;
    public static String TraceControl_EnableEventsProbeNameTooltip;
    public static String TraceControl_EnableEventsFucntionGroupName;
    public static String TraceControl_EnableEventsFunctionEventNameTooltip;
    public static String TraceControl_EnableEventsFunctionNameLabel;

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
    public static String TraceControl_InvalidWildcardError;
    public static String TraceControl_InvalidLogLevelEventNameError;
    public static String TraceControl_InvalidLogLevel;

    public static String TraceControl_AddContextDialogTitle;
    public static String TraceControl_AddContextAvailableContextsLabel;
    public static String TraceControl_AddContextAvailableContextsTooltip;
    public static String TraceControl_AddContextAllLabel;

    public static String TraceControl_ImportDialogTitle;
    public static String TraceControl_ImportDialogTracesGroupName;
    public static String TraceControl_ImportDialogTracesTooltip;
    public static String TraceControl_ImportDialogProjectsGroupName;
    public static String TraceControl_ImportDialogProjectsTooltip;
    public static String TraceControl_ImportDialogOverwriteButtonText;
    public static String TraceControl_ImportDialogCreationError;
    public static String TraceControl_ImportDialogNoProjectSelectedError;
    public static String TraceControl_ImportDialogInvalidTracingProject;
    public static String TraceControl_ImportDialogNoTraceSelectedError;

    public static String TraceControl_ImportDialogConfirmationTitle;
    public static String TraceControl_ImportDialogConfirmationOverwriteLabel;
    public static String TraceControl_ImportDialogConfirmationRenameLabel;
    public static String TraceControl_ImportDialogConfirmationNewNameLabel;
    public static String TraceControl_InvalidTraceNameError;
    public static String TraceControl_ImportDialogTraceAlreadyExistError;

    // Tree structure strings
    public static String TraceControl_KernelDomainDisplayName;
    public static String TraceControl_UstDisplayName;
    public static String TraceControl_UstGlobalDomainDisplayName;
    public static String TraceControl_AllSessionsDisplayName;
    public static String TraceControl_SessionDisplayName;
    public static String TraceControl_DomainDisplayName;
    public static String TraceControl_ChannelDisplayName;
    public static String TraceControl_EventDisplayName;
    public static String TraceControl_ProviderDisplayName;
    public static String TraceControl_KernelProviderDisplayName;

    // Property names
    public static String TraceControl_SessionNamePropertyName;
    public static String TraceControl_EventNamePropertyName;
    public static String TraceControl_EventTypePropertyName;
    public static String TraceControl_LogLevelPropertyName;
    public static String TraceControl_FieldsPropertyName;
    public static String TraceControl_FilterPropertyName;
    public static String TraceControl_StatePropertyName;
    public static String TraceControl_VersionPropertyName;
    public static String TraceControl_DomainNamePropertyName;
    public static String TraceControl_ChannelNamePropertyName;
    public static String TraceControl_OverwriteModePropertyName;
    public static String TraceControl_SubBufferSizePropertyName;
    public static String TraceControl_NbSubBuffersPropertyName;
    public static String TraceControl_SwitchTimerPropertyName;
    public static String TraceControl_ReadTimerPropertyName;
    public static String TraceControl_OutputTypePropertyName;
    public static String TraceControl_HostNamePropertyName;
    public static String TraceControl_HostAddressPropertyName;
    public static String TraceControl_SessionPathPropertyName;
    public static String TraceControl_ProviderNamePropertyName;
    public static String TraceControl_ProcessIdPropertyName;
    public static String TraceControl_ProbeAddressPropertyName;
    public static String TraceControl_ProbeOffsetPropertyName;
    public static String TraceControl_ProbeSymbolPropertyName;

    // Preferences
    public static String TraceControl_TracingGroupPreference;
    public static String TraceControl_LoggingPreference;
    public static String TraceControl_LogfilePath;
    public static String TraceControl_AppendLogfilePreference;
    public static String TraceControl_CommandTimeout;

    public static String TraceControl_VerboseLevelsPreference;
    public static String TraceControl_VerboseLevelNonePreference;
    public static String TraceControl_VerboseLevelVerbosePreference;
    public static String TraceControl_VerboseLevelVeryVerbosePreference;
    public static String TraceControl_VerboseLevelVeryVeryVerbosePreference;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
