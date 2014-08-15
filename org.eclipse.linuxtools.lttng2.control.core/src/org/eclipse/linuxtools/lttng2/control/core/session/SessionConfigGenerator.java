/**********************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *********************************************************************/
package org.eclipse.linuxtools.lttng2.control.core.session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.lttng2.control.core.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Class for generating a session configuration file. A session configuration is
 * used to configure a trace session. It is a XML formatted file that contains
 * values defining the behavior of that specific trace session.
 * <p>
 * Kernel session configuration example:
 *
 * <pre>
 * {@code
 * <sessions>
 *     <session>
 *         <name>test_kernel</name>
 *         <domains>
 *             <domain>
 *                 <type>KERNEL</type>
 *                 <buffer_type>GLOBAL</buffer_type>
 *                 <channels>
 *                     <channel>
 *                         <name>channel0</name>
 *                         <enabled>false</enabled>
 *                         <overwrite_mode>DISCARD</overwrite_mode>
 *                         <subbuffer_size>262144</subbuffer_size>
 *                         <subbuffer_count>4</subbuffer_count>
 *                         <switch_timer_interval>0</switch_timer_interval>
 *                         <read_timer_interval>200000</read_timer_interval>
 *                         <output_type>SPLICE</output_type>
 *                         <tracefile_size>0</tracefile_size>
 *                         <tracefile_count>0</tracefile_count>
 *                         <live_timer_interval>0</live_timer_interval>
 *                         <events>
 *                             <event>
 *                                 <enabled>true</enabled>
 *                                 <type>SYSCALL</type>
 *                             </event>
 *                             <event>
 *                                 <name>snd_soc_cache_sync</name>
 *                                 <enabled>true</enabled>
 *                                 <type>TRACEPOINT</type>
 *                             </event>
 *                         </events>
 *                     </channel>
 *                 </channels>
 *             </domain>
 *         </domains>
 *         <started>false</started>
 *         <output>
 *             <consumer_output>
 *                 <enabled>true</enabled>
 *                 <destination>
 *                     <path>/home/user/lttng-traces/test_kernel</path>
 *                 </destination>
 *             </consumer_output>
 *         </output>
 *     </session>
 * </sessions>
 * }
 * </pre>
 *
 * </p>
 *
 * @author Guilliano Molaire
 * @since 3.0
 */
public final class SessionConfigGenerator {

    /** The name of the session schema */
    private static final String SESSION_XSD_FILENAME = "session.xsd"; //$NON-NLS-1$

    /** The indent size used for the session configuration XML file */
    private static final String INDENT_AMOUNT_PROPERTY_NAME = "{http://xml.apache.org/xslt}indent-amount"; //$NON-NLS-1$
    private static final String INDENT_AMOUNT_PROPERTY_VALUE = "4"; //$NON-NLS-1$

    /**
     * Private constructor. The class should not be instantiated.
     */
    private SessionConfigGenerator() {
    }

    // ---------------------------------------------------------
    // Methods to generate session configuration files
    // ---------------------------------------------------------

