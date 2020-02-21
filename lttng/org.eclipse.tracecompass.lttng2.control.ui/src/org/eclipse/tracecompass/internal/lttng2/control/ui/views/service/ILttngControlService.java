/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.service;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISnapshotInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IUstProviderInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;


/**
* <p>
* Interface for LTTng trace control command service.
* </p>
*
* @author Bernd Hufmann
*/
public interface ILttngControlService {


    /**
     * List to enable all events
     */
    @NonNull List<String> ALL_EVENTS = Collections.singletonList("*");  //$NON-NLS-1$

    /**
     * @return the LTTng version object
     */
    @NonNull LttngVersion getVersion();

    /**
     * @return the version string
     */
    @NonNull String getVersionString();

    /**
     * Checks if given version is supported by this ILTTngControlService implementation.
     *
     * @param version The version to check
     * @return <code>true</code> if version is supported else <code>false</code>
     */
    boolean isVersionSupported(String version);

    /**
     * Retrieves the existing sessions names from the node.
     *
     * @param monitor
     *            - a progress monitor
     * @return a list of session names.
     * @throws ExecutionException
     *             If the command fails
     */
    @NonNull List<String> getSessionNames(IProgressMonitor monitor) throws ExecutionException;

    /**
     * Retrieves the session information with the given name the node.
     *
     * @param sessionName
     *            - the session name
     * @param monitor
     *            - a progress monitor
     * @return session information
     * @throws ExecutionException
     *             If the command fails
     */
    @Nullable ISessionInfo getSession(String sessionName, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Retrieves the snapshot output information from the node
     * @param sessionName
     *            - the session name
     * @param monitor
     *            - a progress monitor
     * @return snapshot output information
     * @throws ExecutionException
     *          if command fails
     */
    @Nullable ISnapshotInfo getSnapshotInfo(String sessionName, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Retrieves the kernel provider information (i.e. the kernel events)
     *
     * @param monitor
     *            - a progress monitor
     * @return the list of existing kernel events.
     * @throws ExecutionException
     *             If the command fails
     */
    @NonNull List<IBaseEventInfo> getKernelProvider(IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Retrieves the UST provider information from the node.
     *
     * @return - the UST provider information.
     * @throws ExecutionException
     *             If the command fails
     */
    @NonNull public List<IUstProviderInfo> getUstProvider() throws ExecutionException;

    /**
     * Retrieves the UST provider information from the node.
     *
     * @param monitor
     *            - a progress monitor
     * @return the UST provider information.
     * @throws ExecutionException
     *             If the command fails
     */
    @NonNull List<IUstProviderInfo> getUstProvider(IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Creates a session with given session name and location.
     *
     * @param sessionInfo
     *            the session information used to create the session
     * @param monitor
     *            - a progress monitor
     *
     * @return the session information
     * @throws ExecutionException
     *             If the command fails
     */
    @Nullable ISessionInfo createSession(ISessionInfo sessionInfo, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Destroys a session with given session name.
     *
     * @param sessionName
     *            - a session name to destroy
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void destroySession(String sessionName, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Starts a session with given session name.
     *
     * @param sessionName
     *            - a session name to start
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void startSession(String sessionName, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Stops a session with given session name.
     *
     * @param sessionName
     *            - a session name to stop
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void stopSession(String sessionName, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Enables a list of channels for given session and given channel
     * information (configuration).
     *
     * @param sessionName
     *            - a session name to create
     * @param channelNames
     *            - a list of channel names to be enabled
     * @param domain
     *            - indicate the domain type ({@link TraceDomainType})
     * @param info
     *            - channel information used for creation of a channel (or null
     *            for default)
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void enableChannels(String sessionName, List<String> channelNames,
            TraceDomainType domain, IChannelInfo info, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Disables a list of channels for given session and given channel
     * information (configuration).
     *
     * @param sessionName
     *            - a session name to create
     * @param channelNames
     *            - a list of channel names to be enabled
     * @param domain
     *            - indicate the domain type ({@link TraceDomainType})
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void disableChannels(String sessionName, List<String> channelNames,
            TraceDomainType domain, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Enables a list of events with no additional parameters.
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name or null for default channel
     * @param eventNames
     *            - a list of event names to be enabled, or null (list of size =
     *            0)for all events .
     * @param domain
     *            - indicate the domain type ({@link TraceDomainType})
     * @param filterExpression
     *            - a filter expression
     * @param excludedEvents
     *            - a list of event names to be excluded, or null
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void enableEvents(String sessionName, String channelName,
            List<String> eventNames, TraceDomainType domain, String filterExpression, List<String> excludedEvents,
            IProgressMonitor monitor)
            throws ExecutionException;


    /**
     * Enables all syscall events.
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name or null for default channel
     * @param eventNames
     *            - a list of event names to be enabled, or null or empty List
     *            for all events.
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void enableSyscalls(String sessionName, String channelName,
            List<String> eventNames, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Enables a dynamic probe or dynamic function entry/return probe.
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name or null for default channel
     * @param eventName
     *            - a event name
     * @param isFunction
     *            - true for dynamic function entry/return probe else false
     * @param probe
     *            - a dynamic probe information
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void enableProbe(String sessionName, String channelName,
            String eventName, boolean isFunction, String probe,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * Enables events using log level
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name (null for default channel)
     * @param eventNames
     *            - a list of event names
     * @param logLevelType
     *            - a log level type
     * @param level
     *            - a log level
     * @param filterExpression
     *            - a filter expression
     * @param domain
     *            - the domain type
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void enableLogLevel(String sessionName, String channelName,
            List<String> eventNames, LogLevelType logLevelType, ITraceLogLevel level,
            String filterExpression, TraceDomainType domain,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * Disables a list of events with no additional parameters.
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name (null for default channel)
     * @param eventNames
     *            - a list of event names to enabled.
     * @param domain
     *            - indicate the domain type ({@link TraceDomainType})
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void disableEvent(String sessionName, String channelName,
            List<String> eventNames, TraceDomainType domain, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Gets all available context names to be added to channels/events.
     *
     * @param monitor
     *            The progress monitor
     * @return the list of available contexts
     * @throws ExecutionException
     *             If the command fails
     */
    @NonNull List<String> getContextList(IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Add contexts to given channels and or events
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name (null for all channels)
     * @param eventName
     *            - a event name (null for all events)
     * @param domain
     *            - indicate the domain type ({@link TraceDomainType})
     * @param contexts
     *            - a list of name of contexts to add
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void addContexts(String sessionName, String channelName,
            String eventName, TraceDomainType domain, List<String> contexts,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * Records a snapshot.
     *
     * @param sessionName
     *            - a session name
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void recordSnapshot(String sessionName, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Executes a list of commands
     *
     * @param monitor
     *      - a progress monitor
     * @param commands
     *      - array of commands
     * @throws ExecutionException
     *      If a command fails
     */
    void runCommands(IProgressMonitor monitor, List<String> commands)
            throws ExecutionException;

    /**
     * Load all or a given session.
     *
     * @param inputPath
     *            a input path to load session from or null for load all from default
     * @param isForce
     *            flag whether to overwrite existing or not
     * @param monitor
     *            a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void loadSession(@Nullable String inputPath, boolean isForce, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Save all or a given session.
     *
     * @param session
     *            a session name to save or null for all
     * @param outputPath
     *            a path to save session or null for default location
     * @param isForce
     *            flag whether to overwrite existing or not
     * @param monitor
     *            a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    void saveSession(@Nullable String session, @Nullable String outputPath, boolean isForce, IProgressMonitor monitor)
            throws ExecutionException;
}
