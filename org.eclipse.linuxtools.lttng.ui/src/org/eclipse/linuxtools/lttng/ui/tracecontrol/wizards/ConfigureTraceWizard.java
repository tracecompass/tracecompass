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
package org.eclipse.linuxtools.lttng.ui.tracecontrol.wizards;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannel;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannels;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceConfig;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.service.ILttControllerService;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <b><u>ConfigureTraceWizard</u></b>
 * <p>
 *  Wizard implementation to configure a trace.
 * </p>
 */
public class ConfigureTraceWizard extends Wizard implements INewWizard {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    ITraceChannelConfigurationPage channelConfigPage;
    TraceConfigurationPage traceConfigPage;

    private TraceResource fSelectedTrace = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public ConfigureTraceWizard() {
        super();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
 
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
	public void addPages() {
        traceConfigPage = new TraceConfigurationPage(this);
        addPage(traceConfigPage);

        if (fSelectedTrace.isUst()) {
            // User space trace
            TraceChannels channels = null;
            if ((fSelectedTrace.getTraceConfig()) != null && fSelectedTrace.getTraceConfig().getTraceChannels() != null) {
                channels = fSelectedTrace.getTraceConfig().getTraceChannels().clone(); 
            }
            else {
                channels = new TraceChannels();
                channels.put(TraceChannel.UST_TRACE_CHANNEL_NAME, new TraceChannel(TraceChannel.UST_TRACE_CHANNEL_NAME));
            }
            channelConfigPage = new UstTraceChannelConfigurationPage(channels);
            addPage(channelConfigPage);
        } else {
            // Kernel trace 
            TraceChannels channels = null;
            if ((fSelectedTrace.getTraceConfig()) != null && (fSelectedTrace.getTraceConfig().getTraceChannels() != null)) {
                channels = fSelectedTrace.getTraceConfig().getTraceChannels().clone(); 
            }
            else {
                String[] channelNames = new String[0];
                try {
                    final ILttControllerService service = ((TraceSubSystem)fSelectedTrace.getSubSystem()).getControllerService();
                    channelNames = new TCFTask<String[]>() {
                        @Override
                        public void run() {
                            // Get targets using Lttng controller service proxy
                            service.getChannels(fSelectedTrace.getParent().getParent().getName(), fSelectedTrace.getParent().getName(), fSelectedTrace.getName(), new ILttControllerService.DoneGetChannels() {

                                @Override
                                public void doneGetChannels(IToken token, Exception error, String[] str) {
                                    if (error != null) {
                                        // Notify with error
                                        error(error);
                                        return;
                                    }
                                    // Notify with active trace list
                                    done(str);
                                }
                            });
                        }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
                } catch (Exception e) {
                    if (e instanceof SystemMessageException) {
                        SystemBasePlugin.logError("Trace Configuration", e); //$NON-NLS-1$;
                    }
                    else {
                        SystemBasePlugin.logError("TraceSubSystem", new SystemMessageException(LTTngUiPlugin.getDefault().getMessage(e))); //$NON-NLS-1$
                    }
                }

                channels = new TraceChannels();
                channels.putAll(channelNames);
            }

            channelConfigPage = new KernelTraceChannelConfigurationPage(channels, fSelectedTrace.getTraceState());
            addPage(channelConfigPage);
        }

        getShell().setImage(LTTngUiPlugin.getDefault().getImage(LTTngUiPlugin.ICON_ID_CONFIG_TRACE));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    @SuppressWarnings("unchecked")
	public void init(IWorkbench workbench, IStructuredSelection selection) {

        fSelectedTrace = null;

        // store the selected targets to be used when running
        Iterator<IStructuredSelection> theSet = selection.iterator();
        while (theSet.hasNext()) {
            Object obj = theSet.next();
            if (obj instanceof TraceResource) {
                fSelectedTrace = (TraceResource)obj;
                break; // only one is allowed
            }
        }
    }

    /**
     * Gets the relevant selected trace that will be configured.
     * 
     * @return selected trace.
     */
    public TraceResource getSelectedTrace() {
        return fSelectedTrace;
    }
    
    /**
     * Gets the new trace configuration of the trace.
     *  
     * @return trace configuration
     */
    public TraceConfig getTraceConfig() {
        TraceConfig config = traceConfigPage.getTraceConfig();
        config.setTraceChannels(channelConfigPage.getTraceChannels());
        return config;
    }
}
