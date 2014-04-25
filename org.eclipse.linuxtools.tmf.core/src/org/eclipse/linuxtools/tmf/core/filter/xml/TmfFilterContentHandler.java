/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yuriy Vashchuk (yvashchuk@gmail.com) - Initial API and implementation
 *       based on http://smeric.developpez.com/java/cours/xml/sax/
 *   Patrick Tasse - Refactoring
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.filter.xml;

import java.util.Stack;

import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterEventTypeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterCompareNode.Type;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The SAX Content Handler
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 * @author Patrick Tasse
 */
public class TmfFilterContentHandler extends DefaultHandler {

    private ITmfFilterTreeNode fRoot = null;
    private Stack<ITmfFilterTreeNode> fFilterTreeStack = null;

    /**
     * The default constructor
     */
    public TmfFilterContentHandler() {
        super();
        fFilterTreeStack = new Stack<>();
    }

    /**
     * Getter of tree
     *
     * @return The builded tree
     */
    public ITmfFilterTreeNode getTree() {
        return fRoot;
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        ITmfFilterTreeNode node = null;

        if (localName.equalsIgnoreCase(TmfFilterRootNode.NODE_NAME)) {

            node = new TmfFilterRootNode();

        } else if (localName.equals(TmfFilterNode.NODE_NAME)) {

            node = new TmfFilterNode(atts.getValue(TmfFilterNode.NAME_ATTR));

        } else if (localName.equals(TmfFilterEventTypeNode.NODE_NAME)) {

            node = new TmfFilterEventTypeNode(null);
            ((TmfFilterEventTypeNode) node).setEventType(atts.getValue(TmfFilterEventTypeNode.TYPE_ATTR));
            ((TmfFilterEventTypeNode) node).setName(atts.getValue(TmfFilterEventTypeNode.NAME_ATTR));

        } else if (localName.equals(TmfFilterAndNode.NODE_NAME)) {

            node = new TmfFilterAndNode(null);
            String value = atts.getValue(TmfFilterAndNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterAndNode) node).setNot(true);
            }

        } else if (localName.equals(TmfFilterOrNode.NODE_NAME)) {

            node = new TmfFilterOrNode(null);
            String value = atts.getValue(TmfFilterOrNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterOrNode) node).setNot(true);
            }

        } else if (localName.equals(TmfFilterContainsNode.NODE_NAME)) {

            node = new TmfFilterContainsNode(null);
            String value = atts.getValue(TmfFilterContainsNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterContainsNode) node).setNot(true);
            }
            ((TmfFilterContainsNode) node).setField(atts.getValue(TmfFilterContainsNode.FIELD_ATTR));
            ((TmfFilterContainsNode) node).setValue(atts.getValue(TmfFilterContainsNode.VALUE_ATTR));
            value = atts.getValue(TmfFilterContainsNode.IGNORECASE_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterContainsNode) node).setIgnoreCase(true);
            }

        } else if (localName.equals(TmfFilterEqualsNode.NODE_NAME)) {

            node = new TmfFilterEqualsNode(null);
            String value = atts.getValue(TmfFilterEqualsNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterEqualsNode) node).setNot(true);
            }
            ((TmfFilterEqualsNode) node).setField(atts.getValue(TmfFilterEqualsNode.FIELD_ATTR));
            ((TmfFilterEqualsNode) node).setValue(atts.getValue(TmfFilterEqualsNode.VALUE_ATTR));
            value = atts.getValue(TmfFilterEqualsNode.IGNORECASE_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterEqualsNode) node).setIgnoreCase(true);
            }

        } else if (localName.equals(TmfFilterMatchesNode.NODE_NAME)) {

            node = new TmfFilterMatchesNode(null);
            String value = atts.getValue(TmfFilterMatchesNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterMatchesNode) node).setNot(true);
            }
            ((TmfFilterMatchesNode) node).setField(atts.getValue(TmfFilterMatchesNode.FIELD_ATTR));
            ((TmfFilterMatchesNode) node).setRegex(atts.getValue(TmfFilterMatchesNode.REGEX_ATTR));

        } else if (localName.equals(TmfFilterCompareNode.NODE_NAME)) {

            node = new TmfFilterCompareNode(null);
            String value = atts.getValue(TmfFilterCompareNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterCompareNode) node).setNot(true);
            }
            ((TmfFilterCompareNode) node).setField(atts.getValue(TmfFilterCompareNode.FIELD_ATTR));
            value = atts.getValue(TmfFilterCompareNode.TYPE_ATTR);
            if (value != null) {
                ((TmfFilterCompareNode) node).setType(Type.valueOf(value));
            }
            value = atts.getValue(TmfFilterCompareNode.RESULT_ATTR);
            if (value != null) {
                if (value.equals(Integer.toString(-1))) {
                    ((TmfFilterCompareNode) node).setResult(-1);
                } else if (value.equals(Integer.toString(1))) {
                    ((TmfFilterCompareNode) node).setResult(1);
                } else {
                    ((TmfFilterCompareNode) node).setResult(0);
                }
            }
            ((TmfFilterCompareNode) node).setValue(atts.getValue(TmfFilterCompareNode.VALUE_ATTR));

        }

        fFilterTreeStack.push(node);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        ITmfFilterTreeNode node = fFilterTreeStack.pop();

        if (fFilterTreeStack.isEmpty()) {
            fRoot = node;
        } else if (fFilterTreeStack.lastElement() instanceof TmfFilterTreeNode &&
                node instanceof TmfFilterTreeNode) {
            fFilterTreeStack.lastElement().addChild(node);
        }

    }

}
