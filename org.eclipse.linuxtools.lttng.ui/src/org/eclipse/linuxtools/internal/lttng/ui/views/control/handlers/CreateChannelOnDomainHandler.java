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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.dialogs.ICreateChannelDialog;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <b><u>CreateChannelOnDomainHandler</u></b>
 * <p>
 * Command handler implementation to create a trace channel for known domain.
 * </p>
 */
public class CreateChannelOnDomainHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The the domain component the command is to be executed on. 
     */
    private TraceDomainComponent fDomain; 

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Get channel information from user
        final ICreateChannelDialog dialog = TraceControlDialogFactory.getInstance().getCreateChannelDialog();
        dialog.setDomainComponent(fDomain);

        if (dialog.open() != Window.OK) {
            return null;
        }

        Job job = new Job(Messages.TraceControl_ChangeChannelStateJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                String errorString = null;

                List<String> channelNames = new ArrayList<String>();
                channelNames.add(dialog.getChannelInfo().getName());

                try {
                    fDomain.enableChannels(channelNames, dialog.getChannelInfo(), monitor);
                } catch (ExecutionException e) {
                    if (errorString == null) {
                        errorString = new String();
                    } 
                    errorString += e.toString() + "\n"; //$NON-NLS-1$
                }

                // get session configuration in all cases
                try {
                    fDomain.getConfigurationFromNode(monitor);
                } catch (ExecutionException e) {
                    if (errorString == null) {
                        errorString = new String();
                    }
                    errorString += Messages.TraceControl_ListSessionFailure + ": " + e.toString();  //$NON-NLS-1$ 
                } 

                if (errorString != null) {
                    return new Status(Status.ERROR, Activator.PLUGIN_ID, errorString);
                }
                return Status.OK_STATUS;
            }
        };
        
        job.setUser(true);
        job.schedule();
            
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
        fDomain = null;

        // Check if one domain is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = (Object) iterator.next();
                if (element instanceof TraceDomainComponent) {
                    TraceDomainComponent domain = (TraceDomainComponent) element;
                    TraceSessionComponent session = (TraceSessionComponent) domain.getParent();
                    // Add only TraceDomainComponent whose TraceSessionComponent parent is inactive and not destroyed
                    if ((session.getSessionState() == TraceSessionState.INACTIVE) && (!session.isDestroyed())) {
                        fDomain = domain;
                    }
                }
            }
        }
        return fDomain != null;
    }
}
