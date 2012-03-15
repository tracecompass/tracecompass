/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.control.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.dialogs.INewConnectionDialog;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>NewConnectionHandler</u></b>
 * <p>
 * Command handler for creation new connection for trace control.
 * </p>
 */
public class NewConnectionHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The trace control system type defined for LTTng version 2.0 and later. 
     */
    public final static String TRACE_CONTROL_SYSTEM_TYPE = "org.eclipse.linuxtools.internal.lttng.ui.control.systemType"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The parent trace control component the new node will be added to. 
     */
    private ITraceControlComponent fRoot = null;

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        assert (fRoot != null);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
        
        // get system type definition for LTTng 2.x connection
        IRSESystemType sysType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(TRACE_CONTROL_SYSTEM_TYPE);
        
        // get all hosts for this system type
        IHost[] hosts = registry.getHostsBySystemType(sysType);

        // Open dialog box for the node name and address
        final INewConnectionDialog dialog = TraceControlDialogFactory.getInstance().getNewConnectionDialog();
        dialog.setTraceControlParent(fRoot);
        dialog.setHosts(hosts);

        if (dialog.open() != Window.OK) {
            return null;
        }

        String hostName = dialog.getConnectionName(); 
        String hostAddress = dialog.getHostName();

        // get the singleton RSE registry
        IHost host = null;

        for (int i = 0; i < hosts.length; i++) {
            if (hosts[i].getAliasName().equals(hostName)) {
                host = hosts[i];
                break;
            }
        }

        if (host == null) {
            // if there's no host then we will create it
            try {
                // create the host object as an SSH Only connection
                host = registry.createHost(
                        sysType,       //System Type Name
                        hostName,      //Connection name
                        hostAddress,   //IP Address        
                        "Connection to Host"); //description //$NON-NLS-1$
            }
            catch (Exception e) {
                MessageDialog.openError(window.getShell(),
                        Messages.TraceControl_EclipseCommandFailure,
                        Messages.TraceControl_NewNodeCreationFailure + " (" + hostName + ", " + hostAddress + ")" + ":\n" + e.toString());  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                return null;
            }
        }

        if (host != null) {
            // successful creation of host
            TargetNodeComponent node = null;
            if (!fRoot.containsChild(hostName)) {
                node = new TargetNodeComponent(hostName, fRoot, host);
                fRoot.addChild(node);
            }
            else {
                node = (TargetNodeComponent)fRoot.getChild(hostName);
            }

            node.connect();
        }
        return null;
    }


    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        
        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        fRoot = null;

        // no need to verify part because it has been already done in getWorkbenchPage()
        IWorkbenchPart part = page.getActivePart(); 
        fRoot = ((ControlView) part).getTraceControlRoot();
        
        return (fRoot != null);
    }
}
