/*******************************************************************************
 * Copyright (c) 2013 Ericsson
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageOperation;
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
 * An operation that extracts information from the manifest located in an
 * archive
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageExtractManifestOperation extends AbstractTracePackageOperation {

    private static final String SCHEMA_FOLDER_NAME = "schema"; //$NON-NLS-1$
    private static final String EXPORT_MANIFEST_SCHEMA_FILE_NAME = "export-manifest.xsd"; //$NON-NLS-1$

    // Result of reading the manifest
    private TracePackageElement[] fResultElements;

    /**
     * Constructs a new import operation for reading the manifest
     *
     * @param fileName
     *            the output file name
     */
    public TracePackageExtractManifestOperation(String fileName) {
        super(fileName);
    }

    /**
     * Run extract the manifest operation. The status (result) of the operation
     * can be obtained with {@link #getStatus}
     *
     * @param progressMonitor
     *            the progress monitor to use to display progress and receive
     *            requests for cancellation
     */
    @Override
    public void run(IProgressMonitor progressMonitor) {
        TracePackageElement[] elements = null;
        try {
            progressMonitor.worked(1);
            ArchiveFile archiveFile = getSpecifiedArchiveFile();
            progressMonitor.worked(1);
            if (archiveFile == null) {
                setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TracePackageExtractManifestOperation_InvalidFormat));
                return;
            }

            Enumeration<?> entries = archiveFile.entries();

            boolean found = false;
            while (entries.hasMoreElements()) {
                ModalContext.checkCanceled(progressMonitor);

                ArchiveEntry entry = (ArchiveEntry) entries.nextElement();
                IPath p = new Path(entry.getName());
                //Remove project name
                p = p.removeFirstSegments(1);

                if (entry.getName().endsWith(ITracePackageConstants.MANIFEST_FILENAME)) {
                    found = true;
                    InputStream inputStream = archiveFile.getInputStream(entry);
                    validateManifest(inputStream);

                    inputStream = archiveFile.getInputStream(entry);
                    elements = loadElementsFromManifest(inputStream);
                    break;
                }

                progressMonitor.worked(1);
            }

            if (found) {
                setStatus(Status.OK_STATUS);
            }
            else {
                elements = generateElementsFromArchive();
                if (elements.length > 0) {
                    setStatus(Status.OK_STATUS);
                } else {
                    setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.TracePackageExtractManifestOperation_ErrorManifestNotFound, ITracePackageConstants.MANIFEST_FILENAME)));
                }
            }

            fResultElements = elements;

        } catch (InterruptedException e) {
            setStatus(Status.CANCEL_STATUS);
        } catch (Exception e) {
            setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TracePackageExtractManifestOperation_ErrorReadingManifest, e));
        }
    }

    private TracePackageElement[] generateElementsFromArchive() {
        ArchiveFile archiveFile = getSpecifiedArchiveFile();
        Enumeration<?> entries = archiveFile.entries();
        Set<String> traceFileNames = new HashSet<String>();
        while (entries.hasMoreElements()) {
            ArchiveEntry entry = (ArchiveEntry) entries.nextElement();
            String entryName = entry.getName();
            IPath fullArchivePath = new Path(entryName);
            if (!fullArchivePath.hasTrailingSeparator() && fullArchivePath.segmentCount() > 0) {
                traceFileNames.add(fullArchivePath.segment(0));
            }
        }

        List<TracePackageElement> packageElements = new ArrayList<TracePackageElement>();
        for (String traceFileName : traceFileNames) {
            TracePackageTraceElement traceElement = new TracePackageTraceElement(null, traceFileName, null);
            traceElement.setChildren(new TracePackageElement[] { new TracePackageFilesElement(traceElement, traceFileName) });
            packageElements.add(traceElement);
        }

        return packageElements.toArray(new TracePackageElement[] {});
    }

    /**
     * Get the resulting element from extracting the manifest from the archive
     *
     * @return the resulting element
     */
    public TracePackageElement[] getResultElement() {
        return fResultElements;
    }

    private static void validateManifest(InputStream xml) throws IOException
    {
        URL schemaFileUrl = FileLocator.find(Activator.getDefault().getBundle(), new Path(SCHEMA_FOLDER_NAME).append(EXPORT_MANIFEST_SCHEMA_FILE_NAME), null);
        if (schemaFileUrl == null) {
            throw new IOException(MessageFormat.format(Messages.TracePackageExtractManifestOperation_SchemaFileNotFound, EXPORT_MANIFEST_SCHEMA_FILE_NAME));
        }

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(schemaFileUrl.openStream()));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
        } catch (SAXException e) {
            throw new IOException(Messages.TracePackageExtractManifestOperation_ErrorManifestNotValid, e);
        } catch (IOException e) {
            throw new IOException(Messages.TracePackageExtractManifestOperation_ErrorManifestNotValid, e);
        }
    }

    private static TracePackageElement[] loadElementsFromManifest(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        List<TracePackageElement> packageElements = new ArrayList<TracePackageElement>();
        TracePackageElement element = null;
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

        NodeList traceElements = doc.getDocumentElement().getElementsByTagName(ITracePackageConstants.TRACE_ELEMENT);
        for (int i = 0; i < traceElements.getLength(); ++i) {
            Node traceNode = traceElements.item(i);
            if (traceNode.getNodeType() == Node.ELEMENT_NODE) {
                Element traceElement = (Element) traceNode;
                String traceName = traceElement.getAttribute(ITracePackageConstants.TRACE_NAME_ATTRIB);
                String traceType = traceElement.getAttribute(ITracePackageConstants.TRACE_TYPE_ATTRIB);
                element = new TracePackageTraceElement(null, traceName, traceType);

                List<TracePackageElement> children = new ArrayList<TracePackageElement>();
                NodeList fileElements = traceElement.getElementsByTagName(ITracePackageConstants.TRACE_FILE_ELEMENT);
                for (int j = 0; j < fileElements.getLength(); ++j) {
                    Node fileNode = fileElements.item(j);
                    if (fileNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element fileElement = (Element) fileNode;
                        String fileName = fileElement.getAttribute(ITracePackageConstants.TRACE_FILE_NAME_ATTRIB);
                        children.add(new TracePackageFilesElement(element, fileName));
                    }
                }

                TracePackageSupplFilesElement supplFilesElement = new TracePackageSupplFilesElement(element);

                // Supplementary files
                List<TracePackageSupplFileElement> suppFiles = new ArrayList<TracePackageSupplFileElement>();
                NodeList suppFilesElements = traceElement.getElementsByTagName(ITracePackageConstants.SUPPLEMENTARY_FILE_ELEMENT);
                for (int j = 0; j < suppFilesElements.getLength(); ++j) {
                    Node suppFileNode = suppFilesElements.item(j);
                    if (suppFileNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element suppFileElement = (Element) suppFileNode;
                        String fileName = suppFileElement.getAttribute(ITracePackageConstants.SUPPLEMENTARY_FILE_NAME_ATTRIB);
                        TracePackageSupplFileElement supplFile = new TracePackageSupplFileElement(fileName, supplFilesElement);
                        suppFiles.add(supplFile);
                    }
                }

                if (!suppFiles.isEmpty()) {
                    supplFilesElement.setChildren(suppFiles.toArray(new TracePackageElement[] {}));
                    children.add(supplFilesElement);
                }

                // bookmarks
                List<Map<String, String>> bookmarkAttribs = new ArrayList<Map<String, String>>();
                NodeList bookmarksElements = traceElement.getElementsByTagName(ITracePackageConstants.BOOKMARKS_ELEMENT);
                for (int j = 0; j < bookmarksElements.getLength(); ++j) {
                    Node bookmarksNode = bookmarksElements.item(j);
                    if (bookmarksNode.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList bookmarkElements = traceElement.getElementsByTagName(ITracePackageConstants.BOOKMARK_ELEMENT);
                        for (int k = 0; k < bookmarkElements.getLength(); ++k) {
                            Node bookmarkNode = bookmarkElements.item(k);
                            if (bookmarkNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element bookmarkElement = (Element) bookmarkNode;
                                NamedNodeMap attributesMap = bookmarkElement.getAttributes();
                                Map<String, String> attribs = new HashMap<String, String>();
                                for (int l = 0; l < attributesMap.getLength(); ++l) {
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

                element.setChildren(children.toArray(new TracePackageElement[] {}));
                packageElements.add(element);
            }
        }
        return packageElements.toArray(new TracePackageElement[] {});
    }
}
