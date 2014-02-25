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
 *   Simon Delisle - Updated for support of LTTng Tools 2.2
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IDomainInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IFieldInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IProbeEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.ISnapshotInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IUstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.BaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.BufferType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.DomainInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.EventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.FieldInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.ProbeEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.SessionInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.SnapshotInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.UstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.logging.ControlCommandLogger;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.preferences.ControlPreferences;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.remote.ICommandResult;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.remote.ICommandShell;

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
    private final ICommandShell fCommandShell;

    /**
     * The version string.
     */
    private LttngVersion fVersion = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param shell
     *            - the command shell implementation to use
     */
    public LTTngControlService(ICommandShell shell) {
        fCommandShell = shell;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public String getVersion() {
        if (fVersion == null) {
            return "Unknown"; //$NON-NLS-1$
        }
        return fVersion.toString();
    }

    /**
     * Sets the version of the LTTng 2.0 control service.
     * @param version - a version to set
     */
    public void setVersion(String version) {
        fVersion = new LttngVersion(version);
    }

    @Override
    public boolean isVersionSupported(String version) {
        LttngVersion tmp = new LttngVersion(version);
        return (fVersion != null && fVersion.compareTo(tmp) >= 0) ? true : false;
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
    public String[] getSessionNames(IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_LIST);

        ICommandResult result = executeCommand(command.toString(), monitor);

        // Output:
        // Available tracing sessions:
        // 1) mysession1 (/home/user/lttng-traces/mysession1-20120123-083928) [inactive]
        // 2) mysession (/home/user/lttng-traces/mysession-20120123-083318) [inactive]
        //
        // Use lttng list <session_name> for more details

        ArrayList<String> retArray = new ArrayList<>();
        int index = 0;
        while (index < result.getOutput().length) {
            String line = result.getOutput()[index];
            Matcher matcher = LTTngControlServiceConstants.SESSION_PATTERN.matcher(line);
            if (matcher.matches()) {
                retArray.add(matcher.group(2).trim());
            }
            index++;
        }
        return retArray.toArray(new String[retArray.size()]);
    }

    @Override
    public ISessionInfo getSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_LIST, sessionName);
        ICommandResult result = executeCommand(command.toString(), monitor);

        int index = 0;

        // Output:
        // Tracing session mysession2: [inactive]
        // Trace path: /home/eedbhu/lttng-traces/mysession2-20120123-110330
        ISessionInfo sessionInfo = new SessionInfo(sessionName);

        while (index < result.getOutput().length) {
            // Tracing session mysession2: [inactive]
            // Trace path: /home/eedbhu/lttng-traces/mysession2-20120123-110330
            //
            // === Domain: Kernel ===
            //
            String line = result.getOutput()[index];
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
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_LIST_SNAPSHOT_OUTPUT, LTTngControlServiceConstants.OPTION_SESSION, sessionName);
        ICommandResult result = executeCommand(command.toString(), monitor);

        int index = 0;

        // Output:
        // [1] snapshot-1: /home/user/lttng-traces/my-20130909-114431
        // or
        // [3] snapshot-3: net4://172.0.0.1/
        ISnapshotInfo snapshotInfo = new SnapshotInfo(""); //$NON-NLS-1$

        while (index < result.getOutput().length) {
            String line = result.getOutput()[index];
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
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_LIST_KERNEL);
        ICommandResult result = executeCommand(command.toString(), monitor, false);

        List<IBaseEventInfo> events = new ArrayList<>();

        if (result.getOutput() != null) {
            // Ignore the following 2 cases:
            // Spawning a session daemon
            // Error: Unable to list kernel events
            // or:
            // Error: Unable to list kernel events
            //
            int index = 0;
            while (index < result.getOutput().length) {
                String line = result.getOutput()[index];
                Matcher matcher = LTTngControlServiceConstants.LIST_KERNEL_NO_KERNEL_PROVIDER_PATTERN.matcher(line);
                if (matcher.matches()) {
                    return events;
                }
                index++;
            }
        }

        if (isError(result)) {
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + formatOutput(result)); //$NON-NLS-1$ //$NON-NLS-2$
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
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_LIST_UST);

        if (isVersionSupported("2.1.0")) { //$NON-NLS-1$
            command.append(LTTngControlServiceConstants.OPTION_FIELDS);
        }

        ICommandResult result = executeCommand(command.toString(), monitor, false);
        List<IUstProviderInfo> allProviders = new ArrayList<>();

        // Workaround for versions 2.0.x which causes a segmentation fault for this command
        // if LTTng Tools is compiled without UST support.
        if (!isVersionSupported("2.1.0") && (result.getResult() != 0)) { //$NON-NLS-1$
            return allProviders;
        }

        if (result.getOutput() != null) {
            // Ignore the following 2 cases:
            // Spawning a session daemon
            // Error: Unable to list UST events: Listing UST events failed
            // or:
            // Error: Unable to list UST events: Listing UST events failed
            //
            int index = 0;
            while (index < result.getOutput().length) {
                String line = result.getOutput()[index];
                Matcher matcher = LTTngControlServiceConstants.LIST_UST_NO_UST_PROVIDER_PATTERN.matcher(line);
                if (matcher.matches()) {
                    return allProviders;
                }
                index++;
            }
        }

        if (isError(result)) {
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + formatOutput(result)); //$NON-NLS-1$ //$NON-NLS-2$
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
        //    field: doublefield (float)
        //    field: floatfield (float)
        //    field: stringfield (string)
        //
        // PID: 6459 - Name:
        // /home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello
        // ust_tests_hello:tptest_sighandler (loglevel: TRACE_EMERG0) (type:
        // tracepoint)
        // ust_tests_hello:tptest (loglevel: TRACE_EMERG0) (type: tracepoint)
        //    field: doublefield (float)
        //    field: floatfield (float)
        //    field: stringfield (string)

        IUstProviderInfo provider = null;

        int index = 0;
        while (index < result.getOutput().length) {
            String line = result.getOutput()[index];
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
    public ISessionInfo createSession(String sessionName, String sessionPath, boolean isSnapshot, IProgressMonitor monitor) throws ExecutionException {

        String newName = formatParameter(sessionName);
        String newPath = formatParameter(sessionPath);

        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_CREATE_SESSION, newName);

        if (newPath != null && !"".equals(newPath)) { //$NON-NLS-1$
            command.append(LTTngControlServiceConstants.OPTION_OUTPUT_PATH);
            command.append(newPath);
        }

        if (isSnapshot) {
            command.append(LTTngControlServiceConstants.OPTION_SNAPSHOT);
        }

        ICommandResult result = executeCommand(command.toString(), monitor);

        //Session myssession2 created.
        //Traces will be written in /home/user/lttng-traces/myssession2-20120209-095418
        String[] output = result.getOutput();

        // Get and session name and path
        String name = null;
        String path = null;

        int index = 0;
        while (index < output.length) {
            String line = output[index];
            Matcher nameMatcher = LTTngControlServiceConstants.CREATE_SESSION_NAME_PATTERN.matcher(line);
            Matcher pathMatcher = LTTngControlServiceConstants.CREATE_SESSION_PATH_PATTERN.matcher(line);
            if (nameMatcher.matches()) {
                name = String.valueOf(nameMatcher.group(1).trim());
            } else if (pathMatcher.matches()) {
                path = String.valueOf(pathMatcher.group(1).trim());
            }
            index++;
        }

        // Verify session name
        if ((name == null) || (!"".equals(sessionName) && !name.equals(sessionName))) { //$NON-NLS-1$
            // Unexpected name returned
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.TraceControl_UnexpectedNameError + ": " + name); //$NON-NLS-1$
        }

        SessionInfo sessionInfo = new SessionInfo(name);

        // Verify session path
        if (!isSnapshot &&
                ((path == null) || ((sessionPath != null) && (!path.contains(sessionPath))))) {
            // Unexpected path
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.TraceControl_UnexpectedPathError + ": " + name); //$NON-NLS-1$
        }

        if (isSnapshot) {
            // Make it a snapshot session - content of snapshot info need to
            // set afterwards using getSession() or getSnapshotInfo()
            sessionInfo.setSnapshotInfo(new SnapshotInfo("")); //$NON-NLS-1$
        } else {
            sessionInfo.setSessionPath(path);
        }

        return sessionInfo;

    }

    @Override
    public ISessionInfo createSession(String sessionName, String networkUrl, String controlUrl, String dataUrl, boolean isSnapshot, IProgressMonitor monitor) throws ExecutionException {

        String newName = formatParameter(sessionName);
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_CREATE_SESSION, newName);

        if (isSnapshot) {
            command.append(LTTngControlServiceConstants.OPTION_SNAPSHOT);
        }

        if (networkUrl != null) {
            command.append(LTTngControlServiceConstants.OPTION_NETWORK_URL);
            command.append(networkUrl);
        } else {
            command.append(LTTngControlServiceConstants.OPTION_CONTROL_URL);
            command.append(controlUrl);

            command.append(LTTngControlServiceConstants.OPTION_DATA_URL);
            command.append(dataUrl);
        }

        ICommandResult result = executeCommand(command.toString(), monitor);

        // Verify output
        String[] output = result.getOutput();

        // Get and session name and path
        String name = null;
        String path = null;

        int index = 0;
        while (index < output.length) {
            String line = output[index];
            Matcher nameMatcher = LTTngControlServiceConstants.CREATE_SESSION_NAME_PATTERN.matcher(line);
            Matcher pathMatcher = LTTngControlServiceConstants.CREATE_SESSION_PATH_PATTERN.matcher(line);

            if (nameMatcher.matches()) {
                name = String.valueOf(nameMatcher.group(1).trim());
            } else if (pathMatcher.matches() && (networkUrl != null)) {
                path = String.valueOf(pathMatcher.group(1).trim());
            }
            index++;
        }

        // Verify session name
        if ((name == null) || (!"".equals(sessionName) && !name.equals(sessionName))) { //$NON-NLS-1$
            // Unexpected name returned
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.TraceControl_UnexpectedNameError + ": " + name); //$NON-NLS-1$
        }

        SessionInfo sessionInfo = new SessionInfo(name);

        sessionInfo.setStreamedTrace(true);

        // Verify session path
        if (networkUrl != null) {
            if (!isSnapshot && (path == null)) {
                // Unexpected path
                throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                        Messages.TraceControl_UnexpectedPathError + ": " + name); //$NON-NLS-1$
            }

            if (isSnapshot) {
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

        // When using controlUrl and dataUrl the full session path is not known yet
        // and will be set later on when listing the session

        return sessionInfo;
    }

    @Override
    public void destroySession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        String newName = formatParameter(sessionName);

        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_DESTROY_SESSION, newName);

        ICommandResult result = executeCommand(command.toString(), monitor, false);
        String[] output = result.getOutput();

        boolean isError = isError(result);
        if (isError && (output != null)) {
            int index = 0;
            while (index < output.length) {
                String line = output[index];
                Matcher matcher = LTTngControlServiceConstants.SESSION_NOT_FOUND_ERROR_PATTERN.matcher(line);
                if (matcher.matches()) {
                    // Don't treat this as an error
                    isError = false;
                }
                index++;
            }
        }

        if (isError) {
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + formatOutput(result)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        //Session <sessionName> destroyed
    }

    @Override
    public void startSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {

        String newSessionName = formatParameter(sessionName);

        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_START_SESSION, newSessionName);

        executeCommand(command.toString(), monitor);

        //Session <sessionName> started
    }

    @Override
    public void stopSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        String newSessionName = formatParameter(sessionName);
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_STOP_SESSION, newSessionName);

        executeCommand(command.toString(), monitor);

        //Session <sessionName> stopped

    }

    @Override
    public void enableChannels(String sessionName, List<String> channelNames, boolean isKernel, IChannelInfo info, IProgressMonitor monitor) throws ExecutionException {

        // no channels to enable
        if (channelNames.isEmpty()) {
            return;
        }

        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_ENABLE_CHANNEL);

        for (Iterator<String> iterator = channelNames.iterator(); iterator.hasNext();) {
            String channel = iterator.next();
            command.append(channel);
            if (iterator.hasNext()) {
                command.append(',');
            }
        }

        if (isKernel) {
            command.append(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.append(LTTngControlServiceConstants.OPTION_UST);
        }

        String newSessionName = formatParameter(sessionName);
        command.append(LTTngControlServiceConstants.OPTION_SESSION);
        command.append(newSessionName);

        if (info != null) {
//            --discard            Discard event when buffers are full (default)

//            --overwrite          Flight recorder mode
            if (info.isOverwriteMode()) {
                command.append(LTTngControlServiceConstants.OPTION_OVERWRITE);
            }
//            --subbuf-size SIZE   Subbuffer size in bytes
//                                     (default: 4096, kernel default: 262144)
            if (info.getSubBufferSize() != LTTngControlServiceConstants.UNUSED_VALUE) {
                command.append(LTTngControlServiceConstants.OPTION_SUB_BUFFER_SIZE);
                command.append(String.valueOf(info.getSubBufferSize()));
            }

//            --num-subbuf NUM     Number of subbufers
            if (info.getNumberOfSubBuffers() != LTTngControlServiceConstants.UNUSED_VALUE) {
                command.append(LTTngControlServiceConstants.OPTION_NUM_SUB_BUFFERS);
                command.append(String.valueOf(info.getNumberOfSubBuffers()));
            }

//            --switch-timer USEC  Switch timer interval in usec
            if (info.getSwitchTimer() != LTTngControlServiceConstants.UNUSED_VALUE) {
                command.append(LTTngControlServiceConstants.OPTION_SWITCH_TIMER);
                command.append(String.valueOf(info.getSwitchTimer()));
            }

//            --read-timer USEC    Read timer interval in usec
            if (info.getReadTimer() != LTTngControlServiceConstants.UNUSED_VALUE) {
                command.append(LTTngControlServiceConstants.OPTION_READ_TIMER);
                command.append(String.valueOf(info.getReadTimer()));
            }

            if (isVersionSupported("2.2.0")) { //$NON-NLS-1$
//                --buffers-uid  Every application sharing the same UID use the same buffers
//                --buffers-pid Buffers are allocated per PID
                if (!isKernel) {
                    if (info.getBufferType() == BufferType.BUFFER_PER_PID) {
                        command.append(LTTngControlServiceConstants.OPTION_PER_PID_BUFFERS);

                    } else if (info.getBufferType() == BufferType.BUFFER_PER_UID) {
                        command.append(LTTngControlServiceConstants.OPTION_PER_UID_BUFFERS);
                    }
                }

//                -C SIZE   Maximum size of trace files in bytes
                if (info.getMaxSizeTraceFiles() != LTTngControlServiceConstants.UNUSED_VALUE) {
                    command.append(LTTngControlServiceConstants.OPTION_MAX_SIZE_TRACE_FILES);
                    command.append(String.valueOf(info.getMaxSizeTraceFiles()));
                }

//                -W NUM   Maximum number of trace files
                if (info.getMaxNumberTraceFiles() != LTTngControlServiceConstants.UNUSED_VALUE) {
                    command.append(LTTngControlServiceConstants.OPTION_MAX_TRACE_FILES);
                    command.append(String.valueOf(info.getMaxNumberTraceFiles()));
                }
            }
        }

        executeCommand(command.toString(), monitor);

    }

    @Override
    public void disableChannels(String sessionName, List<String> channelNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {

        // no channels to enable
        if (channelNames.isEmpty()) {
            return;
        }

        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_DISABLE_CHANNEL);

        for (Iterator<String> iterator = channelNames.iterator(); iterator.hasNext();) {
            String channel = iterator.next();
            command.append(channel);
            if (iterator.hasNext()) {
                command.append(',');
            }
        }

        if (isKernel) {
            command.append(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.append(LTTngControlServiceConstants.OPTION_UST);
        }

        String newSessionName = formatParameter(sessionName);
        command.append(LTTngControlServiceConstants.OPTION_SESSION);
        command.append(newSessionName);

        executeCommand(command.toString(), monitor);
    }

    @Override
    public void enableEvents(String sessionName, String channelName, List<String> eventNames, boolean isKernel, String filterExpression, IProgressMonitor monitor) throws ExecutionException {

        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_ENABLE_EVENT);

        if (eventNames == null || eventNames.isEmpty()) {
            command.append(LTTngControlServiceConstants.OPTION_ALL);
        } else {

            StringBuffer eventNameParameter = new StringBuffer();
            for (Iterator<String> iterator = eventNames.iterator(); iterator.hasNext();) {
                String event = iterator.next();
                eventNameParameter.append(event);
                if (iterator.hasNext()) {
                    eventNameParameter.append(',');
                }
            }
            command.append(formatParameter(eventNameParameter.toString()));
        }

        if (isKernel) {
            command.append(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.append(LTTngControlServiceConstants.OPTION_UST);
        }

        String newSessionName = formatParameter(sessionName);

        command.append(LTTngControlServiceConstants.OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.append(channelName);
        }

        command.append(LTTngControlServiceConstants.OPTION_TRACEPOINT);

        if (filterExpression != null) {
            command.append(LTTngControlServiceConstants.OPTION_FILTER);
            command.append('\'');
            command.append(filterExpression);
            command.append('\'');
        }

        executeCommand(command.toString(), monitor);

    }

    @Override
    public void enableSyscalls(String sessionName, String channelName, IProgressMonitor monitor) throws ExecutionException {

        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_ENABLE_EVENT);

        command.append(LTTngControlServiceConstants.OPTION_ALL);
        command.append(LTTngControlServiceConstants.OPTION_KERNEL);

        String newSessionName = formatParameter(sessionName);

        command.append(LTTngControlServiceConstants.OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.append(channelName);
        }

        command.append(LTTngControlServiceConstants.OPTION_SYSCALL);

        executeCommand(command.toString(), monitor);
    }

    @Override
    public void enableProbe(String sessionName, String channelName, String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_ENABLE_EVENT);

        command.append(eventName);
        command.append(LTTngControlServiceConstants.OPTION_KERNEL);

        String newSessionName = formatParameter(sessionName);
        command.append(LTTngControlServiceConstants.OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.append(channelName);
        }
        if (isFunction) {
            command.append(LTTngControlServiceConstants.OPTION_FUNCTION_PROBE);
        } else {
            command.append(LTTngControlServiceConstants.OPTION_PROBE);
        }

        command.append(probe);

        executeCommand(command.toString(), monitor);
    }

    @Override
    public void enableLogLevel(String sessionName, String channelName, String eventName, LogLevelType logLevelType, TraceLogLevel level, String filterExpression, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_ENABLE_EVENT);

        command.append(eventName);
        command.append(LTTngControlServiceConstants.OPTION_UST);

        String newSessionName = formatParameter(sessionName);
        command.append(LTTngControlServiceConstants.OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.append(channelName);
        }

        if (logLevelType == LogLevelType.LOGLEVEL) {
            command.append(LTTngControlServiceConstants.OPTION_LOGLEVEL);
        } else if (logLevelType == LogLevelType.LOGLEVEL_ONLY) {
            command.append(LTTngControlServiceConstants.OPTION_LOGLEVEL_ONLY);

        } else {
            return;
        }
        command.append(level.getInName());

        executeCommand(command.toString(), monitor);
    }

    @Override
    public void disableEvent(String sessionName, String channelName, List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_DISABLE_EVENT);

        if (eventNames == null) {
            command.append(LTTngControlServiceConstants.OPTION_ALL);
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
            command.append(formatParameter(eventNameParameter.toString()));
        }

        if (isKernel) {
            command.append(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.append(LTTngControlServiceConstants.OPTION_UST);
        }

        String newSessionName = formatParameter(sessionName);
        command.append(LTTngControlServiceConstants.OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.append(channelName);
        }

        executeCommand(command.toString(), monitor);
    }

    @Override
    public List<String> getContextList(IProgressMonitor monitor) throws ExecutionException {

        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_ADD_CONTEXT, LTTngControlServiceConstants.OPTION_HELP);

        ICommandResult result = executeCommand(command.toString(), monitor);

        String[] output = result.getOutput();

        List<String> contexts = new ArrayList<>(0);

        int index = 0;
        boolean inList = false;
        while (index < output.length) {
            String line = result.getOutput()[index];

            Matcher startMatcher = LTTngControlServiceConstants.ADD_CONTEXT_HELP_CONTEXTS_INTRO.matcher(line);
            Matcher endMatcher = LTTngControlServiceConstants.ADD_CONTEXT_HELP_CONTEXTS_END_LINE.matcher(line);

            if (startMatcher.matches()) {
                inList = true;
            } else if (endMatcher.matches()) {
                break;
            } else if (inList == true) {
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
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_ADD_CONTEXT);

        String newSessionName = formatParameter(sessionName);
        command.append(LTTngControlServiceConstants.OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(LTTngControlServiceConstants.OPTION_CHANNEL);
            command.append(channelName);
        }

        if (eventName != null) {
            command.append(LTTngControlServiceConstants.OPTION_EVENT);
            command.append(eventName);
        }

        if (isKernel) {
            command.append(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.append(LTTngControlServiceConstants.OPTION_UST);
        }

        for (Iterator<String> iterator = contextNames.iterator(); iterator.hasNext();) {
            String context = iterator.next();
            command.append(LTTngControlServiceConstants.OPTION_CONTEXT_TYPE);
            command.append(context);
        }

        executeCommand(command.toString(), monitor);

    }

    @Override
    public void calibrate(boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_CALIBRATE);

        if (isKernel) {
            command.append(LTTngControlServiceConstants.OPTION_KERNEL);
        } else {
            command.append(LTTngControlServiceConstants.OPTION_UST);
        }

        command.append(LTTngControlServiceConstants.OPTION_FUNCTION_PROBE);

        executeCommand(command.toString(), monitor);
    }

    @Override
    public void recordSnapshot(String sessionName, IProgressMonitor monitor)
            throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_RECORD_SNAPSHOT);

        String newSessionName = formatParameter(sessionName);
        command.append(LTTngControlServiceConstants.OPTION_SESSION);
        command.append(newSessionName);

        executeCommand(command.toString(), monitor);
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
        if ((result.getResult()) != 0 || (result.getOutput().length < 1)) {
            return true;
        }

        // Look for error pattern
        int index = 0;
        while (index < result.getOutput().length) {
            String line = result.getOutput()[index];
            Matcher matcher = LTTngControlServiceConstants.ERROR_PATTERN.matcher(line);
            if (matcher.matches()) {
                return true;
            }
            index++;
        }

        return false;
    }

    /**
     * Formats the output string as single string.
     *
     * @param result
     *            - output array
     * @return - the formatted output
     */
    public static String formatOutput(ICommandResult result) {
        if ((result == null) || result.getOutput() == null || result.getOutput().length == 0) {
            return ""; //$NON-NLS-1$
        }
        String[] output = result.getOutput();
        StringBuffer ret = new StringBuffer();
        ret.append("Return Value: "); //$NON-NLS-1$
        ret.append(result.getResult());
        ret.append("\n"); //$NON-NLS-1$
        for (int i = 0; i < output.length; i++) {
            ret.append(output[i]).append("\n"); //$NON-NLS-1$
        }
        return ret.toString();
    }

    /**
     * Parses the domain information.
     *
     * @param output
     *            - a command output array
     * @param currentIndex
     *            - current index in command output array
     * @param channels
     *            - list for returning channel information
     * @param domainInfo
     *            - The domain information
     * @return the new current index in command output array
     */
    protected int parseDomain(String[] output, int currentIndex, List<IChannelInfo> channels, IDomainInfo domainInfo) {
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

        while (index < output.length) {
            String line = output[index];

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
                while (index < output.length) {
                    String subLine = output[index];

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
     *            - a command output array
     * @param currentIndex
     *            - current index in command output array
     * @param events
     *            - list for returning event information
     * @return the new current index in command output array
     */
    protected int parseEvents(String[] output, int currentIndex, List<IEventInfo> events) {
        int index = currentIndex;

        while (index < output.length) {
            String line = output[index];
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
                eventInfo.setLogLevel(matcher.group(2).trim());
                eventInfo.setEventType(matcher.group(3).trim());
                eventInfo.setState(matcher.group(4));
                String filter = matcher.group(5);
                if (filter != null) {
                    filter = filter.substring(1, filter.length() - 1); // remove '[' and ']'
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
                    filter = filter.substring(1, filter.length() - 1); // remove '[' and ']'
                    eventInfo.setFilterExpression(filter);
                }

                if ((eventInfo.getEventType() == TraceEventType.PROBE) ||
                        (eventInfo.getEventType() == TraceEventType.FUNCTION)){
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
                    while (index < output.length) {
                        String probeLine = output[index];
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
     *            - a command output array
     * @param currentIndex
     *            - current index in command output array
     * @param events
     *            - list for returning event information
     * @return the new current index in command output array
     */
    protected int getProviderEventInfo(String[] output, int currentIndex, List<IBaseEventInfo> events) {
        int index = currentIndex;
        IBaseEventInfo eventInfo = null;
        while (index < output.length) {
            String line = output[index];
            Matcher matcher = LTTngControlServiceConstants.PROVIDER_EVENT_PATTERN.matcher(line);
            if (matcher.matches()) {
                // sched_kthread_stop (loglevel: TRACE_EMERG0) (type: tracepoint)
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
     *            A command output array
     * @param currentIndex
     *            The current index in the command output array
     * @param fields
     *            List for returning the field information
     * @return The new current index in the command output array
     */
    protected int getFieldInfo(String[] output, int currentIndex, List<IFieldInfo> fields) {
        int index = currentIndex;
        IFieldInfo fieldInfo = null;
        while (index < output.length) {
            String line = output[index];
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
     * Formats a command parameter for the command execution i.e. adds quotes
     * at the beginning and end if necessary.
     * @param parameter - parameter to format
     * @return formated parameter
     */
    protected String formatParameter(String parameter) {
        if (parameter != null) {
            StringBuffer newString = new StringBuffer();
            newString.append(parameter);

            if (parameter.contains(" ") || parameter.contains("*")) { //$NON-NLS-1$ //$NON-NLS-2$
                newString.insert(0, "\""); //$NON-NLS-1$
                newString.append("\""); //$NON-NLS-1$
            }
            return newString.toString();
        }
        return null;
    }

    /**
     * @param strings array of string that makes up a command line
     * @return string buffer with created command line
     */
    protected StringBuffer createCommand(String... strings) {
        StringBuffer command = new StringBuffer();
        command.append(LTTngControlServiceConstants.CONTROL_COMMAND);
        command.append(getTracingGroupOption());
        command.append(getVerboseOption());
        for (String string : strings) {
            command.append(string);
        }
        return command;
    }

    /**
     * @return the tracing group option if configured in the preferences
     */
    protected String getTracingGroupOption() {
        if (!ControlPreferences.getInstance().isDefaultTracingGroup() && !ControlPreferences.getInstance().getTracingGroup().equals("")) { //$NON-NLS-1$
            return LTTngControlServiceConstants.OPTION_TRACING_GROUP + ControlPreferences.getInstance().getTracingGroup();
        }
        return ""; //$NON-NLS-1$
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
    protected ICommandResult executeCommand(String command,
            IProgressMonitor monitor) throws ExecutionException {
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
    protected ICommandResult executeCommand(String command,
            IProgressMonitor monitor, boolean checkForError)
            throws ExecutionException {
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.log(command);
        }

        ICommandResult result = fCommandShell.executeCommand(
                command.toString(), monitor);

        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.log(formatOutput(result));
        }

        if (checkForError && isError(result)) {
            throw new ExecutionException(Messages.TraceControl_CommandError
                    + " " + command.toString() + "\n" + formatOutput(result)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }
}
