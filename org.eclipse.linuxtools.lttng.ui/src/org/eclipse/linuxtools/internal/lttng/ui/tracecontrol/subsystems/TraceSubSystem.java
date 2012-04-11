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
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng.core.LttngConstants;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.ProviderResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TargetResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource.TraceState;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceConfig;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.service.ILttControllerService;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.service.LttControllerServiceProxy;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.utility.LiveTraceManager;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.actions.ImportToProject;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.actions.PauseTrace;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.connectorservice.TraceConnectorService;
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
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.util.TCFTask;

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
        return (fProviders != null) ? Arrays.copyOf(fProviders, fProviders.length) : null;
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
            throw new SystemMessageException(Activator.getDefault().getMessage(e));
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
            throw new SystemMessageException(Activator.getDefault().getMessage(e));
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
            throw new SystemMessageException(Activator.getDefault().getMessage(e));
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
                throw new SystemMessageException(Activator.getDefault().getMessage(e));
            }
            
            // get active trace information
            for (int j = 0; j < activeTraceNames.length; j++) {
                final TraceResource trace = target.getTrace(activeTraceNames[j]);
                if (trace != null) {
                    // get trace info
                    TraceConfig traceConfig;

                    try {
                        final ILttControllerService service = getControllerService();
                        traceConfig = new TCFTask<TraceConfig>() {
                            @Override
                            public void run() {
                                // Get targets using Lttng controller service proxy
                                service.getActiveTraceInfo(target.getParent().getName(), target.getName(), trace.getName(), new ILttControllerService.DoneGetActiveTraceInfo() {

                                    @Override
                                    public void doneGetActiveTraceInfo(IToken token, Exception error, String[] strArray) {
                                        if (error != null) {
                                            // Notify with error
                                            error(error);
                                            return;
                                        }

                                        TraceConfig config = new TraceConfig();
                                        config.setTraceName(trace.getName());
                                        config.setTraceTransport(TraceControlConstants.Lttng_Trace_Transport_Relay); 
                                        config.setIsAppend(false); 
                                        for (String pair : strArray) {
                                            String[] pairArray = pair.split(LttngConstants.Lttng_Control_GetActiveTraceInfoSeparator);
                                            if (pairArray.length != 2) {
                                                continue;
                                            }
                                            String param = pairArray[0];
                                            String value = pairArray[1];
                                            if (param.equals(TraceControlConstants.ACTIVE_TRACE_INFO_PARAM_DESTINATION)) {
                                                if (value.startsWith(TraceControlConstants.ACTIVE_TRACE_INFO_DESTINATION_PREFIX_LOCAL)) {
                                                    config.setNetworkTrace(false);
                                                    config.setTracePath(value.substring(TraceControlConstants.ACTIVE_TRACE_INFO_DESTINATION_PREFIX_LOCAL.length()));
                                                } else if (value.startsWith(TraceControlConstants.ACTIVE_TRACE_INFO_DESTINATION_PREFIX_NETWORK)) {
                                                    config.setNetworkTrace(true);
                                                    config.setTracePath(value.substring(TraceControlConstants.ACTIVE_TRACE_INFO_DESTINATION_PREFIX_NETWORK.length()));
                                                }
                                            } else if (param.equals(TraceControlConstants.ACTIVE_TRACE_INFO_PARAM_NUM_THREAD)) {
                                                config.setNumChannel(Integer.valueOf(value));
                                            } else if (param.equals(TraceControlConstants.ACTIVE_TRACE_INFO_PARAM_NORMAL_ONLY)) {
                                                if (value.equals(Boolean.toString(true))) {
                                                    config.setMode(TraceConfig.NORMAL_MODE);
                                                }
                                            } else if (param.equals(TraceControlConstants.ACTIVE_TRACE_INFO_PARAM_FLIGHT_ONLY)) {
                                                if (value.equals(Boolean.toString(true))) {
                                                    config.setMode(TraceConfig.FLIGHT_RECORDER_MODE);
                                                }
                                            } else if (param.equals(TraceControlConstants.ACTIVE_TRACE_INFO_PARAM_ENABLED)) {
                                                if (value.equals(Boolean.toString(true))) {
                                                    trace.setTraceState(TraceState.STARTED);
                                                } else {
                                                    trace.setTraceState(TraceState.PAUSED);  
                                                }
                                            }
                                        }

                                        // Notify with active trace list
                                        done(config);
                                    }
                                });
                            }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
                        trace.setTraceConfig(traceConfig);
                        if (traceConfig != null) {
                            if (traceConfig.isNetworkTrace()) {
                                // stop and restart the network transfer since TCF channel may be different
                                if (fProviders == null) { // do this only on startup, not on refresh
                                    restartTraceNetwork(service, trace, traceConfig);
                                }
                                LiveTraceManager.setLiveTrace(traceConfig.getTracePath(), true);
                            }
                        }
                    } catch (Exception e) {
                        if (e instanceof SystemMessageException) throw (SystemMessageException)e;
                        throw new SystemMessageException(Activator.getDefault().getMessage(e));
                    }
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
                            try {
                                Thread.sleep(2000); // allow time for target to pause traces before disconnecting the channel
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
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

    /*
     * Stop and restart the network transfer. Only normal channels are written while trace is started.  
     */
    private void restartTraceNetwork(final ILttControllerService service, final TraceResource trace, final TraceConfig traceConfig) throws Exception {
        File newDir = new File(traceConfig.getTracePath());
        if (!newDir.exists()) {
            boolean created = newDir.mkdirs();
            if (!created) {
                throw new Exception(Messages.Lttng_Control_ErrorCreateTracePath + ": " + traceConfig.getTracePath()); //$NON-NLS-1$
            }
            if (traceConfig.getProject() != null) {
                ImportToProject.linkTrace(getShell(), trace, traceConfig.getProject(), traceConfig.getTraceName());
            }
        }

        // stop the previous lttd
        boolean ok = new TCFTask<Boolean>() {
            @Override
            public void run() {

                // Setup trace transport using Lttng controller service proxy
                service.stopWriteTraceNetwork(trace.getParent().getParent().getName(), 
                        trace.getParent().getName(), 
                        traceConfig.getTraceName(), 
                        new ILttControllerService.DoneStopWriteTraceNetwork() {

                    @Override
                    public void doneStopWriteTraceNetwork(IToken token, Exception error, Object str) {
                        if (error != null) {
                            // Notify with error
                            error(error);
                            return;
                        }

                        // Notify about success
                        done(true);
                    }
                });
            }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);

        if (!ok) {
            return;
        }

        // lttd will only perform the shutdown after stopWriteTraceNetwork
        // when it receives the next on_read_subbuffer callback

        if (trace.getTraceState() == TraceState.PAUSED) {
            // we need to start the trace to make sure that the network transfer is stopped
            ok = new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Start the trace
                    service.startTrace(trace.getParent().getParent().getName(), 
                            trace.getParent().getName(), 
                            traceConfig.getTraceName(), 
                            new ILttControllerService.DoneStartTrace() {

                        @Override
                        public void doneStartTrace(IToken token, Exception error, Object str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Notify about success
                            done(true);
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);

            if (!ok) {
                return;
            }

            trace.setTraceState(TraceState.STARTED);

            // wait for the lttd shutdown
            Thread.sleep(1000);

            // return to paused state
            ok = new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Pause the trace
                    service.pauseTrace(trace.getParent().getParent().getName(), 
                            trace.getParent().getName(), 
                            traceConfig.getTraceName(), 
                            new ILttControllerService.DonePauseTrace() {

                        @Override
                        public void donePauseTrace(IToken token, Exception error, Object str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Notify about success
                            done(true);
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);

            if (!ok) {
                return;
            }

            trace.setTraceState(TraceState.PAUSED);

        } else {
            // wait for the lttd shutdown
            Thread.sleep(1000);
        }

        // start a new lttd
        new TCFTask<Boolean>() {
            @Override
            public void run() {

                // Setup trace transport using Lttng controller service proxy
                service.writeTraceNetwork(trace.getParent().getParent().getName(), 
                        trace.getParent().getName(), 
                        traceConfig.getTraceName(), 
                        traceConfig.getTracePath(), 
                        traceConfig.getNumChannel(), 
                        traceConfig.getIsAppend(), 
                        false, 
                        true, // write only normal channels 
                        new ILttControllerService.DoneWriteTraceNetwork() {

                    @Override
                    public void doneWriteTraceNetwork(IToken token, Exception error, Object str) {
                        if (error != null) {
                            // Notify with error
                            error(error);
                            return;
                        }

                        // Notify about success
                        done(true);
                    }
                });
            }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
    }
}
