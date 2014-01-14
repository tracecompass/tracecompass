/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.module;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to store and interpret the header information of XML-defined components
 *
 * @author Geneviève Bastien
 */
public class XmlHeadInfo {

    private final Node fHeadInfo;

    /**
     * Constructor
     *
     * @param item
     *            The XML node corresponding to this header
     */
    public XmlHeadInfo(Node item) {
        fHeadInfo = item;
    }

    /**
     * Get a list of child elements with the requested name
     *
     * @param nodeName
     *            The name of the nodes to get
     * @return List of child elements
     */
    public List<Element> getElements(String nodeName) {
        List<Element> list = new ArrayList<>();
        NodeList nodes = fHeadInfo.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (element.getNodeName().equals(nodeName)) {
                    list.add(element);
                }
            }
        }
        return list;
    }

    /**
     * Check whether, if this header information has trace types defined, this
     * component applies to a given trace type.
     *
     * @param traceClass
     *            The trace type to check for
     * @return True if there is no trace type information in header or if the
     *         trace type applies
     */
    public boolean checkTraceType(Class<? extends ITmfTrace> traceClass) {
        /*
         * By default this returns true, child implementation who have access to
         * trace types will override this
         *
         * TODO: actually check the trace type here when trace types are moved
         * to o.e.l.tmf.core instead of o.e.l.tmf.ui
         */
        return true;
    }

    /**
     * Get the name of this component from the header information
     *
     * @return The name of the component
     */
    public String getName() {
        List<Element> elements = getElements(TmfXmlStrings.LABEL);
        if (elements.isEmpty()) {
            return null;
        }

        Element element = elements.get(0);
        return element.getAttribute(TmfXmlStrings.VALUE);
    }

}
