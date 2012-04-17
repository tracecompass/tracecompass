/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.logging.ControlCommandLogger;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IDomainInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IProbeEventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IUstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.DomainInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.EventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.ProbeEventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.SessionInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.UstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.preferences.ControlPreferences;

/**
 * <b><u>LTTngControlService</u></b>
 * <p>
 * Service for sending LTTng trace control commands to remote host.
 * </p>
 */
public class LTTngControlService implements ILttngControlService {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    // Command constants
    /**
     * The lttng tools command.
     */
    private final static String CONTROL_COMMAND = "lttng"; //$NON-NLS-1$
    /**
     * Command: lttng list.
     */
    private final static String COMMAND_LIST = " list "; //$NON-NLS-1$
    /**
     * Command to list kernel tracer information.
     */
    private final static String COMMAND_LIST_KERNEL = COMMAND_LIST + "-k"; //$NON-NLS-1$
    /**
     * Command to list user space trace information.
     */
    private final static String COMMAND_LIST_UST = COMMAND_LIST + "-u";  //$NON-NLS-1$
    /**
     * Command to create a session. 
     */
    private final static String COMMAND_CREATE_SESSION = " create "; //$NON-NLS-1$
    /**
     * Command to destroy a session. 
     */
    private final static String COMMAND_DESTROY_SESSION = " destroy "; //$NON-NLS-1$
    /**
     * Command to destroy a session. 
     */
    private final static String COMMAND_START_SESSION = " start "; //$NON-NLS-1$
    /**
     * Command to destroy a session. 
     */
    private final static String COMMAND_STOP_SESSION = " stop "; //$NON-NLS-1$
    /**
     * Command to enable a channel. 
     */
    private final static String COMMAND_ENABLE_CHANNEL = " enable-channel "; //$NON-NLS-1$
    /**
     * Command to disable a channel. 
     */
    private final static String COMMAND_DISABLE_CHANNEL = " disable-channel "; //$NON-NLS-1$
    /**
     * Command to enable a event. 
     */
    private final static String COMMAND_ENABLE_EVENT = " enable-event "; //$NON-NLS-1$
    /**
     * Command to disable a event. 
     */
    private final static String COMMAND_DISABLE_EVENT = " disable-event "; //$NON-NLS-1$
    /**
     * Command to add a context to channels and/or events
     */
    private final static String COMMAND_ADD_CONTEXT = " add-context "; //$NON-NLS-1$
    /**
     * Command to execute calibrate command to quantify LTTng overhead
     */
    private final static String COMMAND_CALIBRATE = " calibrate "; //$NON-NLS-1$

    // Command options constants
    /**
     * Command line option to add tracing group of user.
     */
    private final static String OPTION_TRACING_GROUP = " -g ";  //$NON-NLS-1$
    /**
     * Command line option for verbose output.
     */
    private final static String OPTION_VERBOSE = " -v ";  //$NON-NLS-1$
    /**
     * Command line option for verbose output.
     */
    private final static String OPTION_VERY_VERBOSE = " -vv ";  //$NON-NLS-1$
    /**
     * Command line option for verbose output.
     */
    private final static String OPTION_VERY_VERY_VERBOSE = " -vvv ";  //$NON-NLS-1$
    /**
     * Command line option for output path.
     */
    private final static String OPTION_OUTPUT_PATH = " -o "; //$NON-NLS-1$
    /**
     * Command line option for kernel tracer.
     */
    private final static String OPTION_KERNEL = " -k "; //$NON-NLS-1$
    /**
     * Command line option for UST tracer.
     */
    private final static String OPTION_UST = " -u "; //$NON-NLS-1$
    /**
     * Command line option for specifying a session.
     */
    private final static String OPTION_SESSION = " -s ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a channel.
     */
    private final static String OPTION_CHANNEL = " -c ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a event.
     */
    private final static String OPTION_EVENT = " -e ";  //$NON-NLS-1$
    /**
     * Command line option for specifying all events.
     */
    private final static String OPTION_ALL = " -a ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a context.
     */
    private final static String OPTION_CONTEXT_TYPE = " -t ";  //$NON-NLS-1$
    /**
     * Command line option for specifying tracepoint events.
     */
    private final static String OPTION_TRACEPOINT = " --tracepoint ";  //$NON-NLS-1$
    /**
     * Command line option for specifying syscall events.
     */
    private final static String OPTION_SYSCALL = " --syscall ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a dynamic probe.
     */
    private final static String OPTION_PROBE = " --probe ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a dynamic function entry/return probe.
     */
    private final static String OPTION_FUNCTION_PROBE = " --function ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a log level range.
     */
    private final static String OPTION_LOGLEVEL = " --loglevel ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a specific log level.
     */
    private final static String OPTION_LOGLEVEL_ONLY = " --loglevel-only ";  //$NON-NLS-1$
    /**
     * Optional command line option for configuring a channel's overwrite mode.
     */
    private final static String OPTION_OVERWRITE = " --overwrite ";  //$NON-NLS-1$ 
    /**
     * Optional command line option for configuring a channel's number of sub buffers.
     */
    private final static String OPTION_NUM_SUB_BUFFERS = " --num-subbuf ";  //$NON-NLS-1$
    /**
     * Optional command line option for configuring a channel's sub buffer size.
     */
    private final static String OPTION_SUB_BUFFER_SIZE = " --subbuf-size ";  //$NON-NLS-1$
    /**
     * Optional command line option for configuring a channel's switch timer interval.
     */
    private final static String OPTION_SWITCH_TIMER = " --switch-timer ";  //$NON-NLS-1$
    /**
     * Optional command line option for configuring a channel's read timer interval.
     */
    private final static String OPTION_READ_TIMER = " --read-timer ";  //$NON-NLS-1$
    /**
     * Command line option for printing the help of a specif command 
     */
    private final static String OPTION_HELP = " -h ";  //$NON-NLS-1$
    
