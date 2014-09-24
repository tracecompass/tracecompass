/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yuriy Vashchuk (yvashchuk@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.filter.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterEventTypeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterOrNode;
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
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
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

        if (treenode instanceof TmfFilterNode) {

            TmfFilterNode node = (TmfFilterNode) treenode;
            element.setAttribute(TmfFilterNode.NAME_ATTR, node.getFilterName());

        } else if (treenode instanceof TmfFilterEventTypeNode) {

            TmfFilterEventTypeNode node = (TmfFilterEventTypeNode) treenode;
            element.setAttribute(TmfFilterEventTypeNode.TYPE_ATTR, node.getEventType());
            element.setAttribute(TmfFilterEventTypeNode.NAME_ATTR, node.getName());

        } else if (treenode instanceof TmfFilterAndNode) {

            TmfFilterAndNode node = (TmfFilterAndNode) treenode;
            element.setAttribute(TmfFilterAndNode.NOT_ATTR, Boolean.toString(node.isNot()));

        } else if (treenode instanceof TmfFilterOrNode) {

            TmfFilterOrNode node = (TmfFilterOrNode) treenode;
            element.setAttribute(TmfFilterOrNode.NOT_ATTR, Boolean.toString(node.isNot()));

        } else if (treenode instanceof TmfFilterContainsNode) {

            TmfFilterContainsNode node = (TmfFilterContainsNode) treenode;
            element.setAttribute(TmfFilterContainsNode.NOT_ATTR, Boolean.toString(node.isNot()));
            element.setAttribute(TmfFilterContainsNode.FIELD_ATTR, node.getField());
            element.setAttribute(TmfFilterContainsNode.VALUE_ATTR, node.getValue());
            element.setAttribute(TmfFilterContainsNode.IGNORECASE_ATTR, Boolean.toString(node.isIgnoreCase()));

        } else if (treenode instanceof TmfFilterEqualsNode) {

            TmfFilterEqualsNode node = (TmfFilterEqualsNode) treenode;
            element.setAttribute(TmfFilterEqualsNode.NOT_ATTR, Boolean.toString(node.isNot()));
            element.setAttribute(TmfFilterEqualsNode.FIELD_ATTR, node.getField());
            element.setAttribute(TmfFilterEqualsNode.VALUE_ATTR, node.getValue());
            element.setAttribute(TmfFilterEqualsNode.IGNORECASE_ATTR, Boolean.toString(node.isIgnoreCase()));

        } else if (treenode instanceof TmfFilterMatchesNode) {

            TmfFilterMatchesNode node = (TmfFilterMatchesNode) treenode;
            element.setAttribute(TmfFilterMatchesNode.NOT_ATTR, Boolean.toString(node.isNot()));
            element.setAttribute(TmfFilterMatchesNode.FIELD_ATTR, node.getField());
            element.setAttribute(TmfFilterMatchesNode.REGEX_ATTR, node.getRegex());

        } else if (treenode instanceof TmfFilterCompareNode) {

            TmfFilterCompareNode node = (TmfFilterCompareNode) treenode;
            element.setAttribute(TmfFilterCompareNode.NOT_ATTR, Boolean.toString(node.isNot()));
            element.setAttribute(TmfFilterCompareNode.FIELD_ATTR, node.getField());
            element.setAttribute(TmfFilterCompareNode.RESULT_ATTR, Integer.toString(node.getResult()));
            element.setAttribute(TmfFilterCompareNode.TYPE_ATTR, node.getType().toString());
            element.setAttribute(TmfFilterCompareNode.VALUE_ATTR, node.getValue());

        }

        parentElement.appendChild(element);

        for (int i = 0; i < treenode.getChildrenCount(); i++) {
            buildXMLTree(document, treenode.getChild(i), element);
        }
    }

    /**
     * Save the tree
     *
     * @param uri The new Filter XML path
     */
    public void saveTree(final String uri) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result =  new StreamResult(new File(uri));
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

}
