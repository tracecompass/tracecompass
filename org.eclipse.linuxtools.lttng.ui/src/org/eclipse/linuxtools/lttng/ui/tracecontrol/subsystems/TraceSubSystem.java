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
package org.eclipse.linuxtools.lttng.ui.tracecontrol.subsystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.lttng.LttngConstants;
import org.eclipse.linuxtools.lttng.tracecontrol.model.ProviderResource;
import org.eclipse.linuxtools.lttng.tracecontrol.model.TargetResource;
import org.eclipse.linuxtools.lttng.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.lttng.tracecontrol.model.TraceResource.TraceState;
import org.eclipse.linuxtools.lttng.tracecontrol.model.config.TraceConfig;
import org.eclipse.linuxtools.lttng.tracecontrol.service.ILttControllerService;
import org.eclipse.linuxtools.lttng.tracecontrol.service.LttControllerServiceProxy;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.actions.PauseTrace;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.connectorservice.TraceConnectorService;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.util.TCFTask;

/**
 * <b><u>TraceSubSystem</u></b>
 * <p>
 * Implementation of the trace subsystem. Provides methods to initialize connections
 * to the remote system, connection handling, filtering and retrival of remote
 * system configuration. 
 * </p>
 */
public class TraceSubSystem extends SubSystem implements ICommunicationsListener {
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private ProviderResource[] fProviders; // master list of Providers

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * @param host
     * @param connectorService
     */
    public TraceSubSystem(IHost host, IConnectorService connectorService) {
        super(host, connectorService);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystem#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void initializeSubSystem(IProgressMonitor monitor) {
        getConnectorService().addCommunicationsListener(this);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystem#uninitializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void uninitializeSubSystem(IProgressMonitor monitor) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystem#getObjectWithAbsoluteName(java.lang.String)
     *
     * For drag and drop, and clipboard support of remote objects.
     * 
     * Return the remote object within the subsystem that corresponds to the specified unique ID. Because each subsystem maintains it's own objects, it's the responsability of the subsystem to determine how an ID (or key) for a given object maps to
     * the real object. By default this returns null.
     */
    @Override
    public Object getObjectWithAbsoluteName(String key) {
        return null;
    }
 
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterString(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected Object[] internalResolveFilterString(String filterString, IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
        
        ProviderResource[] allProviders;

        try {
             allProviders = getAllProviders();
        } catch (SystemMessageException e) {
            SystemBasePlugin.logError("TraceSubSystem", e); //$NON-NLS-1$
            Object[] children = new SystemMessageObject[1];
            children[0] = new SystemMessageObject(e.getSystemMessage(), ISystemMessageObject.MSGTYPE_ERROR, null);
            return children;
        }

        // Now, subset master list, based on filter string...
        NamePatternMatcher subsetter = new NamePatternMatcher(filterString);
        Vector<ProviderResource> v = new Vector<ProviderResource>();
        for (int idx = 0; idx < allProviders.length; idx++) {
            if (subsetter.matches(allProviders[idx].getName())) {
                v.addElement(allProviders[idx]);
            }
        }
        return allProviders;
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterString(java.lang.Object, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected Object[] internalResolveFilterString(Object parent, String filterString, IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystem#filterEventFilterCreated(java.lang.Object, org.eclipse.rse.core.filters.ISystemFilter)
     */
    @Override
    public void filterEventFilterCreated(Object selectedObject, ISystemFilter newFilter) {
        super.filterEventFilterCreated(selectedObject, newFilter);
        ISystemRegistry registry = SystemStartHere.getSystemRegistry();
        registry.fireEvent(new SystemResourceChangeEvent(this, ISystemResourceChangeEvents.EVENT_REFRESH, null));
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystem#filterEventFilterPoolReferenceCreated(org.eclipse.rse.core.filters.ISystemFilterPoolReference)
     */
    @Override
    public void filterEventFilterPoolReferenceCreated(ISystemFilterPoolReference newPoolRef) {
        super.filterEventFilterPoolReferenceCreated(newPoolRef);
        if (getSystemFilterPoolReferenceManager().getSystemFilterPoolReferenceCount() == 1) {
            ISystemRegistry registry = SystemStartHere.getSystemRegistry();
            registry.fireEvent(new SystemResourceChangeEvent(this, ISystemResourceChangeEvents.EVENT_REFRESH, null));
        }
    }
    
    /**
     * Retrieves all provider resources from the remote system and updates local references.  
     * 
     * @return provider resources
     * @throws SystemMessageException
     * @throws InterruptedException
     */
    public ProviderResource[] getAllProviders() throws SystemMessageException, InterruptedException {
        ProviderResource[] providers = createProviders(); 
        if (fProviders == null) {
            fProviders = providers;
        }
        else {
            for (int i = 0; i < fProviders.length; i++) {
                for (int j = 0; j < providers.length; j++) {
                    if(fProviders[i].getName().equals(providers[j].getName())) {
                        // Check if all targets already exist
                        fProviders[i].refreshTargets(providers[j].getTargets());
                    }
                }
            }
        }
        return fProviders;
    }

    /**
     * Get the list of all targets.
     * 
     * @return targets The list of targets.
     * @throws SystemMessageException
     */
    public TargetResource[] getAllTargets() throws SystemMessageException {
    	ArrayList<TargetResource> targets = new ArrayList<TargetResource>();
    	if (fProviders != null) {
            for (int i = 0; i < fProviders.length; i++) {
                targets.addAll(Arrays.asList(fProviders[i].getTargets()));
            }
    	}
        return targets.toArray(new TargetResource[0]);
    }

    /**
     * Get the list of all traces.
     * 
     * @return traces The list of traces.
     * @throws SystemMessageException
     */
    public TraceResource[] getAllTraces() throws SystemMessageException {
    	ArrayList<TraceResource> traces = new ArrayList<TraceResource>();
    	if (fProviders != null) {
            for (int i = 0; i < fProviders.length; i++) {
            	ProviderResource provider = fProviders[i];
            	int numTargets = provider.getTargets().length;
                for (int j = 0; j < numTargets; j++) {
                	TargetResource target = provider.getTargets()[j];
	            	if (provider.getName().equals(LttngConstants.Lttng_Provider_Kernel)) {
	            		traces.addAll(Arrays.asList(target.getTraces()));
	            	}
                }
            }
        }
        return traces.toArray(new TraceResource[0]);
    }

    /**
     * Get the list of all traces for given provider and target.
     * 
     * @param provider
     * @param target
     * @returns trace resources
     */
    public TraceResource[] getAllTraces(String providerName, String targetName) throws SystemMessageException {
        ArrayList<TraceResource> traces = new ArrayList<TraceResource>();
        ProviderResource selectedProvider = null;
        if (fProviders != null) {
            for (int i = 0; i < fProviders.length; i++) {
                ProviderResource provider = fProviders[i];
                if (provider.getName().equals(providerName)) {
                    selectedProvider = fProviders[i];
                    break;
                }
            }
            
            if (selectedProvider != null) {
                int numTargets = selectedProvider.getTargets().length;
                for (int j = 0; j < numTargets; j++) {
                    TargetResource target = selectedProvider.getTargets()[j];
                    if (target.getName().equals(targetName)) {
                        traces.addAll(Arrays.asList(target.getTraces()));
                        break;
                    }
                }
            }
        }
        return traces.toArray(new TraceResource[0]);
    }
    
    /**
     * Finds a trace resource within a given provider and target for a given trace name 
     * 
     * @param targetName - target name to be searched 
     * @param traceName - trace name to be searched
     * @return trace resource or null (if not found)
     */
    public TraceResource findTrace(String providerName, String targetName, String traceName) {
        TraceResource trace = null;
        TraceResource[] traces;
        try {
            traces = getAllTraces(providerName, targetName);
            for (int i = 0; i < traces.length; i++) {
                if (traces[i].getName().equals(traceName)) {
                    trace = traces[i];
                    break;
                }
            }
        } catch (SystemMessageException e) {
            SystemBasePlugin.logError("TraceSubSystem", e); //$NON-NLS-1$
        }

        return trace;
    }
    
    /*
     * Retrieves the providers from the remote system.
     */
    private ProviderResource[] createProviders() throws SystemMessageException {
        ProviderResource[] providers = null;
        try {
            
            final ILttControllerService service = getControllerService();

            // Create future task
            providers = new TCFTask<ProviderResource[]>() {
                @Override
                public void run() {

                    // Get provider using Lttng controller service proxy
                    service.getProviders(new ILttControllerService.DoneGetProviders() {

                        @Override
                        public void doneGetProviders(IToken token, Exception error, String[] str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Create provider list
                            ProviderResource[] providers = new ProviderResource[str.length]; 

                            for (int i = 0; i < str.length; i++) {
                                ProviderResource tempProvider = new ProviderResource(TraceSubSystem.this);
                                tempProvider.setName(str[i]);
                                providers[i] = tempProvider;
                            }

                            // Notify with provider list
                            done(providers);
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (e instanceof SystemMessageException) throw (SystemMessageException)e;
            throw new SystemMessageException(LTTngUiPlugin.getDefault().getMessage(e));
        }

        for (int i = 0; i < providers.length; i++) {
            createTargets(providers[i]);
        }

        return providers;
    }

    /*
     * Retrieves the targets for given provider from the remote system.
     */
    private TargetResource[] createTargets(final ProviderResource provider) throws SystemMessageException {
    	TargetResource[] targets;
    	try {
            final ILttControllerService service = getControllerService();

            // Create future task
            targets = new TCFTask<TargetResource[]>() {
                @Override
                public void run() {
                    
                    // Get targets using Lttng controller service proxy
                    service.getTargets(provider.getName(), new ILttControllerService.DoneGetTargets() {

                        @Override
                        public void doneGetTargets(IToken token, Exception error, String[] str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Create targets
                            TargetResource[] targets = new TargetResource[str.length];
                            for (int i = 0; i < str.length; i++) {
                                TargetResource tempTarget = new TargetResource(TraceSubSystem.this);
                                tempTarget.setName(str[i]);
                                tempTarget.setParent(provider);
                                targets[i] = tempTarget;
                            }
                            // Notify with target list
                            done(targets);
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
        	provider.setTargets(new TargetResource[0]);
            if (e instanceof SystemMessageException) throw (SystemMessageException)e;
            throw new SystemMessageException(LTTngUiPlugin.getDefault().getMessage(e));
        }

        provider.setTargets(targets);
        for (int i = 0; i < targets.length; i++) {
        	if (targets[i].getParent().getName().equals(LttngConstants.Lttng_Provider_Kernel)) {
	            createTraces(targets[i]);
        	}
        }

        return targets;
    }

    /*
     * Retrieves the trace instances for a given target from the remote system.
     */
    private TraceResource[] createTraces(final TargetResource target) throws SystemMessageException {
    	TraceResource[] traces;
        try {
            final ILttControllerService service = getControllerService();

            // Create future task
            traces = new TCFTask<TraceResource[]>() {
                @Override
                public void run() {
                    // Get targets using Lttng controller service proxy
                    service.getTraces(target.getParent().getName(), target.getName(), new ILttControllerService.DoneGetTraces() {

                        @Override
                        public void doneGetTraces(IToken token, Exception error, String[] str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }
                            
                            // Create trace list
                            TraceResource[] traces = new TraceResource[str.length];
                            for (int i = 0; i < str.length; i++) {
                                TraceResource trace = new TraceResource(TraceSubSystem.this, service);
                                trace.setName(str[i]);
                                trace.setParent(target);
                                trace.setTraceState(TraceState.CREATED);
                                traces[i] = trace;
                            }

                            // Notify with trace list
                            done(traces);
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            target.setTraces(new TraceResource[0]);
            if (e instanceof SystemMessageException) throw (SystemMessageException)e;
            throw new SystemMessageException(LTTngUiPlugin.getDefault().getMessage(e));
        }

        target.setTraces(traces);

        // get active trace information (is only supported for kernel traces)
        createTraceConfigurations(target, traces);
        return traces;
    }

    /*
     * Retrieves the trace configurations for the given trace from the remote system.
     */
    private void createTraceConfigurations(final TargetResource target, TraceResource[] traces) throws SystemMessageException {
        if (!target.isUst() && (traces.length > 0)) {
            // get active traces 
            String[] activeTraceNames;
            try {
                final ILttControllerService service = getControllerService();
                activeTraceNames = new TCFTask<String[]>() {
                    @Override
                    public void run() {
                        // Get targets using Lttng controller service proxy
                        service.getActiveTraces(target.getParent().getName(), target.getName(), new ILttControllerService.DoneGetActiveTraces() {

                            @Override
                            public void doneGetActiveTraces(IToken token, Exception error, String[] str) {
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
                if (e instanceof SystemMessageException) throw (SystemMessageException)e;
                throw new SystemMessageException(LTTngUiPlugin.getDefault().getMessage(e));
            }
            
            // get active trace information
            for (int j = 0; j < activeTraceNames.length; j++) {
                final TraceResource trace = target.getTrace(activeTraceNames[j]);
                if (trace != null) {
                    // get trace info
                    TraceConfig traceConfig;

                    // Currently, if a trace is active then all the setup commands have been executed
                    // and it's either started or paused. However, currently there is no means to retrieve
                    // the state (paused or started). So we set it to state started (even if trace is not actually
                    // started on target the command pause will be successful. However, the use will have the wrong
                    // impression that the trace is started) Therefore ... the state needs to be retrievable.
                    // TODO update to correct state if there is a possibility to retrieve the correct state. 
                    trace.setTraceState(TraceState.STARTED);  
                    try {
                        final ILttControllerService service = getControllerService();
                        traceConfig = new TCFTask<TraceConfig>() {
                            @Override
                            public void run() {
                                // Get targets using Lttng controller service proxy
                                service.getActiveTraceInfo(target.getParent().getName(), target.getName(), trace.getName(), new ILttControllerService.DoneGetActiveTraceInfo() {

                                    @Override
                                    public void doneGetActiveTraceInfo(IToken token, Exception error, String[] str) {
                                        if (error != null) {
                                            // Notify with error
                                            error(error);
                                            return;
                                        }

                                        TraceConfig config = new TraceConfig();
                                        config.setIsAppend(false); 
                                        if (str[3].equals("true")) { //$NON-NLS-1$
                                            config.setMode(TraceConfig.FLIGHT_RECORDER_MODE);    
                                        }
                                        else if (str[1].equals("true")) { //$NON-NLS-1$
                                            config.setMode(TraceConfig.NORMAL_MODE);
                                        }

                                        if (str[5].equals(TraceConfig.InvalidTracePath)) {
                                            config.setNetworkTrace(true); 
                                        }
                                        else {
                                            config.setNetworkTrace(false);
                                        }
                                        config.setNumChannel(Integer.valueOf(str[0]));
                                        config.setTraceName(trace.getName());
                                        config.setTracePath(str[5]);
                                        config.setTraceTransport(TraceControlConstants.Lttng_Trace_Transport_Relay); 
                                        
                                        // Notify with active trace list
                                        done(config);
                                    }
                                });
                            }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        if (e instanceof SystemMessageException) throw (SystemMessageException)e;
                        throw new SystemMessageException(LTTngUiPlugin.getDefault().getMessage(e));
                    }
                    trace.setTraceConfig(traceConfig);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.ICommunicationsListener#communicationsStateChange(org.eclipse.rse.core.subsystems.CommunicationsEvent)
     */
    @Override
    public void communicationsStateChange(CommunicationsEvent e) {
        switch (e.getState())
        {
        case CommunicationsEvent.BEFORE_CONNECT :
            break;
        case CommunicationsEvent.AFTER_CONNECT :
            break;
        case CommunicationsEvent.BEFORE_DISCONNECT :
            
            try {
                final TraceResource[] traces = getAllTraces();

                StringBuffer traceNames = new StringBuffer("");  //$NON-NLS-1$
                String filler = ""; //$NON-NLS-1$
                for (int j = 0; j < traces.length; j++) {
                    // For network traces, ask user to pause tracing
                    if (traces[j].isNetworkTraceAndStarted()) {
                        traceNames.append(filler);
                        traceNames.append(traces[j].getName());
                    }
                    filler = ", "; //$NON-NLS-1$
                }
                if (!"".equals(traceNames.toString())) { //$NON-NLS-1$
                    final String finalTraceNames = traceNames.toString();
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            MessageDialog.openWarning(Display.getDefault().getActiveShell(), Messages.Ltt_ShutdownWarning, Messages.Ltt_NetworkTraceRunningWarning + ":\n" + finalTraceNames); //$NON-NLS-1$

                            // Pause tracing
                            PauseTrace pauseAction = new PauseTrace();
                            pauseAction.setSelectedTraces(new ArrayList<TraceResource>(Arrays.asList(traces)));
                            pauseAction.run(null);
                        }
                    });
                }

                if (fProviders != null) {
                    // reset all providers and it's children
                    for (int i = 0; i < fProviders.length; i++) {
                        fProviders[i].removeAllTargets();
                    }
                    fProviders = null;
                }

            } catch (SystemMessageException ex) {
                SystemBasePlugin.logError("TraceSubSystem", ex); //$NON-NLS-1$
            }
            break;
        case CommunicationsEvent.AFTER_DISCONNECT :
            getConnectorService().removeCommunicationsListener(this);
            break;
        case CommunicationsEvent.CONNECTION_ERROR :
            // TODO notify user about the lost connection ?!
            getConnectorService().removeCommunicationsListener(this);
            try {
                this.disconnect();
            } catch (Exception e1) {
                // Nothing to do
            }
            break;
        default :
            break;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.ICommunicationsListener#isPassiveCommunicationsListener()
     */
    @Override
    public boolean isPassiveCommunicationsListener() {
        return true;    
    }
    
    /**
     * Returns the trace controller service.
     * 
     * @return trace controller service
     * @throws Exception
     */
    public LttControllerServiceProxy getControllerService() throws Exception {
        return ((TraceConnectorService)getConnectorService()).getControllerService();
    }
}
