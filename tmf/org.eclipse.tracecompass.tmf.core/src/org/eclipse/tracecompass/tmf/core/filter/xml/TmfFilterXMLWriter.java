/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Yuriy Vashchuk (yvashchuk@gmail.com) - Initial API and implementation
 *   Patrick Tasse - Update filter nodes
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.tracecompass.common.core.xml.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfEventFieldAspect;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterWithNot;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterWithValue;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAspectNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTraceTypeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The SAX based XML writer
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 * @author Patrick Tasse
 */
public class TmfFilterXMLWriter {

    private Document document = null;

    /**
     * The XMLParser constructor
     *
     * @param root The tree root
        * @throws ParserConfigurationException if a DocumentBuilder
     *   cannot be created which satisfies the configuration requested.
     */
    public TmfFilterXMLWriter(final ITmfFilterTreeNode root) throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = XmlUtils.newSafeDocumentBuilderFactory();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();

        Element rootElement = document.createElement(root.getNodeName());
        document.appendChild(rootElement);

        for (ITmfFilterTreeNode node : root.getChildren()) {
            buildXMLTree(document, node, rootElement);
        }
    }

    /**
     * The Tree to XML parser
     *
     * @param document The XML document
     * @param treenode The node to write
     * @param parentElement The XML element of the parent
     */
    public static void buildXMLTree(final Document document, final ITmfFilterTreeNode treenode, Element parentElement) {
        Element element = document.createElement(treenode.getNodeName());
        if (treenode instanceof ITmfFilterWithNot && ((ITmfFilterWithNot) treenode).isNot()) {
            element.setAttribute(ITmfFilterWithNot.NOT_ATTRIBUTE, Boolean.TRUE.toString());
        }
        if (treenode instanceof ITmfFilterWithValue) {
            ITmfFilterWithValue node = (ITmfFilterWithValue) treenode;
            element.setAttribute(ITmfFilterWithValue.VALUE_ATTRIBUTE, node.getValue());
        }
        if (treenode instanceof TmfFilterNode) {
            TmfFilterNode node = (TmfFilterNode) treenode;
            element.setAttribute(TmfFilterNode.NAME_ATTR, node.getFilterName());
        } else if (treenode instanceof TmfFilterTraceTypeNode) {

            TmfFilterTraceTypeNode node = (TmfFilterTraceTypeNode) treenode;
            element.setAttribute(TmfFilterTraceTypeNode.TYPE_ATTR, node.getTraceTypeId());
            element.setAttribute(TmfFilterTraceTypeNode.NAME_ATTR, node.getName());

        } else if (treenode instanceof TmfFilterContainsNode) {

            TmfFilterContainsNode node = (TmfFilterContainsNode) treenode;
            setAspectAttributes(element, node);
            element.setAttribute(TmfFilterContainsNode.IGNORECASE_ATTR, Boolean.toString(node.isIgnoreCase()));

        } else if (treenode instanceof TmfFilterEqualsNode) {

            TmfFilterEqualsNode node = (TmfFilterEqualsNode) treenode;
            setAspectAttributes(element, node);
            element.setAttribute(TmfFilterEqualsNode.IGNORECASE_ATTR, Boolean.toString(node.isIgnoreCase()));

        } else if (treenode instanceof TmfFilterMatchesNode) {

            TmfFilterMatchesNode node = (TmfFilterMatchesNode) treenode;
            setAspectAttributes(element, node);
            element.setAttribute(TmfFilterMatchesNode.REGEX_ATTR, node.getRegex());

        } else if (treenode instanceof TmfFilterCompareNode) {

            TmfFilterCompareNode node = (TmfFilterCompareNode) treenode;
            setAspectAttributes(element, node);
            element.setAttribute(TmfFilterCompareNode.RESULT_ATTR, Integer.toString(node.getResult()));
            element.setAttribute(TmfFilterCompareNode.TYPE_ATTR, node.getType().toString());
        }

        parentElement.appendChild(element);

        for (int i = 0; i < treenode.getChildrenCount(); i++) {
            buildXMLTree(document, treenode.getChild(i), element);
        }
    }

    private static void setAspectAttributes(Element element, TmfFilterAspectNode node) {
        if (node.getEventAspect() != null) {
            element.setAttribute(TmfFilterAspectNode.EVENT_ASPECT_ATTR, node.getEventAspect().getName());
            element.setAttribute(TmfFilterAspectNode.TRACE_TYPE_ID_ATTR, node.getTraceTypeId());
            if (node.getEventAspect() instanceof TmfEventFieldAspect) {
                TmfEventFieldAspect aspect = (TmfEventFieldAspect) node.getEventAspect();
                if (aspect.getFieldPath() != null) {
                    element.setAttribute(TmfFilterAspectNode.FIELD_ATTR, aspect.getFieldPath());
                }
            }
        }
    }

    /**
     * Save the tree
     *
     * @param uri The new Filter XML path
     */
    public void saveTree(final String uri) {
        try {
            Transformer transformer = XmlUtils.newSecureTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result =  new StreamResult(new File(uri));
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            Activator.logError("Error to transformer the configuration ", e);  //$NON-NLS-1$
        } catch (TransformerException e) {
            Activator.logError("Got transformer exception ", e);  //$NON-NLS-1$
        }
    }

}
