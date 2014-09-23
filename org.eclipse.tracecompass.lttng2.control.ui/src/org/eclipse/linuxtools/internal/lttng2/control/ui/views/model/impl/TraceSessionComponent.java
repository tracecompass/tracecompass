/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 *   Marc-Andre Laperle - Support for opening a live session
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISnapshotInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.SessionInfo;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.property.TraceSessionPropertySource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <p>
 * Implementation of the trace session component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceSessionComponent extends TraceControlComponent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component (inactive state).
     */
    public static final String TRACE_SESSION_ICON_FILE_INACTIVE = "icons/obj16/session_inactive.gif"; //$NON-NLS-1$
    /**
     * Path to icon file for this component (active state).
     */
    public static final String TRACE_SESSION_ICON_FILE_ACTIVE = "icons/obj16/session_active.gif"; //$NON-NLS-1$
    /**
     * Path to icon file for this component (destroyed state).
     */
    public static final String TRACE_SESSION_ICON_FILE_DESTROYED = "icons/obj16/session_destroyed.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The session information.
     */
    private ISessionInfo fSessionInfo = null;
    /**
     * A flag to indicate if session has been destroyed.
     */
    private boolean fIsDestroyed = false;
    /**
     * The image to be displayed in state active.
     */
    private Image fActiveImage = null;
    /**
     * The image to be displayed in state destroyed
     */
    private Image fDestroyedImage = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public TraceSessionComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_SESSION_ICON_FILE_INACTIVE);
        setToolTip(Messages.TraceControl_SessionDisplayName);
        fSessionInfo = new SessionInfo(name);
        fActiveImage = Activator.getDefault().loadIcon(TRACE_SESSION_ICON_FILE_ACTIVE);
        fDestroyedImage = Activator.getDefault().loadIcon(TRACE_SESSION_ICON_FILE_DESTROYED);
    }

    /**
     * Constructor
     *
     * @param sessionInfo
     *            the session information used to create the session
     * @param parent
     *            the parent of this component.
     */
    public TraceSessionComponent(ISessionInfo sessionInfo, ITraceControlComponent parent) {
        this(sessionInfo.getName(), parent);
        copyLiveInfo(sessionInfo);
    }

    private void copyLiveInfo(ISessionInfo sessionInfo) {
        // Since we can't retrieve this information from the node, we copy it over
        fSessionInfo.setLive(sessionInfo.isLive());
        fSessionInfo.setLiveDelay(sessionInfo.getLiveDelay());
        fSessionInfo.setLivePort(sessionInfo.getLivePort());
        fSessionInfo.setLiveUrl(sessionInfo.getLiveUrl());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public Image getImage() {
        if (fIsDestroyed) {
            return fDestroyedImage;
        }

        if (fSessionInfo.getSessionState() == TraceSessionState.INACTIVE) {
            return super.getImage();
        }

        return fActiveImage;
    }

    /**
     * @return the whether the session is destroyed or not.
     */
    public boolean isDestroyed() {
        return fIsDestroyed;
    }

    /**
     * Sets the session destroyed state to the given value.
     * @param destroyed - value to set.
     */
    public void setDestroyed(boolean destroyed) {
        fIsDestroyed = destroyed;
    }

    /**
     * @return the session state state (active or inactive).
     */
    public TraceSessionState getSessionState() {
        return fSessionInfo.getSessionState();
    }

    /**
     * Sets the session state  to the given value.
     * @param state - state to set.
     */
    public void setSessionState(TraceSessionState state) {
        fSessionInfo.setSessionState(state);
    }

    /**
     * Sets the event state to the value specified by the given name.
     * @param stateName - state to set.
     */
    public void setSessionState(String stateName) {
        fSessionInfo.setSessionState(stateName);
    }

    /**
     * @return path string where session is located.
     */
    public String getSessionPath() {
        return fSessionInfo.getSessionPath();
    }

    /**
     * Sets the path string (where session is located) to the given value.
     * @param sessionPath - session path to set.
     */
    public void setSessionPath(String sessionPath) {
        fSessionInfo.setSessionPath(sessionPath);
    }

    /**
     * Returns if session is streamed over network
     * @return <code>true</code> if streamed over network else <code>false</code>
     */
    public boolean isStreamedTrace() {
        return fSessionInfo.isStreamedTrace();
    }

    /**
     * Sets whether the trace is streamed or not
     * @param isStreamedTrace <code>true</code> if streamed over network else <code>false</code>
     */
    public void setIsStreamedTrace(boolean isStreamedTrace) {
        fSessionInfo.setStreamedTrace(isStreamedTrace);
    }

    /**
     * Returns whether the session is snapshot session or not
     * @return <code>true</code> if it is snapshot session else <code>false</code>
     */
    public boolean isSnapshotSession() {
        return fSessionInfo.isSnapshotSession();
    }

    /**
     * Gets the snapshot information if available whether the session is a snapshot session or not
     * @return the snapshot information or null if it is not a snapshot session
     */
    public ISnapshotInfo getSnapshotInfo() {
        return fSessionInfo.getSnapshotInfo();
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new TraceSessionPropertySource(this);
        }
        return null;
    }

    /**
     * @return all available domains of this session.
     */
    public TraceDomainComponent[] getDomains() {
        List<ITraceControlComponent> sessions = getChildren(TraceDomainComponent.class);
        return sessions.toArray(new TraceDomainComponent[sessions.size()]);
    }

    /**
     * @return the parent target node
     */
    public TargetNodeComponent getTargetNode() {
        return ((TraceSessionGroup)getParent()).getTargetNode();
    }

    /**
     * Returns whether the kernel provider is available or not
     * @return <code>true</code> if kernel provide is available or <code>false</code>
     */
    public boolean hasKernelProvider() {
        List<ITraceControlComponent> providerGroups = getTargetNode().getChildren(TraceProviderGroup.class);
        return (!providerGroups.isEmpty() ? ((TraceProviderGroup) providerGroups.get(0)).hasKernelProvider() : false);
    }

    /**
     * Returns if node supports filtering of events
     * @return <code>true</code> if node supports filtering else <code>false</code>
     */
    public boolean isEventFilteringSupported() {
        return ((TargetNodeComponent)getParent().getParent()).isEventFilteringSupported();
    }

    /**
     * Returns if node supports snapshots or not
     * @return <code>true</code> if it supports snapshots else <code>false</code>
     *
     */
    public boolean isSnapshotSupported() {
        return ((TargetNodeComponent)getParent().getParent()).isSnapshotSupported();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Retrieves the session configuration from the node.
     *
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void getConfigurationFromNode(IProgressMonitor monitor)
            throws ExecutionException {
        removeAllChildren();
        ISessionInfo oldSessionInfo = fSessionInfo;
        fSessionInfo = getControlService().getSession(getName(), monitor);
        copyLiveInfo(oldSessionInfo);

        IDomainInfo[] domains = fSessionInfo.getDomains();
        for (int i = 0; i < domains.length; i++) {
            TraceDomainComponent domainComponent = new TraceDomainComponent(
                    domains[i].getName(), this);
            addChild(domainComponent);
            domainComponent.setDomainInfo(domains[i]);
        }
    }

    /**
     * Starts the session.
     *
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void startSession(IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().startSession(getName(), monitor);
    }

    /**
     * Starts the session.
     *
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void stopSession(IProgressMonitor monitor) throws ExecutionException {
        getControlService().stopSession(getName(), monitor);
    }

    /**
     * Enables channels with given names which are part of this domain. If a
     * given channel doesn't exists it creates a new channel with the given
     * parameters (or default values if given parameter is null).
     *
     * @param channelNames
     *            - a list of channel names to enable on this domain
     * @param info
     *            - channel information to set for the channel (use null for
     *            default)
     * @param isKernel
     *            - a flag for indicating kernel or UST.
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableChannels(List<String> channelNames, IChannelInfo info,
            boolean isKernel, IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().enableChannels(getName(), channelNames, isKernel,
                info, monitor);
    }

    /**
     * Enables a list of events with no additional parameters.
     *
     * @param eventNames
     *            - a list of event names to enabled.
     * @param isKernel
     *            - a flag for indicating kernel or UST.
     * @param filterExpression
     *            - a filter expression
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableEvents(List<String> eventNames, boolean isKernel,
            String filterExpression, IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableEvents(getName(), null, eventNames, isKernel,
                filterExpression, monitor);
    }

    /**
     * Enables all syscalls (for kernel domain)
     *
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableSyscalls(IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().enableSyscalls(getName(), null, monitor);
    }

    /**
     * Enables a dynamic probe (for kernel domain)
     *
     * @param eventName
     *            - event name for probe
     * @param isFunction
     *            - true for dynamic function entry/return probe else false
     * @param probe
     *            - the actual probe
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableProbe(String eventName, boolean isFunction, String probe,
            IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableProbe(getName(), null, eventName, isFunction,
                probe, monitor);
    }

    /**
     * Enables events using log level.
     *
     * @param eventName
     *            - a event name
     * @param logLevelType
     *            - a log level type
     * @param level
     *            - a log level
     * @param filterExpression
     *            - a filter expression
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableLogLevel(String eventName, LogLevelType logLevelType,
            TraceLogLevel level, String filterExpression, IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().enableLogLevel(getName(), null, eventName,
                logLevelType, level, null, monitor);
    }

    /**
     * Gets all available contexts to be added to channels/events.
     *
     * @param monitor
     *            The monitor that will indicate the progress
     * @return the list of available contexts
     * @throws ExecutionException
     *             If the command fails
     */
    public List<String> getContextList(IProgressMonitor monitor)
            throws ExecutionException {
        return getControlService().getContextList(monitor);
    }

    /**
     * Records a snapshot.
     *
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void recordSnapshot(IProgressMonitor monitor) throws ExecutionException {
        getControlService().recordSnapshot(getName(), monitor);
    }

    /**
     * Returns if session is live.
     * @return <code>true</code> if session if live else <code>false</code>
     */
    public boolean isLiveTrace() {
        return fSessionInfo.isLive();
    }

    /**
     * Get the live URL.
     *
     * @return the live URL
     */
    public String getLiveUrl() {
        return fSessionInfo.getLiveUrl();
    }

    /**
     * Get the live port.
     *
     * @return the live port
     */
    public Integer getLivePort() {
        return fSessionInfo.getLivePort();
    }
}
