/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.ITracePackageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageBookmarkElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageSupplFileElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageSupplFilesElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads a manifest from an input stream
 *
 * @author Marc-Andre Laperle
 */
public class ManifestReader {

    private static final String SCHEMA_FOLDER_NAME = "schema"; //$NON-NLS-1$
    private static final String EXPORT_MANIFEST_SCHEMA_FILE_NAME = "export-manifest.xsd"; //$NON-NLS-1$
    private static final TracePackageElement [] EMPTY_ARRAY = new TracePackageElement[0];

    /**
     * Validate the content of a manifest from an input stream
     *
     * @param input the input stream to validate from
     * @throws IOException on error
     */
    public static void validateManifest(InputStream input) throws IOException
    {
        URL schemaFileUrl = FileLocator.find(Activator.getDefault().getBundle(), new Path(SCHEMA_FOLDER_NAME).append(EXPORT_MANIFEST_SCHEMA_FILE_NAME), null);
        if (schemaFileUrl == null) {
            throw new IOException(MessageFormat.format(Messages.TracePackageExtractManifestOperation_SchemaFileNotFound, EXPORT_MANIFEST_SCHEMA_FILE_NAME));
        }

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(schemaFileUrl.openStream()));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(input));
        } catch (SAXException e) {
            throw new IOException(Messages.TracePackageExtractManifestOperation_ErrorManifestNotValid, e);
        } catch (IOException e) {
            throw new IOException(Messages.TracePackageExtractManifestOperation_ErrorManifestNotValid, e);
        }
    }

    /**
     * Load package elements from a manifest (input stream)
     *
     * The manifest looks like this:
     *
     * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
     * <tmf-export>
     *     <trace name="trace2" type="org.eclipse.linuxtools.lttng2.kernel.tracetype">
     *         <file name="Traces/trace2"/> <supplementary-file name=".tracing/trace2/stateHistory.ht"/>
     *         <bookmarks>
     *             <bookmark location="4" message= "15:50:47.314 069 885, channel0_0, sys_recvmsg, fd=16, msg=0x7faada7d1ae0, flags=256" />
     *         </bookmarks>
     *     </trace>
     * </tmf-export>
     *
     * See schema/export-manifest.xsd for details.
     *
     * @param inputStream
     *            the input stream that contains the manifest
     * @return the loaded elements
     * @throws IOException
     *             when an error occurs when parsing
     * @throws SAXException
     *             when an error occurs when parsing
     * @throws ParserConfigurationException
     *             when an error occurs when parsing
     */
    public static TracePackageElement[] loadElementsFromManifest(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {

        List<TracePackageElement> packageElements = new ArrayList<>();
        TracePackageElement element = null;
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

        NodeList traceElements = doc.getDocumentElement().getElementsByTagName(ITracePackageConstants.TRACE_ELEMENT);
        for (int i = 0; i < traceElements.getLength(); i++) {
            Node traceNode = traceElements.item(i);
            if (traceNode.getNodeType() == Node.ELEMENT_NODE) {
                Element traceElement = (Element) traceNode;
                String traceName = traceElement.getAttribute(ITracePackageConstants.TRACE_NAME_ATTRIB);
                String traceType = traceElement.getAttribute(ITracePackageConstants.TRACE_TYPE_ATTRIB);
                element = new TracePackageTraceElement(null, traceName, traceType);

                List<TracePackageElement> children = new ArrayList<>();
                NodeList fileElements = traceElement.getElementsByTagName(ITracePackageConstants.TRACE_FILE_ELEMENT);
                for (int j = 0; j < fileElements.getLength(); j++) {
                    Node fileNode = fileElements.item(j);
                    if (fileNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element fileElement = (Element) fileNode;
                        String fileName = fileElement.getAttribute(ITracePackageConstants.TRACE_FILE_NAME_ATTRIB);
                        children.add(new TracePackageFilesElement(element, fileName));
                    }
                }

                TracePackageSupplFilesElement supplFilesElement = new TracePackageSupplFilesElement(element);

                // Supplementary files
                List<TracePackageSupplFileElement> suppFiles = new ArrayList<>();
                NodeList suppFilesElements = traceElement.getElementsByTagName(ITracePackageConstants.SUPPLEMENTARY_FILE_ELEMENT);
                for (int j = 0; j < suppFilesElements.getLength(); j++) {
                    Node suppFileNode = suppFilesElements.item(j);
                    if (suppFileNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element suppFileElement = (Element) suppFileNode;
                        String fileName = suppFileElement.getAttribute(ITracePackageConstants.SUPPLEMENTARY_FILE_NAME_ATTRIB);
                        TracePackageSupplFileElement supplFile = new TracePackageSupplFileElement(fileName, supplFilesElement);
                        suppFiles.add(supplFile);
                    }
                }

                if (!suppFiles.isEmpty()) {
                    supplFilesElement.setChildren(suppFiles.toArray(EMPTY_ARRAY));
                    children.add(supplFilesElement);
                }

                // bookmarks
                List<Map<String, String>> bookmarkAttribs = new ArrayList<>();
                NodeList bookmarksElements = traceElement.getElementsByTagName(ITracePackageConstants.BOOKMARKS_ELEMENT);
                for (int j = 0; j < bookmarksElements.getLength(); j++) {
                    Node bookmarksNode = bookmarksElements.item(j);
                    if (bookmarksNode.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList bookmarkElements = traceElement.getElementsByTagName(ITracePackageConstants.BOOKMARK_ELEMENT);
                        for (int k = 0; k < bookmarkElements.getLength(); k++) {
                            Node bookmarkNode = bookmarkElements.item(k);
                            if (bookmarkNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element bookmarkElement = (Element) bookmarkNode;
                                NamedNodeMap attributesMap = bookmarkElement.getAttributes();
                                Map<String, String> attribs = new HashMap<>();
                                for (int l = 0; l < attributesMap.getLength(); l++) {
                                    Node item = attributesMap.item(l);
                                    attribs.put(item.getNodeName(), item.getNodeValue());
                                }
                                bookmarkAttribs.add(attribs);
                            }
                        }
                    }
                }
                if (!bookmarkAttribs.isEmpty()) {
                    children.add(new TracePackageBookmarkElement(element, bookmarkAttribs));
                }

                element.setChildren(children.toArray(EMPTY_ARRAY));
                packageElements.add(element);
            }
        }
        return packageElements.toArray(EMPTY_ARRAY);
    }

}