    // Parsing constants
    /**
     * Pattern to match for error output
     */
    private final static Pattern ERROR_PATTERN = Pattern.compile("\\s*Error\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for session information (lttng list)
     */
    private final static Pattern SESSION_PATTERN = Pattern.compile("\\s+(\\d+)\\)\\s+(.*)\\s+\\((.*)\\)\\s+\\[(active|inactive)\\].*"); //$NON-NLS-1$
    /**
     * Pattern to match for session information (lttng list <session>)
     */
    private final static Pattern TRACE_SESSION_PATTERN = Pattern.compile("\\s*Tracing\\s+session\\s+(.*)\\:\\s+\\[(active|inactive)\\].*"); //$NON-NLS-1$
    /**
     * Pattern to match for session path information (lttng list <session>)
     */
    private final static Pattern TRACE_SESSION_PATH_PATTERN = Pattern.compile("\\s*Trace\\s+path\\:\\s+(.*)"); //$NON-NLS-1$
    /**
     * Pattern to match for kernel domain information (lttng list <session>)
     */
    private final static Pattern DOMAIN_KERNEL_PATTERN = Pattern.compile("=== Domain: Kernel ==="); //$NON-NLS-1$
    /**
     * Pattern to match for ust domain information (lttng list <session>)
     */
    private final static Pattern DOMAIN_UST_GLOBAL_PATTERN = Pattern.compile("=== Domain: UST global ==="); //$NON-NLS-1$
    /**
     * Pattern to match for channels section (lttng list <session>)
     */
    private final static Pattern CHANNELS_SECTION_PATTERN = Pattern.compile("\\s*Channels\\:"); //$NON-NLS-1$
    /**
     * Pattern to match for channel information (lttng list <session>)
     */
    private final static Pattern CHANNEL_PATTERN = Pattern.compile("\\s*-\\s+(.*)\\:\\s+\\[(enabled|disabled)\\]"); //$NON-NLS-1$
    /**
     * Pattern to match for events section information (lttng list <session>)
     */
    private final static Pattern EVENT_SECTION_PATTERN = Pattern.compile("\\s*Events\\:"); //$NON-NLS-1$
    /**
     * Pattern to match for event information (no enabled events) (lttng list <session>)
     */
    //    private final static String EVENT_NONE_PATTERN = "\\s+None"; //$NON-NLS-1$
    /**
     * Pattern to match for event information (lttng list <session>)
     */
    private final static Pattern EVENT_PATTERN = Pattern.compile("\\s+(.*)\\s+\\(loglevel:\\s+(.*)\\s+\\(\\d*\\)\\)\\s+\\(type:\\s+(.*)\\)\\s+\\[(enabled|disabled)\\].*"); //$NON-NLS-1$
    /**
     * Pattern to match a wildcarded event information (lttng list <session>)
     */
    private final static Pattern WILDCARD_EVENT_PATTERN = Pattern.compile("\\s+(.*)\\s+\\(type:\\s+(.*)\\)\\s+\\[(enabled|disabled)\\].*"); //$NON-NLS-1$
    /**
     * Pattern to match a probe address information (lttng list <session>)
     */
    private final static Pattern PROBE_ADDRESS_PATTERN = Pattern.compile("\\s+(addr)\\:\\s+(0x[0-9a-fA-F]{1,8})"); //$NON-NLS-1$
    /**
     * Pattern to match a probe OFFSET information (lttng list <session>)
     */
    private final static Pattern PROBE_OFFSET_PATTERN = Pattern.compile("\\s+(offset)\\:\\s+(0x[0-9a-fA-F]{1,8})"); //$NON-NLS-1$
    /**
     * Pattern to match a probe SYMBOL information (lttng list <session>)
     */
    private final static Pattern PROBE_SYMBOL_PATTERN = Pattern.compile("\\s+(symbol)\\:\\s+(.+)"); //$NON-NLS-1$
    /**
     * Pattern to match for channel (overwite mode) information (lttng list
     * <session>)
     */
    private final static Pattern OVERWRITE_MODE_ATTRIBUTE = Pattern.compile("\\s+overwrite\\s+mode\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match indicating false for overwrite mode
     */
    private final static String OVERWRITE_MODE_ATTRIBUTE_FALSE = "0"; //$NON-NLS-1$
    /**
     * Pattern to match for channel (sub-buffer size) information (lttng list
     * <session>)
     */
    private final static Pattern SUBBUFFER_SIZE_ATTRIBUTE = Pattern.compile("\\s+subbufers\\s+size\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for channel (number of sub-buffers) information (lttng
     * list <session>)
     */
    private final static Pattern NUM_SUBBUFFERS_ATTRIBUTE = Pattern.compile("\\s+number\\s+of\\s+subbufers\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for channel (switch timer) information (lttng list
     * <session>)
     */
    private final static Pattern SWITCH_TIMER_ATTRIBUTE = Pattern.compile("\\s+switch\\s+timer\\s+interval\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for channel (read timer) information (lttng list
     * <session>)
     */
    private final static Pattern READ_TIMER_ATTRIBUTE = Pattern.compile("\\s+read\\s+timer\\s+interval\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for channel (output type) information (lttng list
     * <session>)
     */
    private final static Pattern OUTPUT_ATTRIBUTE = Pattern.compile("\\s+output\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for provider information (lttng list -k/-u)
     */
    private final static Pattern PROVIDER_EVENT_PATTERN = Pattern.compile("\\s*(.*)\\s+\\(loglevel:\\s+(.*)\\s+\\(\\d*\\)\\)\\s+\\(type:\\s+(.*)\\)"); //$NON-NLS-1$
    /**
     * Pattern to match for UST provider information (lttng list -u)
     */
    private final static Pattern UST_PROVIDER_PATTERN = Pattern.compile("\\s*PID\\:\\s+(\\d+)\\s+-\\s+Name\\:\\s+(.*)"); //$NON-NLS-1$
    /**
     * Pattern to match for session information (lttng create <session name>)
     */
    private final static Pattern CREATE_SESSION_NAME_PATTERN = Pattern.compile("\\s*Session\\s+(.*)\\s+created\\."); //$NON-NLS-1$
    /**
     * Pattern to match for session path information (lttng create <session name>)
     */
    private final static Pattern CREATE_SESSION_PATH_PATTERN = Pattern.compile("\\s*Traces\\s+will\\s+be\\s+written\\s+in\\s+(.*).*"); //$NON-NLS-1$
    /**
     * Pattern to match for session command output for "session name not found".
     */
    private final static Pattern SESSION_NOT_FOUND_ERROR_PATTERN = Pattern.compile("\\s*Error:\\s+Session\\s+name\\s+not\\s+found"); //$NON-NLS-1$
    /**
     * Pattern to match introduction line of context list.
     */
    private final static Pattern ADD_CONTEXT_HELP_CONTEXTS_INTRO = Pattern.compile("\\s*TYPE can\\s+be\\s+one\\s+of\\s+the\\s+strings\\s+below.*"); //$NON-NLS-1$
    
    /**
     * Pattern to match introduction line of context list.
     */
    private final static Pattern ADD_CONTEXT_HELP_CONTEXTS_END_LINE = Pattern.compile("\\s*Example.*"); //$NON-NLS-1$
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The command shell implementation
     */
    private ICommandShell fCommandShell = null;

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
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService
     * #getSessionNames(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public String[] getSessionNames(IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(COMMAND_LIST);

        ICommandResult result = executeCommand(command.toString(), monitor);

        // Output:
        // Available tracing sessions:
        // 1) mysession1 (/home/user/lttng-traces/mysession1-20120123-083928)
        // [inactive]
        // 2) mysession (/home/user/lttng-traces/mysession-20120123-083318)
        // [inactive]
        //
        // Use lttng list <session_name> for more details

        ArrayList<String> retArray = new ArrayList<String>();
        int index = 0;
        while (index < result.getOutput().length) {
            String line = result.getOutput()[index];
            Matcher matcher = SESSION_PATTERN.matcher(line);
            if (matcher.matches()) {
                retArray.add(matcher.group(2).trim());
            }
            index++;
        }
        return retArray.toArray(new String[retArray.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService
     * #getSession(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public ISessionInfo getSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(COMMAND_LIST, sessionName);
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
            Matcher matcher = TRACE_SESSION_PATTERN.matcher(line);
            if (matcher.matches()) {
                sessionInfo.setSessionState(matcher.group(2));
                index++;
                continue;
            }

            matcher = TRACE_SESSION_PATH_PATTERN.matcher(line);
            if (matcher.matches()) {
                sessionInfo.setSessionPath(matcher.group(1).trim());
                index++;
                continue;
            }

            matcher = DOMAIN_KERNEL_PATTERN.matcher(line);
            if (matcher.matches()) {
                // Create Domain
                IDomainInfo domainInfo = new DomainInfo(Messages.TraceControl_KernelDomainDisplayName);
                sessionInfo.addDomain(domainInfo);

                // in domain kernel
                ArrayList<IChannelInfo> channels = new ArrayList<IChannelInfo>();
                index = parseDomain(result.getOutput(), index, channels);

                // set channels
                domainInfo.setChannels(channels);
                
                // set kernel flag
                domainInfo.setIsKernel(true);
                continue;
            }

            matcher = DOMAIN_UST_GLOBAL_PATTERN.matcher(line);
            if (matcher.matches()) {
                IDomainInfo domainInfo = new DomainInfo(Messages.TraceControl_UstGlobalDomainDisplayName);
                sessionInfo.addDomain(domainInfo);

                // in domain UST
                ArrayList<IChannelInfo> channels = new ArrayList<IChannelInfo>();
                index = parseDomain(result.getOutput(), index, channels);

                // set channels
                domainInfo.setChannels(channels);
                
                // set kernel flag
                domainInfo.setIsKernel(false);
                continue;
            }
            index++;
        }
        return sessionInfo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService
     * #getKernelProvider(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public List<IBaseEventInfo> getKernelProvider(IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(COMMAND_LIST_KERNEL);
        ICommandResult result = executeCommand(command.toString(), monitor);

        // Kernel events:
        // -------------
        // sched_kthread_stop (type: tracepoint)
        List<IBaseEventInfo> events = new ArrayList<IBaseEventInfo>();
        getProviderEventInfo(result.getOutput(), 0, events);
        return events;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService
     * #getUstProvider()
     */
    @Override
    public List<IUstProviderInfo> getUstProvider() throws ExecutionException {
        return getUstProvider(new NullProgressMonitor());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService
     * #getUstProvider(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public List<IUstProviderInfo> getUstProvider(IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(COMMAND_LIST_UST);

        ICommandResult result = executeCommand(command.toString(), monitor);

        // UST events:
        // -------------
        //
        // PID: 3635 - Name:
        // /home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello
        // ust_tests_hello:tptest_sighandler (loglevel: TRACE_EMERG0) (type:
        // tracepoint)
        // ust_tests_hello:tptest (loglevel: TRACE_EMERG0) (type: tracepoint)
        //
        // PID: 6459 - Name:
        // /home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello
        // ust_tests_hello:tptest_sighandler (loglevel: TRACE_EMERG0) (type:
        // tracepoint)
        // ust_tests_hello:tptest (loglevel: TRACE_EMERG0) (type: tracepoint)

        List<IUstProviderInfo> allProviders = new ArrayList<IUstProviderInfo>();
        IUstProviderInfo provider = null;

        int index = 0;
        while (index < result.getOutput().length) {
            String line = result.getOutput()[index];
            Matcher matcher = UST_PROVIDER_PATTERN.matcher(line);
            if (matcher.matches()) {

                provider = new UstProviderInfo(matcher.group(2).trim());
                provider.setPid(Integer.valueOf(matcher.group(1).trim()));
                List<IBaseEventInfo> events = new ArrayList<IBaseEventInfo>();
                index = getProviderEventInfo(result.getOutput(), ++index, events);
                provider.setEvents(events);
                allProviders.add(provider);

            } else {
                index++;
            }

        }
        return allProviders;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#createSession(java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public ISessionInfo createSession(String sessionName, String sessionPath, IProgressMonitor monitor) throws ExecutionException {

        String newName = formatParameter(sessionName);
        String newPath = formatParameter(sessionPath);

        StringBuffer command = createCommand(COMMAND_CREATE_SESSION, newName);

        if (newPath != null && !"".equals(newPath)) { //$NON-NLS-1$
            command.append(OPTION_OUTPUT_PATH);
            command.append(newPath);
        }

        ICommandResult result = executeCommand(command.toString(), monitor);

        //Session myssession2 created.
        //Traces will be written in /home/user/lttng-traces/myssession2-20120209-095418
        String[] output = result.getOutput();
        
        // Get and verify session name
        Matcher matcher = CREATE_SESSION_NAME_PATTERN.matcher(output[0]);
        String name = null;

        if (matcher.matches()) {
            name = String.valueOf(matcher.group(1).trim());
        } else {
            // Output format not expected
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + //$NON-NLS-1$ //$NON-NLS-2$ 
                    Messages.TraceControl_UnexpectedCommandOutputFormat + ":\n" + //$NON-NLS-1$ 
                    formatOutput(result)); 
        }

        if ((name == null) || (!name.equals(sessionName))) {
            // Unexpected name returned
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + //$NON-NLS-1$ //$NON-NLS-2$ 
                    Messages.TraceControl_UnexpectedNameError + ": " + name); //$NON-NLS-1$ 
        }
        
        // Get and verify session path
        matcher = CREATE_SESSION_PATH_PATTERN.matcher(output[1]);
        String path = null;
        
        if (matcher.matches()) {
            path = String.valueOf(matcher.group(1).trim());
        } else {
            // Output format not expected
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + //$NON-NLS-1$ //$NON-NLS-2$ 
                    Messages.TraceControl_UnexpectedCommandOutputFormat + ":\n" + //$NON-NLS-1$ 
                    formatOutput(result)); 
        }

        if ((path == null) || ((sessionPath != null) && (!path.contains(sessionPath)))) {
            // Unexpected path
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + //$NON-NLS-1$ //$NON-NLS-2$ 
                    Messages.TraceControl_UnexpectedPathError + ": " + name); //$NON-NLS-1$
        }
        
        SessionInfo sessionInfo = new SessionInfo(name);
        sessionInfo.setSessionPath(path);

        return sessionInfo;
    }
    
