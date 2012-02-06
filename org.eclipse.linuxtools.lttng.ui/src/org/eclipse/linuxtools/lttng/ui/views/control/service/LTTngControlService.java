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
package org.eclipse.linuxtools.lttng.ui.views.control.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ISessionInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IUstProviderInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.BaseEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.ChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.DomainInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.EventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.SessionInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.UstProviderInfo;
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
    private final static String COMMAND_LIST = CONTROL_COMMAND + " list "; //$NON-NLS-1$
    /**
     * Command to list kernel tracer information. 
     */
    private final static String COMMAND_LIST_KERNEL = COMMAND_LIST + "-k";  //$NON-NLS-1$
    /**
     * Command to list user space trace information. 
     */
    private final static String COMMAND_LIST_UST = COMMAND_LIST + "-u";  //$NON-NLS-1$

    // Parsing constants
    /**
     * Pattern to match for error output
     */
    private final static String ERROR_PATTERN = "\\s*Error\\:.*"; //$NON-NLS-1$
    /**
     * Pattern to match for session information (lttng list)
     */
    private final static String SESSION_PATTERN = "\\s+(\\d+)\\)\\s+(.*)\\s+\\((.*)\\)\\s+\\[(active|inactive)\\].*"; //$NON-NLS-1$
    /**
     * Pattern to match for session information (lttng list <session>)
     */
    private final static String TRACE_SESSION_PATTERN = "\\s*Tracing\\s+session\\s+(.*)\\:\\s+\\[(active|inactive)\\].*"; //$NON-NLS-1$
    /**
     * Pattern to match for session path information (lttng list <session>)
     */
    private final static String TRACE_SESSION_PATH_PATTERN = "\\s*Trace\\s+path\\:\\s+(.*)"; //$NON-NLS-1$
    /**
     * Pattern to match for kernel domain information (lttng list <session>)
     */
    private final static String DOMAIN_KERNEL_PATTERN = "=== Domain: Kernel ==="; //$NON-NLS-1$
    /**
     * Pattern to match for ust domain information (lttng list <session>)
     */
    private final static String DOMAIN_UST_GLOBAL_PATTERN = "=== Domain: UST global ==="; //$NON-NLS-1$
    /**
     * Pattern to match for channels section (lttng list <session>)
     */
    private final static String CHANNELS_SECTION_PATTERN = "\\s*Channels\\:";  //$NON-NLS-1$
    /**
     * Pattern to match for channel information (lttng list <session>)
     */
    private final static String CHANNEL_PATTERN = "\\s*-\\s+(.*)\\:\\s+\\[(enabled|disabled)\\]"; //$NON-NLS-1$
    /**
     * Pattern to match for events section information (lttng list <session>)
     */
    private final static String EVENT_SECTION_PATTERN = "\\s*Events\\:"; //$NON-NLS-1$
    /**
     * Pattern to match for event information (no enabled events) (lttng list <session>)
     */
