/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jonathan Rajotte - Initial support for machine interface lttng 2.6
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.ui.views.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IFieldInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IProbeEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISnapshotInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IUstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.BaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.DomainInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.EventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.FieldInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.ProbeEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.SessionInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.SnapshotInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.UstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.handlers.XmlMiValidationErrorHandler;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.ICommandResult;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.ICommandShell;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Service for sending LTTng trace control commands to remote host via machine
 * interface mode.
 *
 * @author Jonathan Rajotte
 */
public class LTTngControlServiceMI extends LTTngControlService {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final DocumentBuilder fDocumentBuilder;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param shell
     *            the command shell implementation to use
     * @param xsdUrl
     *            the xsd schema file for validation
     * @throws ExecutionException
     *             if the creation of the Schema and DocumentBuilder objects
     *             fails
     */
    public LTTngControlServiceMI(ICommandShell shell, URL xsdUrl) throws ExecutionException {
        super(shell);

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setValidating(false);

        // TODO: Add xsd validation for machine interface via mi_lttng.xsd from LTTng
        try {
            fDocumentBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ExecutionException(Messages.TraceControl_XmlDocumentBuilderError, e);
        }

        fDocumentBuilder.setErrorHandler(new XmlMiValidationErrorHandler());

    }

    /**
     * Generate a Document object from an array of String.
     *
     * @param xmlStrings
     *            array of strings representing an xml input
     * @return Document generated from strings input
     * @throws ExecutionException
     *             when parsing has failed
     */
    private Document getDocumentFromStrings(String[] xmlStrings) throws ExecutionException {
        StringBuilder concatenedString = new StringBuilder();
        for (String string : xmlStrings) {
            concatenedString.append(string);
        }
        InputSource stream = new InputSource(new StringReader(concatenedString.toString()));

        Document document;
        try {
            document = fDocumentBuilder.parse(stream);
        } catch (SAXException | IOException e) {
            throw new ExecutionException(Messages.TraceControl_XmlParsingError, e);
        }
        return document;

    }

    /**
     * Parse, populate and set the internal LTTngVersion variable
     *
     * @param xmlOutput
     *            the mi xml output of lttng version
     * @throws ExecutionException
     *             when xml extraction fail
     */
    public void setVersion(String[] xmlOutput) throws ExecutionException {
        Document doc = getDocumentFromStrings(xmlOutput);
        NodeList element = doc.getElementsByTagName(MIStrings.VERSION);
        int major = 0;
        int minor = 0;
        int patchLevel = 0;
        String license = ""; //$NON-NLS-1$
        String commit = ""; //$NON-NLS-1$
        String name = ""; //$NON-NLS-1$
        String description = ""; //$NON-NLS-1$
        String url = ""; //$NON-NLS-1$
        String fullVersion = ""; //$NON-NLS-1$
        if (element.getLength() == 1) {
            NodeList child = element.item(0).getChildNodes();
            // Get basic information
            for (int i = 0; i < child.getLength(); i++) {
                Node node = child.item(i);
                switch (node.getNodeName()) {
                case MIStrings.VERSION_MAJOR:
                    major = Integer.parseInt(node.getTextContent());
                    break;
                case MIStrings.VERSION_MINOR:
                    minor = Integer.parseInt(node.getTextContent());
                    break;
                case MIStrings.VERSION_PATCH_LEVEL:
                    patchLevel = Integer.parseInt(node.getTextContent());
                    break;
                case MIStrings.VERSION_COMMIT:
                    commit = node.getTextContent();
                    break;
                case MIStrings.VERSION_DESCRIPTION:
                    description = node.getTextContent();
                    break;
                case MIStrings.VERSION_LICENSE:
                    license = node.getTextContent();
                    break;
                case MIStrings.VERSION_NAME:
                    name = node.getTextContent();
                    break;
                case MIStrings.VERSION_STR:
                    fullVersion = node.getTextContent();
                    break;
                case MIStrings.VERSION_WEB:
                    url = node.getTextContent();
                    break;
                default:
                    break;
                }
            }
            setVersion(new LttngVersion(major, minor, patchLevel, license, commit, name, description, url, fullVersion));
        } else {
            throw new ExecutionException(Messages.TraceControl_UnsupportedVersionError);
        }
    }

