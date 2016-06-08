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
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ILoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.DomainInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.TraceDomainPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <p>
 * Implementation of the trace domain component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceDomainComponent extends TraceControlComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */
    public static final String TRACE_DOMAIN_ICON_FILE = "icons/obj16/domain.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The domain information.
     */
    private IDomainInfo fDomainInfo = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public TraceDomainComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_DOMAIN_ICON_FILE);
        setToolTip(Messages.TraceControl_DomainDisplayName);
        fDomainInfo = new DomainInfo(name);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Sets the domain information.
     * @param domainInfo - the domain information to set.
     */
    public void setDomainInfo(IDomainInfo domainInfo) {
        fDomainInfo = domainInfo;
        IChannelInfo[] channels = fDomainInfo.getChannels();
        for (int i = 0; i < channels.length; i++) {
            TraceChannelComponent channel = new TraceChannelComponent(channels[i].getName(), this);
            channel.setChannelInfo(channels[i]);
            addChild(channel);
        }

        // Since the loggers are not in a channel in the JUL domain, the loggers
        // won't be added by the previous loop.
        if (TraceDomainType.JUL.equals(domainInfo.getDomain())) {
            List<ILoggerInfo> loggers = fDomainInfo.getLoggers();
            for (ILoggerInfo loggerInfo : loggers) {
                TraceLoggerComponent logger = new TraceLoggerComponent(loggerInfo.getName(), this);
                logger.setLoggerInfo(loggerInfo);
                addChild(logger);
            }
        }
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IPropertySource.class) {
            return adapter.cast(new TraceDomainPropertySource(this));
        }
        return null;
    }

    /**
     * @return session name from parent
     */
    public String getSessionName() {
        return ((TraceSessionComponent)getParent()).getName();
    }

    /**
     * @return session from parent
     */
    public TraceSessionComponent getSession() {
       return (TraceSessionComponent)getParent();
    }

    /**
     * @return the domain type ({@link TraceDomainType})
     */
    public TraceDomainType getDomain() {
        return fDomainInfo.getDomain();
    }

    /**
     * Sets the domain type
     *
     * @param domain
     *            the domain type ({@link TraceDomainType})
     */
    public void setDomain(TraceDomainType domain) {
        fDomainInfo.setDomain(domain);
    }

    /**
     * @return returns all available channels for this domain.
     */
    public TraceChannelComponent[] getChannels() {
        List<ITraceControlComponent> channels = getChildren(TraceChannelComponent.class);
        return channels.toArray(new TraceChannelComponent[channels.size()]);
    }

    /**
     * @return the parent target node
     */
    public TargetNodeComponent getTargetNode() {
        return ((TraceSessionComponent)getParent()).getTargetNode();
    }

    /**
     * @return the buffer type
     */
    public BufferType getBufferType(){
        return fDomainInfo.getBufferType();
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
    public void getConfigurationFromNode(IProgressMonitor monitor) throws ExecutionException {
        TraceSessionComponent session = (TraceSessionComponent) getParent();
        session.getConfigurationFromNode(monitor);
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
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableChannels(List<String> channelNames, IChannelInfo info,
            IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableChannels(getParent().getName(), channelNames,
                getDomain(), info, monitor);
    }

    /**
     * Disables channels with given names which are part of this domain.
     *
     * @param channelNames
     *            - a list of channel names to enable on this domain
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void disableChannels(List<String> channelNames,
            IProgressMonitor monitor) throws ExecutionException {
        getControlService().disableChannels(getParent().getName(),
                channelNames, getDomain(), monitor);
    }

    /**
     * Enables a list of events with no additional parameters.
     *
     * @param eventNames
     *            - a list of event names to enabled.
     * @param filterExpression
     *            - a filter expression
     * @param excludedEvents
     *            - a list of events to exclude.
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableEvents(List<String> eventNames, String filterExpression, List<String> excludedEvents, IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().enableEvents(getSessionName(), null, eventNames,
                getDomain(), filterExpression, excludedEvents, monitor);
    }

    /**
     * Disables events with given names which are part of this domain.
     *
     * @param loggerNames
     *            - a list of logger names to disable
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void disableLoggers(List<String> loggerNames,
            IProgressMonitor monitor) throws ExecutionException {
        getControlService().disableEvent(getSessionName(), null, loggerNames, getDomain(), monitor);
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
        getControlService().enableSyscalls(getSessionName(), null, monitor);
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
        getControlService().enableProbe(getSessionName(), null, eventName,
                isFunction, probe, monitor);
    }

    /**
     * Enables events using log level.
     *
     * @param eventNames
     *            - a list of event names
     * @param logLevelType
     *            - a log level type
     * @param level
     *            - a log level
     * @param filterExpression
     *            - a filter expression
     * @param domain
     *            - the domain type ({@link TraceDomainType})
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableLogLevel(List<String> eventNames, LogLevelType logLevelType,
            ITraceLogLevel level, String filterExpression, TraceDomainType domain, IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().enableLogLevel(getSessionName(), null, eventNames,
                logLevelType, level, filterExpression, domain, monitor);
    }

    /**
     * Add contexts to given channels and or events
     *
     * @param contexts
     *            - a list of contexts to add
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void addContexts(List<String> contexts, IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().addContexts(getSessionName(), null, null,
                getDomain(), contexts, monitor);
    }

}
