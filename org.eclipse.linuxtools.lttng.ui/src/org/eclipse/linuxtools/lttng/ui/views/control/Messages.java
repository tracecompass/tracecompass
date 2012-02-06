/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control;

import org.eclipse.osgi.util.NLS;

/**
 * <b><u>Messages</u></b>
 * <p>
 * Messages file for the trace control package. 
 * </p>
 */
final public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.lttng.ui.views.control.messages"; //$NON-NLS-1$
    
    // Failures
    public static String TraceControl_ConnectionFailure;
    public static String TraceControl_DisconnectionFailure;
    public static String TraceControl_ExecutionCancelled;
    public static String TraceControl_ExecutionFailure;
    public static String TraceControl_ExecutionTimeout;
    public static String TraceControl_ShellNotConnected;

    public static String TraceControl_CommandShellError;
    public static String TraceControl_CommandError;
    
    // Commands
    public static String TraceControl_RetrieveNodeConfigurationJob;
    public static String TraceControl_ListSessionFailure;
    public static String TraceControl_EclipseCommandFailure;
    public static String TraceControl_NewNodeCreationFailure;
    
    // Dialogs
    public static String TraceControl_NewDialogTitle;
    public static String TraceControl_NewNodeExistingConnetionsGroupName;
    public static String TraceControl_NewNodeEditButtonName;
    public static String TraceControl_NewNodeComboToolTip;
    public static String TraceControl_NewNodeNameLabel;
    public static String TraceControl_NewNodeNameTooltip;
    public static String TraceControl_NewNodeAddressLabel;
    public static String TraceControl_NewNodeAddressTooltip;
    public static String TraceControl_AlreadyExistsError;
    
    // Tree structure strings
    public static String TraceControl_KernelDomainDisplayName;
    public static String TraceControl_UstGlobalDomainDisplayName;
    public static String TraceControl_AllSessionsDisplayName;
    public static String TraceControl_SessionDisplayName;
    public static String TraceControl_DomainDisplayName;
    public static String TraceControl_ChannelDisplayName;
    public static String TraceControl_EventDisplayName;
    public static String TraceControl_ProviderDisplayName;
    public static String TraceControl_KernelProviderDisplayName;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
