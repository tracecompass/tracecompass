/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yuriy Vashchuk (yvashchuk@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.xml;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * The SAX based XML parser
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 * @author Patrick Tasse
 */
public class TmfFilterXMLParser {

    private static ITmfFilterTreeNode fRoot = null;

    /**
     * The XMLParser constructor
     *
     * @param uri The XML file to parse
     * @throws SAXException  SAX exception
     * @throws IOException  IO exception
     */
    public TmfFilterXMLParser(final String uri) throws SAXException, IOException {

        SAXParserFactory m_parserFactory = null;
        m_parserFactory = SAXParserFactory.newInstance();
        m_parserFactory.setNamespaceAware(true);

        XMLReader saxReader = null;
        try {

            saxReader = m_parserFactory.newSAXParser().getXMLReader();
            saxReader.setContentHandler(new TmfFilterContentHandler());
            saxReader.parse(uri);

            fRoot = ((TmfFilterContentHandler) saxReader.getContentHandler()).getTree();

        } catch (ParserConfigurationException e) {
            Activator.logError("Error to parse the configuration ", e);  //$NON-NLS-1$
        }
    }

    /**
     * Getter of tree
     *
     * @return The builded tree
     */
    public ITmfFilterTreeNode getTree() {
        return fRoot;
    }
}
