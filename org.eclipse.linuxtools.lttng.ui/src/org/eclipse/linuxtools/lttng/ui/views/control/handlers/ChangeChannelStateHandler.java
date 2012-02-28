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
package org.eclipse.linuxtools.lttng.ui.views.control.handlers;

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
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.views.control.ControlView;
import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>ChangeChannelStateHandler</u></b>
 * <p>
 * Abstract command handler implementation to enable or disabling a trace channel.
 * </p>
 */
abstract public class ChangeChannelStateHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Kernel domain component reference.
     */
    protected TraceDomainComponent fKernelDomain = null;
    /**
     * UST domain component reference.
     */
    protected TraceDomainComponent fUstDomain = null;
    /**
     * The list of kernel channel components the command is to be executed on. 
     */
    protected List<TraceChannelComponent> fKernelChannels = new ArrayList<TraceChannelComponent>();
    /**
     * The list of UST channel components the command is to be executed on. 
     */
    protected List<TraceChannelComponent> fUstChannels = new ArrayList<TraceChannelComponent>();

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the new state to set
     */
    abstract protected TraceEnablement getNewState(); 

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Changes the state of the given channels.
     * @param domain - the domain of the channels.
     * @param channelNames - a list of channel names 
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    abstract protected void changeState(TraceDomainComponent domain, List<String> channelNames, IProgressMonitor monitor) throws ExecutionException; 

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        Job job = new Job(Messages.TraceControl_ChangeChannelStateJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                String errorString = null;

                TraceSessionComponent session = null;

                try {
                    if (fKernelDomain != null) {
                        session = (TraceSessionComponent)fKernelDomain.getParent();
                        List<String> channelNames = new ArrayList<String>();
                        for (Iterator<TraceChannelComponent> iterator = fKernelChannels.iterator(); iterator.hasNext();) {
                            // Enable all selected channels which are disabled
                            TraceChannelComponent channel = (TraceChannelComponent) iterator.next();
                            channelNames.add(channel.getName());
                        }
                        
                        changeState(fKernelDomain, channelNames, monitor);

                        for (Iterator<TraceChannelComponent> iterator = fKernelChannels.iterator(); iterator.hasNext();) {
                            // Enable all selected channels which are disabled
                            TraceChannelComponent channel = (TraceChannelComponent) iterator.next();
                            channel.setState(getNewState());
                        }
                    }

                    if (fUstDomain != null) {
                        if (session == null) {
                            session = (TraceSessionComponent)fUstDomain.getParent();
                        }

                        List<String> channelNames = new ArrayList<String>();
                        for (Iterator<TraceChannelComponent> iterator = fUstChannels.iterator(); iterator.hasNext();) {
                            // Enable all selected channels which are disabled
                            TraceChannelComponent channel = (TraceChannelComponent) iterator.next();
                            channelNames.add(channel.getName());
                        }

                        changeState(fUstDomain, channelNames, monitor);
                        
                        for (Iterator<TraceChannelComponent> iterator = fUstChannels.iterator(); iterator.hasNext();) {
                            // Enable all selected channels which are disabled
                            TraceChannelComponent channel = (TraceChannelComponent) iterator.next();
                            channel.setState(getNewState());
                        }
                    }
                } catch (ExecutionException e) {
                    errorString = e.toString() + "\n"; //$NON-NLS-1$
                }

                // In all cases notify listeners  
                session.fireComponentChanged(session);

                if (errorString != null) {
                    return new Status(Status.ERROR, LTTngUiPlugin.PLUGIN_ID, errorString);
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
        reset();

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }
        
        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            String sessionName = null;
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = (Object) iterator.next();
                 
                if (element instanceof TraceChannelComponent) {
                    
                    // Add only TraceChannelComponents that are disabled
                    TraceChannelComponent channel = (TraceChannelComponent) element;
                    if (sessionName == null) {
                        sessionName = String.valueOf(channel.getSessionName());
                    }

                    // Enable command only for channels of same session
                    if (!sessionName.equals(channel.getSessionName())) {
                        reset();
                        break;
                    }

                    if ((channel.getState() != getNewState())) {
                        if (channel.isKernel()) {
                            fKernelChannels.add(channel);
                            if (fKernelDomain == null) {
                                fKernelDomain = (TraceDomainComponent) channel.getParent();
                            }
                        } else {
                            fUstChannels.add(channel);
                            if (fUstDomain == null) {
                                fUstDomain = (TraceDomainComponent) channel.getParent();
                            }
                        }
                    }
                }
            }
        }
        return fKernelChannels.size() + fUstChannels.size() > 0;
    }

    /**
     * Reset members
     */
    private void reset() {
        fKernelDomain = null;
        fUstDomain = null;
        fKernelChannels.clear();
        fUstChannels.clear();
    }
}