    @Override
    public void destroySession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        String newName = formatParameter(sessionName);

        StringBuffer command = createCommand(COMMAND_DESTROY_SESSION, newName);

        ICommandResult result = executeCommand(command.toString(), monitor, false);
        String[] output = result.getOutput();
        
        if (isError(result) && ((output == null) || (!SESSION_NOT_FOUND_ERROR_PATTERN.matcher(output[0]).matches()))) {
                throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + formatOutput(result)); //$NON-NLS-1$ //$NON-NLS-2$
       }
        //Session <sessionName> destroyed
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#startSession(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void startSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {

        String newSessionName = formatParameter(sessionName);

        StringBuffer command = createCommand(COMMAND_START_SESSION, newSessionName);

        executeCommand(command.toString(), monitor);

        //Session <sessionName> started
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#stopSession(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void stopSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        String newSessionName = formatParameter(sessionName);
        StringBuffer command = createCommand(COMMAND_STOP_SESSION, newSessionName);

        executeCommand(command.toString(), monitor);

        //Session <sessionName> stopped
        
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#enableChannel(java.lang.String, java.util.List, boolean, org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableChannels(String sessionName, List<String> channelNames, boolean isKernel, IChannelInfo info, IProgressMonitor monitor) throws ExecutionException {

        // no channels to enable
        if (channelNames.isEmpty()) {
            return;
        }

        StringBuffer command = createCommand(COMMAND_ENABLE_CHANNEL);

        for (Iterator<String> iterator = channelNames.iterator(); iterator.hasNext();) {
            String channel = (String) iterator.next();
            command.append(channel);
            if (iterator.hasNext()) {
                command.append(',');
            }
        }

        if (isKernel) {
            command.append(OPTION_KERNEL);
        } else {
            command.append(OPTION_UST);
        }

        String newSessionName = formatParameter(sessionName);
        command.append(OPTION_SESSION);
        command.append(newSessionName);

        if (info != null) {
//            --discard            Discard event when buffers are full (default)

//            --overwrite          Flight recorder mode
            if (info.isOverwriteMode()) {
                command.append(OPTION_OVERWRITE);
            }
//            --subbuf-size SIZE   Subbuffer size in bytes
//                                     (default: 4096, kernel default: 262144)
            command.append(OPTION_SUB_BUFFER_SIZE);
            command.append(String.valueOf(info.getSubBufferSize()));

//            --num-subbuf NUM     Number of subbufers
//                                     (default: 8, kernel default: 4)
            command.append(OPTION_NUM_SUB_BUFFERS);
            command.append(String.valueOf(info.getNumberOfSubBuffers()));
            
//            --switch-timer USEC  Switch timer interval in usec (default: 0)
            command.append(OPTION_SWITCH_TIMER);
            command.append(String.valueOf(info.getSwitchTimer()));

//            --read-timer USEC    Read timer interval in usec (default: 200)
            command.append(OPTION_READ_TIMER);
            command.append(String.valueOf(info.getReadTimer()));
        } 

        executeCommand(command.toString(), monitor);
        
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#disableChannel(java.lang.String, java.util.List, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void disableChannels(String sessionName, List<String> channelNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        
        // no channels to enable
        if (channelNames.isEmpty()) {
            return;
        }

        StringBuffer command = createCommand(COMMAND_DISABLE_CHANNEL);

        for (Iterator<String> iterator = channelNames.iterator(); iterator.hasNext();) {
            String channel = (String) iterator.next();
            command.append(channel);
            if (iterator.hasNext()) {
                command.append(',');
            }
        }

        if (isKernel) {
            command.append(OPTION_KERNEL);
        } else {
            command.append(OPTION_UST);
        }

        String newSessionName = formatParameter(sessionName);
        command.append(OPTION_SESSION);
        command.append(newSessionName);

        executeCommand(command.toString(), monitor);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#enableEvent(java.lang.String, java.lang.String, java.util.List, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableEvents(String sessionName, String channelName, List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {

        StringBuffer command = createCommand(COMMAND_ENABLE_EVENT);

        if (eventNames == null || eventNames.isEmpty()) {
            command.append(OPTION_ALL);
        } else {

            for (Iterator<String> iterator = eventNames.iterator(); iterator.hasNext();) {
                String event = (String) iterator.next();
                command.append(event);
                if (iterator.hasNext()) {
                    command.append(',');
                }
            }
        }

        if (isKernel) {
            command.append(OPTION_KERNEL);
        } else {
            command.append(OPTION_UST);
        }

        String newSessionName = formatParameter(sessionName);

        command.append(OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(OPTION_CHANNEL);
            command.append(channelName);
        }
        
        command.append(OPTION_TRACEPOINT);
        
        executeCommand(command.toString(), monitor);
        
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#enableSyscalls(java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableSyscalls(String sessionName, String channelName, IProgressMonitor monitor) throws ExecutionException {

        StringBuffer command = createCommand(COMMAND_ENABLE_EVENT);

        command.append(OPTION_ALL);
        command.append(OPTION_KERNEL);

        String newSessionName = formatParameter(sessionName);

        command.append(OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(OPTION_CHANNEL);
            command.append(channelName);
        }
        
        command.append(OPTION_SYSCALL);
        
        executeCommand(command.toString(), monitor);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#enableProbe(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableProbe(String sessionName, String channelName, String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(COMMAND_ENABLE_EVENT);

        command.append(eventName);
        command.append(OPTION_KERNEL);

        String newSessionName = formatParameter(sessionName);
        command.append(OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(OPTION_CHANNEL);
            command.append(channelName);
        }
        if (isFunction) {
            command.append(OPTION_FUNCTION_PROBE);
        } else {
            command.append(OPTION_PROBE);
        }
        
        command.append(probe);
        
        executeCommand(command.toString(), monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#enableLogLevel(java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.LogLevelType, org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceLogLevel, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableLogLevel(String sessionName, String channelName, String eventName, LogLevelType logLevelType, TraceLogLevel level, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(COMMAND_ENABLE_EVENT);

        command.append(eventName);
        command.append(OPTION_UST);

        String newSessionName = formatParameter(sessionName);
        command.append(OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(OPTION_CHANNEL);
            command.append(channelName);
        }
        
        if (logLevelType == LogLevelType.LOGLEVEL) {
            command.append(OPTION_LOGLEVEL);
        } else if (logLevelType == LogLevelType.LOGLEVEL_ONLY) {
            command.append(OPTION_LOGLEVEL_ONLY);
            
        } else {
            return;
        }
        command.append(level.getInName());
        
        executeCommand(command.toString(), monitor);
        
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#disableEvent(java.lang.String, java.lang.String, java.util.List, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void disableEvent(String sessionName, String channelName, List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(COMMAND_DISABLE_EVENT);

        if (eventNames == null) {
            command.append(OPTION_ALL);
        } else {
            // no events to enable
            if (eventNames.isEmpty()) {
                return;
            }

            for (Iterator<String> iterator = eventNames.iterator(); iterator.hasNext();) {
                String event = (String) iterator.next();
                command.append(event);
                if (iterator.hasNext()) {
                    command.append(',');
                }
            }
        }

        if (isKernel) {
            command.append(OPTION_KERNEL);
        } else {
            command.append(OPTION_UST);
        }

        String newSessionName = formatParameter(sessionName);
        command.append(OPTION_SESSION);
        command.append(newSessionName);

        if (channelName != null) {
            command.append(OPTION_CHANNEL);
            command.append(channelName);
        }

        executeCommand(command.toString(), monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#getContexts(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public List<String> getContextList(IProgressMonitor monitor) throws ExecutionException {

        StringBuffer command = createCommand(COMMAND_ADD_CONTEXT, OPTION_HELP);

        ICommandResult result = executeCommand(command.toString(), monitor);

        String[] output = result.getOutput(); 
        
        List<String> contexts = new ArrayList<String>(0);
        
        int index = 0;
        boolean inList = false;
        while (index < output.length) {
            String line = result.getOutput()[index];
            
            Matcher startMatcher = ADD_CONTEXT_HELP_CONTEXTS_INTRO.matcher(line);
            Matcher endMatcher = ADD_CONTEXT_HELP_CONTEXTS_END_LINE.matcher(line);

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

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#addContexts(java.lang.String, java.lang.String, java.lang.String, boolean, java.util.List, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void addContexts(String sessionName, String channelName, String eventName, boolean isKernel, List<String> contextNames, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(COMMAND_ADD_CONTEXT);

        String newSessionName = formatParameter(sessionName);
        command.append(OPTION_SESSION);
        command.append(newSessionName);
        
        if (channelName != null) {
            command.append(OPTION_CHANNEL);
            command.append(channelName);
        }

        if (eventName != null) {
            command.append(OPTION_EVENT);
            command.append(eventName);
        }

        if (isKernel) {
            command.append(OPTION_KERNEL);
        } else {
            command.append(OPTION_UST);
        }
        
        for (Iterator<String> iterator = contextNames.iterator(); iterator.hasNext();) {
            String context = (String) iterator.next();
            command.append(OPTION_CONTEXT_TYPE);
            command.append(context);
        }

        executeCommand(command.toString(), monitor);
        
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService#calibrate(boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void calibrate(boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
//        String newSessionName = formatParameter(sessionName);
        StringBuffer command = createCommand(COMMAND_CALIBRATE);
//
//        command.append(OPTION_SESSION);
//        command.append(newSessionName);

        if (isKernel) {
            command.append(OPTION_KERNEL);
        } else {
            command.append(OPTION_UST);
        }

        command.append(OPTION_FUNCTION_PROBE);

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
    private boolean isError(ICommandResult result) {
        if ((result.getResult()) != 0 || (result.getOutput().length < 1 || ERROR_PATTERN.matcher(result.getOutput()[0]).matches())) {
            return true;
        }
        return false;
    }

    /**
     * Formats the output string as single string.
     * 
     * @param output
     *            - output array
     * @return - the formatted output
     */
    private String formatOutput(ICommandResult result) {
        if ((result == null) || result.getOutput() == null || result.getOutput().length == 0) {
            return ""; //$NON-NLS-1$
        }
        String[] output = result.getOutput();
        StringBuffer ret = new StringBuffer();
        ret.append("Return Value: "); //$NON-NLS-1$
        ret.append(result.getResult());
        ret.append("\n"); //$NON-NLS-1$
        for (int i = 0; i < output.length; i++) {
            ret.append(output[i] + "\n"); //$NON-NLS-1$
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
     * @return the new current index in command output array
     */
    private int parseDomain(String[] output, int currentIndex, List<IChannelInfo> channels) {
        int index = currentIndex;

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

            Matcher outerMatcher = CHANNELS_SECTION_PATTERN.matcher(line);
            if (outerMatcher.matches()) {
                IChannelInfo channelInfo = null;
                while (index < output.length) {
                    String subLine = output[index];

                    Matcher innerMatcher = CHANNEL_PATTERN.matcher(subLine);
                    if (innerMatcher.matches()) {
                        channelInfo = new ChannelInfo(""); //$NON-NLS-1$
                        // get channel name
                        channelInfo.setName(innerMatcher.group(1));

                        // get channel enablement
                        channelInfo.setState(innerMatcher.group(2));

                        // add channel
                        channels.add(channelInfo);

                    } else if (OVERWRITE_MODE_ATTRIBUTE.matcher(subLine).matches()) {
                        String value = getAttributeValue(subLine);
                        channelInfo.setOverwriteMode(!OVERWRITE_MODE_ATTRIBUTE_FALSE.equals(value));
                    } else if (SUBBUFFER_SIZE_ATTRIBUTE.matcher(subLine).matches()) {
                        channelInfo.setSubBufferSize(Long.valueOf(getAttributeValue(subLine)));

                    } else if (NUM_SUBBUFFERS_ATTRIBUTE.matcher(subLine).matches()) {
                        channelInfo.setNumberOfSubBuffers(Integer.valueOf(getAttributeValue(subLine)));

                    } else if (SWITCH_TIMER_ATTRIBUTE.matcher(subLine).matches()) {
                        channelInfo.setSwitchTimer(Long.valueOf(getAttributeValue(subLine)));

                    } else if (READ_TIMER_ATTRIBUTE.matcher(subLine).matches()) {
                        channelInfo.setReadTimer(Long.valueOf(getAttributeValue(subLine)));

                    } else if (OUTPUT_ATTRIBUTE.matcher(subLine).matches()) {
                        channelInfo.setOutputType(getAttributeValue(subLine));

                    } else if (EVENT_SECTION_PATTERN.matcher(subLine).matches()) {
                        List<IEventInfo> events = new ArrayList<IEventInfo>();
                        index = parseEvents(output, index, events);
                        channelInfo.setEvents(events);
                        // we want to stay at the current index to be able to
                        // exit the domain
                        continue;
                    } else if (DOMAIN_KERNEL_PATTERN.matcher(subLine).matches()) {
                        return index;

                    } else if (DOMAIN_UST_GLOBAL_PATTERN.matcher(subLine).matches()) {
                        return index;
                    }
                    index++;
                }
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
    private int parseEvents(String[] output, int currentIndex, List<IEventInfo> events) {
        int index = currentIndex;

        while (index < output.length) {
            String line = output[index];
            if (CHANNEL_PATTERN.matcher(line).matches()) {
                // end of channel
                return index;
            } else if (DOMAIN_KERNEL_PATTERN.matcher(line).matches()) {
                // end of domain
                return index;
            } else if (DOMAIN_UST_GLOBAL_PATTERN.matcher(line).matches()) {
                // end of domain
                return index;
            } 

            Matcher matcher = EVENT_PATTERN.matcher(line);
            Matcher matcher2 = WILDCARD_EVENT_PATTERN.matcher(line);

            if (matcher.matches()) {
                IEventInfo eventInfo = new EventInfo(matcher.group(1).trim());
                eventInfo.setLogLevel(matcher.group(2).trim());
                eventInfo.setEventType(matcher.group(3).trim());
                eventInfo.setState(matcher.group(4));
                events.add(eventInfo);
                index++;
            } else if (matcher2.matches()) {
                IEventInfo eventInfo = new EventInfo(matcher2.group(1).trim());
                eventInfo.setLogLevel(TraceLogLevel.LEVEL_UNKNOWN);
                eventInfo.setEventType(matcher2.group(2).trim());
                eventInfo.setState(matcher2.group(3));
                
                if (eventInfo.getEventType() == TraceEventType.PROBE) {
                    IProbeEventInfo probeEvent = new ProbeEventInfo(eventInfo.getName());
                    probeEvent.setLogLevel(eventInfo.getLogLevel());
                    probeEvent.setEventType(eventInfo.getEventType());
                    probeEvent.setState(eventInfo.getState());

                    // Overwrite eventinfo
                    eventInfo = probeEvent;

                    // myevent2 (type: probe) [enabled]
                    // addr: 0xc0101340
                    // myevent0 (type: probe) [enabled]
                    // offset: 0x0
                    // symbol: init_post
                    index++;
                    while (index < output.length) {
                        String probeLine = output[index];
                        // parse probe
                        Matcher addrMatcher = PROBE_ADDRESS_PATTERN.matcher(probeLine);
                        Matcher offsetMatcher = PROBE_OFFSET_PATTERN.matcher(probeLine);
                        Matcher symbolMatcher = PROBE_SYMBOL_PATTERN.matcher(probeLine);
                        if (addrMatcher.matches()) {
                            String addr = addrMatcher.group(2).trim();
                            probeEvent.setAddress(addr);
                        } else if (offsetMatcher.matches()) {
                            String offset = offsetMatcher.group(2).trim();
                            probeEvent.setOffset(offset);
                        } else if (symbolMatcher.matches()) {
                            String symbol = symbolMatcher.group(2).trim();
                            probeEvent.setSymbol(symbol);
                        } else if ((EVENT_PATTERN.matcher(probeLine).matches()) || (WILDCARD_EVENT_PATTERN.matcher(probeLine).matches())) {
                            break;
                        } else if (CHANNEL_PATTERN.matcher(probeLine).matches()) {
                            break;
                        } else if (DOMAIN_KERNEL_PATTERN.matcher(probeLine).matches()) {
                            // end of domain
                            break;
                        } else if (DOMAIN_UST_GLOBAL_PATTERN.matcher(probeLine).matches()) {
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
//            else if (line.matches(EVENT_NONE_PATTERN)) {
                // do nothing
//            } else 

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
    private String getAttributeValue(String line) {
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
    private int getProviderEventInfo(String[] output, int currentIndex, List<IBaseEventInfo> events) {
        int index = currentIndex;
        while (index < output.length) {
            String line = output[index];
            Matcher matcher = PROVIDER_EVENT_PATTERN.matcher(line);
            if (matcher.matches()) {
                // sched_kthread_stop (loglevel: TRACE_EMERG0) (type:
                // tracepoint)
                IBaseEventInfo eventInfo = new BaseEventInfo(matcher.group(1).trim());
                eventInfo.setLogLevel(matcher.group(2).trim());
                eventInfo.setEventType(matcher.group(3).trim());
                events.add(eventInfo);
            } else if (UST_PROVIDER_PATTERN.matcher(line).matches()) {
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
    private String formatParameter(String parameter) {
        if (parameter != null) {
            StringBuffer newString = new StringBuffer();
            newString.append(parameter);

            if (parameter.contains(" ")) { //$NON-NLS-1$
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
    private StringBuffer createCommand(String... strings) {
        StringBuffer command = new StringBuffer();
        command.append(CONTROL_COMMAND);
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
    private String getTracingGroupOption() {
        if (!ControlPreferences.getInstance().isDefaultTracingGroup() && !ControlPreferences.getInstance().getTracingGroup().equals("")) { //$NON-NLS-1$
            return OPTION_TRACING_GROUP + ControlPreferences.getInstance().getTracingGroup();
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * @return the verbose option as configured in the preferences
     */
    private String getVerboseOption() {
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            String level = ControlPreferences.getInstance().getVerboseLevel();
            if (ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_VERBOSE.equals(level)) {
                return OPTION_VERBOSE;
            }
            if (ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_V_VERBOSE.equals(level)) {
                return OPTION_VERY_VERBOSE;
            } 
            if (ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_V_V_VERBOSE.equals(level)) {
                return OPTION_VERY_VERY_VERBOSE;
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Method that logs the command and command result if logging is enabled as well as forwards
     * the command execution to the shell.
     * @param command - the command to execute
     * @param monitor - a progress monitor
     * @return the command result
     * @throws ExecutionException
     */
    private ICommandResult executeCommand(String command, IProgressMonitor monitor) throws ExecutionException {
        return executeCommand(command, monitor, true);
    }
    
    /**
     * Method that logs the command and command result if logging is enabled as well as forwards
     * the command execution to the shell.
     * @param command - the command to execute
     * @param monitor - a progress monitor
     * @param - checkForError - true to verify command result, else false
     * @return the command result
     * @throws ExecutionException in case of error result
     */
    private ICommandResult executeCommand(String command, IProgressMonitor monitor, boolean checkForError) throws ExecutionException {
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.log(command);
        }

        ICommandResult result = fCommandShell.executeCommand(command.toString(), monitor);
        
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.log(formatOutput(result));
        }

        if (isError(result)) {
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command.toString() + "\n" + formatOutput(result)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        return result;
    }
}
