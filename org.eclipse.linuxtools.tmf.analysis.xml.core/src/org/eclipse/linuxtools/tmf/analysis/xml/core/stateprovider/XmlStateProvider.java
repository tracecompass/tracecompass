/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.stateprovider.model.TmfXmlEventHandler;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.stateprovider.model.TmfXmlLocation;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.Messages;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This is the state change input plug-in for TMF's state system which handles
 * the XML Format
 *
 * @author Florian Wininger
 */
public class XmlStateProvider extends AbstractTmfStateProvider {

    /** Root quark, to get values at the root of the state system */
    public static final int ROOT_QUARK = -1;
    /**
     * Error quark, value taken when a state system quark query is in error.
     *
     * FIXME: Originally in the code, the -1 was used for both root quark and
     * return errors, so it has the same value as root quark, but maybe it can
     * be changed to something else -2? A quark can never be negative
     */
    public static final int ERROR_QUARK = -1;

    private final IPath fFilePath;
    private final String fStateId;

    /** List of all Event Handlers */
    private final Set<TmfXmlEventHandler> fEventHandlers = new HashSet<>();

    /** List of all Locations */
    private final Set<TmfXmlLocation> fLocations;

    /** Map for defined values */
    private final Map<String, String> fDefinedValues = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plug-in.
     *
     * @param trace
     *            The trace
     * @param stateid
     *            The state system id, corresponding to the analysis_id
     *            attribute of the state provider element of the XML file
     * @param file
     *            Path to the XML file containing the state provider definition
     */
    public XmlStateProvider(ITmfTrace trace, String stateid, IPath file) {
        super(trace, ITmfEvent.class, stateid);
        fStateId = stateid;
        fFilePath = file;
        Element doc = loadXMLNode();
        if (doc == null) {
            fLocations = new HashSet<>();
            return;
        }

        /* parser for defined Values */
        NodeList definedStateNodes = doc.getElementsByTagName(TmfXmlStrings.DEFINED_VALUE);
        for (int i = 0; i < definedStateNodes.getLength(); i++) {
            Element element = (Element) definedStateNodes.item(i);
            fDefinedValues.put(element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.VALUE));
        }

        /* parser for the locations */
        NodeList locationNodes = doc.getElementsByTagName(TmfXmlStrings.LOCATION);
        Set<TmfXmlLocation> locations = new HashSet<>();
        for (int i = 0; i < locationNodes.getLength(); i++) {
            Element element = (Element) locationNodes.item(i);
            TmfXmlLocation location = new TmfXmlLocation(element, this);
            locations.add(location);
        }
        fLocations = Collections.unmodifiableSet(locations);

        /* parser for the event handlers */
        NodeList nodes = doc.getElementsByTagName(TmfXmlStrings.EVENT_HANDLER);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            TmfXmlEventHandler handler = new TmfXmlEventHandler(element, this);
            fEventHandlers.add(handler);
        }
    }

    /**
     * Get the state id of the state provider
     *
     * @return The state id of the state provider
     */
    public String getStateId() {
        return fStateId;
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        Node ssNode = loadXMLNode();
        if (ssNode instanceof Element) {
            Element element = (Element) ssNode;
            return Integer.valueOf(element.getAttribute(TmfXmlStrings.VERSION));
        }
        /*
         * The version attribute is mandatory and XML files that don't validate
         * with the XSD are ignored, so this should never happen
         */
        throw new IllegalStateException("The state provider XML node should have a version attribute"); //$NON-NLS-1$
    }

    @Override
    public XmlStateProvider getNewInstance() {
        return new XmlStateProvider(this.getTrace(), getStateId(), fFilePath);
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        if (event == null) {
            return;
        }
        for (TmfXmlEventHandler eventHandler : fEventHandlers) {
            eventHandler.handleEvent(event);
        }
    }

    @Override
    public ITmfStateSystemBuilder getAssignedStateSystem() {
        return ss;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Loads the XML file and returns the element at the root of the current
     * state provider.
     *
     * @return The XML node at the root of the state provider
     */
    private Element loadXMLNode() {

        try {
            File XMLFile = fFilePath.toFile();
            if (XMLFile == null || !XMLFile.exists() || !XMLFile.isFile()) {
                return null;
            }

            /* Load the XML File */
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XMLFile);
            doc.getDocumentElement().normalize();

            /* get the state providers and find the corresponding one */
            NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);
            Element stateproviderNode = null;

            for (int i = 0; i < stateproviderNodes.getLength(); i++) {
                Element node = (Element) stateproviderNodes.item(i);
                String analysisid = node.getAttribute(TmfXmlStrings.ANALYSIS_ID);
                if (analysisid.equals(fStateId)) {
                    stateproviderNode = node;
                }
            }

            return stateproviderNode;
        } catch (ParserConfigurationException | IOException e) {
            Activator.logError("Error loading XML file", e); //$NON-NLS-1$
        } catch (SAXException e) {
            Activator.logError(NLS.bind(Messages.XmlUtils_XmlValidationError, e.getLocalizedMessage()), e);
        }

        return null;
    }

    /**
     * Get the list of locations defined in this state provider
     *
     * @return The list of {@link TmfXmlLocation}
     */
    public Iterable<TmfXmlLocation> getLocations() {
        return fLocations;
    }

    /**
     * Get the defined value associated with a constant
     *
     * @param constant
     *            The constant defining this value
     * @return The actual value corresponding to this constant
     */
    public String getDefinedValue(String constant) {
        return fDefinedValues.get(constant);
    }

    /**
     * Get the requested value for an attribute. If the value is a pre-defined
     * value, we return the string corresponding in the defined values map.
     *
     * @param name
     *            the string to get
     * @return the actual string value
     */
    public String getAttributeValue(String name) {
        String attribute = name;
        if (attribute.startsWith(TmfXmlStrings.VARIABLE_PREFIX)) {
            /* search the attribute in the map without the fist character $ */
            attribute = getDefinedValue(attribute.substring(1));
        }
        return attribute;
    }

}