    /**
     * Generates a session configuration file from a set of session information.
     *
     * @param sessions
     *            The session informations
     * @param sessionFileDestination
     *            The path of the locally saved session configuration file
     * @return The status of the session configuration generation
     */
    public static IStatus generateSessionConfig(Set<ISessionInfo> sessions, IPath sessionFileDestination) {
        /* Parameters validation */
        if (sessions == null || sessions.isEmpty()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SessionConfigXML_InvalidSessionInfoList);
        } else if (sessionFileDestination == null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SessionConfigXML_InvalidTraceSessionPath);
        }

        /* Generate the session configuration file */
        try {
            Document sessionConfigDocument = generateSessionConfig(sessions);

            if (sessionConfigDocument != null) {
                saveSessionConfig(sessionConfigDocument, sessionFileDestination.toString());
            } else {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SessionConfigXML_SessionConfigGenerationError);
            }
        } catch (TransformerException | IllegalArgumentException | ParserConfigurationException e) {
            Activator.getDefault().logError("Error generating the session configuration file: " + sessionFileDestination.toString(), e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
        }

        return Status.OK_STATUS;
    }

    /**
     * Generates a session configuration from a set of session informations.
     *
     * @param sessions
     *            The session informations
     * @return The document with all session configuration nodes
     * @throws IllegalArgumentException
     *             On an illegal argument inside sessions
     * @throws ParserConfigurationException
     *             On an parser configuration error
     */
    private static Document generateSessionConfig(Iterable<ISessionInfo> sessions) throws IllegalArgumentException, ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document document = docBuilder.newDocument();

        Element rootElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_SESSIONS);
        document.appendChild(rootElement);

        for (ISessionInfo session : sessions) {
            /* All elements under "sessions" elements */
            Element sessionElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_SESSION);

            /* Contents of session element */
            String enabled = session.getSessionState().equals(TraceSessionState.ACTIVE) ? SessionConfigStrings.CONFIG_STRING_TRUE : SessionConfigStrings.CONFIG_STRING_FALSE;

            addElementContent(document, sessionElement, SessionConfigStrings.CONFIG_ELEMENT_NAME, session.getName());
            addElementContent(document, sessionElement, SessionConfigStrings.CONFIG_ELEMENT_STARTED, enabled);

            if (session.isSnapshotSession()) {
                /* If it's a snapshot, we must add an attribute telling it is */
                Element attributesElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_ATTRIBUTES);
                addElementContent(document, attributesElement, SessionConfigStrings.CONFIG_ELEMENT_SNAPSHOT_MODE, SessionConfigStrings.CONFIG_STRING_TRUE);
                sessionElement.appendChild(attributesElement);
            }

            sessionElement.appendChild(getDomainsElement(document, session));
            sessionElement.appendChild(getOutputElement(document, session));
            rootElement.appendChild(sessionElement);
        }

        return document;
    }

    // ---------------------------------------------------------
    // Getters for each element of the configuration file
    // ---------------------------------------------------------

    /**
     * Gets the 'domains' element after creating it.
     *
     * @param document
     *            The document in which the nodes are being added
     * @param session
     *            The session informations
     * @return The domains element as an XML element
     */
    private static Element getDomainsElement(Document document, ISessionInfo session) {
        Element domainsElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_DOMAINS);

        for (IDomainInfo domain : session.getDomains()) {
            Element domainElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_DOMAIN);

            /*
             * Add everything specific to a domain
             *
             * TODO: We suppose here that domain is either kernel or UST. It
             * will have to change if other domains are supported
             */
            String domainType = domain.isKernel() ? SessionConfigStrings.CONFIG_DOMAIN_TYPE_KERNEL : SessionConfigStrings.CONFIG_DOMAIN_TYPE_UST;
            addElementContent(document, domainElement, SessionConfigStrings.CONFIG_ELEMENT_TYPE, domainType);

            String bufferType = null;
            switch (domain.getBufferType()) {
            case BUFFER_PER_UID:
                bufferType = SessionConfigStrings.CONFIG_BUFFER_TYPE_PER_UID;
                break;
            case BUFFER_PER_PID:
                bufferType = SessionConfigStrings.CONFIG_BUFFER_TYPE_PER_PID;
                break;
            case BUFFER_SHARED:
                bufferType = SessionConfigStrings.CONFIG_BUFFER_TYPE_GLOBAL;
                break;
            case BUFFER_TYPE_UNKNOWN:
            default:
                throw new IllegalArgumentException(Messages.SessionConfigXML_UnknownDomainBufferType);
            }
            addElementContent(document, domainElement, SessionConfigStrings.CONFIG_ELEMENT_DOMAIN_BUFFER_TYPE, bufferType);

            /* Add the channels */
            domainElement.appendChild(getChannelsElement(document, domain.isKernel(), domain.getChannels()));
            domainsElement.appendChild(domainElement);
        }

        return domainsElement;
    }

    /**
     * Gets the 'output' element after creating it. If the session is a
     * snapshot, it will be composed of a snapshot outputs element. Otherwise,
     * it will contain the consumer output element.
     *
     * @param document
     *            The document in which the nodes are being added
     * @param session
     *            The session informations
     * @return The output element as an XML node
     */
    private static Element getOutputElement(Document document, ISessionInfo session) {
        Element outputElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_OUTPUT);

        if (session.isSnapshotSession()) {
            outputElement.appendChild(getSnapshotOuputsElement(document, session));
        } else if (session.isStreamedTrace()) {
            outputElement.appendChild(getNetOutputElement(document, session));
        } else {
            outputElement.appendChild(getConsumerOutputElement(document, session));
        }

        return outputElement;
    }

    /**
     * Gets the 'channels' element after creating it.
     *
     * @param document
     *            The document in which the nodes are being added
     * @param isKernel
     *            Is it a kernel domain type
     * @param channels
     *            The channels to be added as elements
     * @return The channels element as an XML element
     */
    private static Element getChannelsElement(Document document, boolean isKernel, IChannelInfo[] channels) {
        Element channelsElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_CHANNELS);

        for (IChannelInfo channel : channels) {
            Element channelElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_CHANNEL);

            /* Add everything related to a channel */
            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_NAME, channel.getName());

            String overwriteMode = channel.isOverwriteMode() ? SessionConfigStrings.CONFIG_OVERWRITE_MODE_OVERWRITE : SessionConfigStrings.CONFIG_OVERWRITE_MODE_DISCARD;
            String enabled = channel.getState().equals(TraceEnablement.ENABLED) ? SessionConfigStrings.CONFIG_STRING_TRUE : SessionConfigStrings.CONFIG_STRING_FALSE;

            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_ENABLED, enabled);
            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_OVERWRITE_MODE, overwriteMode);
            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_SUBBUFFER_SIZE, channel.getSubBufferSize());
            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_SUBBUFFER_COUNT, channel.getNumberOfSubBuffers());
            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_SWITCH_TIMER_INTERVAL, channel.getSwitchTimer());
            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_READ_TIMER_INTERVAL, channel.getReadTimer());

            String outputType = channel.getOutputType().getInName().startsWith(SessionConfigStrings.CONFIG_OUTPUT_TYPE_MMAP) ?
                    outputType = SessionConfigStrings.CONFIG_OUTPUT_TYPE_MMAP : SessionConfigStrings.CONFIG_OUTPUT_TYPE_SPLICE;
            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_OUTPUT_TYPE, outputType);
            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_TRACEFILE_SIZE, channel.getMaxSizeTraceFiles());
            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_TRACEFILE_COUNT, channel.getMaxNumberTraceFiles());

            /*
             * TODO: Replace the 0 value by the channel live timer property from
             * SessionInfo once live session tracing is supported
             */
            addElementContent(document, channelElement, SessionConfigStrings.CONFIG_ELEMENT_LIVE_TIMER_INTERVAL, SessionConfigStrings.CONFIG_STRING_ZERO);

            /* Add the events */
            channelElement.appendChild(getEventsElement(document, isKernel, channel.getEvents()));
            channelsElement.appendChild(channelElement);
        }

        return channelsElement;
    }

    /**
     * Gets the 'events' element after creating it. It is composed of the event
     * informations from a list of IEventInfo.
     *
     * @param document
     *            The document in which the nodes are being added
     * @param isKernel
     *            Is the domain type kernel
     * @param events
     *            The event informations to be added
     * @return An element containing all the event informations as XML elements
     */
    private static Element getEventsElement(Document document, boolean isKernel, IEventInfo[] events) {
        Element eventsElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_EVENTS);

        for (IEventInfo event : events) {
            Element eventElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_EVENT);

            /* Enabled attribute */
            String enabled = event.getState().equals(TraceEnablement.ENABLED) ? SessionConfigStrings.CONFIG_STRING_TRUE : SessionConfigStrings.CONFIG_STRING_FALSE;

            /* Add the attributes to the event node */
            addElementContent(document, eventElement, SessionConfigStrings.CONFIG_ELEMENT_NAME, event.getName());
            addElementContent(document, eventElement, SessionConfigStrings.CONFIG_ELEMENT_ENABLED, enabled);
            TraceEventType eventType = event.getEventType();
            if (!eventType.equals(TraceEventType.UNKNOWN)) {
                addElementContent(document, eventElement, SessionConfigStrings.CONFIG_ELEMENT_TYPE, eventType.getInName().toUpperCase());
            } else {
                throw new IllegalArgumentException(Messages.SessionConfigXML_UnknownEventType);
            }

            /* Specific to UST session config: the log level */
            if (!isKernel && !event.getLogLevel().equals(TraceLogLevel.LEVEL_UNKNOWN)) {
                addElementContent(document, eventElement, SessionConfigStrings.CONFIG_ELEMENT_LOGLEVEL, event.getLogLevel().ordinal());
            }

            /* Add the node to the parent node events */
            eventsElement.appendChild(eventElement);
        }

        return eventsElement;
    }

    /**
     * Gets the 'consumer_output' element after creating it.
     *
     * @param document
     *            The document in which the nodes are being added
     * @param session
     *            The session informations
     * @return The consumer output element with his informations as XML elements
     */
    private static Element getConsumerOutputElement(Document document, ISessionInfo session) {
        Element consumerOutputElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_CONSUMER_OUTPUT);
        Element destinationElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_DESTINATION);

        /* Value of consumer output element */
        addElementContent(document, consumerOutputElement, SessionConfigStrings.CONFIG_ELEMENT_ENABLED, SessionConfigStrings.CONFIG_STRING_TRUE);

        if (session.isStreamedTrace()) {
            /* If it is a streamed session, add the net output element */
            destinationElement.appendChild(getNetOutputElement(document, session));
        } else {
            addElementContent(document, destinationElement, SessionConfigStrings.CONFIG_ELEMENT_PATH, session.getSessionPath());
        }

        consumerOutputElement.appendChild(destinationElement);
        return consumerOutputElement;
    }

    /**
     * Gets the 'net_output' element after creating it. It is composed of the
     * control and data URIs.
     *
     * @param document
     *            The document in which the nodes are being added
     * @param session
     *            The session informations
     * @return The net output element
     */
    private static Element getNetOutputElement(Document document, ISessionInfo session) {
        Element netOutputElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_NET_OUTPUT);

        String networkUrl = session.getNetworkUrl();
        String controlUri = networkUrl == null ? session.getControlUrl() : networkUrl;
        String dataUri = networkUrl == null ? session.getDataUrl() : networkUrl;
        addElementContent(document, netOutputElement, SessionConfigStrings.CONFIG_ELEMENT_CONTROL_URI, controlUri);
        addElementContent(document, netOutputElement, SessionConfigStrings.CONFIG_ELEMENT_DATA_URI, dataUri);

        return netOutputElement;
    }

    /**
     * Gets the 'snapshot_outputs' element after creating it.
     *
     * @param document
     *            The document in which the nodes are being added
     * @param session
     *            The session informations
     * @return The snapshot outputs element with snapshot informations as XML
     *         elements
     */
    private static Element getSnapshotOuputsElement(Document document, ISessionInfo session) {
        Element snapshotOutputsElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_SNAPSHOT_OUTPUTS);
        Element outputElement = document.createElement(SessionConfigStrings.CONFIG_ELEMENT_OUTPUT);

        /* Add the name of the snapshot and the max size element */
        addElementContent(document, outputElement, SessionConfigStrings.CONFIG_ELEMENT_NAME, session.getSnapshotInfo().getName());

        /*
         * TODO: find the proper max size value of output element. For now it is
         * set to the default 0 value which means unlimited for lttng.
         */
        addElementContent(document, outputElement, SessionConfigStrings.CONFIG_ELEMENT_MAX_SIZE, SessionConfigStrings.CONFIG_STRING_ZERO);
        outputElement.appendChild(getConsumerOutputElement(document, session));

        snapshotOutputsElement.appendChild(outputElement);
        return snapshotOutputsElement;
    }

    // ---------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------

    /**
     * Validates the session configuration file against its schema.
     *
     * @param sessionFile
     *            The session configuration file
     * @return The status of the validation
     */
    public static IStatus sessionValidate(File sessionFile) {
        URL url = SessionConfigGenerator.class.getResource(SESSION_XSD_FILENAME);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source xmlSource = new StreamSource(sessionFile);

        try {
            Schema schema = schemaFactory.newSchema(url);
            Validator validator = schema.newValidator();
            validator.validate(xmlSource);
        } catch (SAXParseException e) {
            String error = NLS.bind(Messages.SessionConfigXML_XmlParseError, e.getLineNumber(), e.getLocalizedMessage());
            Activator.getDefault().logError(error);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        } catch (SAXException e) {
            String error = NLS.bind(Messages.SessionConfigXML_XmlValidationError, e.getLocalizedMessage());
            Activator.getDefault().logError(error);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        } catch (IOException e) {
            String error = Messages.SessionConfigXML_XmlValidateError;
            Activator.getDefault().logError("IO exception occurred", e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        }
        return Status.OK_STATUS;
    }

    /**
     * Saves the session configuration into a XML file.
     *
     * @param document
     *            The document representing the session configuration file
     * @param destination
     *            The path of the locally saved session configuration file
     * @throws TransformerException
     *             On an transformation process
     */
    private static void saveSessionConfig(Document document, String destination) throws TransformerException {
        /* Write the content into a XML file */
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.setOutputProperty(INDENT_AMOUNT_PROPERTY_NAME, INDENT_AMOUNT_PROPERTY_VALUE);

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(destination));

        transformer.transform(source, result);
    }

    /**
     * Adds to a parent node an element with his content.
     *
     * @param document
     *            The document in which the nodes are being added
     * @param parent
     *            The parent node that contains the element and his content
     * @param elementName
     *            The element container name
     * @param elementContent
     *            The content itself
     */
    private static void addElementContent(Document document, Element parent, String elementName, Object elementContent) {
        Element contentElement = document.createElement(elementName);
        contentElement.appendChild(document.createTextNode(elementContent.toString()));
        parent.appendChild(contentElement);
    }
}
