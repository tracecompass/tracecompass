/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation (based on TCFConnectorService)
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.tracecontrol.connectorservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.lttng.core.LttngConstants;
import org.eclipse.linuxtools.lttng.core.tracecontrol.service.ILttControllerService;
import org.eclipse.linuxtools.lttng.core.tracecontrol.service.LttControllerServiceProxy;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.utility.DownloadProxy;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.subsystems.StandardConnectorService;
import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IChannel.IEventListener;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;

/**
 * <b><u>TraceConnectorService</u></b>
 * <p>
 * Implementation of the Trace Connector class to connect to the remote agent.
 * </p>
 */
public class TraceConnectorService extends StandardConnectorService {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private final static int INVOCATION_TIMEOUT = 1000;
    private boolean fIsConnected = false;
    private IChannel fChannel;
    private Throwable fChannelError;

    private final List<Runnable> fWaitList = new ArrayList<Runnable>();
    private boolean fPollTimerStarted;
    private DownloadProxy fDownloadProxy = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor for TraceConnectorService.
     * 
     * @param host - host reference
     * @param port - port
     */
    public TraceConnectorService(IHost host, int port) {
        super(Messages.Trace_Connector_Service_Name, Messages.Trace_Connector_Service_Description, host, port);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.IConnectorService#isConnected()
     */
    @Override
    public boolean isConnected() {
        return fIsConnected;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#internalConnect(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void internalConnect(final IProgressMonitor monitor) throws Exception {
        assert !Protocol.isDispatchThread();
        final Exception[] res = new Exception[1];
        // Fire comm event to signal state about to change
        fireCommunicationsEvent(CommunicationsEvent.BEFORE_CONNECT);
        monitor.beginTask(Messages.Trace_Connector_Service_Connect_Msg + " " + getHostName(), 1); //$NON-NLS-1$
        synchronized (res) {
            Protocol.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (!connectTCFChannel(res, monitor)) {
                        add_to_wait_list(this);
                    }
                }
            });
            res.wait();
        }
        monitor.done();
        if (res[0] != null) {
            throw res[0];
        }
        // pretend. Normally, we'd connect to our remote server-side code here
        fIsConnected = true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#internalDisconnect(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void internalDisconnect(final IProgressMonitor monitor) throws Exception {
        assert !Protocol.isDispatchThread();
        final Exception[] res = new Exception[1];
        // Fire comm event to signal state about to change
        fireCommunicationsEvent(CommunicationsEvent.BEFORE_DISCONNECT);
        monitor.beginTask(Messages.Trace_Connector_Service_Disconnect_Msg + " " + getHostName(), 1); //$NON-NLS-1$
        synchronized (res) {
            Protocol.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (!disconnectTCFChannel(res, monitor)) {
                        add_to_wait_list(this);
                    }
                }
            });
            res.wait();
        }
        monitor.done();
        if (res[0] != null) {
            throw res[0];
        }
        fIsConnected = false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsRemoteServerLaunching()
     */
    @Override
    public boolean supportsRemoteServerLaunching() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsServerLaunchProperties()
     */
    @Override
    public boolean supportsServerLaunchProperties() {
        return false;
    }

    /*
     * Add Runnable to wait list.
     */
    private void add_to_wait_list(Runnable cb) {
        fWaitList.add(cb);
        if (fPollTimerStarted) {
            return;
        }
        Protocol.invokeLater(INVOCATION_TIMEOUT, new Runnable() {
            @Override
            public void run() {
                fPollTimerStarted = false;
                run_wait_list();
            }
        });
        fPollTimerStarted = true;
    }

    /*
     * Run the runnables of the wait list.
     */
    private void run_wait_list() {
        if (fWaitList.isEmpty()) {
            return;
        }
        Runnable[] runnables = fWaitList.toArray(new Runnable[fWaitList.size()]);
        fWaitList.clear();
        for (int i = 0; i < runnables.length; i++) {
            runnables[i].run();
        }
    }

    /*
     * Connect the TCF channel.
     */
    private boolean connectTCFChannel(Exception[] res, IProgressMonitor monitor) {
        if (fChannel != null) {
            switch (fChannel.getState()) {
            case IChannel.STATE_OPEN:
            case IChannel.STATE_CLOSED:
                synchronized (res) {
                    if (fChannelError instanceof Exception) {
                        res[0] = (Exception) fChannelError;
                    }
                    else if (fChannelError != null) {
                        res[0] = new Exception(fChannelError);
                    }
                    else {
                        res[0] = null;
                    }
                    res.notifyAll();
                    return true;
                }
                default:
            }
        }
        if (monitor.isCanceled()) {
            synchronized (res) {
                res[0] = new Exception(Messages.Trace_Connector_Service_Canceled_Msg); 
                if (fChannel != null) {
                    fChannel.terminate(res[0]);
                }
                res.notifyAll();
                return true;
            }
        }
        if (fChannel == null) {
            String host = getHostName().toLowerCase();
            int port = getConnectPort();
            if (port <= 0) {
                // Default fallback
                port = TraceConnectorServiceManager.TCF_PORT;
            }
            IPeer peer = null;
            String port_str = Integer.toString(port);
            ILocator locator = Protocol.getLocator();
            for (IPeer p : locator.getPeers().values()) {
                Map<String, String> attrs = p.getAttributes();
                if ("TCP".equals(attrs.get(IPeer.ATTR_TRANSPORT_NAME)) &&  //$NON-NLS-1$
                        host.equalsIgnoreCase(attrs.get(IPeer.ATTR_IP_HOST)) && port_str.equals(attrs.get(IPeer.ATTR_IP_PORT))) {
                    peer = p;
                    break;
                }
            }
            if (peer == null) {
                Map<String, String> attrs = new HashMap<String, String>();
                attrs.put(IPeer.ATTR_ID, "RSE:" + host + ":" + port_str);  //$NON-NLS-1$ //$NON-NLS-2$
                attrs.put(IPeer.ATTR_NAME, getName());
                attrs.put(IPeer.ATTR_TRANSPORT_NAME, "TCP");  //$NON-NLS-1$
                attrs.put(IPeer.ATTR_IP_HOST, host);
                attrs.put(IPeer.ATTR_IP_PORT, port_str);
                peer = new AbstractPeer(attrs);
            }
            fChannel = peer.openChannel();
            fChannel.addChannelListener(new IChannel.IChannelListener() {
                @Override
                public void onChannelOpened() {
                    assert fChannel != null;

                    // Check if remote server provides LTTng service
                    if (fChannel.getRemoteService(ILttControllerService.NAME) == null) {
                        return;
                    }
                    // Create service proxy, passing the fChannel to the proxy
                    ILttControllerService controllerService = new LttControllerServiceProxy(fChannel);

                    fChannel.setServiceProxy(ILttControllerService.class, controllerService);

                    ISubSystem[] subSystems = getSubSystems();

                    for (int i = 0; i < subSystems.length; i++) {
                        if (subSystems[i] instanceof TraceSubSystem) {
                            // There is only one trace subsystem per trace connector service 
                            fDownloadProxy = new DownloadProxy((TraceSubSystem)subSystems[i]);
                        }
                    }

                    final IEventListener listener = new IEventListener() {

                        @Override
                        public void event(String name, byte[] data) {
                            if (fDownloadProxy != null) {
                                if (name.compareTo(TraceControlConstants.Lttng_Control_New_Event_Data) == 0) {
                                    fDownloadProxy.writeDownloadedTrace(data);
                                    }
                                else if (name.compareTo(TraceControlConstants.Lttng_Control_Unwrite_Trace_Data_Event) == 0) {
                                        // only for UST
                                        // TODO implement handling
                                    }
                                else if (name.compareTo(TraceControlConstants.Lttng_Control_Trace_Done_Event) == 0) {
                                    // finished 
                                    fDownloadProxy.handleTraceDoneEvent(data);
                                } else {
                                    try {
                                        throw new IOException(LttngConstants.Lttng_Control_Command + ": " + Messages.Lttng_Control_Unknown_Event_Msg + ": " + name);  //$NON-NLS-1$ //$NON-NLS-2$  
                                    } catch (IOException e) {
                                        SystemBasePlugin.logError("TraceConnectorService", e); //$NON-NLS-1$
                                    }
                                }
                            }
                        }
                    };
                    fChannel.addEventListener(controllerService, listener);
                    run_wait_list();
                }

                @Override
                public void congestionLevel(int level) {
                }

                @Override
                public void onChannelClosed(Throwable error) {
                    assert fChannel != null;
                    fChannel.removeChannelListener(this);
                    fChannelError = error;
                    if (fWaitList.isEmpty()) {
                        fireCommunicationsEvent(CommunicationsEvent.CONNECTION_ERROR);
                    } else {
                        run_wait_list();
                    }
                    fChannel = null;
                    fChannelError = null;
                }

            });

            assert fChannel.getState() == IChannel.STATE_OPENING;
        }
        return false;
    }

    /*
     * Disconnect the TCF channel.
     */
    private boolean disconnectTCFChannel(Exception[] res, IProgressMonitor monitor) {
        if (fChannel == null || fChannel.getState() == IChannel.STATE_CLOSED) {
            synchronized (res) {
                res[0] = null;
                res.notifyAll();
                return true;
            }
        }
        if (monitor.isCanceled()) {
            synchronized (res) {
                res[0] = new Exception("Canceled"); //$NON-NLS-1$
                res.notifyAll();
                return true;
            }
        }
        if (fChannel.getState() == IChannel.STATE_OPEN) {
            fChannel.close();
        }
        return false;
    }

    /**
     * Retrieve the remote service for given service interface.
     * 
     * @param <V>
     * @param service_interface
     * @return Service
     * @throws Exception
     */
    public <V extends IService> V getService(Class<V> service_interface) throws Exception {
        if (fChannel == null || fChannel.getState() != IChannel.STATE_OPEN) {
            throw new Exception(Messages.Ltt_Controller_Service_Not_Connected_Msg + ": " + service_interface.getName()); //$NON-NLS-1$
        }
        V service = fChannel.getRemoteService(service_interface);
        if (service == null) {
            throw new Exception(Messages.Ltt_Controller_Service_Unsupported_Msg + ": " + service_interface.getName()); //$NON-NLS-1$
        }
        return service;
    }

    /**
     * Retrieve the LTTng remote service.
     * 
     * @return LTTng remote Service
     * @throws Exception
     */
    public LttControllerServiceProxy getControllerService() throws Exception {
        return (LttControllerServiceProxy)getService(ILttControllerService.class);
    }
}
