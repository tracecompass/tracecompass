/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.actions;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource.TraceState;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannel;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannels;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceConfig;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.service.ILttControllerService;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.wizards.ConfigureTraceWizard;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.util.TCFTask;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * <b><u>ConfigureTrace</u></b>
 * <p>
 * Action implementation to configure a trace.
 * </p>
 */
public class ConfigureTrace implements IObjectActionDelegate, IWorkbenchWindowActionDelegate, IViewActionDelegate {
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private TraceResource fSelectedTrace = null;
    private IStructuredSelection fSelection = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public ConfigureTrace() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction arg0) {
        ConfigureTraceWizard wizard = new ConfigureTraceWizard();
        wizard.init(SystemBasePlugin.getActiveWorkbenchWindow().getWorkbench(), fSelection);
        WizardDialog wDialog= new WizardDialog(getShell(), wizard);
        wDialog.open();
        if (wDialog.getReturnCode() != Window.OK) {
            return;
        }
        
        final TraceConfig result = wizard.getTraceConfig();

        if (result != null) {
//            try {

                // Update channel settings
                TraceChannels channels = result.getTraceChannels();

                for (Iterator<String> iterator = channels.keySet().iterator(); iterator.hasNext();) {
                    String chanName = (String) iterator.next();
                    TraceChannel chan = channels.get(chanName);

                    boolean doIt = false;

                    // If we channel settings have been updated send the relevant command to the agent
                    TraceChannel other = new TraceChannel(chan.getName());

                    if (fSelectedTrace.getTraceConfig() == null || fSelectedTrace.getTraceConfig().getTraceChannels() == null) {
                        // Do the update since channels haven't been configured previously (or we re-connected to the agent)  
                        doIt = true; 
                    } else if (fSelectedTrace.getTraceConfig() != null && fSelectedTrace.getTraceConfig().getTraceChannels() != null) {
                        // Channels has been configured previously, compare new settings with old. Do the update if necessary 
                        TraceChannel other2 = fSelectedTrace.getTraceConfig().getTraceChannels().get(chanName);
                        if (other2 != null) {
                            other = other2;
                        }

                        doIt = !(chan.equals(other));
                    }

                    // Please note that currently, the agent doesn't support the retrieval of channel settings. 
                    // Therefore, the current settings might not be known!

                    if (doIt) {
                        if (!fSelectedTrace.isUst()) {
                            // Update kernel tracing related parameters (not applicable for UST)

                            if (chan.isEnabledStatusKnown()) {
                                if(!other.isEnabledStatusKnown() || (chan.isEnabled() != other.isEnabled()) ) {
                                    setChannelEnable(chan.getName(), chan.isEnabled());
                                }
                            }

                            if (chan.isChannelOverrideStatusKnown()) {
                                if(!other.isChannelOverrideStatusKnown() || (chan.isChannelOverride() != other.isChannelOverride())) {
                                    setChannelOverwrite(chanName, chan.isChannelOverride());
                                }
                            }

                            if (chan.getSubbufNum() != other.getSubbufNum()) {
                                setChannelSubbufNum(chanName, chan.getSubbufNum());
                            }

                            if (chan.getSubbufSize() != other.getSubbufSize()) {
                                setChannelSubbufSize(chanName, chan.getSubbufSize());
                            }
                        }

                        if (chan.getTimer() != other.getTimer()) {
                            setChannelTimer(chanName, chan.getTimer());    
                        }
                    }
                }
                // Update state of trace
                if (fSelectedTrace.getTraceState() == TraceState.CREATED) {
                    fSelectedTrace.setTraceState(TraceState.CONFIGURED);
                }

                fSelectedTrace.setTraceConfig(result);

                ISystemRegistry registry = SystemStartHere.getSystemRegistry();
                registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CHANGED, fSelectedTrace, fSelectedTrace.getParent(), fSelectedTrace.getSubSystem(), null);

//            } catch (SystemMessageException e) {
//                SystemMessageException sysExp;
//                if (e instanceof SystemMessageException) {
//                    sysExp = (SystemMessageException)e;
//                } else {
//                    sysExp = new SystemMessageException(LTTngUiPlugin.getDefault().getMessage(e));    
//                }
//                
//                SystemBasePlugin.logError(Messages.Lttng_Control_ErrorConfigureTrace + " (" +  //$NON-NLS-1$
//                        Messages.Lttng_Resource_Trace + ": "  + fSelectedTrace.getName() + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$
//            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            // store the selected targets to be used when running
            Iterator<IStructuredSelection> theSet = ((IStructuredSelection) selection).iterator();
            fSelection = (IStructuredSelection) selection;
            while (theSet.hasNext()) {
                Object obj = theSet.next();
                if (obj instanceof TraceResource) {
                    fSelectedTrace = (TraceResource)obj;
                    break;
                }
            }
        }
        else {
            fSelection = null;
        }
        
    }

    /**
     * Returns the active workbench shell of this plug-in.
     * 
     * @return active workbench shell.
     */
    protected Shell getShell() {
        return SystemBasePlugin.getActiveWorkbenchShell();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    }
    
    /*
     * Enable or disable a channel on the remote system. 
     */
    private void setChannelEnable(final String channelName, final boolean enabled) {
        try {
            final ILttControllerService service = ((TraceSubSystem)fSelectedTrace.getSubSystem()).getControllerService();

            // Create future task
            new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Set marker enable using Lttng controller service proxy
                    service.setChannelEnable(fSelectedTrace.getParent().getParent().getName(),
                            fSelectedTrace.getParent().getName(), 
                            fSelectedTrace.getName(), 
                            channelName, 
                            enabled,  
                            new ILttControllerService.DoneSetChannelEnable() {

                        @Override
                        public void doneSetChannelEnable(IToken token, Exception error, Object str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Notify about success
                            done(Boolean.valueOf(true));
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            SystemMessageException sysExp;
            if (e instanceof SystemMessageException) {
                sysExp = (SystemMessageException)e;
            } else {
                sysExp = new SystemMessageException(Activator.getDefault().getMessage(e));    
            }
            SystemBasePlugin.logError(Messages.Lttng_Control_ErrorSetChannelEnable + " (" +  //$NON-NLS-1$
                    Messages.Lttng_Resource_Trace + ": "  + fSelectedTrace.getName() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.Lttng_Resource_Channel + ": " + channelName + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    // setChannelOverwrite* provider target trace channel enable
    private void setChannelOverwrite(final String channelName, final boolean override) {
        try {
            final ILttControllerService service = ((TraceSubSystem)fSelectedTrace.getSubSystem()).getControllerService();

            // Create future task
            new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Set marker enable using Lttng controller service proxy
                    service.setChannelOverwrite(fSelectedTrace.getParent().getParent().getName(),
                            fSelectedTrace.getParent().getName(), 
                            fSelectedTrace.getName(), 
                            channelName, 
                            override,  
                            new ILttControllerService.DoneSetChannelOverwrite() {

                        @Override
                        public void doneSetChannelOverwrite(IToken token, Exception error, Object str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Notify about success
                            done(Boolean.valueOf(true));
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            SystemMessageException sysExp;
            if (e instanceof SystemMessageException) {
                sysExp = (SystemMessageException)e;
            } else {
                sysExp = new SystemMessageException(Activator.getDefault().getMessage(e));    
            }
            SystemBasePlugin.logError(Messages.Lttng_Control_ErrorSetChannelOverwrite + " (" +  //$NON-NLS-1$
                    Messages.Lttng_Resource_Trace + ": "  + fSelectedTrace.getName() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.Lttng_Resource_Channel + ": " + channelName + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /*
     * Setup the number of sub-buffers on the remote system.
     */
    private void setChannelSubbufNum(final String channelName, final long numSubBuf) {
        try {
            final ILttControllerService service = ((TraceSubSystem)fSelectedTrace.getSubSystem()).getControllerService();

            // Create future task
            new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Set marker enable using Lttng controller service proxy
                    service.setChannelSubbufNum(fSelectedTrace.getParent().getParent().getName(),
                            fSelectedTrace.getParent().getName(), 
                            fSelectedTrace.getName(), 
                            channelName, 
                            numSubBuf,  
                            new ILttControllerService.DoneSetChannelSubbufNum() {

                        @Override
                        public void doneSetChannelSubbufNum(IToken token, Exception error, Object str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Notify about success
                            done(Boolean.valueOf(true));
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            SystemMessageException sysExp;
            if (e instanceof SystemMessageException) {
                sysExp = (SystemMessageException)e;
            } else {
                sysExp = new SystemMessageException(Activator.getDefault().getMessage(e));    
            }
            SystemBasePlugin.logError(Messages.Lttng_Control_ErrorSetSubbufNum + " (" +  //$NON-NLS-1$
                    Messages.Lttng_Resource_Trace + ": "  + fSelectedTrace.getName() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.Lttng_Resource_Channel + ": " + channelName + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /*
     * Setup the size of the sub-buffer on the remote system.
     */
    private void setChannelSubbufSize(final String channelName, final long subBufSize) {
        try {
            final ILttControllerService service = ((TraceSubSystem)fSelectedTrace.getSubSystem()).getControllerService();

            // Create future task
            new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Set marker enable using Lttng controller service proxy
                    service.setChannelSubbufSize(fSelectedTrace.getParent().getParent().getName(),
                            fSelectedTrace.getParent().getName(), 
                            fSelectedTrace.getName(), 
                            channelName, 
                            subBufSize,  
                            new ILttControllerService.DoneSetChannelSubbufSize() {

                        @Override
                        public void doneSetChannelSubbufSize(IToken token, Exception error, Object str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Notify about success
                            done(Boolean.valueOf(true));
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            SystemMessageException sysExp;
            if (e instanceof SystemMessageException) {
                sysExp = (SystemMessageException)e;
            } else {
                sysExp = new SystemMessageException(Activator.getDefault().getMessage(e));    
            }
            SystemBasePlugin.logError(Messages.Lttng_Control_ErrorSetSubbufSize + " (" +  //$NON-NLS-1$
                    Messages.Lttng_Resource_Trace + ": "  + fSelectedTrace.getName() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.Lttng_Resource_Channel + ": " + channelName + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /*
     * Setup up the channel timer on the remote system.
     */
    private void setChannelTimer(final String channelName, final long timer) {
        try {
            final ILttControllerService service = ((TraceSubSystem)fSelectedTrace.getSubSystem()).getControllerService();

            // Create future task
            new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Set marker enable using Lttng controller service proxy
                    service.setChannelTimer(fSelectedTrace.getParent().getParent().getName(),
                            fSelectedTrace.getParent().getName(), 
                            fSelectedTrace.getName(), 
                            channelName, 
                            timer,  
                            new ILttControllerService.DoneSetChannelTimer() {

                        @Override
                        public void doneSetChannelTimer(IToken token, Exception error, Object str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Notify about success
                            done(Boolean.valueOf(true));
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            SystemMessageException sysExp;
            if (e instanceof SystemMessageException) {
                sysExp = (SystemMessageException)e;
            } else {
                sysExp = new SystemMessageException(Activator.getDefault().getMessage(e));    
            }
            SystemBasePlugin.logError(Messages.Lttng_Control_ErrorSetChannelTimer + " (" +  //$NON-NLS-1$
                    Messages.Lttng_Resource_Trace + ": "  + fSelectedTrace.getName() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.Lttng_Resource_Channel + ": " + channelName + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$
        }        
    }
}
