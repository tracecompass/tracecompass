/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.xml.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.importexport.ManifestReader;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.importexport.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads a profiles file from an input stream
 *
 * @author Marc-Andre Laperle
 */
public class RemoteImportProfilesReader {

    private static final String SCHEMA_FOLDER_NAME = "schema"; //$NON-NLS-1$
    private static final String PROFILES_SCHEMA_FILE_NAME = "remote-profile.xsd"; //$NON-NLS-1$
    private static final @NonNull TracePackageElement @NonNull [] EMPTY_ARRAY =
            new @NonNull TracePackageElement[0];

    private RemoteImportProfilesReader() {
        // Do nothing, private constructor
    }

    /**
     * Validate the content of the profiles file from an input stream
     *
     * @param input
     *            the input stream to validate from
     * @throws IOException
     *             on error
     * @throws SAXException
     *             on error
     */
    public static void validate(InputStream input) throws IOException, SAXException {
        URL schemaFileUrl = FileLocator.find(
                Activator.getDefault().getBundle(),
                new Path(SCHEMA_FOLDER_NAME).append(PROFILES_SCHEMA_FILE_NAME),
                null);
        if (schemaFileUrl == null) {
            throw new IOException(
                    MessageFormat.format(
                            Messages.TracePackageExtractManifestOperation_SchemaFileNotFound,
                            PROFILES_SCHEMA_FILE_NAME));
        }

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(
                    schemaFileUrl.openStream()));
            XmlUtils.safeValidate(schema, new StreamSource(input));
        } catch (SAXException e) {
            throw new SAXException(
                    Messages.TracePackageExtractManifestOperation_ErrorManifestNotValid,
                    e);
        } catch (IOException e) {
            throw new IOException(
                    Messages.TracePackageExtractManifestOperation_ErrorManifestNotValid,
                    e);
        }
    }

    /**
     * Load profile model elements the profiles file (input stream).
     *
     * The file format looks like this:
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?>
     * &lt;profiles>
     *   &lt;version>0.1&lt;/version>
     *   &lt;profile name="myProfile">
     *     &lt;node name="myhost">
     *       &lt;uri>ssh://user@127.0.0.1:22&lt;/uri>
     *       &lt;traceGroup root="/home/user/lttng-traces/" recursive="true">
     *         &lt;trace name="" type="org.eclipse.linuxtools.tmf.ui.type.ctf">
     *           &lt;file name=".*" />
     *         &lt;/trace>
     *       &lt;/traceGroup>
     *     &lt;/node>
     *   &lt;/profile>
     * &lt;/profiles>
     * </pre>
     * See schema/remote-profile.xsd for details.
     *
     * @param inputStream
     *            the input stream that contains the profiles
     * @return the loaded elements
     * @throws IOException
     *             when an error occurs when parsing
     * @throws SAXException
     *             when an error occurs when parsing
     * @throws ParserConfigurationException
     *             when an error occurs when parsing
     */
    public static TracePackageElement[] loadElementsFromProfiles(InputStream inputStream)
            throws IOException, SAXException, ParserConfigurationException {

        List<TracePackageElement> packageElements = new ArrayList<>();
        RemoteImportProfileElement profile = null;
        Document doc = XmlUtils.newSafeDocumentBuilderFactory().newDocumentBuilder().parse(
                inputStream);

        NodeList profileNodes = doc.getDocumentElement().getElementsByTagName(
                RemoteImportProfileConstants.PROFILE_ELEMENT);
        for (int i = 0; i < profileNodes.getLength(); i++) {
            Node profileNode = profileNodes.item(i);
            if (profileNode.getNodeType() == Node.ELEMENT_NODE) {
                Element profileElement = (Element) profileNode;
                String traceName = profileElement.getAttribute(RemoteImportProfileConstants.PROFILE_NAME_ATTRIB);
                profile = new RemoteImportProfileElement(null, traceName);

                NodeList nodeNodes = profileElement.getElementsByTagName(RemoteImportProfileConstants.NODE_ELEMENT);
                for (int j = 0; j < nodeNodes.getLength(); j++) {
                    Node nodeNode = nodeNodes.item(j);
                    if (nodeNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element nodeElement = (Element) nodeNode;
                        String nameAttr = nodeElement.getAttribute(RemoteImportProfileConstants.NODE_NAME_ATTRIB);

                        NodeList uriNodes = nodeElement.getElementsByTagName(RemoteImportProfileConstants.NODE_URI_ELEMENT);
                        String uri = ""; //$NON-NLS-1$
                        for (int k = 0; k < uriNodes.getLength(); k++) {
                            Node uriNode = uriNodes.item(k);
                            if (uriNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element uriElement = (Element) uriNode;
                                uri = uriElement.getFirstChild().getNodeValue();
                                break;
                            }
                        }

                        RemoteImportConnectionNodeElement connectionNode = new RemoteImportConnectionNodeElement(profile,
                                nameAttr, uri);

                        NodeList traceGroupNodes = nodeElement.getElementsByTagName(RemoteImportProfileConstants.TRACE_GROUP_ELEMENT);
                        for (int k = 0; k < traceGroupNodes.getLength(); k++) {
                            Node traceGroupNode = traceGroupNodes.item(k);
                            if (traceGroupNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element traceGroupElement = (Element) traceGroupNode;
                                String rootAttr = traceGroupElement.getAttribute(RemoteImportProfileConstants.TRACE_GROUP_ROOT_ATTRIB);
                                String recursiveAttr = traceGroupElement.getAttribute(RemoteImportProfileConstants.TRACE_GROUP_RECURSIVE_ATTRIB);
                                RemoteImportTraceGroupElement traceGroup = new RemoteImportTraceGroupElement(
                                        connectionNode, rootAttr);
                                traceGroup.setRecursive(Boolean.TRUE.toString().equals(
                                        recursiveAttr));
                                TracePackageElement[] e = ManifestReader.loadElementsFromNode(traceGroupElement);
                                for (TracePackageElement a : e) {
                                    traceGroup.addChild(a);
                                }
                            }
                        }

                    }
                }

                packageElements.add(profile);
            }
        }
        return packageElements.toArray(EMPTY_ARRAY);
    }
}