    @Override
    public String[] getSessionNames(IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_LIST);
        ICommandResult result = executeCommand(command.toString(), monitor);

        Document doc = getDocumentFromStrings(result.getOutput());

        NodeList elements = doc.getElementsByTagName(MIStrings.NAME);

        ArrayList<String> retArray = new ArrayList<>();
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (node.getParentNode().getNodeName().equalsIgnoreCase(MIStrings.SESSION)) {
                retArray.add(node.getTextContent());
            }
        }
        return retArray.toArray(new String[retArray.size()]);
    }

    @Override
    public ISessionInfo getSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_LIST, sessionName);
        ICommandResult result = executeCommand(command.toString(), monitor);

        ISessionInfo sessionInfo = new SessionInfo(sessionName);
        Document document = getDocumentFromStrings(result.getOutput());

        NodeList sessionsNode = document.getElementsByTagName(MIStrings.SESSION);
        // There should be only one session
        if (sessionsNode.getLength() != 1) {
            throw new ExecutionException(Messages.TraceControl_MiInvalidNumberOfElementError);
        }

        // Populate session information
        NodeList rawSessionInfos = sessionsNode.item(0).getChildNodes();
        for (int i = 0; i < rawSessionInfos.getLength(); i++) {
            Node rawInfo = rawSessionInfos.item(i);
            switch (rawInfo.getNodeName()) {
            case MIStrings.PATH:
                sessionInfo.setSessionPath(rawInfo.getTextContent());
                break;
            case MIStrings.ENABLED:
                sessionInfo.setSessionState(rawInfo.getTextContent());
                break;
            case MIStrings.SNAPSHOT_MODE:
                if (rawInfo.getTextContent().equals(LTTngControlServiceConstants.TRUE_NUMERICAL)) {
                    // real name will be set later
                    ISnapshotInfo snapshotInfo = new SnapshotInfo(""); //$NON-NLS-1$
                    sessionInfo.setSnapshotInfo(snapshotInfo);
                }
                break;
            case MIStrings.LIVE_TIMER_INTERVAL:
                // TODO : live mode not supported yet in TMF:lttng-control
                break;
            case MIStrings.DOMAINS:
                // Extract the domains node
                NodeList rawDomains = rawInfo.getChildNodes();
                IDomainInfo domain = null;
                for (int j = 0; j < rawDomains.getLength(); j++) {
                    if (rawDomains.item(j).getNodeName().equalsIgnoreCase(MIStrings.DOMAIN)) {
                        domain = parseDomain(rawDomains.item(j));
                        sessionInfo.addDomain(domain);
                    }
                }
                break;
            default:
                break;
            }
        }

        if (!sessionInfo.isSnapshotSession()) {
            Matcher matcher = LTTngControlServiceConstants.TRACE_NETWORK_PATTERN.matcher(sessionInfo.getSessionPath());
            if (matcher.matches()) {
                sessionInfo.setStreamedTrace(true);
            }
        }


        // Fetch the snapshot info
        if (sessionInfo.isSnapshotSession()) {
            ISnapshotInfo snapshot = getSnapshotInfo(sessionName, monitor);
            sessionInfo.setSnapshotInfo(snapshot);
        }

        return sessionInfo;
    }

    /**
     * Parse a raw domain XML node to a IDomainInfo object
     *
     * @param rawDomain
     *            a domain xml node
     * @return a populated {@link DomainInfo} object
     * @throws ExecutionException
     *             when missing required xml element (type)
     */
    protected IDomainInfo parseDomain(Node rawDomain) throws ExecutionException {
        IDomainInfo domain = null;
        // Get the type
        Node rawType = getFirstOf(rawDomain.getChildNodes(), MIStrings.TYPE);
        if (rawType == null) {
            throw new ExecutionException(Messages.TraceControl_MiMissingRequiredError);
        }
        String rawTypeString = rawType.getTextContent().toLowerCase();
        TraceDomainType domainType = TraceDomainType.valueOfString(rawTypeString);
        switch (domainType) {
        case KERNEL:
            domain = new DomainInfo(Messages.TraceControl_KernelProviderDisplayName);
            domain.setIsKernel(true);
            break;
        case UST:
            domain = new DomainInfo(Messages.TraceControl_UstGlobalDomainDisplayName);
            domain.setIsKernel(false);
            break;
        case JUL:
            /**
             * TODO: Support for JUL JUL substructure and semantic is not the
             * same as a regular UST or Kernel Domain There is no channel under
             * JUL domain only events. The channel is activated in UST Channel
             */
            domain = new DomainInfo(Messages.TraceControl_JULDomainDisplayName);
            domain.setIsKernel(false);
            break;
        case UNKNOWN:
            domain = new DomainInfo(Messages.TraceControl_UnknownDomainDisplayName);
            domain.setIsKernel(false);
            break;
        default:
            throw new ExecutionException(Messages.TraceControl_MiInvalidElementError);
        }

        NodeList rawInfos = rawDomain.getChildNodes();
        for (int i = 0; i < rawInfos.getLength(); i++) {
            Node rawInfo = rawInfos.item(i);
            switch (rawInfo.getNodeName()) {
            case MIStrings.BUFFER_TYPE:
                BufferType bufferType = BufferType.valueOfString(rawInfo.getTextContent());
                domain.setBufferType(bufferType);
                break;
            case MIStrings.CHANNELS:
                ArrayList<IChannelInfo> channels = new ArrayList<>();
                parseChannels(rawInfo.getChildNodes(), channels);
                if (channels.size() > 0) {
                    domain.setChannels(channels);
                }
                break;
            default:
                break;
            }
        }

        return domain;
    }

    /**
     * Parse a list of raw channel XML node into an ArrayList of IChannelInfo
     *
     * @param rawChannes
     *            List of raw channel XML node
     * @param channels
     *            the parsed channels list
     * @throws ExecutionException
     *             when missing required xml element (type)
     */
    private static void parseChannels(NodeList rawChannels, ArrayList<IChannelInfo> channels) throws ExecutionException {
        IChannelInfo channel = null;
        for (int i = 0; i < rawChannels.getLength(); i++) {
            Node rawChannel = rawChannels.item(i);
            if (rawChannel.getNodeName().equalsIgnoreCase(MIStrings.CHANNEL)) {
                channel = new ChannelInfo(""); //$NON-NLS-1$

                // Populate the channel
                NodeList rawInfos = rawChannel.getChildNodes();
                Node rawInfo = null;
                for (int j = 0; j < rawInfos.getLength(); j++) {
                    rawInfo = rawInfos.item(j);
                    switch (rawInfo.getNodeName()) {
                    case MIStrings.NAME:
                        channel.setName(rawInfo.getTextContent());
                        break;
                    case MIStrings.ENABLED:
                        channel.setState(TraceEnablement.valueOfString(rawInfo.getTextContent()));
                        break;
                    case MIStrings.EVENTS:
                        List<IEventInfo> events = new ArrayList<>();
                        getEventInfo(rawInfo.getChildNodes(), events);
                        channel.setEvents(events);
                        break;
                    case MIStrings.ATTRIBUTES:
                        NodeList rawAttributes = rawInfo.getChildNodes();
                        for (int k = 0; k < rawAttributes.getLength(); k++) {
                            Node attribute = rawAttributes.item(k);
                            switch (attribute.getNodeName()) {
                            case MIStrings.OVERWRITE_MODE:
                                channel.setOverwriteMode(!LTTngControlServiceConstants.OVERWRITE_MODE_ATTRIBUTE_FALSE_MI.equalsIgnoreCase(attribute.getTextContent()));
                                break;
                            case MIStrings.SUBBUF_SIZE:
                                channel.setSubBufferSize(Long.valueOf(attribute.getTextContent()));
                                break;
                            case MIStrings.NUM_SUBBUF:
                                channel.setNumberOfSubBuffers(Integer.valueOf(attribute.getTextContent()));
                                break;
                            case MIStrings.SWITCH_TIMER_INTERVAL:
                                channel.setSwitchTimer(Long.valueOf(attribute.getTextContent()));
                                break;
                            case MIStrings.READ_TIMER_INTERVAL:
                                channel.setReadTimer(Long.valueOf(attribute.getTextContent()));
                                break;
                            case MIStrings.OUTPUT_TYPE:
                                channel.setOutputType(attribute.getTextContent());
                                break;
                            case MIStrings.TRACEFILE_SIZE:
                                channel.setMaxSizeTraceFiles(Integer.parseInt(attribute.getTextContent()));
                                break;
                            case MIStrings.TRACEFILE_COUNT:
                                channel.setMaxNumberTraceFiles(Integer.parseInt(attribute.getTextContent()));
                                break;
                            case MIStrings.LIVE_TIMER_INTERVAL:
                                // TODO: currently not supported by tmf
                                break;
                            default:
                                break;
                            }
                        }
                        break;
                    default:
                        break;
                    }
                }
                channels.add(channel);
            }
        }

    }

    @Override
    public ISnapshotInfo getSnapshotInfo(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        // TODO JRJ - STUB
        return null;
    }

    @Override
    public List<IBaseEventInfo> getKernelProvider(IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_LIST_KERNEL);
        ICommandResult result = executeCommand(command.toString(), monitor, false);
        List<IBaseEventInfo> events = new ArrayList<>();

        if (isError(result)) {
            return events;
        }

        Document document = getDocumentFromStrings(result.getOutput());
        NodeList rawEvents = document.getElementsByTagName(MIStrings.EVENT);
        getBaseEventInfo(rawEvents, events);
        return events;
    }

    @Override
    public List<IUstProviderInfo> getUstProvider(IProgressMonitor monitor) throws ExecutionException {
        StringBuffer command = createCommand(LTTngControlServiceConstants.COMMAND_LIST_UST);
        // Get the field to
        command.append(LTTngControlServiceConstants.OPTION_FIELDS);

        // Execute
        ICommandResult result = executeCommand(command.toString(), monitor, false);
        List<IUstProviderInfo> allProviders = new ArrayList<>();

        if (isError(result)) {
            return allProviders;
        }

        Document document = getDocumentFromStrings(result.getOutput());
        NodeList rawProviders = document.getElementsByTagName(MIStrings.PID);

        IUstProviderInfo providerInfo = null;

        for (int i = 0; i < rawProviders.getLength(); i++) {
            Node provider = rawProviders.item(i);
            Node name = getFirstOf(provider.getChildNodes(), MIStrings.NAME);
            if (name == null) {
                throw new ExecutionException(Messages.TraceControl_MiInvalidProviderError);
            }
            providerInfo = new UstProviderInfo(name.getTextContent());

            // Populate provider
            NodeList infos = provider.getChildNodes();
            for (int j = 0; j < infos.getLength(); j++) {
                Node info = infos.item(j);
                switch (info.getNodeName()) {
                case MIStrings.PID_ID:
                    providerInfo.setPid(Integer.parseInt(info.getTextContent()));
                    break;
                case MIStrings.EVENTS:
                    List<IBaseEventInfo> events = new ArrayList<>();
                    NodeList rawEvents = info.getChildNodes();
                    getBaseEventInfo(rawEvents, events);
                    providerInfo.setEvents(events);
                    break;
                default:
                    break;
                }
            }
            allProviders.add(providerInfo);
        }

        return allProviders;
    }

    @Override
    public ISessionInfo createSession(ISessionInfo sessionInfo, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void destroySession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void startSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void enableChannels(String sessionName, List<String> channelNames, boolean isKernel, IChannelInfo info, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void disableChannels(String sessionName, List<String> channelNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void enableEvents(String sessionName, String channelName, List<String> eventNames, boolean isKernel, String filterExpression, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void enableSyscalls(String sessionName, String channelName, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void enableProbe(String sessionName, String channelName, String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void enableLogLevel(String sessionName, String channelName, String eventName, LogLevelType logLevelType, TraceLogLevel level, String filterExpression, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void disableEvent(String sessionName, String channelName, List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getContextList(IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addContexts(String sessionName, String channelName, String eventName, boolean isKernel, List<String> contexts, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void calibrate(boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void recordSnapshot(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void runCommands(IProgressMonitor monitor, List<String> commands) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    /**
     * @param strings
     *            array of string that make up a command line
     * @return string buffer with created command line
     */
    @Override
    protected StringBuffer createCommand(String... strings) {
        StringBuffer command = new StringBuffer();
        command.append(LTTngControlServiceConstants.CONTROL_COMMAND_MI_XML);
        command.append(getTracingGroupOption());
        for (String string : strings) {
            command.append(string);
        }
        return command;
    }

    /**
     * @param xmlBaseEvents
     *            a Node list of base xml event element
     * @param events
     *            list of event generated by the parsing of the xml event
     *            element
     * @throws ExecutionException
     *             when a raw event is not a complete/valid xml event
     */
    private static void getBaseEventInfo(NodeList xmlBaseEvents, List<IBaseEventInfo> events) throws ExecutionException {
        IBaseEventInfo eventInfo = null;
        for (int i = 0; i < xmlBaseEvents.getLength(); i++) {
            NodeList rawInfos = xmlBaseEvents.item(i).getChildNodes();
            // Search for name
            if (xmlBaseEvents.item(i).getNodeName().equalsIgnoreCase(MIStrings.EVENT)) {
                Node rawName = getFirstOf(rawInfos, MIStrings.NAME);
                if (rawName == null) {
                    throw new ExecutionException(Messages.TraceControl_MiMissingRequiredError);
                }
                eventInfo = new BaseEventInfo(rawName.getTextContent());

                // Populate the event
                for (int j = 0; j < rawInfos.getLength(); j++) {
                    Node infoNode = rawInfos.item(j);
                    switch (infoNode.getNodeName()) {
                    case MIStrings.TYPE:
                        eventInfo.setEventType(infoNode.getTextContent());
                        break;
                    case MIStrings.LOGLEVEL:
                        eventInfo.setLogLevel(infoNode.getTextContent());
                        break;
                    case MIStrings.EVENT_FIELDS:
                        List<IFieldInfo> fields = new ArrayList<>();
                        getFieldInfo(infoNode.getChildNodes(), fields);
                        eventInfo.setFields(fields);
                        break;
                    default:
                        break;
                    }
                }
                events.add(eventInfo);
            }
        }
    }

    /**
     * @param xmlBaseEvents
     *            a Node list of xml event element linked to a session
     * @param events
     *            list of event generated by the parsing of the xml event
     *            element
     * @throws ExecutionException
     *             when a raw event is not a complete/valid xml event
     */
    static void getEventInfo(NodeList xmlEvents, List<IEventInfo> events) throws ExecutionException {
        IEventInfo eventInfo = null;
        for (int i = 0; i < xmlEvents.getLength(); i++) {
            NodeList rawInfos = xmlEvents.item(i).getChildNodes();
            // Search for name
            if (xmlEvents.item(i).getNodeName().equalsIgnoreCase(MIStrings.EVENT)) {
                Node rawName = getFirstOf(rawInfos, MIStrings.NAME);
                if (rawName == null) {
                    throw new ExecutionException(Messages.TraceControl_MiMissingRequiredError);
                }

                eventInfo = new EventInfo(rawName.getTextContent());

                // Basic information
                for (int j = 0; j < rawInfos.getLength(); j++) {
                    Node infoNode = rawInfos.item(j);
                    switch (infoNode.getNodeName()) {
                    case MIStrings.TYPE:
                        eventInfo.setEventType(infoNode.getTextContent());
                        break;
                    case MIStrings.LOGLEVEL_TYPE:
                        eventInfo.setLogLevelType(LogLevelType.valueOfString(infoNode.getTextContent()));
                        break;
                    case MIStrings.LOGLEVEL:
                        eventInfo.setLogLevel(TraceLogLevel.valueOfString(infoNode.getTextContent()));
                        break;
                    case MIStrings.ENABLED:
                        eventInfo.setState(TraceEnablement.valueOfString(infoNode.getTextContent()));
                        break;
                    case MIStrings.FILTER:
                        // TODO
                        // See bug 334 http://bugs.lttng.org/issues/334 from
                        // LTTng
                        // For now we emulate the a behavior, and simply put
                        // "with filter"
                        eventInfo.setFilterExpression("with filter"); //$NON-NLS-1$
                        break;
                    case MIStrings.EXCLUSION:
                        // TODO:Currently not supported by tmf
                        // ExclusionS element is ignored
                        break;
                    default:
                        break;
                    }
                }

                boolean isProbeFunction = (eventInfo.getEventType().equals(TraceEventType.PROBE)) || (eventInfo.getEventType().equals(TraceEventType.FUNCTION));
                if (isProbeFunction) {
                    IProbeEventInfo probeEvent = new ProbeEventInfo(eventInfo);
                    eventInfo = probeEvent;
                    // get attributes
                    Node rawAttributes = getFirstOf(rawInfos, MIStrings.ATTRIBUTES);
                    if (rawAttributes == null) {
                        throw new ExecutionException(Messages.TraceControl_MiMissingRequiredError);
                    }

                    Node rawDataNode = null;
                    switch (probeEvent.getEventType()) {
                    case PROBE:
                        rawDataNode = getFirstOf(rawAttributes.getChildNodes(), MIStrings.PROBE_ATTRIBUTES);
                        break;
                    case FUNCTION:
                        rawDataNode = getFirstOf(rawAttributes.getChildNodes(), MIStrings.FUNCTION_ATTRIBUTES);
                        break;
                    case SYSCALL:
                    case TRACEPOINT:
                    case UNKNOWN:
                    default:
                        throw new ExecutionException(Messages.TraceControl_MiInvalidElementError);
                    }

                    if (rawDataNode == null) {
                        throw new ExecutionException(Messages.TraceControl_MiInvalidElementError);
                    }

                    // Extract info
                    NodeList rawDatas = rawDataNode.getChildNodes();
                    for (int j = 0; j < rawDatas.getLength(); j++) {
                        Node rawData = rawDatas.item(j);
                        switch (rawData.getNodeName()) {
                        case MIStrings.SYMBOL_NAME:
                            probeEvent.setSymbol(rawData.getTextContent());
                            break;
                        case MIStrings.ADDRESS:
                            probeEvent.setAddress(rawData.getTextContent());
                            break;
                        case MIStrings.OFFSET:
                            probeEvent.setOffset(rawData.getTextContent());
                            break;
                        default:
                            break;
                        }
                    }
                }

                // Syscalls does not have name.
                // Let put one to make sure this is user friendly via UI
                if (eventInfo.getEventType().equals(TraceEventType.SYSCALL)) {
                    eventInfo.setName(TraceEventType.SYSCALL.getInName());
                }

                // Add the event
                events.add(eventInfo);
            }
        }
    }

    /**
     * @param fieldsList
     *            a list of xml event_field element
     * @param fields
     *            a list of field generated by xml parsing
     * @throws ExecutionException
     *             when parsing fail or required elements are missing
     */
    private static void getFieldInfo(NodeList fieldsList, List<IFieldInfo> fields) throws ExecutionException {
        IFieldInfo fieldInfo = null;
        for (int i = 0; i < fieldsList.getLength(); i++) {
            Node field = fieldsList.item(i);
            if (field.getNodeName().equalsIgnoreCase(MIStrings.EVENT_FIELD)) {
                // Get name
                Node name = getFirstOf(field.getChildNodes(), MIStrings.NAME);
                if (name == null) {
                    throw new ExecutionException(Messages.TraceControl_MiMissingRequiredError);
                }
                fieldInfo = new FieldInfo(name.getTextContent());

                // Populate the field information
                NodeList infos = field.getChildNodes();
                for (int j = 0; j < infos.getLength(); j++) {
                    Node info = infos.item(j);
                    switch (info.getNodeName()) {
                    case MIStrings.TYPE:
                        fieldInfo.setFieldType(info.getTextContent());
                        break;
                    default:
                        break;
                    }
                }
                fields.add(fieldInfo);
            }
        }
    }

    /**
     * Retrieve the fist instance of a given node with tag name equal to tagName
     * parameter
     *
     * @param nodeList
     *            the list of Node to search against
     * @param tagName
     *            the tag name of the desired node
     * @return the first occurrence of a node with a tag name equals to tagName
     */
    private static Node getFirstOf(NodeList nodeList, String tagName) {
        Node node = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeName() == tagName) {
                node = nodeList.item(i);
                break;
            }
        }
        return node;
    }

}
