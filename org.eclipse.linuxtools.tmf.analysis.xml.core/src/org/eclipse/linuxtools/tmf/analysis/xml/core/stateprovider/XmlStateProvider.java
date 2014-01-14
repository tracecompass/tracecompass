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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.Messages;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
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

    private final IPath fFilePath;
    private final String fStateId;

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
        /* TODO: This method will be filled a few patches later */
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
    protected Node loadXMLNode() {

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
     * Function to load the XML file structure
     */
    protected void loadXML() {
        Element doc = (Element) loadXMLNode();
        if (doc == null) {
            return;
        }

        /* TODO: This method will be filled a few patches later */
    }

}