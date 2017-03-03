/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class containing some utilities methods that can be used by analyses
 * extending the XML schema
 *
 * @author Geneviève Bastien
 * @since 2.2
 */
public final class TmfXmlUtils {

    private TmfXmlUtils() {

    }

    /**
     * Get the XML children element of an XML element, but only those of a
     * certain type
     *
     * @param parent
     *            The parent element to get the children from
     * @param elementTag
     *            The tag of the elements to return
     * @return The list of children {@link Element} of the parent
     */
    public static List<@NonNull Element> getChildElements(Element parent, String elementTag) {
        /* get the state providers and find the corresponding one */
        NodeList nodes = parent.getElementsByTagName(elementTag);
        List<@NonNull Element> childElements = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if (node.getParentNode().equals(parent)) {
                childElements.add(node);
            }
        }
        return childElements;
    }

    /**
     * Return the node element corresponding to the requested type in the file.
     *
     * TODO: Nothing prevents from having duplicate type -> id in a same file.
     * That should not be allowed. If you want an element with the same ID as
     * another one, it should be in a different file and we should check it at
     * validation time.
     *
     * @param filePath
     *            The absolute path to the XML file
     * @param elementType
     *            The type of top level element to search for
     * @param elementId
     *            The ID of the desired element
     * @return The XML element or <code>null</code> if not found
     */
    public static Element getElementInFile(String filePath, @NonNull String elementType, @NonNull String elementId) {

        if (filePath == null) {
            return null;
        }

        IPath path = new Path(filePath);
        File file = path.toFile();
        if (file == null || !file.exists() || !file.isFile() || !XmlUtils.xmlValidate(file).isOK()) {
            return null;
        }

        try {
            Document doc = XmlUtils.getDocumentFromFile(file);

            /* get the state providers and find the corresponding one */
            NodeList nodes = doc.getElementsByTagName(elementType);
            Element foundNode = null;

            for (int i = 0; i < nodes.getLength(); i++) {
                Element node = (Element) nodes.item(i);
                String id = node.getAttribute(TmfXmlStrings.ID);
                if (id.equals(elementId)) {
                    foundNode = node;
                }
            }
            return foundNode;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }

    }

}