//    private final static String EVENT_NONE_PATTERN = "\\s+None"; //$NON-NLS-1$
    /**
     * Pattern to match for event information (lttng list <session>)
     */
    private final static String EVENT_PATTERN = "\\s+(.*)\\s+\\(loglevel:\\s+(.*)\\)\\s+\\(type:\\s+(.*)\\)\\s+\\[(enabled|disabled)\\].*"; //$NON-NLS-1$
    /**
     * Pattern to match for channel (overwite mode) information (lttng list <session>)
     */
    private final static String OVERWRITE_MODE_ATTRIBUTE = "\\s+overwrite\\s+mode\\:.*";  //$NON-NLS-1$
    /**
     * Pattern to match indicating false for overwrite mode
     */
    private final static String OVERWRITE_MODE_ATTRIBUTE_FALSE = "0"; //$NON-NLS-1$
    /**
     * Pattern to match for channel (sub-buffer size) information (lttng list <session>)
     */
    private final static String SUBBUFFER_SIZE_ATTRIBUTE = "\\s+subbufers\\s+size\\:.*"; //$NON-NLS-1$
    /**
     * Pattern to match for channel (number of sub-buffers) information (lttng list <session>)
     */
    private final static String NUM_SUBBUFFERS_ATTRIBUTE = "\\s+number\\s+of\\s+subbufers\\:.*"; //$NON-NLS-1$
    /**
     * Pattern to match for channel (switch timer) information (lttng list <session>)
     */
    private final static String SWITCH_TIMER_ATTRIBUTE = "\\s+switch\\s+timer\\s+interval\\:.*"; //$NON-NLS-1$
    /**
     * Pattern to match for channel (read timer) information (lttng list <session>)
     */
    private final static String READ_TIMER_ATTRIBUTE = "\\s+read\\s+timer\\s+interval\\:.*"; //$NON-NLS-1$
    /**
     * Pattern to match for channel (output type) information (lttng list <session>)
     */
    private final static String OUTPUT_ATTRIBUTE = "\\s+output\\:.*"; //$NON-NLS-1$
    /**
     * Pattern to match for provider information (lttng list -k/-u)
     */
    private final static String PROVIDER_EVENT_PATTERN = "\\s*(.*)\\s+\\(loglevel:\\s+(.*)\\)\\s+\\(type:\\s+(.*)\\)"; //$NON-NLS-1$
    /**
     * Pattern to match for UST provider information (lttng list -u)
     */   
    private final static String UST_PROVIDER_PATTERN = "\\s*PID\\:\\s+(\\d+)\\s+-\\s+Name\\:\\s+(.*)"; //$NON-NLS-1$

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
     * @param shell - the command shell implementation to use
     */
    public LTTngControlService(ICommandShell shell) {
        fCommandShell = shell;
    }
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------  

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.service.ILttngControlService#getSessionNames()
     */
    @Override
    public String[] getSessionNames() throws ExecutionException {
        return getSessionNames(new NullProgressMonitor());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.service.ILttngControlService#getSessionNames(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public String[] getSessionNames(IProgressMonitor monitor) throws ExecutionException {

      String command = COMMAND_LIST;
      ICommandResult result = fCommandShell.executeCommand(command, monitor);

      if (isError(result)) {
          // TODO: no session available shouldn't be an error!
          if (result.getOutput().length > 0 && result.getOutput()[0].matches(ERROR_PATTERN)) {
              // no sessions available
              return new String[0];
          }
          throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + formatOutput(result.getOutput())); //$NON-NLS-1$ //$NON-NLS-2$
      }

      // Output:
      // Available tracing sessions:
      //   1) mysession1 (/home/user/lttng-traces/mysession1-20120123-083928) [inactive]
      //   2) mysession (/home/user/lttng-traces/mysession-20120123-083318) [inactive]
      //
      // Use lttng list <session_name> for more details 
      
      ArrayList<String> retArray = new ArrayList<String>();
      int index = 0;
      while (index < result.getOutput().length) {
          String line = result.getOutput()[index];
          try {
              Pattern pattern = Pattern.compile(SESSION_PATTERN);
              Matcher matcher = pattern.matcher(line);
              if (matcher.matches()) {
                  retArray.add(matcher.group(2).trim());
              }
          } catch (PatternSyntaxException e) {
          }
          index++;
      }
      return retArray.toArray(new String[retArray.size()]);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.service.ILttngControlService#getSession(java.lang.String)
     */
    @Override
    public ISessionInfo getSession(String sessionName) throws ExecutionException {
        return getSession(sessionName, new NullProgressMonitor());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.service.ILttngControlService#getSession(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public ISessionInfo getSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        String command = COMMAND_LIST + sessionName;
        ICommandResult result = fCommandShell.executeCommand(command, monitor);

        if (isError(result)) {
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + formatOutput(result.getOutput())); //$NON-NLS-1$ //$NON-NLS-2$
        }

        int index = 0;

        // Output:
        //   Tracing session mysession2: [inactive]
        //   Trace path: /home/eedbhu/lttng-traces/mysession2-20120123-110330
        ISessionInfo sessionInfo = new SessionInfo(sessionName);

        try {
            while (index < result.getOutput().length) {
                //   Tracing session mysession2: [inactive]
                //   Trace path: /home/eedbhu/lttng-traces/mysession2-20120123-110330
                //
                //    === Domain: Kernel ===
                //
                String line = result.getOutput()[index];
                if (line.matches(TRACE_SESSION_PATTERN)) {
                    Pattern pattern = Pattern.compile(TRACE_SESSION_PATTERN);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        sessionInfo.setSessionState(matcher.group(2));
                    }
                    index++;
                } else if (line.matches(TRACE_SESSION_PATH_PATTERN)) {
                    Pattern pattern = Pattern.compile(TRACE_SESSION_PATH_PATTERN);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        sessionInfo.setSessionPath(matcher.group(1).trim());
                    }
                    index++;
                }
                else if (line.matches(DOMAIN_KERNEL_PATTERN)) {
                    // Create Domain
                    IDomainInfo domainInfo = new DomainInfo(Messages.TraceControl_KernelDomainDisplayName);
                    sessionInfo.addDomain(domainInfo);

                    // in domain kernel
                    ArrayList<IChannelInfo> channels = new ArrayList<IChannelInfo>();
                    index = parseDomain(result.getOutput(), index, channels);

                    // set channels
                    domainInfo.setChannels(channels);

                } else if (line.contains(DOMAIN_UST_GLOBAL_PATTERN)) {
                    IDomainInfo domainInfo = new DomainInfo(Messages.TraceControl_UstGlobalDomainDisplayName);
                    sessionInfo.addDomain(domainInfo);

                    // in domain kernel
                    ArrayList<IChannelInfo> channels = new ArrayList<IChannelInfo>();
                    index = parseDomain(result.getOutput(), index, channels);

                    // set channels
                    domainInfo.setChannels(channels);
                } else {
                    index++;
                }
            }
        } catch (PatternSyntaxException e) {
            throw new ExecutionException("Invalid regular expression", e); //$NON-NLS-1$
        }   
        return sessionInfo;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.service.ILttngControlService#getKernelProvider()
     */
    @Override
    public List<IBaseEventInfo> getKernelProvider() throws ExecutionException {
        return getKernelProvider(new NullProgressMonitor());
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.service.ILttngControlService#getKernelProvider(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public List<IBaseEventInfo> getKernelProvider(IProgressMonitor monitor) throws ExecutionException {
        String command = COMMAND_LIST_KERNEL;
        ICommandResult result = fCommandShell.executeCommand(command, monitor);
        if (isError(result)) {
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + formatOutput(result.getOutput())); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
//        Kernel events:
//        -------------
//        sched_kthread_stop (type: tracepoint)
        List<IBaseEventInfo> events = new ArrayList<IBaseEventInfo>();
        getProviderEventInfo(result.getOutput(), 0, events);
        return events;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.service.ILttngControlService#getUstProvider()
     */
    @Override
    public List<IUstProviderInfo> getUstProvider() throws ExecutionException {
        return getUstProvider(new NullProgressMonitor());
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.service.ILttngControlService#getUstProvider(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public List<IUstProviderInfo> getUstProvider(IProgressMonitor monitor) throws ExecutionException {
        String command = COMMAND_LIST_UST;
        ICommandResult result = fCommandShell.executeCommand(command, monitor);

        if (isError(result)) {
            throw new ExecutionException(Messages.TraceControl_CommandError + " " + command + "\n" + formatOutput(result.getOutput())); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
//      UST events:
//      -------------
//
//      PID: 3635 - Name: /home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello
//            ust_tests_hello:tptest_sighandler (loglevel: TRACE_EMERG0) (type: tracepoint)
//            ust_tests_hello:tptest (loglevel: TRACE_EMERG0) (type: tracepoint)
//        
//      PID: 6459 - Name: /home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello
//            ust_tests_hello:tptest_sighandler (loglevel: TRACE_EMERG0) (type: tracepoint)
//            ust_tests_hello:tptest (loglevel: TRACE_EMERG0) (type: tracepoint)

        List<IUstProviderInfo> allProviders = new ArrayList<IUstProviderInfo>();
        IUstProviderInfo provider = null;
        
        int index = 0;
        while (index < result.getOutput().length) {
            String line = result.getOutput()[index];
            if (line.matches(UST_PROVIDER_PATTERN)) {
                Pattern pattern = Pattern.compile(UST_PROVIDER_PATTERN);

                Matcher matcher = pattern.matcher(line);

                if (matcher.matches()) {

                    provider = new UstProviderInfo(matcher.group(2).trim());
                    provider.setPid(Integer.valueOf(matcher.group(1).trim()));
                    List<IBaseEventInfo> events = new ArrayList<IBaseEventInfo>();        
                    index = getProviderEventInfo(result.getOutput(), ++index, events);
                    provider.setEvents(events);
                    allProviders.add(provider);
                }

            } else {
                index++;
            }
            
        }
        return allProviders;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    /**
     * Checks if command result is an error result.
     * @param result - the command result to check
     * @return true if error else false
     */
    private boolean isError(ICommandResult result) {
        if ((result.getResult()) != 0 || (result.getOutput().length < 1 || result.getOutput()[0].matches(ERROR_PATTERN))) {
            return true;
        }
        return false;
    }
    
    /**
     * Formats the output string as single string.
     * @param output - output array
     * @return - the formatted output
     */
    private String formatOutput(String[] output) {
        if (output == null || output.length == 0) {
            return ""; //$NON-NLS-1$
        }

        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < output.length; i++) {
            ret.append(output[i] + "\n"); //$NON-NLS-1$
        }
        return ret.toString();
    }
    
    /**
     * Parses the domain information.
     * 
     * @param output - a command output array
     * @param currentIndex - current index in command output array
     * @param channels - list for returning channel information
     * @return the new current index in command output array 
     * @throws PatternSyntaxException
     */
    private int parseDomain(String[] output, int currentIndex, List<IChannelInfo> channels) throws PatternSyntaxException {
        int index = currentIndex;

//      Channels:
//      -------------
//      - channnel1: [enabled]
//
//          Attributes:
//            overwrite mode: 0
//            subbufers size: 262144
//            number of subbufers: 4
//            switch timer interval: 0
//            read timer interval: 200
//            output: splice()
       
        while (index < output.length) {
            String line = output[index];
            if (line.matches(CHANNELS_SECTION_PATTERN)) {
                IChannelInfo channelInfo = null;
                while (index < output.length) {
                    String subLine = output[index];
                    if (subLine.matches(CHANNEL_PATTERN)) {
                        
                        Pattern pattern = Pattern.compile(CHANNEL_PATTERN);
                        Matcher matcher = pattern.matcher(subLine);
                        channelInfo = new ChannelInfo(""); //$NON-NLS-1$
                        if (matcher.matches()) {
                            // get channel name
                            channelInfo.setName(matcher.group(1));
                            
                            // get channel enablement
                            channelInfo.setState(matcher.group(2));

                            // add channel
                            channels.add(channelInfo);
                        }
                    } else if (subLine.matches(OVERWRITE_MODE_ATTRIBUTE)) {
                        String value = getAttributeValue(subLine);
                        channelInfo.setOverwriteMode(!OVERWRITE_MODE_ATTRIBUTE_FALSE.equals(value));
                    } else if (subLine.matches(SUBBUFFER_SIZE_ATTRIBUTE)) {
                        channelInfo.setSubBufferSize(Long.valueOf(getAttributeValue(subLine)));
                        
                    } else if (subLine.matches(NUM_SUBBUFFERS_ATTRIBUTE)) {
                        channelInfo.setNumberOfSubBuffers(Integer.valueOf(getAttributeValue(subLine)));
                        
                    } else if (subLine.matches(SWITCH_TIMER_ATTRIBUTE)) {
                        channelInfo.setSwitchTimer(Long.valueOf(getAttributeValue(subLine)));
                        
                    } else if (subLine.matches(READ_TIMER_ATTRIBUTE)) {
                        channelInfo.setReadTimer(Long.valueOf(getAttributeValue(subLine)));
                        
                    } else if (subLine.matches(OUTPUT_ATTRIBUTE)) {
                        channelInfo.setOutputType(getAttributeValue(subLine));
                        
                    } else if (subLine.matches(EVENT_SECTION_PATTERN)) {
                        List<IEventInfo> events = new ArrayList<IEventInfo>(); 
                        index = parseEvents(output, index, events);
                        channelInfo.setEvents(events);
                        // we want to stay at the current index to be able to exit the domain
                        continue;
                    }
                    else if (subLine.matches(DOMAIN_KERNEL_PATTERN)) {
                        return index;

                    } else if (subLine.contains(DOMAIN_UST_GLOBAL_PATTERN)) {
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
     * @param output - a command output array
     * @param currentIndex - current index in command output array
     * @param events - list for returning event information
     * @return the new current index in command output array
     * @throws PatternSyntaxException
     */
    private int parseEvents(String[] output, int currentIndex, List<IEventInfo> events) throws PatternSyntaxException {
        int index = currentIndex;

        while (index < output.length) {
            String line = output[index];
            if (line.matches(CHANNEL_PATTERN)) {
                // end of channel
                return index;
            } else if (line.matches(DOMAIN_KERNEL_PATTERN)) {
                // end of domain
                return index;
            } else if (line.contains(DOMAIN_UST_GLOBAL_PATTERN)) {
                // end of domain
                return index;
            } else if (line.matches(EVENT_PATTERN)) {
                Pattern pattern = Pattern.compile(EVENT_PATTERN);

                Matcher matcher = pattern.matcher(line);

                if (matcher.matches()) {
                    IEventInfo eventInfo = new EventInfo(matcher.group(1).trim());
                    eventInfo.setLogLevel(matcher.group(2).trim());
                    eventInfo.setEventType(matcher.group(3).trim());
                    eventInfo.setState(matcher.group(4));
                    events.add(eventInfo);
                }
            }
//            else if (line.matches(EVENT_NONE_PATTERN)) {
                // do nothing
//            } else 
            index++;
        }

        return index;
    }

    /**
     * Parses a line with attributes: <attribute Name>: <attribute value>
     * 
     * @param line - attribute line to parse
     * @return the attribute value as string
     * @throws PatternSyntaxException
     */
    private String getAttributeValue(String line) {
        String[] temp = line.split("\\: "); //$NON-NLS-1$
        return temp[1];
    }

    /**
     * Parses the event information within a provider. 
     * 
     * @param output - a command output array
     * @param currentIndex - current index in command output array
     * @param events - list for returning event information
     * @return the new current index in command output array
     */
    private int getProviderEventInfo(String[] output, int currentIndex, List<IBaseEventInfo> events) {
        int index = currentIndex;
        while (index < output.length) {
            String line = output[index];
            if (line.matches(PROVIDER_EVENT_PATTERN)) {
                // sched_kthread_stop (loglevel: TRACE_EMERG0) (type: tracepoint)
                Pattern pattern = Pattern.compile(PROVIDER_EVENT_PATTERN);

                Matcher matcher = pattern.matcher(line);

                if (matcher.matches()) {
                    IBaseEventInfo eventInfo = new BaseEventInfo(matcher.group(1).trim());
                    eventInfo.setLogLevel(matcher.group(2).trim());
                    eventInfo.setEventType(matcher.group(3).trim());
                    events.add(eventInfo);
                }
            } else if (line.matches(UST_PROVIDER_PATTERN)) {
                return index;
            }
            index++;
        }
        return index;
    }

}
