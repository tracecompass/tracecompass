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

import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.tracecompass.common.core.xml.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.ITracePackageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Writes profiles to XML format.
 *
 * @author Marc-Andre Laperle
 */
public class RemoteImportProfilesWriter {

    /**
     * Write the profiles to XML format.
     *
     * @param profiles
     *            the profile elements to write to XML
     *
     * @return the generated XML
     * @throws ParserConfigurationException
     *             when an error occurs when parsing
     * @throws TransformerException
     *             when an error occurs when transforming the XML
     */
    public static String writeProfilesToXML(TracePackageElement[] profiles)
            throws ParserConfigurationException, TransformerException {

        Document doc = XmlUtils.newSafeDocumentBuilderFactory().newDocumentBuilder().newDocument();
        Element profilesElement = doc.createElement(RemoteImportProfileConstants.PROFILES_ELEMENT);
        doc.appendChild(profilesElement);
        Element versionElement = doc.createElement(RemoteImportProfileConstants.VERSION_ELEMENT);
        versionElement.setTextContent(RemoteImportProfileConstants.VERSION);
        profilesElement.appendChild(versionElement);

        for (TracePackageElement profile : profiles) {
            if (profile instanceof RemoteImportProfileElement) {
                exportProfile(profilesElement,
                        (RemoteImportProfileElement) profile);
            }
        }

        Transformer transformer = XmlUtils.newSecureTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        DOMSource source = new DOMSource(doc);
        StringWriter buffer = new StringWriter();
        StreamResult result = new StreamResult(buffer);
        transformer.transform(source, result);
        String content = buffer.getBuffer().toString();

        return content;
    }

    private static void exportProfile(Node profilesNode,
            RemoteImportProfileElement profile) {
        Element profileElement = profilesNode.getOwnerDocument().createElement(
                RemoteImportProfileConstants.PROFILE_ELEMENT);
        profileElement.setAttribute(
                RemoteImportProfileConstants.PROFILE_NAME_ATTRIB,
                profile.getProfileName());

        for (TracePackageElement connectionNode : profile.getChildren()) {
            if (connectionNode instanceof RemoteImportConnectionNodeElement) {
                exportConnectionNode(profileElement,
                        (RemoteImportConnectionNodeElement) connectionNode);
            }
        }
        profilesNode.appendChild(profileElement);
    }

    private static void exportConnectionNode(Node profileNode,
            RemoteImportConnectionNodeElement connectionNode) {
        Element nodeElement = profileNode.getOwnerDocument().createElement(
                RemoteImportProfileConstants.NODE_ELEMENT);
        nodeElement.setAttribute(RemoteImportProfileConstants.NODE_NAME_ATTRIB,
                connectionNode.getName());
        profileNode.appendChild(nodeElement);
        Element uriElement = profileNode.getOwnerDocument().createElement(
                RemoteImportProfileConstants.NODE_URI_ELEMENT);
        uriElement.setTextContent(connectionNode.getURI());
        nodeElement.appendChild(uriElement);

        for (TracePackageElement traceGroup : connectionNode.getChildren()) {
            if (traceGroup instanceof RemoteImportTraceGroupElement) {
                exportTraceGroup(nodeElement,
                        (RemoteImportTraceGroupElement) traceGroup);
            }
        }
    }

    private static void exportTraceGroup(Node nodeNode,
            RemoteImportTraceGroupElement traceGroup) {
        Element traceGroupElement = nodeNode.getOwnerDocument().createElement(
                RemoteImportProfileConstants.TRACE_GROUP_ELEMENT);
        traceGroupElement.setAttribute(
                RemoteImportProfileConstants.TRACE_GROUP_ROOT_ATTRIB,
                traceGroup.getRootImportPath());
        traceGroupElement.setAttribute(
                RemoteImportProfileConstants.TRACE_GROUP_RECURSIVE_ATTRIB,
                Boolean.toString(traceGroup.isRecursive()));
        for (TracePackageElement trace : traceGroup.getChildren()) {
            if (trace instanceof TracePackageTraceElement) {
                exportTrace(traceGroupElement, (TracePackageTraceElement) trace);
            }
        }
        nodeNode.appendChild(traceGroupElement);
    }

    private static void exportTrace(Node traceGroupNode,
            TracePackageTraceElement trace) {
        Element traceElement = traceGroupNode.getOwnerDocument().createElement(
                ITracePackageConstants.TRACE_ELEMENT);
        traceElement.setAttribute(ITracePackageConstants.TRACE_NAME_ATTRIB,
                trace.getImportName());
        traceElement.setAttribute(ITracePackageConstants.TRACE_TYPE_ATTRIB,
                trace.getTraceType());
        for (TracePackageElement files : trace.getChildren()) {
            if (files instanceof TracePackageFilesElement) {
                exportTraceFiles(traceElement, (TracePackageFilesElement) files);
                break;
            }
        }
        traceGroupNode.appendChild(traceElement);
    }

    private static void exportTraceFiles(Node traceNode,
            TracePackageFilesElement files) {
        Element fileElement = traceNode.getOwnerDocument().createElement(
                ITracePackageConstants.TRACE_FILE_ELEMENT);
        fileElement.setAttribute(ITracePackageConstants.TRACE_FILE_NAME_ATTRIB,
                files.getFileName());
        traceNode.appendChild(fileElement);
    }
}
