/**********************************************************************
 * Copyright (c) 2012, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 *   Simon Delisle - Updated for support of LTTng Tools 2.2
 *   Marc-Andre Laperle - Support for creating a live session
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.service;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IFieldInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IProbeEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISnapshotInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IUstProviderInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.DomainInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.EventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.FieldInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.ProbeEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.SessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.SnapshotInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.UstProviderInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.logging.ControlCommandLogger;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.preferences.ControlPreferences;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandInput;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandResult;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;

/**
 * <p>
 * Service for sending LTTng trace control commands to remote host.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class LTTngControlService implements ILttngControlService {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The command shell implementation
     */
    private final @NonNull ICommandShell fCommandShell;

    /**
     * The version string.
     */
    private @NonNull LttngVersion fVersion = LttngVersion.NULL_VERSION;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param shell
     *            - the command shell implementation to use
     */
    public LTTngControlService(@NonNull ICommandShell shell) {
        fCommandShell = shell;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public String getVersionString() {
        return nullToEmptyString(fVersion.toString());
    }

    @Override
    public LttngVersion getVersion() {
        return fVersion;
    }

    /**
     * Sets the version of the LTTng 2.0 control service.
     *
     * @param version
     *            - a version to set
     */
    public void setVersion(@Nullable String version) {
        if (version != null) {
            fVersion = new LttngVersion(version);
        }
    }

    /**
     * Sets the version of the LTTng 2.x control service.
     *
     * @param version
     *            - a version to set
     */
    protected void setVersion(LttngVersion version) {
        if (version != null) {
            fVersion = version;
        }
    }

    @Override
    public boolean isVersionSupported(String version) {
        LttngVersion tmp = new LttngVersion(version);
        return (fVersion.compareTo(tmp) >= 0) ? true : false;
    }

    /**
     * Returns the command shell implementation.
     *
     * @return the command shell implementation
     */
    protected ICommandShell getCommandShell() {
        return fCommandShell;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public List<String> getSessionNames(IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_LIST);

        ICommandResult result = executeCommand(command, monitor);

        // Output:
        // Available tracing sessions:
        // 1) mysession1 (/home/user/lttng-traces/mysession1-20120123-083928)
        // [inactive]
        // 2) mysession (/home/user/lttng-traces/mysession-20120123-083318)
        // [inactive]
        //
        // Use lttng list <session_name> for more details

        ArrayList<String> retArray = new ArrayList<>();
        for (String line : result.getOutput()) {
            Matcher matcher = LTTngControlServiceConstants.SESSION_PATTERN.matcher(line);
            if (matcher.matches()) {
                retArray.add(matcher.group(2).trim());
            }
        }
        return retArray;
    }

    /**
     * Check if there is a pattern to be ignored into a sequence of string
     *
     * @param input
     *            an input list of Strings
     * @param pattern
     *            the pattern to search for
     * @return if the pattern exist in the array of string
     */
    protected boolean ignoredPattern(List<String> input, Pattern pattern) {
        for (String line : input) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ISessionInfo getSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_LIST, sessionName);
        ICommandResult result = executeCommand(command, monitor);

        int index = 0;

        // Output:
        // Tracing session mysession2: [inactive]
        // Trace path: /home/eedbhu/lttng-traces/mysession2-20120123-110330
        ISessionInfo sessionInfo = new SessionInfo(sessionName);

        while (index < result.getOutput().size()) {
            // Tracing session mysession2: [inactive]
            // Trace path: /home/eedbhu/lttng-traces/mysession2-20120123-110330
            //
            // === Domain: Kernel ===
            //
            String line = result.getOutput().get(index);
            Matcher matcher = LTTngControlServiceConstants.TRACE_SESSION_PATTERN.matcher(line);
            if (matcher.matches()) {
                sessionInfo.setSessionState(matcher.group(2));
                index++;
                continue;
            }

            matcher = LTTngControlServiceConstants.TRACE_SNAPSHOT_SESSION_PATTERN.matcher(line);
            if (matcher.matches()) {
                sessionInfo.setSessionState(matcher.group(2));
                // real name will be set later
                ISnapshotInfo snapshotInfo = new SnapshotInfo(""); //$NON-NLS-1$
                sessionInfo.setSnapshotInfo(snapshotInfo);
                index++;
                continue;
            }

            if (!sessionInfo.isSnapshotSession()) {
                matcher = LTTngControlServiceConstants.TRACE_NETWORK_PATH_PATTERN.matcher(line);
                if (matcher.matches()) {
                    sessionInfo.setStreamedTrace(true);
                }

                matcher = LTTngControlServiceConstants.TRACE_SESSION_PATH_PATTERN.matcher(line);
                if (matcher.matches()) {
                    sessionInfo.setSessionPath(matcher.group(1).trim());
                    index++;
                    continue;
                }
            }

            matcher = LTTngControlServiceConstants.DOMAIN_KERNEL_PATTERN.matcher(line);
            if (matcher.matches()) {
                // Create Domain
                IDomainInfo domainInfo = new DomainInfo(Messages.TraceControl_KernelDomainDisplayName);

                // set kernel flag
                domainInfo.setIsKernel(true);

                // in domain kernel
                ArrayList<IChannelInfo> channels = new ArrayList<>();
                index = parseDomain(result.getOutput(), index, channels, domainInfo);

                if (channels.size() > 0) {
                    // add domain
                    sessionInfo.addDomain(domainInfo);

                    // set channels
                    domainInfo.setChannels(channels);
                }
                continue;
            }

            matcher = LTTngControlServiceConstants.DOMAIN_UST_GLOBAL_PATTERN.matcher(line);
            if (matcher.matches()) {
                IDomainInfo domainInfo = new DomainInfo(Messages.TraceControl_UstGlobalDomainDisplayName);

                // set kernel flag
                domainInfo.setIsKernel(false);

                // in domain UST
                ArrayList<IChannelInfo> channels = new ArrayList<>();
                index = parseDomain(result.getOutput(), index, channels, domainInfo);

                if (channels.size() > 0) {
                    // add domain
                    sessionInfo.addDomain(domainInfo);

                    // set channels
                    domainInfo.setChannels(channels);
                }
                continue;
            }
            matcher = LTTngControlServiceConstants.LIST_LIVE_TIMER_INTERVAL_PATTERN.matcher(line);
            if (matcher.matches()) {
                long liveDelay = Long.parseLong(matcher.group(1));
                if ((liveDelay > 0) && (liveDelay <= LTTngControlServiceConstants.MAX_LIVE_TIMER_INTERVAL)) {
                    sessionInfo.setLive(true);
                    sessionInfo.setLiveUrl(SessionInfo.DEFAULT_LIVE_NETWORK_URL);
                    sessionInfo.setLivePort(SessionInfo.DEFAULT_LIVE_PORT);
                    sessionInfo.setLiveDelay(liveDelay);
                }
                index++;
                continue;
            }

            index++;
        }

        if (sessionInfo.isSnapshotSession()) {
            ISnapshotInfo snapshot = getSnapshotInfo(sessionName, monitor);
            sessionInfo.setSnapshotInfo(snapshot);
        }

        return sessionInfo;
    }

    @Override
    public ISnapshotInfo getSnapshotInfo(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_SNAPSHOT, LTTngControlServiceConstants.COMMAND_LIST_SNAPSHOT_OUTPUT, LTTngControlServiceConstants.OPTION_SESSION, sessionName);
        ICommandResult result = executeCommand(command, monitor);

        int index = 0;

        // Output:
        // [1] snapshot-1: /home/user/lttng-traces/my-20130909-114431
        // or
        // [3] snapshot-3: net4://172.0.0.1/
        ISnapshotInfo snapshotInfo = new SnapshotInfo(""); //$NON-NLS-1$

        while (index < result.getOutput().size()) {
            String line = result.getOutput().get(index);
            Matcher matcher = LTTngControlServiceConstants.LIST_SNAPSHOT_OUTPUT_PATTERN.matcher(line);
            if (matcher.matches()) {
                snapshotInfo.setId(Integer.valueOf(matcher.group(1)));
                snapshotInfo.setName(matcher.group(2));
                snapshotInfo.setSnapshotPath(matcher.group(3));

                Matcher matcher2 = LTTngControlServiceConstants.SNAPSHOT_NETWORK_PATH_PATTERN.matcher(snapshotInfo.getSnapshotPath());
                if (matcher2.matches()) {
                    snapshotInfo.setStreamedSnapshot(true);
                }

                index++;
                break;
            }
            index++;
        }

        return snapshotInfo;
    }

    @Override
    public List<IBaseEventInfo> getKernelProvider(IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_LIST, LTTngControlServiceConstants.OPTION_KERNEL);
        ICommandResult result = executeCommand(command, monitor, false);

        List<IBaseEventInfo> events = new ArrayList<>();

        // Ignore the following 2 cases:
        // Spawning a session daemon
        // Error: Unable to list kernel events
        // or:
        // Error: Unable to list kernel events
        //
        if (ignoredPattern(result.getErrorOutput(), LTTngControlServiceConstants.LIST_KERNEL_NO_KERNEL_PROVIDER_PATTERN)) {
            return events;
        }

        if (isError(result)) {
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + result.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Kernel events:
        // -------------
        // sched_kthread_stop (type: tracepoint)
        getProviderEventInfo(result.getOutput(), 0, events);
        return events;
    }

    @Override
    public List<IUstProviderInfo> getUstProvider() throws ExecutionException {
        return getUstProvider(new NullProgressMonitor());
    }

    @Override
    public List<IUstProviderInfo> getUstProvider(IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_LIST, LTTngControlServiceConstants.OPTION_UST);

        if (isVersionSupported("2.1.0")) { //$NON-NLS-1$
            command.add(LTTngControlServiceConstants.OPTION_FIELDS);
        }

        ICommandResult result = executeCommand(command, monitor, false);
        List<IUstProviderInfo> allProviders = new ArrayList<>();

        // Workaround for versions 2.0.x which causes a segmentation fault for
        // this command
        // if LTTng Tools is compiled without UST support.
        if (!isVersionSupported("2.1.0") && (result.getResult() != 0)) { //$NON-NLS-1$
            return allProviders;
        }

        // Ignore the following 2 cases:
        // Spawning a session daemon
        // Error: Unable to list UST events: Listing UST events failed
        // or:
        // Error: Unable to list UST events: Listing UST events failed
        //
        if (ignoredPattern(result.getErrorOutput(), LTTngControlServiceConstants.LIST_UST_NO_UST_PROVIDER_PATTERN)) {
            return allProviders;
        }

        if (isError(result)) {
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + result.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Note that field print-outs exists for version >= 2.1.0
        //
        // UST events:
        // -------------
        //
        // PID: 3635 - Name:
        // /home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello
        // ust_tests_hello:tptest_sighandler (loglevel: TRACE_EMERG0) (type:
        // tracepoint)
        // ust_tests_hello:tptest (loglevel: TRACE_EMERG0) (type: tracepoint)
        // field: doublefield (float)
        // field: floatfield (float)
        // field: stringfield (string)
        //
        // PID: 6459 - Name:
        // /home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello
        // ust_tests_hello:tptest_sighandler (loglevel: TRACE_EMERG0) (type:
        // tracepoint)
        // ust_tests_hello:tptest (loglevel: TRACE_EMERG0) (type: tracepoint)
        // field: doublefield (float)
        // field: floatfield (float)
        // field: stringfield (string)

        IUstProviderInfo provider = null;

        int index = 0;
        while (index < result.getOutput().size()) {
            String line = result.getOutput().get(index);
            Matcher matcher = LTTngControlServiceConstants.UST_PROVIDER_PATTERN.matcher(line);
            if (matcher.matches()) {
                provider = new UstProviderInfo(matcher.group(2).trim());
                provider.setPid(Integer.valueOf(matcher.group(1).trim()));
                List<IBaseEventInfo> events = new ArrayList<>();
                index = getProviderEventInfo(result.getOutput(), ++index, events);
                provider.setEvents(events);
                allProviders.add(provider);
            } else {
                index++;
            }
        }
        return allProviders;
    }

    @Override
    public ISessionInfo createSession(ISessionInfo sessionInfo, IProgressMonitor monitor) throws ExecutionException {
        if (sessionInfo.isStreamedTrace()) {
            return createStreamedSession(sessionInfo, monitor);
        }

        ICommandInput command = prepareSessionCreationCommand(sessionInfo);

        ICommandResult result = executeCommand(command, monitor);

        // Session myssession2 created.
        // Traces will be written in
        // /home/user/lttng-traces/myssession2-20120209-095418
        List<String> output = result.getOutput();

        // Get and session name and path
        String name = null;
        String path = null;

        for (String line : output) {
            Matcher nameMatcher = LTTngControlServiceConstants.CREATE_SESSION_NAME_PATTERN.matcher(line);
            Matcher pathMatcher = LTTngControlServiceConstants.CREATE_SESSION_PATH_PATTERN.matcher(line);
            if (nameMatcher.matches()) {
                name = String.valueOf(nameMatcher.group(1).trim());
            } else if (pathMatcher.matches()) {
                path = String.valueOf(pathMatcher.group(1).trim());
            }
        }

        // Verify session name
        if ((name == null) || (!"".equals(sessionInfo.getName()) && !name.equals(sessionInfo.getName()))) { //$NON-NLS-1$
            // Unexpected name returned
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.TraceControl_UnexpectedNameError + ": " + name); //$NON-NLS-1$
        }

        sessionInfo.setName(name);
        // Verify session path
        if (!sessionInfo.isSnapshotSession() &&
                ((path == null) || ((sessionInfo.getSessionPath() != null) && (!path.contains(sessionInfo.getSessionPath()))))) {
            // Unexpected path
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.TraceControl_UnexpectedPathError + ": " + name); //$NON-NLS-1$
        }

        if (sessionInfo.isSnapshotSession()) {
            // Make it a snapshot session - content of snapshot info need to
            // set afterwards using getSession() or getSnapshotInfo()
            sessionInfo.setSnapshotInfo(new SnapshotInfo("")); //$NON-NLS-1$
        } else {
            sessionInfo.setSessionPath(path);
        }

        return sessionInfo;

    }

    /**
     * Basic generation of command for session creation
     *
     * @param sessionInfo
     *            the session to create
     * @return the basic command for command creation
     */
    protected @NonNull ICommandInput prepareSessionCreationCommand(ISessionInfo sessionInfo) {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_CREATE_SESSION);
        if (!sessionInfo.getName().isEmpty()) {
            command.add(sessionInfo.getName());
        }

        String newPath = sessionInfo.getSessionPath();
        if (newPath != null && !"".equals(newPath)) { //$NON-NLS-1$
            command.add(LTTngControlServiceConstants.OPTION_OUTPUT_PATH);
            command.add(newPath);
        }

        if (sessionInfo.isSnapshotSession()) {
            command.add(LTTngControlServiceConstants.OPTION_SNAPSHOT);
        }
        return command;
    }

    private @NonNull ISessionInfo createStreamedSession(ISessionInfo sessionInfo, IProgressMonitor monitor) throws ExecutionException {

        ICommandInput command = prepareStreamedSessionCreationCommand(sessionInfo);

        ICommandResult result = executeCommand(command, monitor);

        // Verify output
        List<String> output = result.getOutput();

        // Get and session name and path
        String name = null;
        String path = null;

        for (String line : output) {
            Matcher nameMatcher = LTTngControlServiceConstants.CREATE_SESSION_NAME_PATTERN.matcher(line);
            Matcher pathMatcher = LTTngControlServiceConstants.CREATE_SESSION_PATH_PATTERN.matcher(line);

            if (nameMatcher.matches()) {
                name = String.valueOf(nameMatcher.group(1).trim());
            } else if (pathMatcher.matches() && (sessionInfo.getNetworkUrl() != null)) {
                path = String.valueOf(pathMatcher.group(1).trim());
            }
        }

        // Verify session name
        if ((name == null) || (!"".equals(sessionInfo.getName()) && !name.equals(sessionInfo.getName()))) { //$NON-NLS-1$
            // Unexpected name returned
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.TraceControl_UnexpectedNameError + ": " + name); //$NON-NLS-1$
        }

        sessionInfo.setName(name);

        sessionInfo.setStreamedTrace(true);

        // Verify session path
        if (sessionInfo.getNetworkUrl() != null) {
            if (!sessionInfo.isSnapshotSession() && (path == null)) {
                // Unexpected path
                throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                        Messages.TraceControl_UnexpectedPathError + ": " + name); //$NON-NLS-1$
            }

            if (sessionInfo.isSnapshotSession()) {
                sessionInfo.setStreamedTrace(false);
            } else {
                sessionInfo.setSessionPath(path);
                // Check file protocol
                Matcher matcher = LTTngControlServiceConstants.TRACE_FILE_PROTOCOL_PATTERN.matcher(path);
                if (matcher.matches()) {
                    sessionInfo.setStreamedTrace(false);
                }
            }
        }

        // When using controlUrl and dataUrl the full session path is not known
        // yet and will be set later on when listing the session

        return sessionInfo;
    }

    /**
     * Basic generation of command for streamed session creation
     *
     * @param sessionInfo
     *            the session to create
     * @return the basic command for command creation
     */
     protected @NonNull ICommandInput prepareStreamedSessionCreationCommand(ISessionInfo sessionInfo) {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_CREATE_SESSION);
        if (!sessionInfo.getName().isEmpty()) {
            command.add(sessionInfo.getName());
        }

        if (sessionInfo.isSnapshotSession()) {
            command.add(LTTngControlServiceConstants.OPTION_SNAPSHOT);
        } else if (sessionInfo.isLive()) {
            command.add(LTTngControlServiceConstants.OPTION_LIVE);
            if (sessionInfo.getLiveDelay() != LTTngControlServiceConstants.UNUSED_VALUE) {
                command.add(String.valueOf(sessionInfo.getLiveDelay()));
            }
        }

        if (sessionInfo.getNetworkUrl() != null) {
            command.add(LTTngControlServiceConstants.OPTION_NETWORK_URL);
            command.add(sessionInfo.getNetworkUrl());
        } else {
            command.add(LTTngControlServiceConstants.OPTION_CONTROL_URL);
            command.add(sessionInfo.getControlUrl());

            command.add(LTTngControlServiceConstants.OPTION_DATA_URL);
            command.add(sessionInfo.getDataUrl());
        }
        return command;
    }

    @Override
    public void destroySession(String sessionName, IProgressMonitor monitor) throws ExecutionException {

        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_DESTROY_SESSION, sessionName);

        ICommandResult result = executeCommand(command, monitor, false);
        boolean isError = isError(result);
        if (isError && !ignoredPattern(result.getErrorOutput(), LTTngControlServiceConstants.SESSION_NOT_FOUND_ERROR_PATTERN)) {
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + result.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Session <sessionName> destroyed
    }

    @Override
    public void startSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {

        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_START_SESSION, sessionName);

        executeCommand(command, monitor);

        // Session <sessionName> started
    }

    @Override
    public void stopSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_STOP_SESSION, sessionName);

        executeCommand(command, monitor);

        // Session <sessionName> stopped

    }

    @Override
    public void enableChannels(String sessionName, List<String> channelNames, boolean isKernel, IChannelInfo info, IProgressMonitor monitor) throws ExecutionException {

        // no channels to enable
        if (channelNames.isEmpty()) {
            return;
        }

        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_ENABLE_CHANNEL);

        command.add(toCsv(channelNames));

        if (isKernel) {
            command.add(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.add(LTTngControlServiceConstants.OPTION_UST);
        }

        command.add(LTTngControlServiceConstants.OPTION_SESSION);
        command.add(sessionName);

        if (info != null) {
            // --discard Discard event when buffers are full (default)

            // --overwrite Flight recorder mode
            if (info.isOverwriteMode()) {
                command.add(LTTngControlServiceConstants.OPTION_OVERWRITE);
            }
            // --subbuf-size SIZE Subbuffer size in bytes
            // (default: 4096, kernel default: 262144)
            if (info.getSubBufferSize() != LTTngControlServiceConstants.UNUSED_VALUE) {
                command.add(LTTngControlServiceConstants.OPTION_SUB_BUFFER_SIZE);
                command.add(String.valueOf(info.getSubBufferSize()));
            }

            // --num-subbuf NUM Number of subbufers
            if (info.getNumberOfSubBuffers() != LTTngControlServiceConstants.UNUSED_VALUE) {
                command.add(LTTngControlServiceConstants.OPTION_NUM_SUB_BUFFERS);
                command.add(String.valueOf(info.getNumberOfSubBuffers()));
            }

            // --switch-timer USEC Switch timer interval in usec
            if (info.getSwitchTimer() != LTTngControlServiceConstants.UNUSED_VALUE) {
                command.add(LTTngControlServiceConstants.OPTION_SWITCH_TIMER);
                command.add(String.valueOf(info.getSwitchTimer()));
            }

            // --read-timer USEC Read timer interval in usec
            if (info.getReadTimer() != LTTngControlServiceConstants.UNUSED_VALUE) {
                command.add(LTTngControlServiceConstants.OPTION_READ_TIMER);
                command.add(String.valueOf(info.getReadTimer()));
            }

            if (isVersionSupported("2.2.0")) { //$NON-NLS-1$
                // --buffers-uid Every application sharing the same UID use the
                // same buffers --buffers-pid Buffers are allocated per PID
                if (!isKernel) {
                    if (info.getBufferType() == BufferType.BUFFER_PER_PID) {
                        command.add(LTTngControlServiceConstants.OPTION_PER_PID_BUFFERS);

                    } else if (info.getBufferType() == BufferType.BUFFER_PER_UID) {
                        command.add(LTTngControlServiceConstants.OPTION_PER_UID_BUFFERS);
                    }
                }

                // -C SIZE Maximum size of trace files in bytes
                if (info.getMaxSizeTraceFiles() != LTTngControlServiceConstants.UNUSED_VALUE) {
                    command.add(LTTngControlServiceConstants.OPTION_MAX_SIZE_TRACE_FILES);
                    command.add(String.valueOf(info.getMaxSizeTraceFiles()));
                }

                // -W NUM Maximum number of trace files
                if (info.getMaxNumberTraceFiles() != LTTngControlServiceConstants.UNUSED_VALUE) {
                    command.add(LTTngControlServiceConstants.OPTION_MAX_TRACE_FILES);
                    command.add(String.valueOf(info.getMaxNumberTraceFiles()));
                }
            }
        }

        executeCommand(command, monitor);

    }

    @Override
    public void disableChannels(String sessionName, List<String> channelNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {

        // no channels to enable
        if (channelNames.isEmpty()) {
            return;
        }

        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_DISABLE_CHANNEL);

        command.add(toCsv(channelNames));

        if (isKernel) {
            command.add(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.add(LTTngControlServiceConstants.OPTION_UST);
        }

        command.add(LTTngControlServiceConstants.OPTION_SESSION);
        command.add(sessionName);

        executeCommand(command, monitor);
    }

    @Override
    public void enableEvents(String sessionName, String channelName, List<String> eventNames, boolean isKernel, String filterExpression, IProgressMonitor monitor) throws ExecutionException {

        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_ENABLE_EVENT);
        boolean isAllEvents = ALL_EVENTS.equals(eventNames);

        if (isAllEvents || (eventNames == null) || (eventNames.isEmpty())) {
            command.add(LTTngControlServiceConstants.OPTION_ALL);
        } else {
            command.add(toCsv(eventNames));
        }

        if (isKernel) {
            command.add(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.add(LTTngControlServiceConstants.OPTION_UST);
        }

        command.add(LTTngControlServiceConstants.OPTION_SESSION);
        command.add(sessionName);

        if (channelName != null) {
            command.add(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.add(channelName);
        }

        if (!isAllEvents) {
            command.add(LTTngControlServiceConstants.OPTION_TRACEPOINT);
        }

        if (filterExpression != null) {
            command.add(LTTngControlServiceConstants.OPTION_FILTER);
            command.add(filterExpression);
        }

        executeCommand(command, monitor);

    }

    @Override
    public void enableSyscalls(String sessionName, String channelName, IProgressMonitor monitor) throws ExecutionException {

        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_ENABLE_EVENT);

        command.add(LTTngControlServiceConstants.OPTION_ALL);
        command.add(LTTngControlServiceConstants.OPTION_KERNEL);


        command.add(LTTngControlServiceConstants.OPTION_SESSION);
        command.add(sessionName);

        if (channelName != null) {
            command.add(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.add(channelName);
        }

        command.add(LTTngControlServiceConstants.OPTION_SYSCALL);

        executeCommand(command, monitor);
    }

    @Override
    public void enableProbe(String sessionName, String channelName, String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_ENABLE_EVENT);

        command.add(eventName);
        command.add(LTTngControlServiceConstants.OPTION_KERNEL);

        command.add(LTTngControlServiceConstants.OPTION_SESSION);
        command.add(sessionName);

        if (channelName != null) {
            command.add(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.add(channelName);
        }
        if (isFunction) {
            command.add(LTTngControlServiceConstants.OPTION_FUNCTION_PROBE);
        } else {
            command.add(LTTngControlServiceConstants.OPTION_PROBE);
        }

        command.add(probe);

        executeCommand(command, monitor);
    }

    @Override
    public void enableLogLevel(String sessionName, String channelName, String eventName, LogLevelType logLevelType, TraceLogLevel level, String filterExpression, IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_ENABLE_EVENT);

        command.add(eventName);
        command.add(LTTngControlServiceConstants.OPTION_UST);

        command.add(LTTngControlServiceConstants.OPTION_SESSION);
        command.add(sessionName);

        if (channelName != null) {
            command.add(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.add(channelName);
        }

        if (logLevelType == LogLevelType.LOGLEVEL) {
            command.add(LTTngControlServiceConstants.OPTION_LOGLEVEL);
        } else if (logLevelType == LogLevelType.LOGLEVEL_ONLY) {
            command.add(LTTngControlServiceConstants.OPTION_LOGLEVEL_ONLY);

        } else {
            return;
        }
        command.add(level.getInName());

        executeCommand(command, monitor);
    }

    @Override
    public void disableEvent(String sessionName, String channelName, List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_DISABLE_EVENT);

        if (eventNames == null) {
            command.add(LTTngControlServiceConstants.OPTION_ALL);
        } else {
            // no events to disable
            if (eventNames.isEmpty()) {
                return;
            }

            StringBuffer eventNameParameter = new StringBuffer();
            for (Iterator<String> iterator = eventNames.iterator(); iterator.hasNext();) {
                String event = iterator.next();
                eventNameParameter.append(event);
                if (iterator.hasNext()) {
                    eventNameParameter.append(',');
                }
            }
            command.add(eventNameParameter.toString());
        }

        if (isKernel) {
            command.add(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.add(LTTngControlServiceConstants.OPTION_UST);
        }

        command.add(LTTngControlServiceConstants.OPTION_SESSION);
        command.add(sessionName);

        if (channelName != null) {
            command.add(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.add(channelName);
        }

        executeCommand(command, monitor);
    }

    @Override
    public List<String> getContextList(IProgressMonitor monitor) throws ExecutionException {

        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_ADD_CONTEXT, LTTngControlServiceConstants.OPTION_HELP);

        ICommandResult result = executeCommand(command, monitor);

        List<String> output = result.getOutput();

        List<String> contexts = new ArrayList<>(0);

        int index = 0;
        boolean inList = false;
        while (index < output.size()) {
            String line = output.get(index);

            Matcher startMatcher = LTTngControlServiceConstants.ADD_CONTEXT_HELP_CONTEXTS_INTRO.matcher(line);
            Matcher endMatcher = LTTngControlServiceConstants.ADD_CONTEXT_HELP_CONTEXTS_END_LINE.matcher(line);

            if (startMatcher.matches()) {
                inList = true;
            } else if (endMatcher.matches()) {
                break;
            } else if (inList) {
                String[] tmp = line.split(","); //$NON-NLS-1$
                for (int i = 0; i < tmp.length; i++) {
                    contexts.add(tmp[i].trim());
                }
            }
            index++;
        }
        return contexts;
    }

    @Override
    public void addContexts(String sessionName, String channelName, String eventName, boolean isKernel, List<String> contextNames, IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_ADD_CONTEXT);

        command.add(LTTngControlServiceConstants.OPTION_SESSION);
        command.add(sessionName);

        if (channelName != null) {
            command.add(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.add(channelName);
        }

        if (eventName != null) {
            command.add(LTTngControlServiceConstants.OPTION_EVENT);
            command.add(eventName);
        }

        if (isKernel) {
            command.add(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.add(LTTngControlServiceConstants.OPTION_UST);
        }

        for (Iterator<String> iterator = contextNames.iterator(); iterator.hasNext();) {
            String context = iterator.next();
            command.add(LTTngControlServiceConstants.OPTION_CONTEXT_TYPE);
            command.add(context);
        }

        executeCommand(command, monitor);

    }

    @Override
    public void recordSnapshot(String sessionName, IProgressMonitor monitor)
            throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_SNAPSHOT, LTTngControlServiceConstants.COMMAND_RECORD_SNAPSHOT);

        String newSessionName = sessionName;
        command.add(LTTngControlServiceConstants.OPTION_SESSION);
        command.add(newSessionName);

        executeCommand(command, monitor);
    }

    @Override
    public void loadSession(String inputPath, boolean isForce, IProgressMonitor monitor)
            throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_LOAD_SESSION);

        if (inputPath != null) {
            command.add(LTTngControlServiceConstants.OPTION_INPUT_PATH);
            command.add(inputPath);
        }

        if (isForce) {
            command.add(LTTngControlServiceConstants.OPTION_FORCE);
        }
        executeCommand(command, monitor);
    }

    @Override
    public void saveSession(String session, String outputPath, boolean isForce, IProgressMonitor monitor) throws ExecutionException {
        ICommandInput command = createCommand(LTTngControlServiceConstants.COMMAND_SAVE_SESSION);

        if (outputPath != null) {
            command.add(LTTngControlServiceConstants.OPTION_OUTPUT_PATH);
            command.add(outputPath);
        }

        if (isForce) {
            command.add(LTTngControlServiceConstants.OPTION_FORCE);
        }

        if (session != null) {
            command.add(session);
        }
        executeCommand(command, monitor);
    }

    @Override
    public void runCommands(IProgressMonitor monitor, List<String> commandLines) throws ExecutionException {
        for (String commandLine : commandLines) {
            if (monitor.isCanceled()) {
                return;
            }

            if (commandLine.isEmpty() || commandLine.startsWith("#")) { //$NON-NLS-1$
                continue;
            }
            String[] args = commandLine.split("\\s+"); //$NON-NLS-1$
            ICommandInput command = fCommandShell.createCommand();
            command.addAll(Arrays.asList(args));
            ICommandResult result = executeCommand(command, monitor);

            if (isError(result)) {
                throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + result.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Checks if command result is an error result.
     *
     * @param result
     *            - the command result to check
     * @return true if error else false
     */
    protected boolean isError(ICommandResult result) {
        // Check return code and length of returned strings

        if ((result.getResult()) != 0) {
            return true;
        }

        // Look for error pattern
        for (String line : result.getErrorOutput()) {
            Matcher matcher = LTTngControlServiceConstants.ERROR_PATTERN.matcher(line);
            if (matcher.matches()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates a comma separated string from list of names
     * @param names
     *          List of name to convert
     * @return comma separated string
     */
    protected String toCsv(List<String> names) {
        StringBuilder csvString = new StringBuilder();
        for (Iterator<String> iterator = names.iterator(); iterator.hasNext();) {
            String name = iterator.next();
            csvString.append(name);
            if (iterator.hasNext()) {
                csvString.append(',');
            }
        }
        return csvString.toString();
    }

    /**
     * Parses the domain information.
     *
     * @param output
     *            a command output list
     * @param currentIndex
     *            current index in command output list
     * @param channels
     *            list for returning channel information
     * @param domainInfo
     *            The domain information
     * @return the new current index in command output list
     */
    protected int parseDomain(List<String> output, int currentIndex, List<IChannelInfo> channels, IDomainInfo domainInfo) {
        int index = currentIndex;

        // if kernel set the buffer type to shared
        if (domainInfo.isKernel()) {
            domainInfo.setBufferType(BufferType.BUFFER_SHARED);
        }

        // Channels:
        // -------------
        // - channnel1: [enabled]
        //
        // Attributes:
        // overwrite mode: 0
        // subbufers size: 262144
        // number of subbufers: 4
        // switch timer interval: 0
        // read timer interval: 200
        // output: splice()

        while (index < output.size()) {
            String line = output.get(index);

            if (isVersionSupported("2.2.0")) { //$NON-NLS-1$
                Matcher bufferTypeMatcher = LTTngControlServiceConstants.BUFFER_TYPE_PATTERN.matcher(line);
                if (bufferTypeMatcher.matches()) {
                    String bufferTypeString = getAttributeValue(line);
                    if (BufferType.BUFFER_PER_PID.getInName().equals(bufferTypeString)) {
                        domainInfo.setBufferType(BufferType.BUFFER_PER_PID);
                    } else if (BufferType.BUFFER_PER_UID.getInName().equals(bufferTypeString)) {
                        domainInfo.setBufferType(BufferType.BUFFER_PER_UID);
                    } else {
                        domainInfo.setBufferType(BufferType.BUFFER_TYPE_UNKNOWN);
                    }
                }
            } else {
                domainInfo.setBufferType(BufferType.BUFFER_TYPE_UNKNOWN);
            }
            Matcher outerMatcher = LTTngControlServiceConstants.CHANNELS_SECTION_PATTERN.matcher(line);
            Matcher noKernelChannelMatcher = LTTngControlServiceConstants.DOMAIN_NO_KERNEL_CHANNEL_PATTERN.matcher(line);
            Matcher noUstChannelMatcher = LTTngControlServiceConstants.DOMAIN_NO_UST_CHANNEL_PATTERN.matcher(line);
            if (outerMatcher.matches()) {
                IChannelInfo channelInfo = null;
                while (index < output.size()) {
                    String subLine = output.get(index);

                    Matcher innerMatcher = LTTngControlServiceConstants.CHANNEL_PATTERN.matcher(subLine);
                    if (innerMatcher.matches()) {
                        channelInfo = new ChannelInfo(""); //$NON-NLS-1$
                        // get channel name
                        channelInfo.setName(innerMatcher.group(1));

                        // get channel enablement
                        channelInfo.setState(innerMatcher.group(2));

                        // set BufferType
                        channelInfo.setBufferType(domainInfo.getBufferType());

                        // add channel
                        channels.add(channelInfo);

                    } else if (LTTngControlServiceConstants.OVERWRITE_MODE_ATTRIBUTE.matcher(subLine).matches()) {
                        String value = getAttributeValue(subLine);
                        if (channelInfo != null) {
                            channelInfo.setOverwriteMode(!LTTngControlServiceConstants.OVERWRITE_MODE_ATTRIBUTE_FALSE.equals(value));
                        }
                    } else if (LTTngControlServiceConstants.SUBBUFFER_SIZE_ATTRIBUTE.matcher(subLine).matches()) {
                        if (channelInfo != null) {
                            channelInfo.setSubBufferSize(Long.valueOf(getAttributeValue(subLine)));
                        }

                    } else if (LTTngControlServiceConstants.NUM_SUBBUFFERS_ATTRIBUTE.matcher(subLine).matches()) {
                        if (channelInfo != null) {
                            channelInfo.setNumberOfSubBuffers(Integer.valueOf(getAttributeValue(subLine)));
                        }

                    } else if (LTTngControlServiceConstants.SWITCH_TIMER_ATTRIBUTE.matcher(subLine).matches()) {
                        if (channelInfo != null) {
                            channelInfo.setSwitchTimer(Long.valueOf(getAttributeValue(subLine)));
                        }

                    } else if (LTTngControlServiceConstants.READ_TIMER_ATTRIBUTE.matcher(subLine).matches()) {
                        if (channelInfo != null) {
                            channelInfo.setReadTimer(Long.valueOf(getAttributeValue(subLine)));
                        }

                    } else if (LTTngControlServiceConstants.OUTPUT_ATTRIBUTE.matcher(subLine).matches()) {
                        if (channelInfo != null) {
                            channelInfo.setOutputType(getAttributeValue(subLine));
                        }

                    } else if (LTTngControlServiceConstants.TRACE_FILE_COUNT_ATTRIBUTE.matcher(subLine).matches()) {
                        if (channelInfo != null) {
                            channelInfo.setMaxNumberTraceFiles(Integer.valueOf(getAttributeValue(subLine)));
                        }

                    } else if (LTTngControlServiceConstants.TRACE_FILE_SIZE_ATTRIBUTE.matcher(subLine).matches()) {
                        if (channelInfo != null) {
                            channelInfo.setMaxSizeTraceFiles(Long.valueOf(getAttributeValue(subLine)));
                        }
                    } else if (LTTngControlServiceConstants.EVENT_SECTION_PATTERN.matcher(subLine).matches()) {
                        List<IEventInfo> events = new ArrayList<>();
                        index = parseEvents(output, index, events);
                        if (channelInfo != null) {
                            channelInfo.setEvents(events);
                        }
                        // we want to stay at the current index to be able to
                        // exit the domain
                        continue;
                    } else if (LTTngControlServiceConstants.DOMAIN_KERNEL_PATTERN.matcher(subLine).matches()) {
                        return index;

                    } else if (LTTngControlServiceConstants.DOMAIN_UST_GLOBAL_PATTERN.matcher(subLine).matches()) {
                        return index;
                    }
                    index++;
                }
            } else if (noKernelChannelMatcher.matches() || noUstChannelMatcher.matches()) {
                // domain indicates that no channels were found -> return
                index++;
                return index;
            }
            index++;
        }
        return index;
    }

    /**
     * Parses the event information within a domain.
     *
     * @param output
     *            a command output list
     * @param currentIndex
     *            current index in command output list
     * @param events
     *            list for returning event information
     * @return the new current index in command output list
     */
    protected int parseEvents(List<String> output, int currentIndex, List<IEventInfo> events) {
        int index = currentIndex;

        while (index < output.size()) {
            String line = output.get(index);
            if (LTTngControlServiceConstants.CHANNEL_PATTERN.matcher(line).matches()) {
                // end of channel
                return index;
            } else if (LTTngControlServiceConstants.DOMAIN_KERNEL_PATTERN.matcher(line).matches()) {
                // end of domain
                return index;
            } else if (LTTngControlServiceConstants.DOMAIN_UST_GLOBAL_PATTERN.matcher(line).matches()) {
                // end of domain
                return index;
            }

            Matcher matcher = LTTngControlServiceConstants.EVENT_PATTERN.matcher(line);
            Matcher matcher2 = LTTngControlServiceConstants.WILDCARD_EVENT_PATTERN.matcher(line);

            if (matcher.matches()) {
                IEventInfo eventInfo = new EventInfo(matcher.group(1).trim());
                eventInfo.setLogLevelType(matcher.group(2).trim());
                eventInfo.setLogLevel(matcher.group(3).trim());
                eventInfo.setEventType(matcher.group(4).trim());
                eventInfo.setState(matcher.group(5));
                String filter = matcher.group(6);
                if (filter != null) {
                    // remove '[' and ']'
                    filter = filter.substring(1, filter.length() - 1);
                    eventInfo.setFilterExpression(filter);
                }
                events.add(eventInfo);
                index++;
            } else if (matcher2.matches()) {
                IEventInfo eventInfo = new EventInfo(matcher2.group(1).trim());
                eventInfo.setLogLevel(TraceLogLevel.LEVEL_UNKNOWN);
                eventInfo.setEventType(matcher2.group(2).trim());
                eventInfo.setState(matcher2.group(3));
                String filter = matcher2.group(4);
                if (filter != null) {
                    // remove '[' and ']'
                    filter = filter.substring(1, filter.length() - 1);
                    eventInfo.setFilterExpression(filter);
                }

                if ((eventInfo.getEventType() == TraceEventType.PROBE) ||
                        (eventInfo.getEventType() == TraceEventType.FUNCTION)) {
                    IProbeEventInfo probeEvent = new ProbeEventInfo(eventInfo.getName());
                    probeEvent.setLogLevel(eventInfo.getLogLevel());
                    probeEvent.setEventType(eventInfo.getEventType());
                    probeEvent.setState(eventInfo.getState());

                    // Overwrite eventinfo
                    eventInfo = probeEvent;

                    // myevent2 (type: probe) [enabled]
                    // addr: 0xc0101340
                    // myevent0 (type: function) [enabled]
                    // offset: 0x0
                    // symbol: init_post
                    index++;
                    while (index < output.size()) {
                        String probeLine = output.get(index);
                        // parse probe
                        Matcher addrMatcher = LTTngControlServiceConstants.PROBE_ADDRESS_PATTERN.matcher(probeLine);
                        Matcher offsetMatcher = LTTngControlServiceConstants.PROBE_OFFSET_PATTERN.matcher(probeLine);
                        Matcher symbolMatcher = LTTngControlServiceConstants.PROBE_SYMBOL_PATTERN.matcher(probeLine);
                        if (addrMatcher.matches()) {
                            String addr = addrMatcher.group(2).trim();
                            probeEvent.setAddress(addr);
                        } else if (offsetMatcher.matches()) {
                            String offset = offsetMatcher.group(2).trim();
                            probeEvent.setOffset(offset);
                        } else if (symbolMatcher.matches()) {
                            String symbol = symbolMatcher.group(2).trim();
                            probeEvent.setSymbol(symbol);
                        } else if ((LTTngControlServiceConstants.EVENT_PATTERN.matcher(probeLine).matches()) || (LTTngControlServiceConstants.WILDCARD_EVENT_PATTERN.matcher(probeLine).matches())) {
                            break;
                        } else if (LTTngControlServiceConstants.CHANNEL_PATTERN.matcher(probeLine).matches()) {
                            break;
                        } else if (LTTngControlServiceConstants.DOMAIN_KERNEL_PATTERN.matcher(probeLine).matches()) {
                            // end of domain
                            break;
                        } else if (LTTngControlServiceConstants.DOMAIN_UST_GLOBAL_PATTERN.matcher(probeLine).matches()) {
                            // end of domain
                            break;
                        }
                        index++;
                    }
                    events.add(eventInfo);
                } else {
                    events.add(eventInfo);
                    index++;
                    continue;
                }
            } else {
                index++;
            }
        }

        return index;
    }

    /**
     * Parses a line with attributes: <attribute Name>: <attribute value>
     *
     * @param line
     *            - attribute line to parse
     * @return the attribute value as string
     */
    protected String getAttributeValue(String line) {
        String[] temp = line.split("\\: "); //$NON-NLS-1$
        return temp[1];
    }

    /**
     * Parses the event information within a provider.
     *
     * @param output
     *            a command output list
     * @param currentIndex
     *            current index in command output list
     * @param events
     *            list for returning event information
     * @return the new current index in command output list
     */
    protected int getProviderEventInfo(List<String> output, int currentIndex, List<IBaseEventInfo> events) {
        int index = currentIndex;
        IBaseEventInfo eventInfo = null;
        while (index < output.size()) {
            String line = output.get(index);
            Matcher matcher = LTTngControlServiceConstants.PROVIDER_EVENT_PATTERN.matcher(line);
            if (matcher.matches()) {
                // sched_kthread_stop (loglevel: TRACE_EMERG0) (type:
                // tracepoint)
                eventInfo = new BaseEventInfo(matcher.group(1).trim());
                eventInfo.setLogLevel(matcher.group(2).trim());
                eventInfo.setEventType(matcher.group(3).trim());
                events.add(eventInfo);
                index++;
            } else if (LTTngControlServiceConstants.EVENT_FIELD_PATTERN.matcher(line).matches()) {
                if (eventInfo != null) {
                    List<IFieldInfo> fields = new ArrayList<>();
                    index = getFieldInfo(output, index, fields);
                    eventInfo.setFields(fields);
                } else {
                    index++;
                }
            }
            else if (LTTngControlServiceConstants.UST_PROVIDER_PATTERN.matcher(line).matches()) {
                return index;
            } else {
                index++;
            }
        }
        return index;
    }

    /**
     * Parse a field's information.
     *
     * @param output
     *            A command output list
     * @param currentIndex
     *            The current index in the command output list
     * @param fields
     *            List for returning the field information
     * @return The new current index in the command output list
     */
    protected int getFieldInfo(List<String> output, int currentIndex, List<IFieldInfo> fields) {
        int index = currentIndex;
        IFieldInfo fieldInfo = null;
        while (index < output.size()) {
            String line = output.get(index);
            Matcher matcher = LTTngControlServiceConstants.EVENT_FIELD_PATTERN.matcher(line);
            if (matcher.matches()) {
                // field: content (string)
                fieldInfo = new FieldInfo(matcher.group(2).trim());
                fieldInfo.setFieldType(matcher.group(3).trim());
                fields.add(fieldInfo);
            } else if (LTTngControlServiceConstants.PROVIDER_EVENT_PATTERN.matcher(line).matches()) {
                return index;
            } else if (LTTngControlServiceConstants.UST_PROVIDER_PATTERN.matcher(line).matches()) {
                return index;
            }
            index++;
        }
        return index;
    }

    /**
     * Creates a command input instance
     *
     * @param segments
     *            array of string that makes up a command line
     * @return {@link ICommandInput} instance
     */
    protected @NonNull ICommandInput createCommand(String... segments) {
        ICommandInput command = fCommandShell.createCommand();
        command.add(LTTngControlServiceConstants.CONTROL_COMMAND);
        List<@NonNull String> groupOption = getTracingGroupOption();
        if (!groupOption.isEmpty()) {
            command.addAll(groupOption);
        }
        String verboseOption = getVerboseOption();
        if (!verboseOption.isEmpty()) {
            command.add(verboseOption);
        }
        for (String string : segments) {
            command.add(checkNotNull(string));
        }
        return command;
    }

    /**
     * @return the tracing group option if configured in the preferences
     */
    protected @NonNull List<@NonNull String> getTracingGroupOption() {
        List<@NonNull String> groupOption = new ArrayList<>();
        if (!ControlPreferences.getInstance().isDefaultTracingGroup() && !ControlPreferences.getInstance().getTracingGroup().equals("")) { //$NON-NLS-1$
            groupOption.add(LTTngControlServiceConstants.OPTION_TRACING_GROUP);
            groupOption.add(ControlPreferences.getInstance().getTracingGroup());
        }
        return groupOption;
    }

    /**
     * @return the verbose option as configured in the preferences
     */
    protected String getVerboseOption() {
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            String level = ControlPreferences.getInstance().getVerboseLevel();
            if (ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_VERBOSE.equals(level)) {
                return LTTngControlServiceConstants.OPTION_VERBOSE;
            }
            if (ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_V_VERBOSE.equals(level)) {
                return LTTngControlServiceConstants.OPTION_VERY_VERBOSE;
            }
            if (ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_V_V_VERBOSE.equals(level)) {
                return LTTngControlServiceConstants.OPTION_VERY_VERY_VERBOSE;
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Method that logs the command and command result if logging is enabled as
     * well as forwards the command execution to the shell.
     *
     * @param command
     *            - the command to execute
     * @param monitor
     *            - a progress monitor
     * @return the command result
     * @throws ExecutionException
     *             If the command fails
     */
    protected ICommandResult executeCommand(@NonNull ICommandInput command,
            @Nullable IProgressMonitor monitor) throws ExecutionException {
        return executeCommand(command, monitor, true);
    }

    /**
     * Method that logs the command and command result if logging is enabled as
     * well as forwards the command execution to the shell.
     *
     * @param command
     *            - the command to execute
     * @param monitor
     *            - a progress monitor
     * @param checkForError
     *            - true to verify command result, else false
     * @return the command result
     * @throws ExecutionException
     *             in case of error result
     */
    protected ICommandResult executeCommand(@NonNull ICommandInput command,
            @Nullable IProgressMonitor monitor, boolean checkForError)
            throws ExecutionException {
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.log(command.toString());
        }

        ICommandResult result = fCommandShell.executeCommand(command, monitor);

        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.log(result.toString());
        }

        if (checkForError && isError(result)) {
            throw new ExecutionException(Messages.TraceControl_CommandError
                    + " " + command.toString() + "\n" + result.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }


}
